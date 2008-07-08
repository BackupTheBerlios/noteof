package de.noteof.client.application;

import java.net.Socket;

import de.noteof.core.client.BaseClient;
import de.noteof.core.exception.ActionFailedException;
import de.noteof.core.interfaces.Timeout;

/**
 * Use this class if you want to integrate your own application into the central
 * task monitoring. This offers some alternatives: <br>
 * - Control if one more process of the application may run now and wait for the
 * allowance to run if necessary.<br>
 * - Send actions or states of the process to the service.<br>
 * - Send error informations to the service.<br>
 * - Send alerts to the service.
 * 
 * @author Dirk
 */
public class ApplicationClient extends BaseClient {

    /**
     * Construction of the ApplicationClient with a well prepared socket object
     * which holds ip and port of the server.
     * <p>
     * After the constructor has processed the connection to the service is
     * established (hopefully).
     * 
     * @param socketToServer
     *            An initialized socket object with valid ip and address
     * @param timeout
     *            Object of type {@link Timeout}. May be null. If null the
     *            framework uses default values for timeouts.
     * @throws ActionFailedException
     */
    public ApplicationClient(Socket socketToServer, Timeout timeout, ApplicationConfiguration configuration) throws ActionFailedException {
        super(socketToServer, timeout);
        if (null == timeout) {
            timeout = new ApplicationTimeout();
        }
    }

    /**
     * Construction of the ApplicationClient with ip and port of the server.
     * <p>
     * After the constructor has processed the connection to the service is
     * established (hopefully).
     * 
     * @param socketToServer
     *            An initialized socket object with valid ip and address
     * @param timeout
     *            Object of type {@link Timeout}. May be null. If null the
     *            framework uses default values for timeouts.
     * @throws ActionFailedException
     */
    public ApplicationClient(String ip, int port, Timeout timeout) throws ActionFailedException {
        super(ip, port, timeout);
        if (null == timeout) {
            timeout = new ApplicationTimeout();
        }
    }

    @Override
    protected String type() {
        return this.getClass().getName();
    }

}
