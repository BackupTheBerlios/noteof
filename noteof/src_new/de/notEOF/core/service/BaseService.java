package de.notEOF.core.service;

import java.net.Socket;

import de.notEOF.core.communication.BaseTimeout;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.constants.NotEOFConstants;
import de.notEOF.core.exception.ActionFailedException;
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
public abstract class BaseService implements Runnable {

    private String serviceId;
    private boolean connectedWithClient = false;
    private TalkLine talkLine;
    private boolean stop = false;

    /**
     * If you don't know what to do with the constructor of your derived class -
     * call this constructor... :<br>
     * super(socetToClient);
     * 
     * @param socketToClient
     */
    public BaseService(Socket socketToClient, Timeout timeOut) throws ActionFailedException {
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

    @SuppressWarnings("unchecked")
    public void run() {
        long lastLifeSign = System.currentTimeMillis() + NotEOFConstants.LIFE_TIME_INTERVAL;
        // TODO: Nochmal �berlegen, ob hier der Wert nicht gr��er sein muss, als
        // beinm client
        while (!stop) {
            try {
                String msg = talkLine.readMsgTimedOut(NotEOFConstants.LIFE_TIME_INTERVAL);

                // Check if the lifetime hasn't send longer than allowed
                if (Util.isEmpty(msg) && lastLifeSign < System.currentTimeMillis()) {
                    // TODO
                    // Wenn msg leer, dann Service stoppen, weil lifesigen nicht
                    // eingetroffen ist.
                    // Wenn msg nicht leer, lifesign inkrementieren, da jede msg
                    // wie ein Lebenszeichen ist.
                }

                // Some messages are valid for every service and must be accept
                // by
                // service.
                // Typical events like stop, lifetime, etc. are processed here.

                // Client sends lifetime event

                // Client sends stop signal

                // The rest of messages is client/service specific and must be
                // processed in the method handleMsg() which must be implemented
                // individual in every service.
                Class<Enum> enumClass = (Class<Enum>) getCommunicationTags();
                try {
                    validateMsgToEnum(enumClass, msg);
                } catch (ActionFailedException afx) {
                    LocalLog.error("Mapping der Nachricht auf Enum.", afx);
                }
                processMsg(Enum.valueOf(enumClass, msg));

                lastLifeSign = System.currentTimeMillis() + NotEOFConstants.LIFE_TIME_INTERVAL;
            } catch (ActionFailedException afx) {
                LocalLog.error("Zentrale Entgegennahme von Client-Nachrichten im Service", afx);
            }
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
    protected abstract Class<?> getCommunicationTags();

    /**
     * This abstract method is called by the BaseService class. Here messages
     * must be processed which are specific for the client and the service.
     * 
     * @param incomingMsgEnum
     */
    @SuppressWarnings("unchecked")
    protected abstract void processMsg(Enum incomingMsgEnum);
}