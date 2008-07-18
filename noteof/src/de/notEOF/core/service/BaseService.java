package de.notEOF.core.service;

import java.net.Socket;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.BaseTimeout;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.Timeout;
import de.notEOF.core.logging.LocalLog;
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
public abstract class BaseService implements Runnable, Service {

    private String serviceId;
    private boolean connectedWithClient = false;
    private TalkLine talkLine;
    private boolean stop = false;
    private long nextLifeSign;

//    /**
//     * If you don't know what to do with the constructor of your derived class -
//     * call this constructor... :<br>
//     * super(socetToClient);
//     * 
//     * @param socketToClient
//     */
//    public BaseService(Socket socketToClient, Timeout timeOut) throws ActionFailedException {
//        if (null == timeOut)
//            timeOut = new BaseTimeout();
//        talkLine = new TalkLine(socketToClient, timeOut.getMillisCommunication());
//    }
    
    public void init(Socket socketToClient, String serviceId) throws ActionFailedException {
        setServiceId(serviceId);
        Timeout timeOut = getTimeOutObject();
        if (null == timeOut)
            timeOut = new BaseTimeout();
        talkLine = new TalkLine(socketToClient, timeOut.getMillisCommunication());
    }

    public boolean isConnectedWithClient() {
        return connectedWithClient;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void stopService() {
        stop = true;
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
        talkLine.activateLifeSignSystem(false);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (!stop) {
            try {
                String msg = talkLine.readMsgTimedOut(NotEOFConstants.LIFE_TIME_INTERVAL_SERVICE);

                // Check if the lifetime hasn't send longer than allowed
                // or if any other messages came within the max. allowed time.
                if (Util.isEmpty(msg) && nextLifeSign < System.currentTimeMillis()) {
                    // no message within the lifetime interval
                    // stop service
                    stop = true;
                    break;
                }

                // Some messages are valid for every service and must be accept
                // by them.
                // Typical events like stop etc. are processed here.

                // Client sends stop signal

                // The rest of messages is client/service specific and must be
                // processed in the method handleMsg() which must be implemented
                // individual in every service.
                Class<Enum> enumClass = (Class<Enum>) getCommunicationTagClass();
                try {
                    validateMsgToEnum(enumClass, msg);
                } catch (ActionFailedException afx) {
                    LocalLog.error("Mapping der Nachricht auf Enum.", afx);
                }
                processMsg(Enum.valueOf(enumClass, msg));

            } catch (ActionFailedException afx) {
                LocalLog.error("Zentrale Entgegennahme von Client-Nachrichten im Service", afx);
            }
        }

        // close socket to client
        try {
            talkLine.close();
        } catch (Exception ex) {
            LocalLog.warn("Verbindung zum Client konnte nicht geschlossen werden. Evtl. bestand zu diesem Zeitpunkt keien Verbindung (mehr).", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateMsgToEnum(Class<Enum> enumClass, String msg) throws ActionFailedException {
        try {
            Enum.valueOf(enumClass, msg);
        } catch (Exception ex) {
            throw new ActionFailedException(151L, "Empfangene Nachricht: " + msg);
        }
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
     * Every specialized client/service may use it's own class for timeouts derived from BaseTimeOut. <br>
     * If an own class is defined this method must be overwritten.<br>
     * Sample: return new MySpecialTimeout();
     * 
     * @return Your own Object derived from {@link BaseTimeout}.
     */
    protected BaseTimeout getTimeOutObject() {
        return new BaseTimeout();
    }

    /**
     * This abstract method is called by the BaseService class. Here messages
     * must be processed which are specific for the client and the service.
     * 
     * @param incomingMsgEnum
     */
    public abstract void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException ;
}
