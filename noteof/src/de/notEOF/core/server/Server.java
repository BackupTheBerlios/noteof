package de.notEOF.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.configuration.ConfigurationManager;
import de.notEOF.core.enumeration.ServerTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;

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
    private static Map<String, Map<String, BaseService>> allServiceMaps;

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
    public static void start() throws ActionFailedException {
        int port = 0;
        try {
            port = ConfigurationManager.getProperty("notEOFServer.port").getIntValue(2512);
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            throw new ActionFailedException(100L, "Socket Initialisierung mit Port: " + port);
        }

        // Initialization of map for storing serviceLists
        // the typeNames of clients are the key to assign and find the matching
        // service to the client.
        allServiceMaps = new HashMap<String, Map<String, BaseService>>();

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
                LocalLog.error("Fehler bei Warten auf Connect durch nächsten Client", ex);
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
        talkLine.awaitRequestAnswerImmediate(ServerTag.REQ_REGISTRATION, ServerTag.RESP_REGISTRATION, ServerTag.VAL_OK.name());

        // server asks for perhaps existing service id
        String deliveredServiceId = talkLine.requestTo(ServerTag.REQ_SERVICE_ID, ServerTag.RESP_SERVICE_ID);
        String clientTypeName = talkLine.requestTo(ServerTag.REQ_TYPE_NAME, ServerTag.RESP_TYPE_NAME);

        // next step here...
        assignServiceToClient(clientSocket, deliveredServiceId, clientTypeName);
    }

    /*
     * Second step: Look for matching service by existing serviceId and
     * clientTypeName
     */
    private void assignServiceToClient(Socket clientSocket, String deliveredServiceId, String clientTypeName) throws ActionFailedException {
        // basis service bekommt eigene talkline, weil individuell timeouts (zb
        // applicationtimeout )

        // Search for Map which contains the Map of the clients with the same
        // clientTypeName
        // Then search in the clients Map for the service which has the same
        // deliveredServiceId
        BaseService service = null;
        if (allServiceMaps.containsKey(clientTypeName)) {
            Map<String, BaseService> serviceMap = (Map<String, BaseService>) allServiceMaps.get(clientTypeName);
            if (serviceMap.containsKey(deliveredServiceId)) {
                service = serviceMap.get(deliveredServiceId);
            }
        }

        // not found?
        // create service
        if (null == service) {

        }

        // suche nach serviceid und clienttype
        // evtl. eine eigene Liste mit den existierenden clienttypes?
        // oder je typ eine eine eigene Liste map: <typ>:<id> -> map:
        // <id>:<Liste>
        // dann wird die Suche schneller...
    }

    /**
     * The Server is an application.
     * 
     * @param args
     */
    public static void main(String... args) {
        try {
            Server.start();
        } catch (Exception ex) {
            LocalLog.error("Der zentrale !EOF-Server konnte nicht gestartet werden.", ex);
            throw new RuntimeException("!EOF Server kann nicht gestartet werden.", ex);
        }
    }

}
