package de.notEOF.core.client;

import java.net.Socket;

import de.notEOF.core.communication.BaseTimeout;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Timeout;
import de.notEOF.core.service.BaseService;

/**
 * From this class every other client must be extended. <br>
 * The most important steps for establishing a connection to a service are
 * defined or implemented here.
 * <p>
 * It is highly recommended that the derived class at very first step calls the
 * constructor of BaseClient. I would say this should be the first line in the
 * constructor of the derived class.
 * 
 * @author Dirk
 * 
 */
public abstract class BaseClient {

    private TalkLine talkLine;
    private String serviceId;
    private boolean connectedWithService = false;

    /**
     * The server decides which service is the compatible one to this client by
     * using the classname. <br>
     * Every Client which is derived from BaseClient must implement this method.
     * 
     * @return The service class which is matching with the client.
     */
    protected abstract Class<BaseService> service();

    /**
     * Standard construction of the clients. <br>
     * At first they should initialize the communication with server and
     * service.
     */
    public BaseClient(Socket socketToServer, Timeout timeout, String... args) throws ActionFailedException {
        if (null == timeout) {
            timeout = new BaseTimeout();
        }
        talkLine = new TalkLine(socketToServer, timeout.getMillisCommunication());
        registerAtServer(talkLine, timeout, args);
    }

    /**
     * Standard construction of the clients. <br>
     * At first they should initialize the communication with server and
     * service.
     */
    public BaseClient(String ip, int port, Timeout timeout, String... args) throws ActionFailedException {
        if (null == timeout) {
            timeout = new BaseTimeout();
        }
        talkLine = new TalkLine(ip, port, timeout.getMillisCommunication());
        registerAtServer(talkLine, timeout, args);
    }

    /**
     * Tells if the client is connected with a service at server side.
     * 
     * @return true or false...
     */
    public boolean isConnectedWithService() {
        return connectedWithService;
    }

    /**
     * Delivers the id of the service which is concerned with the client.
     * 
     * @return The internal id which the server has generated. <br>
     *         NULL if there is no service for the client.
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Delivers a valid message interface to the server
     * 
     * @return An initialized Object which simplifies the communication with the
     *         server
     */
    public TalkLine getTalkLine() {
        return talkLine;
    }

    /**
     * Meldet sich vom Server ab und schlieﬂt die physikalische Verbindung.
     * 
     * @throws ActionFailedException
     */
    protected void close() throws ActionFailedException {
        talkLine.close();
    }

    // /**
    // * Every client has to exercise a handshake with the service which the
    // * server has created after the clients registration. <br>
    // * The connections between client and service and the establishing of them
    // * can be very different.
    // */
    // public abstract void connectToService(Object... anyObject);

    /*
     * When calling this method the client registers itself at the server. After
     * a successfull registration at server side exists a service espacialy for
     * this client.
     */
    private final void registerAtServer(TalkLine talkLine, Timeout timeout, String... args) throws ActionFailedException {
        ServerRegistration registration = new ServerRegistration(service(), talkLine, timeout.getMillisConnection(), args);
        connectedWithService = registration.isLinkedToService();
        serviceId = registration.getServiceId();
    }
}
