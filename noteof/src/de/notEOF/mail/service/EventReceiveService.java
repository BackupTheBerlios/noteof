package de.notEOF.mail.service;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.NewMailEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
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
public class EventReceiveService implements EventObserver {

    private MailToken mailDestinations;
    private MailHeaders mailHeaders;
    private List<EventType> observedEventTypes;
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

    public EventReceiveService(TalkLine talkLine, String clientNetId) {
        this.talkLine = talkLine;
        this.clientNetId = clientNetId;
    }

    public String getClientNetId() {
        return this.clientNetId;
    }

    public synchronized void processEvent(NotEOFEvent event) throws ActionFailedException {
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
            } else if (interestedInEvent(event)) {
                talkLine.writeMsg(MailTag.REQ_READY_FOR_ACTION.name());
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

    protected boolean interestedInEvent(NotEOFEvent event) {
        if (null != observedEventTypes) {
            for (EventType eventType : observedEventTypes) {
                if (event.equals(eventType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setObservedEvents(List<EventType> observedEvents) {
        this.observedEventTypes = observedEvents;
    }

    public void addObservedEvents(List<EventType> observedEvents) {
        if (null == this.observedEventTypes)
            this.observedEventTypes = new ArrayList<EventType>();
        this.observedEventTypes.addAll(observedEvents);
    }

    public void addObservedEvent(EventType eventType) {
        if (null == this.observedEventTypes)
            this.observedEventTypes = new ArrayList<EventType>();
        observedEventTypes.add(eventType);
    }

    @Override
    public String getName() {
        return hashCode() + ":" + this.getClass().getSimpleName();
    }

    @Override
    public List<EventType> getObservedEvents() {
        return observedEventTypes;
    }

    @Override
    public void update(Service service, NotEOFEvent event) {
        try {
            processEvent(event);
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
