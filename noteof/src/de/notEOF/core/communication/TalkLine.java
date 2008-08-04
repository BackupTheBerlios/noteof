package de.notEOF.core.communication;

import java.net.ConnectException;
import java.net.Socket;

import de.notEOF.core.exception.ActionFailedException;

/**
 * Class for Communication of !EOF processes. Here are implemented some methods
 * to send and receive messages. <br>
 * Furthermore there is a little comfort by the methods requestToPartner and
 * responseToPartner, which ensure the synchronisation of messages and ensure,
 * that the correct messageheaders will be used.<br>
 * 
 * @see CommTag The class {@link CommTag} contains messageheaders.
 */
public class TalkLine {

    private SocketLayer socketLayer;

    /**
     * Construction of a new notEOF communication (normally with a client) using
     * a socket connection which must be instanciated by the calling class.
     * 
     * @since 0.1 beta
     * @param socketToPartner
     *            Existing socket connection
     * @param timeOutMillis
     *            Timeout in milliseconds, used by the other communikation
     *            methods of this class and perhaps underlying classes. <br>
     *            Is timeout equal to 0, most methods block (read readMsg,
     *            requestToPartner, awaitPartnerRequest, ...)
     * @throws ActionFailedException
     *             Thrown when socket connection could not be established.
     */
    public TalkLine(Socket socketToPartner, int timeOutMillis) throws ActionFailedException {
        if (null == socketToPartner)
            throw new ActionFailedException(10L, "Socket zu Kommunikationspartner ist NULL");
        try {
            socketLayer = new SocketLayer(socketToPartner);
            if (0 < timeOutMillis)
                socketLayer.setTimeOut(timeOutMillis);

        } catch (Exception ex) {
            throw new ActionFailedException(10L, ex);
        }
    }

    /**
     * Construction of client-server-communication by ip address and server
     * port.
     * 
     * @since 0.1 beta
     * @param ip
     *            ip address of server
     * @param port
     *            port within the server to connect to
     * @param timeOutMillis
     *            Timeout in milliseconds, used by the other communikation
     *            methods of this class and perhaps underlying classes. <br>
     *            Is timeout equal to 0, most methods block (read readMsg,
     *            requestToPartner, awaitPartnerRequest, ...)
     * @throws ActionFailedException
     *             Thrown when socket connection could not be established.
     */
    public TalkLine(String ip, int port, int timeOutMillis) throws ActionFailedException {
        try {
            Socket socket = new Socket(ip, port);
            socketLayer = new SocketLayer(socket);
            if (0 < timeOutMillis)
                socketLayer.setTimeOut(timeOutMillis);
        } catch (ConnectException cex) {
            throw new ActionFailedException(10L, "Server nicht erreichbar.", cex);
        } catch (Exception ex) {
            throw new ActionFailedException(10L, "IP: " + ip + "; Port: " + String.valueOf(port), ex);
        }
    }

    public int getPort() {
        return socketLayer.getSocketToPartner().getLocalPort();
    }

    public String getHostAddress() {
        return socketLayer.getSocketToPartner().getInetAddress().getHostAddress();
    }

    public boolean isConnected() {
        return socketLayer.isConnected();
    }

    /**
     * Close of communication connection.
     */
    public void close() {
        socketLayer.close();
    }

    /**
     * Response to the communication partner after receiving a request.
     * 
     * @since 0.1 beta
     * @param respHeader
     *            Must match the header which is demand by the partner.
     * @param value
     *            The returned value, for what the partner is waiting.
     * @throws ActionFailedException
     *             Thrown when communication timeout > 0 and the timeout has
     *             been exceeded.
     */
    @SuppressWarnings("unchecked")
    public void responseTo(Enum respHeader, String value) throws ActionFailedException {
        socketLayer.responseToPartner(respHeader.name(), value);
    }

    /**
     * Fires a request to the communication partner.
     * 
     * @since 0.1 beta
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
    @SuppressWarnings("unchecked")
    public String requestTo(Enum requestHeader, Enum expectedRespHeader) throws ActionFailedException {
        return socketLayer.requestToPartner(requestHeader.name(), expectedRespHeader.name());
    }

    /**
     * Method for awaiting a special info message of the communication partner. <br>
     * This method is only implemented for clearance.
     * 
     * @since 0.1 beta
     * @param expectedInfoHeader
     *            Expected info message header
     * @throws ActionFailedException
     *             Thrown when response header is not equal to the expected one
     *             or when the communication timeout > 0 and the timeout has
     *             been exceeded.
     */
    @SuppressWarnings("unchecked")
    public void awaitInfo(Enum expectedInfoHeader) throws ActionFailedException {
        awaitRequest(expectedInfoHeader);
    }

