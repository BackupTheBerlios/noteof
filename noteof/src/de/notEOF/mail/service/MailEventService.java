package de.notEOF.mail.service;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.NewMailEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.mail.MailDestinations;
import de.notEOF.mail.MailHeaders;
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

    private MailDestinations mailDestinations;
    private MailHeaders mailHeaders;
    private List<String> eventNames;

    @Override
    public Class<?> getCommunicationTagClass() {
        return MailTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

    /**
     * This service is interested in Mails and Events.
     */
    public void implementationFirstSteps() {
        addObservedEventType(EventType.EVENT_MAIL);
        addObservedEventType(EventType.EVENT_EVENT);
        getServer().registerForEvents(this);
    }

    protected void addInterestingDestination(String destination) {
        if (null == mailDestinations)
            mailDestinations = new MailDestinations();
        mailDestinations.add(destination);
    }

    protected void addInterestingDestinations(List<String> destinations) {
        if (null == mailDestinations)
            mailDestinations = new MailDestinations();
        mailDestinations.addAll(destinations);
    }

    protected void addInterestingHeader(String header) {
        if (null == mailHeaders)
            mailHeaders = new MailHeaders();
        mailHeaders.add(header);
    }

    protected void addInterestingHeaders(List<String> headers) {
        if (null == mailHeaders)
            mailHeaders = new MailHeaders();
        mailHeaders.addAll(headers);
    }

    protected void addInterestingEventNames(List<String> eventNames) {
        if (null == this.eventNames)
            this.eventNames = new ArrayList<String>();
        eventNames.addAll(eventNames);
    }

    protected void addInterestingEventName(String eventName) {
        if (null == this.eventNames)
            this.eventNames = new ArrayList<String>();
        this.eventNames.add(eventName);
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
                    mailToClient(((NewMailEvent) event).getMail());
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

    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_EXPRESSIONS)) {
            processNewExpressions();
        }
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_EVENTS)) {
            processNewEvents();
        }
    }

    @SuppressWarnings("unchecked")
    private void processNewExpressions() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_EXPRESSIONS, BaseCommTag.VAL_OK.name());
        System.out.println(this.getClass().getName() + ": Vor requestTo...");
        String type = requestTo(MailTag.REQ_EXPRESSION_TYPE, MailTag.RESP_EXPRESSION_TYPE);
        System.out.println(this.getClass().getName() + ": Nach requestTo...");
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            if (MailDestinations.class.getName().equals(type)) {
                addInterestingDestinations((List<String>) dataObject.getList());
            } else if (MailHeaders.class.getName().equals(type)) {
                addInterestingHeaders((List<String>) dataObject.getList());
            }
        }
        System.out.println("Nach Empfang interestingHeaders.");
    }

    @SuppressWarnings("unchecked")
    private void processNewEvents() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_EVENTS, BaseCommTag.VAL_OK.name());
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            addInterestingEventNames((List<String>) dataObject.getList());
        }
    }
}
