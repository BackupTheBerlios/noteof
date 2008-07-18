package de.notEOF.core.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.ServiceLoader;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;

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

    /**
     * Initialize server socket with configured or default port (2512).
     * 
     * @throws ActionFailedException
     */
    public static void start(int port) throws ActionFailedException {
        // look for NOTEOF_HOME as VM environment variable (-DCFGROOT)
        // and - if not found - as SYSTEM environment variable $NOTEOF_HOME 
        //TODO prüfen, ob das mit der Umgebungsvariablen funktioniert
        notEof_Home = System.getProperty("NOTEOF_HOME");
        if (Util.isEmpty(notEof_Home))   notEof_Home = System.getenv("NOTEOF_HOME");
        
        System.out.println("NOTEOF_HOME=" + notEof_Home);
        
        try { 
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            throw new ActionFailedException(100L, "Socket Initialisierung mit Port: " + port);
        }

        Server server = getInstance();
        serverThread = new Thread(server);
        serverThread.start();
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
                LocalLog.error("Fehler bei Warten auf Connect durch nï¿½chsten Client", ex);
            } catch (ActionFailedException afx) {
                LocalLog.error("Abbruch bei Verbindungsaufbau mit Client", afx);
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
        String clientTypeName = talkLine.requestTo(BaseCommTag.REQ_TYPE_NAME, BaseCommTag.RESP_TYPE_NAME);

        // next step here...
        assignServiceToClient(clientSocket, deliveredServiceId, clientTypeName);
    }
    
    private static String generateServiceId() {
        String hostAddress = "";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			hostAddress = Thread.currentThread().getId() + String.valueOf(System.currentTimeMillis());
		}
        return hostAddress + ":" + String.valueOf(serverSocket.getLocalPort()) + "_" + String.valueOf(lastServiceId++);
    }

    /*
     * Second step: Look for matching service by existing serviceId and
     * clientTypeName
     */
    private void assignServiceToClient(Socket clientSocket, String deliveredServiceId, String serviceTypeName) throws ActionFailedException {
        
        // Initialization of map for storing serviceLists
        // the typeNames of clients are the key to assign and find the matching
        // service to the client.
        if (null == allServiceMaps)
        allServiceMaps = new HashMap<String, Map<String, Service>>();

        // basis service bekommt eigene talkline, weil individuell timeouts (zb
        // applicationtimeout )

        // Search for Map which contains the Map of the clients with the same
        // clientTypeName
        // Then search in the clients Map for the service which has the same
        // deliveredServiceId
        Service service = null;
        Map<String, Service> serviceMap = null;
        if (allServiceMaps.containsKey(serviceTypeName)) {
            serviceMap = (Map<String, Service>) allServiceMaps.get(serviceTypeName);
            if (serviceMap.containsKey(deliveredServiceId)) {
                service = serviceMap.get(deliveredServiceId);
            }
        }

        // not found?
        // create service
        if (null == service) {
            service = ServiceLoader.getServiceObject(notEof_Home, serviceTypeName);
            
            if (null != service) {
                // generate new serviceId
                deliveredServiceId = generateServiceId();
                service.init(clientSocket, deliveredServiceId);

                // if service type did not exist in general service list till now create new map for type
                if (null == serviceMap) {
                    serviceMap = new HashMap<String, Service>();
                    // add new type specific map to general list
                    allServiceMaps.put(serviceTypeName, serviceMap);
                }
                // add new service to type specific map
                serviceMap.put(deliveredServiceId, service);
            }
        }
        
        // start service for client
        if (null != service) {
            service.run();
        }else {
            // service couldn't be created or found in list by type name
            throw new ActionFailedException (150L,"Service Typ unbekannt.");
        }

    }

    /**
     * The Server is an application.
     * Default value for server port is 2512
     * 
     * @param args Use --port=<port> as calling argument for using an individual server port
     */
    public static void main(String... args) {
    	System.out.println("Hello World");
        // needs port
        String portString = "";
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.containsStartsWith("--port")) {
            portString = argsParser.getValue("port");
        }
        int port = Util.parseInt(portString, 2512);
        
        try {
            Server.start(port);
        } catch (Exception ex) {
            LocalLog.error("Der zentrale !EOF-Server konnte nicht gestartet werden.", ex);
            System.out.println("Schade");
            throw new RuntimeException("!EOF Server kann nicht gestartet werden.", ex);
        }
    }

}
