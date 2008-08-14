package de.notEOF.core.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.service.ServiceFinder;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
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
public class Server implements Runnable {

    private static Server server;
    private static Thread serverThread;
    private static Thread serviceObserverThread;
    private boolean stop = false;
    private static ServerSocket serverSocket;
    private static String notEof_Home;
    private static Map<String, Map<String, Service>> allServiceMaps;
    private static int lastServiceId = 0;

    public static Server getInstance() {
        if (null == server) {
            server = new Server();
        }
        return server;
    }

    /*
     * Returns the map with all service types and services
     */
    protected Map<String, Map<String, Service>> getAllServiceMaps() {
        return allServiceMaps;
    }

    /**
     * Initialize server socket with configured or default port (2512).
     * 
     * @throws ActionFailedException
     */
    public static void start(int port, String homeVar) throws ActionFailedException {
        // notEof_Home = ConfigurationManager.getApplicationHome();

        // look for NOTEOF_HOME as VM environment variable (-DCFGROOT)
        // and - if not found - as SYSTEM environment variable $NOTEOF_HOME
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
            try {
                Socket clientSocket = serverSocket.accept();
                acceptClient(clientSocket);
            } catch (IOException ex) {
                LocalLog.error("Fehler bei Warten auf Connect durch n�chsten Client", ex);
            } catch (ActionFailedException afx) {
                LocalLog.error("Abbruch bei Verbindungsaufbau mit Client.", afx);
            }
        }
    }

    /*
     * First step: Be kind, shake hands with client.
     */
    private void acceptClient(Socket clientSocket) throws ActionFailedException {
        TalkLine talkLine = new TalkLine(clientSocket, 0);
        // Client asks for registration
        talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_REGISTRATION, BaseCommTag.RESP_REGISTRATION, BaseCommTag.VAL_OK.name());

        // server asks for perhaps existing service id
        String deliveredServiceId = talkLine.requestTo(BaseCommTag.REQ_SERVICE_ID, BaseCommTag.RESP_SERVICE_ID);
        String serviceTypeName = talkLine.requestTo(BaseCommTag.REQ_TYPE_NAME, BaseCommTag.RESP_TYPE_NAME);

        LocalLog.info("Server acceptClient serviceTypeName = " + serviceTypeName + "; deliveredServiceId = " + deliveredServiceId);

        // Lookup for a service which is assigned to the client. If not found
        // create a new one
        Service service = assignServiceToClient(clientSocket, deliveredServiceId, serviceTypeName);
        // Confirm the serviceId received by client or tell him another one
        talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_SERVICE, BaseCommTag.RESP_SERVICE, service.getServiceId());

        BaseCommTag activateLifeSigns = BaseCommTag.VAL_FALSE;
        if (service.isLifeSignSystemActive())
            activateLifeSigns = BaseCommTag.VAL_TRUE;
        talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_LIFE_SIGN_ACTIVATE, BaseCommTag.RESP_LIFE_SIGN_ACTIVATE, activateLifeSigns.name());

        // start service for client
        // for later use the thread will put into the client
        // LocalLog.info("Server assignServiceToClient service = " +
        // service.getClass().getCanonicalName());
        if (null != service) {
            Thread serviceThread = new Thread((Runnable) service);
            service.setThread(serviceThread);
            serviceThread.start();
        } else {
            // service couldn't be created or found in list by type name
            throw new ActionFailedException(150L, "Service Typ unbekannt.");
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

    /*
     * Second step: Look for matching service by existing serviceId and
     * clientTypeName
     */
    private Service assignServiceToClient(Socket clientSocket, String deliveredServiceId, String serviceTypeName) throws ActionFailedException {

        // Initialization of map for storing serviceLists
        // the typeNames are sent by the clients during the connecting act
        if (null == allServiceMaps)
            allServiceMaps = new HashMap<String, Map<String, Service>>();

        // Search for Map which contains the Map of the services with the same
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
                ((BaseService) service).setServer(this);
                ((BaseService) service).initializeConnection(clientSocket, deliveredServiceId);
                ((BaseService) service).init();

                // if service type did not exist in general service list till
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
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.containsStartsWith("--port")) {
            portString = argsParser.getValue("port");
        }
        if (argsParser.containsStartsWith("--homeVar")) {
            homeVar = argsParser.getValue("homeVar");
            ConfigurationManager.setHomeVariableName(homeVar);
        }
        int port = Util.parseInt(portString, 2512);

        try {
            Server.start(port, homeVar);
        } catch (Exception ex) {
            LocalLog.error("Der zentrale !EOF-Server konnte nicht gestartet werden.", ex);
            throw new RuntimeException("!EOF Server kann nicht gestartet werden.", ex);
        }
    }
}
