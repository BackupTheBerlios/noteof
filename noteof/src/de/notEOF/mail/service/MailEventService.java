package de.notEOF.mail.service;

import java.util.HashMap;
import java.util.Map;

import de.notEOF.core.communication.DataObject;
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
            if (((NewMailEvent) event).getToClientNetId().equals(getClientNetId()) || //
                    interestedInMail(((NewMailEvent) event).getDestination(), ((NewMailEvent) event).getHeader())) {
                try {
                    NotEOFMail mail = super.getServer().getMail(((NewMailEvent) event).getMailId());
                    mailToClient(mail);
                } catch (Exception e) {
                    LocalLog.warn("Mehrere Services versuchen auf eine Nachricht zuzugreifen. Header: " + ((NewMailEvent) event).getHeader()
                            + "; Destination: " + ((NewMailEvent) event).getDestination());
                }
            }
        }
    }

    // TODO implementieren senden der nachricht an den client
    public final void mailToClient(NotEOFMail mail) throws ActionFailedException {
        if (BaseCommTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_MAIL, MailTag.RESP_READY_FOR_MAIL))) {

            // send message informations
            Map<String, String> envelope = new HashMap<String, String>();
            envelope.put("toClientNetId", mail.getToClientNetId());
            envelope.put("header", mail.getHeader());
            envelope.put("mailId", mail.getMailId());
            envelope.put("destination", mail.getDestination());
            envelope.put("generated", String.valueOf(mail.getGenerated().getTime()));

            DataObject envelopeObject = new DataObject();
            envelopeObject.setMap(envelope);
            awaitRequestAnswerImmediate(MailTag.REQ_MAIL_ENVELOPE, MailTag.RESP_MAIL_ENVELOPE, BaseCommTag.VAL_TRUE.name());
            sendDataObject(envelopeObject);

            // body text
            awaitRequestAnswerImmediate(MailTag.REQ_BODY_TEXT, MailTag.RESP_BODY_TEXT, mail.getBodyText());

            // body data
            if (null == mail.getBodyData()) {
                awaitRequestAnswerImmediate(MailTag.REQ_BODY_DATA_EXISTS, MailTag.RESP_BODY_DATA_EXISTS, BaseCommTag.VAL_FALSE.name());
            } else {
                awaitRequestAnswerImmediate(MailTag.REQ_BODY_DATA_EXISTS, MailTag.RESP_BODY_DATA_EXISTS, BaseCommTag.VAL_TRUE.name());
                sendDataObject(mail.getBodyData());
            }
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
