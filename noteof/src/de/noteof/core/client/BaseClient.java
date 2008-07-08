package de.noteof.core.client;

import java.net.Socket;

import de.noteof.core.communication.BaseTimeout;
import de.noteof.core.communication.MessageLayer;
import de.noteof.core.exception.ActionFailedException;
import de.noteof.core.interfaces.Timeout;

/**
 * From this class every other client must be extended. <br>
 * The most important steps for establishing a connection to a service are
 * defined or implemented here.
 * 
 * @author Dirk
 * 
 */
public abstract class BaseClient {

    private MessageLayer messageLayer;
    private String serviceId;
    private boolean connectedWithService = false;

    /**
     * The server decides which service is the compatible one to this client by
     * using the type name.
     * 
     * @return Type name must be unique. <br>
     *         One Simple practice is to deliver the own class name I would say.
     */
    protected abstract String type();

    /**
     * Standard construction of the clients. <br>
     * At first they should initialize the communication with server and
     * service.
     */
    public BaseClient(Socket socketToServer, Timeout timeout) throws ActionFailedException {
        if (null == timeout) {
            timeout = new BaseTimeout();
        }
        if (null == messageLayer) {
            messageLayer = new MessageLayer(socketToServer, timeout.getMillisCommunication());
        }
        registerAtServer(messageLayer, timeout);
    }

    /**
     * Standard construction of the clients. <br>
     * At first they should initialize the communication with server and
     * service.
     */
    public BaseClient(String ip, int port, Timeout timeout) throws ActionFailedException {
        if (null == timeout) {
            timeout = new BaseTimeout();
        }
        if (null == messageLayer) {
            messageLayer = new MessageLayer(ip, port, timeout.getMillisCommunication());
        }
        registerAtServer(messageLayer, timeout);
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
    private final void registerAtServer(MessageLayer messageLayer, Timeout timeout) throws ActionFailedException {
        ServerRegistration registration = new ServerRegistration(type(), messageLayer, timeout.getMillisConnection());
        connectedWithService = registration.isLinkedToService();
        serviceId = registration.getServiceId();
    }

}
