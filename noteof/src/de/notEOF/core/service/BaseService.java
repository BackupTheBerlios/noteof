package de.notEOF.core.service;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.BaseClientOrService;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.event.ServiceStartEvent;
import de.notEOF.core.event.ServiceStopEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.StartEvent;
import de.notEOF.core.interfaces.StopEvent;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Util;

/**
 * Basic class for every !EOF Service.
 * <p>
 * During services will run as threads to serve the clients simultaneously.
 * Therefore derived classes of this class must be {@link Runnable} and have to
 * implement the method run(). <br>
 * The run method is a good place to accept the individual requests of the
 * clients and process them.<br>
 * 
 * @author Dirk
 * 
 */
public abstract class BaseService extends BaseClientOrService implements Service, EventObserver, Runnable {

    private boolean connectedWithClient = false;
    private boolean stopped = false;
    public boolean isRunning = true;
    private Thread serviceThread;
    private Server server;
    protected List<EventObserver> eventObservers;
    protected StartEvent startEvent;
    protected StopEvent stopEvent;

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * This method can be overwritten by service implementations. <br>
     * It is called directly after the connection with the client is
     * established. <br>
     * E.g. this could be the place to activate the lifesign system.
     * 
     * @throws ActionFailedException
     */
    public void implementationFirstSteps() throws ActionFailedException {
    }

    /**
     * This method can be overwritten by service implementations. <br>
     * It is called as last step when the run-method is at end. <br>
     * When this method runs the client connection maybe is closed! That means
     * it is not sure that the communication to the client works.
     * 
     * @throws ActionFailedException
     */
    public void implementationLastSteps() throws ActionFailedException {
    }

    /**
     * By now the service begins his life helpful service because the client can
     * talk with him.
     * 
     * @param socketToClient
     * @param serviceId
     * @throws ActionFailedException
     */
    public void initializeConnection(Socket socketToClient, String serviceId) throws ActionFailedException {
        setServiceId(serviceId);
        TimeOut timeOut = getTimeOutObject();
        setTalkLine(new TalkLine(socketToClient, timeOut.getMillisCommunication()));
        if (isLifeSignSystemActive())
            getTalkLine().activateLifeSignSystem(false);
        this.startEvent = new ServiceStartEvent(serviceId);
    }

    public boolean isConnectedWithClient() {
        return connectedWithClient;
    }

    public void stopService() {
        stopped = true;
    }

    public final Thread getThread() {
        return serviceThread;
    }

    public final void setServer(Server server) {
        this.server = server;
    }

    /**
     * Returns the list of all services which have the name 'serviceTypeName'.
     * 
     * @param serviceTypeNameThe
     *            name of the service types which are searched.
     * @return A list of active (running) services.
     * @throws ActionFailedException
     */
    public final List<Service> getServiceListByTypeName(String serviceTypeName) throws ActionFailedException {
        return server.getServiceListByTypeName(serviceTypeName);
    }

    /**
     * Delivers the port of the local server.
     * 
     * @return Port
     */
    public final int getServerPort() {
        return server.getPort();
    }

    /**
     * Delivers the address (ip) of the local server.
     * 
     * @return Ip
     */
    public final String getServerHostAddress() {
        return server.getHostAddress();
    }

    /**
     * Set the thread as which the client runs.
     * 
     * @param serviceThread
     *            Thread created in server.
     */
    public final void setThread(Thread serviceThread) {
        this.serviceThread = serviceThread;
    }

    /**
     * To observe the service (the events of the client) here one or more
     * observer of type EventObserver can register themself. <br>
     * Whether a service or an extended class of type service really fires
     * events and which events are fired depends to the single business logic.
     * 
     * @param eventObserver
     *            The registered EventObservers.
     */
    public void registerForEvents(EventObserver eventObserver) {
        if (null == eventObservers)
            eventObservers = new ArrayList<EventObserver>();
        eventObservers.add(eventObserver);
    }

    /**
     * Observe all services of a certain type.
     * 
     * @param observer
     *            The observing object.
     * @param serviceTypeName
     *            The type name of the services to observe.
     * @throws ActionFailedException
     */
    protected final void observeServicesByType(EventObserver observer, String serviceTypeName) throws ActionFailedException {
        List<Service> services = getServiceListByTypeName(serviceTypeName);
        if (null != services && services.size() > 0) {
            for (Service service : services) {
                service.registerForEvents(observer);
            }
        }
    }