    /**
     * Method for awaiting a special request of the communication partner. <br>
     * 
     * @since 0.1 beta
     * 
     * @param expectedRequestHeader
     *            Expected message header
     * @throws ActionFailedException
     *             Thrown when request header is not equal to the expected one
     *             or when the communication timeout > 0 and the timeout has
     *             been exceeded.
     */
    @SuppressWarnings("unchecked")
    public void awaitRequest(Enum expectedRequestHeader) throws ActionFailedException {
        socketLayer.awaitPartnerRequest(expectedRequestHeader.name());
    }

    /**
     * Awaits a distinct request and sends response immediately. <br>
     * Simplifies the code by concentrating the methods awaitRequest() and
     * responseTo().
     * 
     * @since 0.1 beta
     * @param expectedRequestHeader
     *            Header which is expected by the partner message
     * @param respHeader
     *            Header which will be sent after the request
     * @param value
     *            The value which the communication partner is waiting for
     * @throws ActionFailedException
     */
    @SuppressWarnings("unchecked")
    public void awaitRequestAnswerImmediate(Enum expectedRequestHeader, Enum respHeader, String value) throws ActionFailedException {
        awaitRequest(expectedRequestHeader);
        responseTo(respHeader, value);
    }

    /**
     * Read a message ignoring the adjusted timeout before. <br>
     * That means that this method blocks any other action...<br>
     * Is useful in some situations.
     * 
     * @since 0.1 beta
     * @return One line message text
     */
    public String readMsgNoTimeOut() throws ActionFailedException {
        return readMsgTimedOut(0);
    }

    public boolean lifeSignSucceeded() {
        return socketLayer.lifeSignSucceeded();
    }

    /**
     * Read a message with a special timeout. <br>
     * Ignores the adjusted timeout for awaiting the message. <br>
     * Attention: In case of throwing ActionFailedException it is possible that
     * the old timeout is overwritten.
     * 
     * @since 0.1 beta
     * @param timeOutMillis
     *            The timeout which is used for reading a message.
     * @return The message of the communication partner.
     * @throws ActionFailedException
     */
    public String readMsgTimedOut(int timeOutMillis) throws ActionFailedException {
        // try {
        int oldTimeOut = socketLayer.getTimeOut();
        socketLayer.setTimeOut(timeOutMillis);
        String msgValue = socketLayer.readMsg();
        socketLayer.setTimeOut(oldTimeOut);
        return msgValue;
        // } catch (Exception ex) {
        // throw new ActionFailedException(7522,
        // "Setzen des Timeouts für Leseoperation auf " + timeOutMillis, ex);
        // }
    }

    /**
     * Method to receive a message from the communication partner.
     * 
     * @since 0.1 beta
     * @return Delivers one line (String terminated with cr) out of the write
     *         buffer of the communication partner.
     * @throws ActionFailedException
     */
    public String readMsg() throws ActionFailedException {
        return socketLayer.readMsg();
    }

    /**
     * Sends text to the communication partner. In this version the text mustn't
     * be longer than one line. The new line character isn't allowed.
     * 
     * @since 0.1 beta
     * 
     * @param msg
     *            The message text. Mustn't contain cr (except at the end).
     * @throws ActionFailedException
     */
    public void writeMsg(String msg) throws ActionFailedException {
        socketLayer.writeMsg(msg);
    }

    /**
     * Activates the system for watching lifesigns between the communication
     * partners.
     * 
     * @param asClient
     *            Please set this to TRUE if the class which uses TalkLine is a
     *            client and to FALSE if it is a service. <br>
     *            If this parameter is null, the system will be started with
     *            special logic for service. <br>
     *            Please consider that the system must be used in a correct
     *            manner for client or service requirements.
     */
    public void activateLifeSignSystem(Boolean asClient) {
        socketLayer.activateLifeSignSystem(asClient);
    }
}
