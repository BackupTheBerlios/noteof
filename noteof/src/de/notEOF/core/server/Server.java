package de.notEOF.core.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.event.NewMailEvent;
import de.notEOF.core.event.NewServiceEvent;
import de.notEOF.core.event.ServiceStopEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObservable;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.service.ServiceFinder;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.service.EventReceiveService;
import de.notIOC.configuration.ConfigurationManager;

/**
 * The central server which has some different tasks: <br>
 * - await client connections <br>
 * - allocate and assign services to clients <br>
 * - inform clients about system states e.g. shutdown
 * 
 * The server is a Bermuda Triangle of threads. <br>
 * By the way - he is a very static Singleton.
 * 
 * @author Dirk
 */
public class Server implements EventObservable, Runnable {

    private static Server server;
    private static Thread serverThread;
    private static Thread serviceObserverThread;
    private boolean stop = false;
    private static ServerSocket serverSocket;
    private static String notEof_Home;
    private static int lastServiceId = 0;
    private Map<String, EventObserver> eventObservers;
    private static Map<String, Map<String, Service>> allServiceMaps;

    public static Server getInstance() {
        if (null == server) {
            server = new Server();
        }
        return server;
    }

    /**
     * Initialize server socket with configured or default port (2512).
     * 
     * @throws ActionFailedException
     */
    public static void start(int port, String homeVar) throws ActionFailedException {
        notEof_Home = System.getProperty(homeVar);
        if (Util.isEmpty(notEof_Home))
            notEof_Home = System.getenv(homeVar);

        if (Util.isEmpty(notEof_Home)) {
            System.out.println("Umgebungsvariable '" + homeVar + "' ist nicht gesetzt. !EOF-Server ben�tigt diese Variable.\n" + //
                    "Wert der Variable ist der Ordner unter dem die noteof.jar liegt.\n");
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            throw new ActionFailedException(100L, "Socket Initialisierung mit Port: " + port);
        }

        Server server = getInstance();
        serverThread = new Thread(server);
        serverThread.start();

        ServiceGarbage observer = new ServiceGarbage(server);
        serviceObserverThread = new Thread(observer);
        serviceObserverThread.start();
    }

    /**
     * Server awaits client connections.
     * <p>
     * He asks the client which type it is of. <br>
     * Then he creates an adequate service or looks if there has been one
     * initialized some times ago.
     */
    public void run() {
        while (!stop) {
            System.out.println("Server.run...");
            try {
                Socket clientSocket = serverSocket.accept();
                ClientAcceptor acceptor = new ClientAcceptor(clientSocket, this);
                Thread acceptorThread = new Thread(acceptor);
                acceptorThread.start();
                // acceptClient(clientSocket);
            } catch (IOException ex) {
                LocalLog.error("Fehler bei Warten auf Connect durch n�chsten Client", ex);
            }
        }
        stopAllServices();
    }

    private void stopAllServices() {
        ServiceStopEvent event = new ServiceStopEvent();
        try {
            event.addAttribute("allServices", "TRUE");
            updateObservers(null, event);
        } catch (ActionFailedException e) {
        }
    }

    private class ClientAcceptor implements Runnable {

        private Socket clientSocket = null;
        private Server server = null;

        public ClientAcceptor(Socket clientSocket, Server server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        public void run() {
            try {
                acceptClient(clientSocket);
            } catch (Exception e) {
                LocalLog.error("Abbruch bei Verbindungsaufbau mit Client.\n", e);
            }
        }

        /*
         * First step: Be kind, shake hands with client.
         */
        private void acceptClient(Socket clientSocket) throws ActionFailedException {
            TalkLine talkLine = new TalkLine(clientSocket, 0);
            // Client asks for registration
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_REGISTRATION, BaseCommTag.RESP_REGISTRATION, BaseCommTag.VAL_OK.name());

            // client wants it's own id
            String clientNetId = talkLine.getHostAddress() + "." + new Date().getTime();
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_CLIENT_ID, BaseCommTag.RESP_CLIENT_ID, clientNetId);