    /**
     * Fires an event to all registered Observer.
     * <p>
     * Precondition for getting information on observer side is to initialize
     * the observed event list.
     * 
     * @param service
     *            The Observable which fires the event.
     * @param event
     *            Implementation of Type NotEOFEvent.
     */
    public void updateAllObserver(Service service, NotEOFEvent event) {
        Util.updateAllObserver(eventObservers, service, event);
    }

    /**
     * Callback method to inform the observer about incoming events.
     * <p>
     * In the base service implementation the event will be forwarded to all
     * observer which have registered to the base service.
     * 
     * @param service
     *            The service which fired the event.
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public void update(Service service, NotEOFEvent event) {
        updateAllObserver(service, event);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (!stopped) {
            try {
                String msg = getTalkLine().readMsgTimedOut(NotEOFConstants.LIFE_TIME_INTERVAL_SERVICE);

                // Check if the lifetime hasn't send longer than allowed
                // or if any other messages came within the max. allowed time.
                if (getTalkLine().lifeSignSucceeded()) {
                    // if (Util.isEmpty(msg) && nextLifeSign <
                    // System.currentTimeMillis()) {
                    // no message within the lifetime interval
                    // stop service
                    stopped = true;
                    break;
                }

                if (!Util.isEmpty(msg)) {

                    // client/service specific messages are processed in the
                    // method processMsg() which must be implemented
                    // individual in every service.
                    Class<Enum> tagEnumClass = (Class<Enum>) getCommunicationTagClass();
                    try {
                        processMsg(validateEnum(tagEnumClass, msg));
                    } catch (ActionFailedException afx) {
                        LocalLog.error("Mapping der Nachricht auf Enum.", afx);
                        stopped = true;
                    }

                } else {
                    stopped = true;
                }

            } catch (ActionFailedException afx) {
                // What happened?
                // errNo 24L is ok - timeout at read action
                // Socket communication problem
                if (afx.getErrNo() == 23L) {
                    LocalLog.info("Kommunikation mit Client ist unterbrochen. Service wird beendet. ServiceType, ServiceId: " + getClass().getSimpleName()
                            + ", " + getServiceId());

                    getTalkLine();
                    stopped = true;
                }
                // Problem when setting timeout
                if (afx.getErrNo() == 12L) {
                    LocalLog.info("Problem bei Lesen von Clientnachrichten mit Timeout. Service wird beendet.");
                }
            }
        }

        // close socket to client
        try {
            getTalkLine().close();
        } catch (Exception ex) {
            LocalLog.warn("Verbindung zum Client konnte nicht geschlossen werden. Evtl. bestand zu diesem Zeitpunkt keien Verbindung (mehr).", ex);
        }
        this.stopEvent = new ServiceStopEvent(this.serviceId);
        update(this, this.stopEvent);
        try {
            implementationLastSteps();
        } catch (ActionFailedException e) {
        }
        isRunning = false;
    }

    @SuppressWarnings("unchecked")
    private Enum validateEnum(Class<Enum> tagEnumClass, String msg) throws ActionFailedException {
        // try {
        Enum[] y = tagEnumClass.getEnumConstants();
        for (int i = 0; i < y.length; i++) {
            if (y[i].name().equals(msg)) {
                return y[i];
            }
        }
        // return null;
        throw new ActionFailedException(151L, "Validierung der Empfangenen Nachricht: " + msg);
    }

    protected void finalize() {
        getTalkLine().update(null, null);
    }

    /**
     * Every specialized client/service has it's own Enum which defines the
     * constant tags. This method is the reasaon why there mustn't be more than
     * one Enum(class) for every client/server solution. <br>
     * The developer implements this method in the simple manner that he returns
     * his specialized Enum class.<br>
     * Sample: return MySpecialTag.class();
     * 
     * @return
     */
    public abstract Class<?> getCommunicationTagClass();

    /**
     * This abstract method is called by the BaseService class. Here messages
     * must be processed which are specific for the client and the service.
     * 
     * @param incomingMsgEnum
     */
    public abstract void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException;

    /**
     * Decides if both communication partner - client and service - use the
     * lifeSignSystem. <br>
     * It is very important that - if the LifeSignSystem is activated for the
     * services - the client.close() method is called at the end of the client
     * application. Otherwise the service doesn't know that the client not
     * longer exists.
     * 
     * @return Set the return value to true if the LifeSignSystem shall be used.
     */
    public abstract boolean isLifeSignSystemActive();
}
