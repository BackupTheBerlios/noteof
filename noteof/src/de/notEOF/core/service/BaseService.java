package de.notEOF.core.service;

import java.net.Socket;

import de.notEOF.core.BaseClientOrService;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.logging.LocalLog;

/**
 * Basic class for every !EOF Service.
 * <p>
 * During services will run as threads to serve the clients simultaneously. Therefore derived classes of this class must be {@link Runnable}
 * and have to implement the method run(). <br>
 * The run method is a good place to accept the individual requests of the clients and process them.<br>
 * 
 * @author Dirk
 * 
 */
public abstract class BaseService extends BaseClientOrService implements Service, Runnable {

    private boolean connectedWithClient = false;
    private boolean stopped = false;
    public boolean isRunning = true;
    private Thread serviceThread;

    // /**
    // * If you don't know what to do with the constructor of your derived class
    // -
    // * call this constructor... :<br>
    // * super(socetToClient);
    // *
    // * @param socketToClient
    // */
    // public BaseService(Socket socketToClient, Timeout timeOut) throws
    // ActionFailedException {
    // if (null == timeOut)
    // timeOut = new BaseTimeout();
    // talkLine = new TalkLine(socketToClient,
    // timeOut.getMillisCommunication());
    // }

    public boolean isRunning() {
        return isRunning;
    }

    public void init(Socket socketToClient, String serviceId) throws ActionFailedException {
        setServiceId(serviceId);
        TimeOut timeOut = getTimeOutObject();
        talkLine = new TalkLine(socketToClient, timeOut.getMillisCommunication());
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
     * Activates the LifeSignSystem to ensure that the client is alive. <br>
     * When the system is activated the service awaits that it's client sends messages within a hardly defined time in the class
     * {@link NotEOFConstants}.<br>
     * If the LifeSignSystem is activated for the service, it is very recommendable to activate it for every client which uses this type of
     * service too!
     * 
     * @see BaseClient
     * @see NotEOFConstants
     */
    public void activateLifeSignSystem() {
        super.activateLifeSignSystem(false);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (!stopped) {
            try {
                System.out.println("Service vor talkLine.readMsg...");
                String msg = talkLine.readMsgTimedOut(NotEOFConstants.LIFE_TIME_INTERVAL_SERVICE);
                System.out.println("Service nach talkLine.readMsg...");

                // Check if the lifetime hasn't send longer than allowed
                // or if any other messages came within the max. allowed time.
                if (talkLine.lifeSignSucceeded()) {
                    // if (Util.isEmpty(msg) && nextLifeSign <
                    // System.currentTimeMillis()) {
                    // no message within the lifetime interval
                    // stop service
                    System.out.println("hier");
                    stopped = true;
                    break;
                }

                // Some messages are valid for every service and must be accept
                // by them.
                // Typical events like stop etc. are processed here.

                // Client sends stop signal

                // The rest of messages is client/service specific and must be
                // processed in the method handleMsg() which must be implemented
                // individual in every service.
                Class<Enum> tagEnumClass = (Class<Enum>) getCommunicationTagClass();
                System.out.println("enum = " + tagEnumClass.getCanonicalName());
                try {
                    processMsg(validateEnum(tagEnumClass, msg));
                } catch (ActionFailedException afx) {
                    LocalLog.error("Mapping der Nachricht auf Enum.", afx);
                }

            } catch (ActionFailedException afx) {
                // What happened?
                // errNo 24L is ok - timeout at read action
                // Socket communication problem
                if (afx.getErrNo() == 23L) {
                    LocalLog.info("Kommunikation mit Client ist unterbrochen. Service wird beendet.");
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
            talkLine.close();
        } catch (Exception ex) {
            LocalLog.warn("Verbindung zum Client konnte nicht geschlossen werden. Evtl. bestand zu diesem Zeitpunkt keien Verbindung (mehr).", ex);
        }
        System.out.println("Service stopped");
        isRunning = false;
    }

    @SuppressWarnings("unchecked")
    private Enum validateEnum(Class<Enum> tagEnumClass, String msg) throws ActionFailedException {
        // try {
        Enum[] y = tagEnumClass.getEnumConstants();
        System.out.println("Anzahl: " + y.length);
        for (int i = 0; i < y.length; i++) {
            if (y[i].name().equals(msg)) {
                System.out.println("hoppla");
                return y[i];
            }
        }
        return null;
        // throw new ActionFailedException(151L, "Validierung der Empfangenen Nachricht: " + msg);
    }

    /**
     * Every specialized client/service has it's own Enum which defines the constant tags. This method is the reasaon why there mustn't be
     * more than one Enum(class) for every client/server solution. <br>
     * The developer implements this method in the simple manner that he returns his specialized Enum class.<br>
     * Sample: return MySpecialTag.class();
     * 
     * @return
     */
    public abstract Class<?> getCommunicationTagClass();

    /**
     * This abstract method is called by the BaseService class. Here messages must be processed which are specific for the client and the
     * service.
     * 
     * @param incomingMsgEnum
     */
    public abstract void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException;

}
