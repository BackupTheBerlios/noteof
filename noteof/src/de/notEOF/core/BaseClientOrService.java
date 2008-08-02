package de.notEOF.core;

import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;

public abstract class BaseClientOrService {

    protected String serviceId;
    protected TalkLine talkLine;

    /**
     * Delivers a valid message interface to the server
     * 
     * @return An initialized Object which simplifies the communication with the server
     */
    public TalkLine getTalkLine() {
        return talkLine;
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
     * Meldet sich vom Server ab und schlieﬂt die physikalische Verbindung.
     * 
     * @throws ActionFailedException
     */
    protected void close() throws ActionFailedException {
        talkLine.close();
    }

    /**
     * Every specialized client/service may use it's own class for timeouts derived from BaseTimeOut. <br>
     * For using your own Timeout overwrite this method in your service/client class.<br>
     * Sample: return new MySpecialTimeout();
     * 
     * @return Your own Object derived from {@link BaseTimeOut}.
     */
    protected TimeOut getTimeOutObject() {
        return new BaseTimeOut();
    }

    protected void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Activates the LifeSignSystem.<br>
     * When the system is activated the client periodicaly sends lifesigns to the service when he has nothing to do. <br>
     * So the service is informed that the client is alive. It is only meaningful to do this if for the type of service which the client is
     * bounded to, is the system activated too.
     */
    protected void activateLifeSignSystem(boolean asClient) {
        talkLine.activateLifeSignSystem(asClient);
    }

    public String requestTo(Enum<?> requestHeader, Enum<?> expectedResponseHeader) throws ActionFailedException {
        return talkLine.requestTo(requestHeader, expectedResponseHeader);
    }

    public void awaitRequest(Enum<?> expectedRequestHeader) throws ActionFailedException {
        talkLine.awaitRequest(expectedRequestHeader);
    }

    public void awaitRequestAnswerImmediate(Enum<?> expectedRequestHeader, Enum<?> responseHeader, String value) throws ActionFailedException {
        talkLine.awaitRequestAnswerImmediate(expectedRequestHeader, responseHeader, value);

    }

    public void responseTo(Enum<?> responseHeader, String value) throws ActionFailedException {
        talkLine.responseTo(responseHeader, value);
    }

    public String readMsg() throws ActionFailedException {
        return talkLine.readMsg();
    }

    public String readMsgNoTimeOut() throws ActionFailedException {
        return talkLine.readMsgNoTimeOut();
    }

    public String readMsgTimedOut(int timeOutMillis) throws ActionFailedException {
        return talkLine.readMsgTimedOut(timeOutMillis);
    }

    public void writeMsg(String msg) throws ActionFailedException {
        talkLine.writeMsg(msg);
    }

}
