package de.notEOF.core.client;

import java.net.Socket;

import de.notEOF.core.BaseClientOrService;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;

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
    private String clientNetId;
    private String[] args;

    /**
     * The server decides which service is the compatible one to this client by
     * using the classname. <br>
     * One of the two methods (serviceForClientByClass, serviceForClientByName)
     * must return a valid value. The other one may return null. If both methods
     * do not return null, the value of this method (serviceForClientByClass)
     * will be used to send the service class name to the server. <br>
     * Please respect that only classes can be found for which the canonical
     * class name exists.
     * 
     * @see Method serviceForClientByName.
     * @return The service class which is matching with the client.
     */
    public abstract Class<?> serviceForClientByClass();

    /**
     * The server decides which service is the compatible one to this client by
     * using the classname. <br>
     * One of the two methods (serviceForClientByClass, serviceForClientByName)
     * must return a valid value. The other one may return null. If both methods
     * do not return null, the value of the method serviceForClientByClass will
     * be used to send the service class name to the server.
     * 
     * @see Method serviceForClientByClass.
     * @return The name of the service class which is matching with the client. <br>
     *         Valid value is the canonical class name.
     */
    public abstract String serviceForClientByName();

    /**
     * If the connection must be build at a later time this constructor can be
     * used.
     */
    public BaseClient(String... args) {
        this.args = args;
    }

    /**
     * If the connection must be build at a later time this constructor can be
     * used. <br>
     * When this constructor is used, the lifeSignSystem will not be activated
     * by the client. If the service awaits a client with activated
     * lifeSignSystem, he will stop after a while.
     */
    public BaseClient() {
    }

    public void connect(String ip, int port, TimeOut timeout) throws ActionFailedException {
        if (linkedToService)
            return;

        if (null == timeout) {
            timeout = getTimeOutObject();
        }
        setTalkLine(new TalkLine(ip, port, timeout.getMillisCommunication()));
        registerAtServer(getTalkLine(), timeout, this.args);
        implementationFirstSteps();
    }

    public void connect(Socket socketToServer, TimeOut timeout) throws ActionFailedException {
        if (linkedToService)
            return;

        if (null == timeout) {
            timeout = getTimeOutObject();
        }
        setTalkLine(new TalkLine(socketToServer, timeout.getMillisCommunication()));
        registerAtServer(getTalkLine(), timeout, this.args);
        implementationFirstSteps();
    }

    /**
     * Standard construction of the clients. <br>
     * At first they should initialize the communication with server and
     * service. Within the super constructors of BaseClient the connection with
     * server and service will be established. If the 'generic' constructor is
     * used the connection must be established later by the method connect().
     * 
     * @throws ActionFailedException
     *             If the connection with server and service couldn't be
     *             established successfull an ActionFailedException will be
     *             thrown.
     */
    public BaseClient(Socket socketToServer, TimeOut timeout, String... args) throws ActionFailedException {
        this.args = args;
        connect(socketToServer, timeout);
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
        this.args = args;
        connect(ip, port, timeout);
    }

    /**
     * Returns the netwide unique identifier of this client.
     * 
     * @return Id.
     */
    public String getClientNetId() {
        return this.clientNetId;
    }

    /**
     * Sends mail to any service.
     * 
     * @param mail
     * @throws ActionFailedException
     */
    public void sendMail(NotEOFMail mail) throws ActionFailedException {
        if (Util.isEmpty(mail.getFromClientNetId()))
            mail.setFromClientNetId(clientNetId);
        System.out.println("VOR writeMsg REQ_READY_FOR_MAIL...");
        writeMsg(MailTag.REQ_READY_FOR_MAIL.name());
        System.out.println("VOR getTalkLine().sendMail()");
        getTalkLine().sendMail(mail);
    }

    /**
     * Sends events to a service.
     * <p>
     * Only the data are sent which match to the interface (EventType, Map
     * attributes, Map descriptions). Special data of an event implementation
     * are not supported.
     * 
     * @param event
     *            The event with EventType, Map attributes and Map descriptions.
     * @throws ActionFailedException
     */
    public void sendBaseEvent(NotEOFEvent event) throws ActionFailedException {
        System.out.println("VOR ...");
        writeMsg(MailTag.REQ_READY_FOR_EVENT.name());
        System.out.println("VOR getTalkLine().sendBaseEvent()...");
        getTalkLine().sendBaseEvent(event);
        System.out.println("Nach getTalkLine().sendBaseEvent()...");
    }

    /**
     * Tells if the client is connected with a service at server side.
     * 
     * @return true or false...
     */
    public boolean isLinkedToService() {
        return (linkedToService && getTalkLine().isConnected());
    }

    // /**
    // * Activates the LifeSignSystem to ensure that the client is alive. <br>
    // * When the system is activated the service awaits that it's client sends
    // * messages within a hardly defined time in the class
    // * {@link NotEOFConstants}.<br>
    // * If the LifeSignSystem is activated for the service, it is very
    // * recommendable to activate it for every client which uses this type of
    // * service too!
    // *
    // * @see BaseClient
    // * @see NotEOFConstants
    // */
    // public void activateLifeSignSystem() {
    // super.activateLifeSignSystem(true);
    // }
    //
    /**
     * Returns the class name of the service which is concerned with this
     * client.
     * 
     * @return
     */
    public String getServiceClassName() throws ActionFailedException {
        String serviceClassName = "";
        if (null != serviceForClientByClass()) {
            serviceClassName = serviceForClientByClass().getCanonicalName();
        }
        if (Util.isEmpty(serviceClassName))
            serviceClassName = serviceForClientByName();
        if (Util.isEmpty(serviceClassName))
            throw new ActionFailedException(22L, "Ermittlung des Klassennamen einer von BaseService abgeleiteten Klasse ist fehlgeschlagen. Clint ist: "
                    + this.getClass().getName());

        return serviceClassName;
    }

    /*
     * When calling this method the client registers itself at the server. After
     * a successfull registration at server side exists a service espacialy for
     * this client.
     */
    private final void registerAtServer(TalkLine talkLine, TimeOut timeout, String... args) throws ActionFailedException {
        System.out.println("BaseClient vor registration...");
        ServerRegistration registration = new ServerRegistration(getServiceClassName(), talkLine, timeout.getMillisConnection(), args);
        clientNetId = registration.getClientNetId();
        linkedToService = registration.isLinkedToService();
        setServiceId(registration.getServiceId());
    }
}
