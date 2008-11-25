package de.notEOF.mail.service;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.NewMailEvent;
import de.notEOF.core.exception.ActionFailedException;
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
public abstract class MailAndEventReceiveService extends BaseService {

    private MailToken mailDestinations;
    private MailHeaders mailHeaders;
    private List<String> eventTypeNames;
    private List<String> ignoredClientNetIds;

    @Override
    public Class<?> getCommunicationTagClass() {
        return MailTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

    protected void addIgnoredMailsByClientNetId(List<String> clientNetIds) {
        if (null == ignoredClientNetIds)
            ignoredClientNetIds = new ArrayList<String>();
        ignoredClientNetIds.addAll(clientNetIds);
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

    protected void addInterestingEventNames(List<String> eventTypeNames) {
        if (null == this.eventTypeNames)
            this.eventTypeNames = new ArrayList<String>();
        this.eventTypeNames.addAll(eventTypeNames);
        for (String eventName : eventTypeNames) {
            addObservedEvent(EventType.valueOf(eventName));
        }
    }

    protected void addInterestingEventName(String eventName) {
        if (null == this.eventTypeNames)
            this.eventTypeNames = new ArrayList<String>();
        this.eventTypeNames.add(eventName);
        addObservedEvent(EventType.valueOf(eventName));
    }

    /**
     * Callback method to process incoming events.
     * <p>
     * 
     * @param service
     *            The service which fired the event.
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public synchronized void processEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        if (null == event) {
            throw new ActionFailedException(1154L, "Event ist NULL");
        }

        try {
            if (EventType.EVENT_MAIL.equals(event.getEventType())) {
                // check if interesting for this service
                if (((NewMailEvent) event).getMail().getToClientNetId().equals(getClientNetId()) || //
                        interestedInMail(((NewMailEvent) event).getMail())) {
                    try {
                        writeMsg(MailTag.REQ_READY_FOR_ACTION);
                        mailToClient(((NewMailEvent) event).getMail());
                    } catch (Exception e) {
                        LocalLog.warn("Fehler bei Verarbeitung einer Mail. Header: " + ((NewMailEvent) event).getMail().getHeader() + "; Destination: "
                                + ((NewMailEvent) event).getMail().getDestination());
                    }
                }
            } else if (interestedInEvent(event)) {
                writeMsg(MailTag.REQ_READY_FOR_ACTION);
                try {
                    eventToClient(event);
                } catch (Exception e) {
                    LocalLog.error("Fehler bei Verarbeitung eines Events.", e);
                }
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
        writeMsg(MailTag.VAL_ACTION_MAIL);
        getTalkLine().sendMail(mail);
    }

    public final void eventToClient(NotEOFEvent event) throws ActionFailedException {
        writeMsg(MailTag.VAL_ACTION_EVENT);
        getTalkLine().sendBaseEvent(event);
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
        if (null != ignoredClientNetIds) {
            for (String ignoredClientNetId : ignoredClientNetIds) {
                if (ignoredClientNetId.equals(mail.getFromClientNetId()))
                    return false;
            }
        }
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

    protected boolean interestedInEvent(NotEOFEvent event) {
        if (null != eventTypeNames) {
            for (String eventName : eventTypeNames) {
                if (eventName.equals(event.getEventType().name())) {
                    return true;
                }
            }
        }
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
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_IGNORED_CLIENTS)) {
            addIgnoredMails();
        }
        // the client tells that he is ready with initializing. now he is able
        // to process mails and events. If the registration at the server is
        // done to early, the service would send events or mails to the client
        // during the client is still initializing. So they both would get in an
        // inconsistant state.
        if (incomingMsgEnum.equals(MailTag.INFO_READY_FOR_EVENTS)) {
            // responseTo(MailTag.VAL_OK, MailTag.VAL_OK.name());
            responseTo(MailTag.VAL_OK, MailTag.VAL_OK.name());
            addObservedEvent(EventType.EVENT_MAIL);
            addObservedEvent(EventType.EVENT_APPLICATION_STOP);
            getServer().registerForEvents(this);
        }
    }

    @SuppressWarnings("unchecked")
    private void addIgnoredMails() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_IGNORED_CLIENTS, MailTag.VAL_OK.name());
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            addIgnoredMailsByClientNetId((List<String>) dataObject.getList());
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
            System.out.println("MailAndEventReceiveService.addEvents: Liste wird ergänzt");
            addInterestingEventNames((List<String>) dataObject.getList());
        }
    }
}
