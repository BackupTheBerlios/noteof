package de.noteof.core.server;

import java.io.IOException;
import java.net.ServerSocket;

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

    private static Server server = new Server();
    private static Thread serverThread;
    private boolean stop = false;
    private ServerSocket serverSocket;

    private Server() {
        try {
            serverSocket = new ServerSocket(ConfigManager.getProperty("paul.serverPort").getIntValue(2512));
        } catch (IOException ex) {
            LOG.warn("Server für Anmeldung von Anwendungen konnte nicht gestartet werden.", ex);
        }
        serverThread = new Thread(server);
    }

    public static void start() {
        serverThread.start();
    }

    /**
     * The Server is an application.
     * 
     * @param args
     */
    public static void main(String... args) {
        Server.start();
    }

    public void run() {
        while (!stop) {

        }
    }
}
