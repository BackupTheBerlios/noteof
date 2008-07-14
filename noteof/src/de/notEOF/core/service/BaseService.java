package de.notEOF.core.service;

import java.net.Socket;

import de.notEOF.core.communication.BaseTimeout;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Timeout;

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

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            String msg = talkLine.readMsg();

            // Lifetime abarbeiten

            // Stop vom Client verarbeiten

            // Weiterreichen der message als enum an abgeleitete Klasse
            Class<Enum> enumClass = (Class<Enum>) getCommunicationTags();
            try {
                validateMsgToEnum(enumClass, msg);
            } catch (ActionFailedException afx) {
                // TODO Fehler abfangen, wenn Nachricht nicht auf Enum gemappt
                // werden kann
                // --> Undefined Msg for this service ....
            }
            handleMsg(Enum.valueOf(enumClass, msg));

        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void validateMsgToEnum(Class<Enum> enumClass, String msg) throws ActionFailedException {
        try {
            Enum.valueOf(enumClass, msg);
        } catch (IllegalArgumentException ix) {
            // TODO
        } catch (NullPointerException nx) {
            // TODO
        }
    }

    protected abstract Class<?> getCommunicationTags();

    @SuppressWarnings("unchecked")
    protected abstract void handleMsg(Enum incomingMsgEnum);
}
