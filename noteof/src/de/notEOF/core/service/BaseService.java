package de.notEOF.core.service;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.BaseClientOrService;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.NewMailEvent;
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
import de.notEOF.core.mail.NotEOFMail;
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
    List<EventType> eventTypes;
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
    public void update(Service service, NotEOFEvent event) {
        if (EventType.EVENT_NEW_MSG.equals(event.getEventType())) {
            // search for msg
            NotEOFMail mail = server.getMail(((NewMailEvent) event).getMailId());

            // wenn mail fuer service bestimmt war,
            // erstmal dem Server mitteilen, dass es einen recipienten gibt,
            // dann an client senden.
            // hier nur eine simple unnuetze Implementierung...
            // Normalerweise sollte sich der Service fuer den Header
            // interessieren.
            // Oder z.B. in Happtick fuer die destination, die eine
            // ApplikationsId enthaelt...
            if (mail.getDestination().equals(getServiceId())) {
                try {
                    server.relateMailToRecipient(mail, this);
                    mailToClient(mail);
                } catch (ActionFailedException e) {
                    LocalLog.warn("Mehrere Services versuchen auf eine Nachricht zuzugreifen. Header: " + mail.getHeader());
                }
            }
        }
    }

    // TODO Nachricht (Request) geht an Server. Der erzeugt ein msg event. die
    // observer (services) pruefen, ob die ApplicationId im Event passt. Dann
    // holen sie sich die Nachricht aus dem nachrichtenpool der mastertable
    // ueber die msgId und senden sie an den client.
    // Der client muss antworten mit response. die response geht ebenfalls in
    // den pool. Dann werden wieder alle services benachrichtig. diesmal wird
    // die serviceid des empfaengers (gleichzeitig eindeutig fuer client)
    // verwendet. Nur der eine client mit dieser id erhaelt die antwort.
    // Aehnlich laeuft's bei events. allerdings wird dafuer keine antwort
    // erwartet.
    public final void mailFromClient(String msg) {
        // muss die requestMailId enthalten
    }

    // TODO implementieren
    public final void mailToClient(NotEOFMail mail) {

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
                        processClientMsg(validateEnum(tagEnumClass, msg));
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
        System.out.println("Service wird beendet: " + this.getClass().getCanonicalName() + "; id: " + getServiceId());
        isRunning = false;
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
        if (null == eventTypes) {
            eventTypes = new ArrayList<EventType>();
            eventTypes.add(EventType.EVENT_NEW_MSG);
        }
        return eventTypes;
    }

    protected void addObservedEventType(EventType type) {
        if (null == eventTypes) {
            eventTypes = new ArrayList<EventType>();
            eventTypes.add(EventType.EVENT_NEW_MSG);
        }
        eventTypes.add(type);
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
}
