package de.notEOF.core.communication;

import java.net.ConnectException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EventFinder;
import de.notEOF.core.event.TransportEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;

/**
 * Class for Communication of !EOF processes. Here are implemented some methods
 * to send and receive messages. <br>
 * Furthermore there is a little comfort by the methods requestToPartner and
 * responseToPartner, which ensure the synchronisation of messages and ensure,
 * that the correct messageheaders will be used.<br>
 * 
 * @see CommTag The class {@link CommTag} contains messageheaders.
 */
public class TalkLine implements Observer {

    private SocketLayer socketLayer;
    private Socket socketToPartner = null;

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

        this.socketToPartner = socketToPartner;
        try {
            socketLayer = new SocketLayer(socketToPartner);
            if (0 < timeOutMillis)
                socketLayer.setTimeOut(timeOutMillis);

        } catch (Exception ex) {
            // perhaps the connection is established...
            // try to close
            try {
                close();
            } catch (Exception ex2) {
                // do nothing
            }
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
            this.socketToPartner = socket;
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
     * Receive more complex data from partner. <br>
     * 
     * @see DataObject
     * @return A new DataObject which hopefully stores the received data.
     * @throws ActionFailedException
     */
    public DataObject receiveDataObject() throws ActionFailedException {
        return socketLayer.receiveDataObject();
    }

    /**
     * Send more complex data to partner.<br>
     * 
     * @param sendObject
     *            A DataObject which must be initialized with data and dataType
     *            before.
     * @see DataObject
     * @throws ActionFailedException
     */
    public void sendDataObject(DataObject dataObject) throws ActionFailedException {
        socketLayer.sendDataObject(dataObject);
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
    public void responseTo(Enum<?> respHeader, String value) throws ActionFailedException {
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
    public String requestTo(Enum<?> requestHeader, Enum<?> expectedRespHeader) throws ActionFailedException {
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
    public void awaitInfo(Enum<?> expectedInfoHeader) throws ActionFailedException {
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
    public void awaitRequest(Enum<?> expectedRequestHeader) throws ActionFailedException {
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
    public void awaitRequestAnswerImmediate(Enum<?> expectedRequestHeader, Enum<?> respHeader, String value) throws ActionFailedException {
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
        int oldTimeOut = socketLayer.getTimeOut();
        socketLayer.setTimeOut(timeOutMillis);
        String msgValue = socketLayer.readMsg();
        socketLayer.setTimeOut(oldTimeOut);
        return msgValue;
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

    public void update(Observable arg0, Object arg1) {
        socketLayer.close();
    }

    public synchronized NotEOFEvent receiveBaseEvent(String applicationHome) throws ActionFailedException {
        try {
            // class name and eventType
            DataObject eventInfo = receiveDataObject();
            String canonicalName = eventInfo.getMap().get("canonicalName");
            String eventTypeOrdinal = eventInfo.getMap().get("eventTypeOrdinal");
            EventType eventType = EventType.values()[Integer.valueOf(eventTypeOrdinal)];

            // receive attributes
            DataObject mapData = receiveDataObject();
            Map<String, String> attributes = mapData.getMap();
            // receive descriptions
            mapData = receiveDataObject();
            Map<String, String> descriptions = mapData.getMap();

            NotEOFEvent event = null;
            try {
                // try to load the same event class as was sent from other side
                // of line.
                event = EventFinder.getNotEOFEvent(applicationHome, canonicalName);
                event.setAttributes(attributes);
                event.setDescriptions(descriptions);
                // event.setEventType(eventType);
            } catch (Exception e) {
                // class couldn't be loaded. So use default one.
                event = new TransportEvent(eventType, attributes, descriptions);
            }
            writeMsg("OK");
            return event;
        } catch (Exception e) {
            throw new ActionFailedException(1151L, "Generieren des TransportEvents", e);
        }
    }

    public synchronized NotEOFMail receiveMail() throws ActionFailedException {
        DataObject contentObject = receiveDataObject();
        Map<String, String> content = contentObject.getMap();

        NotEOFMail mail = new NotEOFMail();
        mail.setToClientNetId(content.get("toClientNetId"));
        mail.setFromClientNetId(content.get("fromClientNetId"));
        mail.setHeader(content.get("header"));
        mail.setMailId(content.get("mailId"));
        mail.setDestination(content.get("destination"));

        Date generated = new Date();
        Long dateAsLong = Util.parseLong(content.get("generated"), 0);
        generated.setTime(dateAsLong);
        mail.setGenerated(generated);

        mail.setBodyText(content.get("bodyText"));

        String isDataObjectSet = readMsg();
        if ("TRUE".equals(isDataObjectSet)) {
            DataObject bodyData = receiveDataObject();
            mail.setBodyData(bodyData);
        }

        return mail;
    }

    public synchronized void sendMail(NotEOFMail mail) throws ActionFailedException {
        // send message informations
        Map<String, String> envelope = new HashMap<String, String>();
        envelope.put("toClientNetId", mail.getToClientNetId());
        envelope.put("fromClientNetId", mail.getFromClientNetId());
        envelope.put("header", mail.getHeader());
        envelope.put("mailId", mail.getMailId());
        envelope.put("destination", mail.getDestination());
        envelope.put("generated", String.valueOf(mail.getGenerated().getTime()));
        envelope.put("bodyText", mail.getBodyText());

        DataObject envelopeObject = new DataObject();
        envelopeObject.setMap(envelope);
        sendDataObject(envelopeObject);

        // body data
        if (null == mail.getBodyData()) {
            writeMsg("FALSE");
        } else {
            // there is a body data object to transmit
            writeMsg("TRUE");
            sendDataObject(mail.getBodyData());
        }
    }

    /**
     * Sends an event.
     * <p>
     * This method is only able to send basically event datas. The Event must
     * extended from {@link NotEOFEvent}. Additional members or functionalities
     * are not supported by this method.
     * 
     * @param event
     *            The event to send.
     * @throws ActionFailedException
     */
    public synchronized void sendBaseEvent(NotEOFEvent event) throws ActionFailedException {
        // set timestamp
        event.setTimeStampSend();

        // send className and eventType as ordinal value
        Map<String, String> infos = new HashMap<String, String>();
        infos.put("canonicalName", event.getClass().getCanonicalName());
        infos.put("eventTypeOrdinal", String.valueOf(event.getEventType().ordinal()));

        DataObject eventInfo = new DataObject();
        eventInfo.setMap(infos);
        sendDataObject(eventInfo);
        // send attributes
        DataObject mapData = new DataObject();
        mapData.setMap(event.getAttributes());
        sendDataObject(mapData);
        // send descriptions
        mapData = new DataObject();
        mapData.setMap(event.getAttributeDescriptions());
        sendDataObject(mapData);

        if (!"OK".equals(readMsg())) {
            throw new ActionFailedException(23L, "Keine Empfangsbestaetigung erhalten");
        }
    }

    /**
     * @return the socketToPartner
     */
    public Socket getSocketToPartner() {
        return socketToPartner;
    }
}