            // server asks for perhaps existing service id
            String deliveredServiceId = talkLine.requestTo(BaseCommTag.REQ_SERVICE_ID, BaseCommTag.RESP_SERVICE_ID);
            String serviceTypeName = talkLine.requestTo(BaseCommTag.REQ_TYPE_NAME, BaseCommTag.RESP_TYPE_NAME);

            LocalLog.info("Server starting service: " + serviceTypeName + " (deliveredServiceId = " + deliveredServiceId + ")");

            // Lookup for a service which is assigned to the client. If not
            // found
            // create a new one
            Service service = assignServiceToClient(clientSocket, clientNetId, deliveredServiceId, serviceTypeName);
            // Confirm the serviceId received by client or tell him another one
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_SERVICE, BaseCommTag.RESP_SERVICE, service.getServiceId());

            BaseCommTag activateLifeSigns = BaseCommTag.VAL_FALSE;
            if (service.isLifeSignSystemActive())
                activateLifeSigns = BaseCommTag.VAL_TRUE;
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_LIFE_SIGN_ACTIVATE, BaseCommTag.RESP_LIFE_SIGN_ACTIVATE, activateLifeSigns.name());

            // start service for client
            // and inform all observer
            if (null != service) {
                Thread serviceThread = new Thread((Runnable) service);
                service.setThread(serviceThread);
                serviceThread.start();

                // Fire event to all observers which are interested in
                NewServiceEvent event = new NewServiceEvent();
                event.addAttribute("serviceId", service.getServiceId());
                Util.updateAllObserver(eventObservers, null, event);
            } else {
                // service couldn't be created or found in list by type name
                throw new ActionFailedException(150L, "Service Typ unbekannt.");
            }

        }

        /*
         * Second step: Look for matching service by existing serviceId and
         * clientTypeName
         */
        private Service assignServiceToClient(Socket clientSocket, String clientNetId, String deliveredServiceId, String serviceTypeName)
                throws ActionFailedException {

            // Initialization of map for storing serviceLists
            // the typeNames are sent by the clients during the connecting act
            if (null == allServiceMaps)
                allServiceMaps = new HashMap<String, Map<String, Service>>();

            // Search for Map which contains the Map of the services with the
            // same
            // serviceTypeName
            // Then search in the service Map for the service which has the same
            // deliveredServiceId
            Service service = getService(deliveredServiceId, Util.simpleClassName(serviceTypeName));

            // not found?
            // create service
            if (null == service) {
                service = ServiceFinder.getService(notEof_Home, serviceTypeName);

                if (null != service) {
                    // generate new serviceId
                    deliveredServiceId = generateServiceId();
                    ((BaseService) service).setServer(this.server);
                    ((BaseService) service).setClientNetId(clientNetId);
                    ((BaseService) service).initializeConnection(clientSocket, deliveredServiceId);
                    ((BaseService) service).implementationFirstSteps();

                    // if service type did not exist in general service list
                    // till
                    // now create new map for type
                    Map<String, Service> serviceMap = getServiceMapByTypeName(Util.simpleClassName(serviceTypeName));
                    if (null == serviceMap) {
                        serviceMap = new HashMap<String, Service>();
                        // add new type specific map to general list
                        allServiceMaps.put(Util.simpleClassName(serviceTypeName), serviceMap);
                    }

                    // add new service to type specific map
                    serviceMap.put(deliveredServiceId, service);
                } else {
                    throw new ActionFailedException(152L, "Suche des Service: " + serviceTypeName);
                }
            }
            return service;
        }
    }

    public static String getApplicationHome() {
        return notEof_Home;
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return null;
    }

    private synchronized static String generateServiceId() {
        String hostAddress = "";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostAddress = Thread.currentThread().getId() + String.valueOf(System.currentTimeMillis());
        }
        return hostAddress + ":" + String.valueOf(serverSocket.getLocalPort()) + "_" + String.valueOf(lastServiceId++);
    }

    public Map<String, Service> getServiceMapByTypeName(String serviceTypeName) throws ActionFailedException {
        if (null == allServiceMaps)
            return null;
        if (allServiceMaps.containsKey(serviceTypeName)) {
            return (Map<String, Service>) allServiceMaps.get(serviceTypeName);
        }
        return null;
    }

    public List<Service> getServiceListByTypeName(String serviceTypeName) throws ActionFailedException {
        Map<String, Service> serviceMap = null;
        serviceMap = getServiceMapByTypeName(serviceTypeName);
        if (null != serviceMap) {
            Collection<Service> servicesOfType = serviceMap.values();
            List<Service> serviceList = new ArrayList<Service>();
            serviceList.addAll(servicesOfType);
            return serviceList;
        }
        return null;
    }

    public Service getService(String deliveredServiceId, String serviceTypeName) throws ActionFailedException {
        Map<String, Service> serviceMap = null;
        serviceMap = getServiceMapByTypeName(serviceTypeName);
        if (null != serviceMap && serviceMap.containsKey(deliveredServiceId)) {
            return serviceMap.get(deliveredServiceId);
        }
        return null;
    }

    /**
     * Fire an event to all registered observers.
     * <p>
     * Only observers that are interested in the raised event will be informed
     * about it. <br>
     * Events are not replied to.
     * 
     * @param service
     *            The service that fired the event.
     * @param event
     *            The event itself.
     */
    public void updateObservers(Service service, NotEOFEvent event) {
        Util.updateAllObserver(eventObservers, service, event);
    }

    /**
     * This method enables the server to inform the services about events or
     * changes of the system.
     * 
     * @param eventObserver
     *            One or more Observers can register them here. This observers
     *            will be informed for events at a later moment.
     */
    public void registerForEvents(EventObserver eventObserver) {
        if (null == eventObservers)
            eventObservers = new HashMap<String, EventObserver>();
        Util.registerForEvents(eventObservers, eventObserver);
    }

    public void unregisterFromEvents(EventObserver eventObserver) {
        Util.unregisterFromEvents(eventObservers, eventObserver);
    }

    /*
     * Returns the map with all service types and services
     */
    protected Map<String, Map<String, Service>> getAllServiceMaps() {
        return allServiceMaps;
    }

    /**
     * Server can be used as post office.
     * <p>
     * The server stores incoming messages and informs services by sending an
     * event. <br>
     * The services by themself proove if the message is for them. Then they
     * send it to their clients. <br>
     */
    public void postMail(NotEOFMail mail, Service fromService) {
        updateObservers(fromService, new NewMailEvent(mail));
    }

    /**
     * Server can be used as post office.
     * <p>
     * The server stores incoming messages and informs services by sending an
     * event. <br>
     * The services by themself proove if the message is for them. Then they
     * send it to their clients. <br>
     */
    public void postEvent(NotEOFEvent event, Service fromService) {
        updateObservers(fromService, event);
    }

    /**
     * Send message directly to a service.
     * 
     * @param service
     *            The recipient.
     * @param mail
     *            The message.
     */
    public void mailToService(NotEOFMail mail, EventReceiveService service) throws ActionFailedException {
        service.mailToClient(mail);
    }

    public void finalize() {
        stopAllServices();
    }

    /**
     * The Server is an application. Default value for server port is 2512
     * 
     * @param args
     *            Use --port=<port> as calling argument for using an individual
     *            server port
     */
    // @TODO Hilfe anzeigen
    public static void main(String... args) {
        String portString = "";
        String homeVar = "NOTEOF_HOME";
        String baseConfFile = "noteof_master.xml";
        String baseConfPath = "conf";
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.containsStartsWith("--port")) {
            portString = argsParser.getValue("port");
        }
        if (argsParser.containsStartsWith("--homeVar")) {
            homeVar = argsParser.getValue("homeVar");
        }
        if (argsParser.containsStartsWith("--baseConfFile")) {
            baseConfFile = argsParser.getValue("baseConfFile");
        }
        if (argsParser.containsStartsWith("--baseConfPath")) {
            baseConfFile = argsParser.getValue("baseConfPath");
        }
        ConfigurationManager.setInitialEnvironment(homeVar, baseConfPath, baseConfFile);

        int port = Util.parseInt(portString, 2512);

        try {
            Server.start(port, homeVar);
        } catch (Exception ex) {
            LocalLog.error("Der zentrale !EOF-Server konnte nicht gestartet werden.", ex);
            throw new RuntimeException("!EOF Server kann nicht gestartet werden.", ex);
        }
    }
}
