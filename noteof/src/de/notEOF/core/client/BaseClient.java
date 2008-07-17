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
 * constructor of BaseClient. The basic constructors establish a connection with
 * the matching service. Not more. All communication acts between client and
 * service are oriented to the very specialized tasks of the derived clients and
 * must be individual implemented by developers.
 * 
 * @author Dirk
 * 
 */
public abstract class BaseClient {

    private TalkLine talkLine;
    private String serviceId;
    private boolean linkedToService = false;

    /**
     * The server decides which service is the compatible one to this client by
     * using the classname. <br>
     * Every Client which is derived from BaseClient must implement this method.
     * 
     * @return The service class which is matching with the client.
     */
    protected abstract Class<?> service();

    /**
     * Standard construction of the clients. <br>
     * At first they should initialize the communication with server and
     * service. Within the super constructors of BaseClient the connection with
     * server and service will be established.
     * 
     * @throws ActionFailedException
     *             If the connection with server and service couldn't be
     *             established successfull an ActionFailedException will be
     *             thrown.
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
     * service. Within the super constructors of BaseClient the connection with
     * server and service will be established.
     * 
     * @throws ActionFailedException
     *             If the connection with server and service couldn't be
     *             established successfull an ActionFailedException will be
     *             thrown.
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
    public boolean isLinkedToService() {
        return linkedToService;
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

    /*
     * When calling this method the client registers itself at the server. After
     * a successfull registration at server side exists a service espacialy for
     * this client.
     */
    @SuppressWarnings("unchecked")
    private final void registerAtServer(TalkLine talkLine, Timeout timeout, String... args) throws ActionFailedException {
        Class<BaseService> serviceCast;
        try {
            serviceCast = (Class<BaseService>) service();
        } catch (Exception ex) {
            throw new ActionFailedException(22L, "Casten einer Klasse auf Klasse BaseService ist fehlgeschlagen: " + service().getName());
        }
        ServerRegistration registration = new ServerRegistration((Class<BaseService>) serviceCast, talkLine, timeout.getMillisConnection(), args);
        linkedToService = registration.isLinkedToService();
        serviceId = registration.getServiceId();
    }
}
