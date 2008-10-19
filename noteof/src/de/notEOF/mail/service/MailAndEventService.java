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
public abstract class MailAndEventService extends BaseService {

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
     * Callback method to process incoming events.
     * <p>
     * 
     * @param service
     *            The service which fired the event.
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public void processEvent(Service service, NotEOFEvent event) {
        System.out.println("MailEventService im processEvent!!!");

        try {
            System.out.println("------------- Service 1 -------------------");
            writeMsg(MailTag.REQ_READY_FOR_ACTION);
            System.out.println("------------- Service 2 -------------------");
        } catch (Exception ex) {
            LocalLog.warn("Nachricht an MailAndEventClient konnte nicht verschick werden. " + ex);
        }
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
        System.out.println("------------- Service 3 -------------------");
        writeMsg(MailTag.VAL_ACTION_MAIL);
        System.out.println("------------- Service 4 -------------------");
        getTalkLine().sendMail(mail);
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
    protected boolean interestedInMail(String destination, String header) {
        if (null != mailDestinations) {
            for (String expression : mailDestinations.getExpressions()) {
                if (expression.equals(destination))
                    return true;
            }
        }
        if (null != mailHeaders) {
            for (String expression : mailHeaders.getExpressions()) {
                if (expression.equals(header))
                    return true;
            }
        }
        return false;
    }

    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_EXPRESSIONS)) {
            addExpressions();
        }
        if (incomingMsgEnum.equals(MailTag.REQ_READY_FOR_EVENT)) {
            addEvents();
        }
    }

    @SuppressWarnings("unchecked")
    private void addExpressions() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_EXPRESSIONS, MailTag.VAL_OK.name());
        String type = requestTo(MailTag.REQ_EXPRESSION_TYPE, MailTag.RESP_EXPRESSION_TYPE);
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            if (MailDestinations.class.getName().equals(type)) {
                addInterestingDestinations((List<String>) dataObject.getList());
            } else if (MailHeaders.class.getName().equals(type)) {
                addInterestingHeaders((List<String>) dataObject.getList());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addEvents() throws ActionFailedException {
        responseTo(MailTag.RESP_READY_FOR_EVENT, MailTag.VAL_OK.name());
        DataObject dataObject = receiveDataObject();
        if (null != dataObject && null != dataObject.getList() && dataObject.getList().size() > 0) {
            addInterestingEventNames((List<String>) dataObject.getList());
        }
    }
}
