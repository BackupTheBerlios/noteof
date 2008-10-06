package de.notEOF.mail.service;

import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.NewMailEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;

/**
 * This class is NOT build to send mails or events FROM client. <br>
 * This class ist build to send new mails or events TO client.
 * <p>
 * 
 * @author Dirk
 */
public abstract class MailEventService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

    @Override
    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
    }

    /**
     * This service is interested in Mails and Events.
     */
    public void implementationFirstSteps() {
        addObservedEventType(EventType.EVENT_MAIL);
        addObservedEventType(EventType.EVENT_EVENT);
    }

    /**
     * Callback method to be informed about incoming events.
     * <p>
     * 
     * @param service
     *            The service which fired the event.
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public void update(Service service, NotEOFEvent event) {
        if (EventType.EVENT_MAIL.equals(event.getEventType())) {
            // check if interesting for this service
            if (((NewMailEvent) event).getMail().getToClientNetId().equals(getClientNetId()) || //
                    interestedInMail(((NewMailEvent) event).getMail().getDestination(), ((NewMailEvent) event).getMail().getHeader())) {
                try {
                    NotEOFMail mail = super.getServer().getMail(((NewMailEvent) event).getMail().getMailId());
                    mailToClient(mail);
                } catch (Exception e) {
                    LocalLog.warn("Mehrere Services versuchen auf eine Nachricht zuzugreifen. Header: " + ((NewMailEvent) event).getMail().getHeader()
                            + "; Destination: " + ((NewMailEvent) event).getMail().getDestination());
                }
            }
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
        if (BaseCommTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_MAIL, MailTag.RESP_READY_FOR_MAIL))) {
            getTalkLine().sendMail(mail);
        }
    }

    /**
     * Derived Service must decide if he is interested in the mail by prooving
     * destination and header. I one of them matches it is enough for returning
     * true.
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
    protected abstract boolean interestedInMail(String destination, String header);
}
