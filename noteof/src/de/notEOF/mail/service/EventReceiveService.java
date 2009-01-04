package de.notEOF.mail.service;

import java.util.List;

import de.notEOF.core.communication.DataObject;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.NewMailEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.mail.MailHeaders;
import de.notEOF.mail.MailToken;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;

/**
 * This class is NOT build to send mails or events FROM client. <br>
 * This class ist build to send new mails or events TO client.
 * <p>
 * 
 * @author Dirk
 */
public class EventReceiveService extends BaseService implements EventObserver {

    private MailToken mailDestinations;
    private MailHeaders mailHeaders;
    private TalkLine talkLine;
    private String clientNetId;

    protected void addInterestingHeader(String header) {
        if (null == mailHeaders)
            mailHeaders = new MailHeaders();
        mailHeaders.add(header);
    }

    protected void addInterestingHeaders(List<String> headers) {
        if (null == this.mailHeaders)
            this.mailHeaders = new MailHeaders();
        this.mailHeaders.addAll(headers);
    }

    protected void addInterestingDestination(String destination) {
        if (null == mailDestinations)
            mailDestinations = new MailToken();
        mailDestinations.add(destination);
    }

    protected void addInterestingDestinations(List<String> destinations) {
        if (null == mailDestinations)
            mailDestinations = new MailToken();
        mailDestinations.addAll(destinations);
    }

    public EventReceiveService(TalkLine talkLine, String clientNetId) {
        this.talkLine = talkLine;
        this.clientNetId = clientNetId;
    }

    public String getClientNetId() {
        return this.clientNetId;
    }

    public synchronized void processEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        if (null == event) {
            throw new ActionFailedException(1154L, "Event ist NULL");
        }
        try {
            if (event.equals(EventType.EVENT_MAIL)) {
                // check if interesting for this service
                if (interestedInMail(((NewMailEvent) event).getMail())) {
                    try {
                        talkLine.writeMsg(MailTag.REQ_READY_FOR_ACTION.name());
                        mailToClient(((NewMailEvent) event).getMail());
                    } catch (Exception e) {
                        LocalLog.warn("Fehler bei Verarbeitung einer Mail. Header: " + ((NewMailEvent) event).getMail().getHeader() + "; Destination: "
                                + ((NewMailEvent) event).getMail().getDestination());
                    }
                }
            } else {
                talkLine.writeMsg(MailTag.REQ_READY_FOR_ACTION.name());
                // try {
                eventToClient(event);
                // } catch (Exception e) {
                // LocalLog.error("Fehler bei Verarbeitung eines Events.", e);
                // }
            }
        } catch (Exception ex) {
            LocalLog.warn("MailAndEventService.processEvent(). Nachricht an MailAndEventClient konnte nicht verschickt werden. " + ex);
            throw new ActionFailedException(1154L, "Client ist vermutlich nicht mehr erreichbar.");
        }
    }

    /**
     * Sends a mail to the client.
     * <p>
     * The client must be a special client of type MailEventClient.
     * 
     * @param mail
     *            The mail to send.
     * @throws ActionFailedException
     */
    public final void mailToClient(NotEOFMail mail) throws ActionFailedException {
        talkLine.writeMsg(MailTag.VAL_ACTION_MAIL.name());
        talkLine.sendMail(mail);
    }

    public final void eventToClient(NotEOFEvent event) throws ActionFailedException {
        talkLine.writeMsg(MailTag.VAL_ACTION_EVENT.name());
        talkLine.sendBaseEvent(event);
    }

    /**
     * Derived Service must decide if he is interested in the mail by prooving
     * destination and header. If one of them matches it is enough for returning
     * true. But additional here is the validation to ignored clientNetIds. If
     * the mail was sent by an ignored client the mail itself of cause is
     * ignored too.
     * 
     * @param destination
     *            Any destination String. Depends to implementation of derived
     *            service.
     * @param header
     *            Any header String. Depends to implementation of derived
     *            service.
     * @return TRUE if the service wants to deliver the message to its client,
     *         FALSE if not.
     */
    protected boolean interestedInMail(NotEOFMail mail) {
        if (null != mailDestinations) {
            for (String expression : mailDestinations.getExpressions()) {
                if (expression.equals(mail.getDestination()))
                    return true;
            }
        }
        if (null != mailHeaders) {
            for (String expression : mailHeaders.getExpressions()) {
                if (expression.equals(mail.getHeader()))
                    return true;
            }
        }
        return false;
    }

    @Override
    public Class<?> getCommunicationTagClass() {
        return MailTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

    /**
     * Here the messages of a MailAndEventClient are interpreted and processed.
     */
    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_EXPRESSIONS)) {
            addExpressions();
        }
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_EVENTLIST)) {
            addEventClientIsInterestedIn();
        }

        // the client tells that he is ready with initializing. now he is able
        // to process mails and events. If the registration at the server is
        // done to early, the service would send events or mails to the client
        // during the client is still initializing. So they both would get in an
        // inconsistant state.
        if (incomingMsgEnum.equals(MailTag.INFO_READY_FOR_EVENTS)) {
            responseTo(MailTag.VAL_OK, MailTag.VAL_OK.name());
            addObservedEvent(EventType.EVENT_MAIL);
            addObservedEvent(EventType.EVENT_APPLICATION_STOP);
            getServer().registerForEvents(this);
        }
        if (incomingMsgEnum.equals(BaseCommTag.REQ_STOP)) {
            super.stopService();
        }

    }

    @SuppressWarnings("unchecked")
    private void addExpressions() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_EXPRESSIONS, MailTag.VAL_OK.name());
        String type = requestTo(MailTag.REQ_EXPRESSION_TYPE, MailTag.RESP_EXPRESSION_TYPE);
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            if (MailToken.class.getName().equals(type)) {
                addInterestingDestinations((List<String>) dataObject.getList());
            } else if (MailHeaders.class.getName().equals(type)) {
                addInterestingHeaders((List<String>) dataObject.getList());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addEventClientIsInterestedIn() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_EVENTLIST, MailTag.VAL_OK.name());
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            for (String typeName : (List<String>) dataObject.getList()) {
                EventType type = EventType.valueOf(typeName);
                addObservedEvent(type);
            }
        }
    }
}
