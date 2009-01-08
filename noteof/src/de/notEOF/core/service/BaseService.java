package de.notEOF.core.service;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.notEOF.core.BaseClientOrService;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Statistics;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;

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
    List<EventType> observedEvents;
    protected String clientNetId;
    private EventProcessor processor;
    private String observerName;

    // private long workerPointer = 0;

    // private List<Long> wpl = new ArrayList<Long>();
    // private Map<Long, Thread> workerThreads = new HashMap<Long, Thread>();
    // private List<NotEOFEvent> eventsToClient;
    // private List<Service> servicesToClient;

    public boolean isRunning() {
        return isRunning;
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
    }

    public boolean isConnectedWithClient() {
        return connectedWithClient;
    }

    public void setClientNetId(String clientNetId) {
        this.clientNetId = clientNetId;
    }

    public String getClientNetId() {
        return this.clientNetId;
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

    public final Server getServer() {
        return this.server;
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
     * Callback method to inform the observer about incoming events.
     * <p>
     * If this method is overwritten it is highly recommended to call this one
     * by super() for easy use of the internal mail system.
     * 
     * @param service
     *            The service which fired the event.
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public final void update(Service service, NotEOFEvent event) throws ActionFailedException {
        System.out.println("BaseService.update. event: " + event.getEventType());
        // Stop Event kill the service
        if (event.equals(EventType.EVENT_SERVICE_STOP) && //
                (event.getAttribute("serviceId").equals(this.getServiceId()) || //
                event.getAttribute("allServices").equalsIgnoreCase("TRUE"))) {
            this.stopService();
        } else {
            // Prozessor zur Abarbeitung der Events ist eine eigene Klasse
            // Nur des Handlings halber...
            if (null == processor) {
                processor = new EventProcessor(this);
            }
            // Dem Prozessor wird das Event zur Verarbeitung vor die Fuesse
            // geworfen
            processor.addAction(service, event);
        }
    }

    /**
     * This method must be overwritten by the service for special tasks.
     * <p>
     * Every time when an event was fired this method will be called. So
     * overwriting in the derived client works like a callback.
     * 
     * @param service
     *            The service which has raised the event
     * @param event
     *            The fired event
     * @throws ActionFailedException
     *             Problems within the derived client class can be thrown as
     *             ActionFailedExceptions.
     */
    public synchronized void processEvent(Service service, NotEOFEvent event) throws ActionFailedException {
    }

    private class EventProcessor {
        private BaseService mainClass;

        private EventProcessor(BaseService mainClass) {
            this.mainClass = mainClass;
        }

        protected synchronized void addAction(Service service, NotEOFEvent event) throws ActionFailedException {
            try {
                processEvent(service, event);
            } catch (ActionFailedException e) {
                LocalLog
                        .error("Fehler bei Abarbeiten der MessageQueue im EventProcessor des BaseService. Der Service wird aus der Event-Benachrichtigung entfernt und beendet!"
                                + e);
                server.unregisterFromEvents(mainClass);
                e.printStackTrace();
                stopService();
                throw new ActionFailedException(e.getErrNo(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (!stopped) {
            try {
                String msg = readMsgTimedOut(NotEOFConstants.LIFE_TIME_INTERVAL_SERVICE);

                // Check if the lifetime hasn't send longer than allowed
                // or if any other messages came within the max. allowed time.
                if (getTalkLine().lifeSignSucceeded()) {
                    // no message within the lifetime interval
                    // stop service
                    stopped = true;
                    break;
                }

                if (!Util.isEmpty(msg)) {
                    if (msg.equals(MailTag.REQ_READY_FOR_MAIL.name())) {
                        try {
                            processClientMail();
                        } catch (Exception e) {
                            LocalLog.warn("Problem bei Verarbeitung einer Mail-Nachricht.", e);
                        }
                    } else if (msg.equals(MailTag.REQ_READY_FOR_EVENT.name())) {
                        try {
                            processClientEvent();
                        } catch (Exception e) {
                            LocalLog.warn("Problem bei Verarbeitung einer Event-Nachricht.", e);
                        }
                    } else if (msg.equals(BaseCommTag.REQ_STOP.name())) {
                        stopService();
                    } else if (msg.equals(BaseCommTag.REQ_TEST.name())) {
                        stopService();
                    } else {
                        // client/service specific messages are processed in the
                        // method processMsg() which must be implemented
                        // individual in every service.
                        Class<Enum<?>> tagEnumClass = (Class<Enum<?>>) getCommunicationTagClass();
                        try {
                            processClientMsg(validateEnum(tagEnumClass, msg));
                        } catch (ActionFailedException afx) {
                            LocalLog.error("Mapping der Nachricht auf Enum.", afx);
                            stopped = true;
                        }
                    }
                } else {
                    stopped = true;
                }

            } catch (ActionFailedException afx) {
                // What happened?
                // errNo 24L is ok - timeout at read action
                // Socket communication problem
                if (afx.getErrNo() == 23L) {
                    stopped = true;
                }
                // Problem when setting timeout
                if (afx.getErrNo() == 12L) {
                    LocalLog.info("Problem bei Lesen von Clientnachrichten mit Timeout. Service wird beendet.");
                }
            }
        }

        // close socket to client
        // the close function also calls the implementationLastSteps() method
        try {
            close();
        } catch (Exception ex) {
            LocalLog.warn("Verbindung zum Client konnte nicht geschlossen werden. Evtl. bestand zu diesem Zeitpunkt keine Verbindung (mehr).", ex);
        } finally {
            server.unregisterFromEvents(this);
            System.out.println("Service stopped: " + this.getClass().getCanonicalName() + "; id: " + getServiceId());
            Statistics.addFinishedService();
            isRunning = false;
        }
    }

    /**
     * Used by framework.
     * <p>
     * Overwrite this method if the specialized service is interested in events.
     * <br>
     * In your implementation you have to return a simple List which contains
     * the relevant types.
     */
    public List<EventType> getObservedEvents() {
        return observedEvents;
    }

    protected final void addObservedEvent(EventType eventType) {
        if (null == observedEvents) {
            observedEvents = new ArrayList<EventType>();
        }
        observedEvents.add(eventType);
    }

    // @SuppressWarnings("unchecked")
    private Enum<?> validateEnum(Class<Enum<?>> tagEnumClass, String msg) throws ActionFailedException {
        try {
            for (Enum<?> enume : tagEnumClass.getEnumConstants()) {
                if (enume.name().equals(msg)) {
                    return enume;
                }
            }
        } catch (Exception e) {
            throw new ActionFailedException(151L, "Validierung der Empfangenen Nachricht. EnumClass: " + tagEnumClass + "; Message: " + msg);
        }
        // return null;
        throw new ActionFailedException(151L, "Validierung der Empfangenen Nachricht: " + msg);
    }

    /**
     * When client sends a {@link NotEOFMail} this method is called by this base
     * class itself.
     * <p>
     * The service forwards the mail to the server which informs all observers
     * about the incoming message.
     * 
     * @throws ActionFailedException
     */
    public synchronized void processClientMail() throws ActionFailedException {
        NotEOFMail mail = getTalkLine().receiveMail();
        server.postMail(mail, this);
    }

    /**
     * When client sends a {@link NotEOFEvent} this method is called by this
     * base class itself.
     * <p>
     * The service forwards the event to the server which informs all observers
     * about the incoming event.
     * 
     * @throws ActionFailedException
     */
    public synchronized void processClientEvent() throws ActionFailedException {
        NotEOFEvent event = getTalkLine().receiveBaseEvent(Server.getApplicationHome());
        postEvent(event, this);
    }

    protected synchronized void postEvent(NotEOFEvent event, Service service) throws ActionFailedException {
        server.postEvent(event, service);
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
    public abstract void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException;

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

    public String getName() {
        if (null == observerName) {
            Random rnd = new Random();
            rnd.setSeed(new Date().getTime());
            observerName = this.getClass().getSimpleName() + ":#" + hashCode() + ":sid" + serviceId + ":?" + rnd.nextInt();
        }
        return observerName;
    }
}
