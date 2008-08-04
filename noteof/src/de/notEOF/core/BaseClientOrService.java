package de.notEOF.core;

import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;

public abstract class BaseClientOrService {

    protected String serviceId;
    protected TalkLine talkLine;

    /**
     * Delivers the port of the connected service.
     * 
     * @return port
     */
    protected final int getPartnerPort() {
        return getTalkLine().getPort();
    }

    /**
     * Delivers the ip of the connected service.
     * 
     * @return ip (host address)
     */
    protected final String getPartnerHostAddress() {
        return getTalkLine().getHostAddress();
    }

    /**
     * Delivers a valid message interface to the server
     * 
     * @return An initialized Object which simplifies the communication with the
     *         server
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
     * Disconnect from server, close physical connection.
     * 
     * @throws ActionFailedException
     */
    public void close() throws ActionFailedException {
        talkLine.close();
    }

    /**
     * Every specialized client/service may use it's own class for timeouts
     * derived from BaseTimeOut. <br>
     * For using your own Timeout overwrite this method in your service/client
     * class.<br>
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
     * When the system is activated the client periodicaly sends lifesigns to
     * the service when he has nothing to do. <br>
     * So the service is informed that the client is alive. It is only
     * meaningful to do this if for the type of service which the client is
     * bounded to, is the system activated too.
     */
    protected final void activateLifeSignSystem(boolean asClient) {
        talkLine.activateLifeSignSystem(asClient);
    }

    /**
     * Fires a request to the communication partner.
     * 
     * @param requestHeader
     *            Special header, which qualifies the request.
     * @param expectedRespHeader
     *            Expected response header.
     * @return The value of the response, which also can be "".
     * @throws ActionFailedException
     *             Thrown when response header is not equal to the expected one
     *             or when the communication timeout > 0 and the timeout has
     *             been exceeded.
     */
    public String requestTo(Enum<?> requestHeader, Enum<?> expectedResponseHeader) throws ActionFailedException {
        return talkLine.requestTo(requestHeader, expectedResponseHeader);
    }

    /**
     * Method for awaiting a special request of the communication partner. <br>
     * 
     * @param expectedRequestHeader
     *            Expected message header
     * @throws ActionFailedException
     *             Thrown when request header is not equal to the expected one
     *             or when the communication timeout > 0 and the timeout has
     *             been exceeded.
     */
    public void awaitRequest(Enum<?> expectedRequestHeader) throws ActionFailedException {
        talkLine.awaitRequest(expectedRequestHeader);
    }

    /**
     * Awaits a distinct request and sends response immediately. <br>
     * Simplifies the code by concentrating the methods awaitRequest() and
     * responseTo().
     * 
     * @param expectedRequestHeader
     *            Header which is expected by the partner message
     * @param respHeader
     *            Header which will be sent after the request
     * @param value
     *            The value which the communication partner is waiting for
     * @throws ActionFailedException
     */
    public void awaitRequestAnswerImmediate(Enum<?> expectedRequestHeader, Enum<?> responseHeader, String value) throws ActionFailedException {
        talkLine.awaitRequestAnswerImmediate(expectedRequestHeader, responseHeader, value);

    }

    /**
     * Response to the communication partner after receiving a request.
     * 
     * @param respHeader
     *            Must match the header which is demand by the partner.
     * @param value
     *            The returned value, for what the partner is waiting.
     * @throws ActionFailedException
     *             Thrown when communication timeout > 0 and the timeout has
     *             been exceeded.
     */
    public void responseTo(Enum<?> responseHeader, String value) throws ActionFailedException {
        talkLine.responseTo(responseHeader, value);
    }

    /**
     * Method to receive a message from the communication partner.
     * 
     * @return Delivers one line (String terminated with cr) out of the write
     *         buffer of the communication partner.
     * @throws ActionFailedException
     */
    public String readMsg() throws ActionFailedException {
        return talkLine.readMsg();
    }

    /**
     * Read a message ignoring the adjusted timeout before. <br>
     * That means that this method blocks any other action...<br>
     * Is useful in some situations.
     * 
     * @return One line message text
     */
    public String readMsgNoTimeOut() throws ActionFailedException {
        return talkLine.readMsgNoTimeOut();
    }

    /**
     * Read a message with a special timeout. <br>
     * Ignores the adjusted timeout for awaiting the message. <br>
     * Attention: In case of throwing ActionFailedException it is possible that
     * the old timeout is overwritten.
     * 
     * @param timeOutMillis
     *            The timeout which is used for reading a message.
     * @return The message of the communication partner.
     * @throws ActionFailedException
     */
    public String readMsgTimedOut(int timeOutMillis) throws ActionFailedException {
        return talkLine.readMsgTimedOut(timeOutMillis);
    }

    /**
     * Sends text to the communication partner. In this version the text mustn't
     * be longer than one line. The new line character isn't allowed.
     * 
     * @param msg
     *            The message text. Mustn't contain cr (except at the end).
     * @throws ActionFailedException
     */
    public void writeMsg(String msg) throws ActionFailedException {
        talkLine.writeMsg(msg);
    }

    /**
     * Sends text to the communication partner. In this version the text mustn't
     * be longer than one line. The new line character isn't allowed.
     * 
     * @param requestHeader
     *            The header like defined in an enum.
     * @throws ActionFailedException
     */
    public void writeMsg(Enum<?> requestHeader) throws ActionFailedException {
        talkLine.writeMsg(requestHeader.name());
    }
}
