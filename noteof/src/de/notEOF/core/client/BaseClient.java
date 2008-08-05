package de.notEOF.core.client;

import java.net.Socket;

import de.notEOF.core.BaseClientOrService;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
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
public abstract class BaseClient extends BaseClientOrService {

    private boolean linkedToService = false;

    public BaseClient() {

    }

    /**
     * The server decides which service is the compatible one to this client by
     * using the classname. <br>
     * Every Client which is derived from BaseClient must implement this method.
     * 
     * @return The service class which is matching with the client.
     */
    public abstract Class<?> serviceClassForClient();

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
    public BaseClient(Socket socketToServer, TimeOut timeout, String... args) throws ActionFailedException {
        if (null == timeout) {
            timeout = getTimeOutObject();
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
    public BaseClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        if (null == timeout) {
            timeout = getTimeOutObject();
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
        return (linkedToService && talkLine.isConnected());
    }

    /**
     * Activates the LifeSignSystem to ensure that the client is alive. <br>
     * When the system is activated the service awaits that it's client sends
     * messages within a hardly defined time in the class
     * {@link NotEOFConstants}.<br>
     * If the LifeSignSystem is activated for the service, it is very
     * recommendable to activate it for every client which uses this type of
     * service too!
     * 
     * @see BaseClient
     * @see NotEOFConstants
     */
    public void activateLifeSignSystem() {
        super.activateLifeSignSystem(true);
    }

    /*
     * When calling this method the client registers itself at the server. After
     * a successfull registration at server side exists a service espacialy for
     * this client.
     */
    @SuppressWarnings("unchecked")
    private final void registerAtServer(TalkLine talkLine, TimeOut timeout, String... args) throws ActionFailedException {
        Class<BaseService> serviceCast;
        try {
            serviceCast = (Class<BaseService>) serviceClassForClient();
        } catch (Exception ex) {
            throw new ActionFailedException(22L, "Casten einer Klasse auf Klasse BaseService ist fehlgeschlagen: " + serviceClassForClient().getName());
        }
        ServerRegistration registration = new ServerRegistration((Class<BaseService>) serviceCast, talkLine, timeout.getMillisConnection(), args);
        linkedToService = registration.isLinkedToService();
        setServiceId(registration.getServiceId());
    }
}
