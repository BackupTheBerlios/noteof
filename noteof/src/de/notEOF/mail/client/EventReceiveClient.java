package de.notEOF.mail.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;
import de.notEOF.mail.interfaces.EventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;
import de.notIOC.configuration.ConfigurationManager;
import de.notIOC.util.Util;

public abstract class EventReceiveClient extends BaseClient {

    private MailAndEventAcceptor acceptor;
    private EventRecipient recipient;
    private long workerPointer = 0;
    private boolean acceptorStopped = true;

    private List<String> ignoredClientNetIds = new ArrayList<String>();

    public EventReceiveClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public EventReceiveClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
    }

    /**
     * This method is called by the basic class when close() is called.
     */
    public void implementationLastSteps() {
        acceptor.stop();
        // sendStopSignal();
    }

    public void awaitMailOrEvent(EventRecipient recipient) throws ActionFailedException {
        if (null == this.recipient)
            this.recipient = recipient;
        activateAccepting();
    }

    public void stop() {
        System.out.println("EventReceiveClient soll jetzt stoppen...");
        sendStopSignal();
        acceptor.stop();
        System.out.println("EventReceiveClient stoppen theoretisch abgearbeitet...");
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activateAccepting() throws ActionFailedException {
        System.out.println("EventReceiveClient.activateAccepting wurde aufgerufen......");

        // beware of multiple start!
        if (null == acceptor || acceptor.isStopped()) {
            acceptor = new MailAndEventAcceptor();
            Thread acceptorThread = new Thread(acceptor);
            acceptorThread.start();

            while (acceptor.isStopped()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else
            throw new ActionFailedException(1090L, "Recipient: " + this.recipient.getClass().getCanonicalName());
    }

    private class MailAndEventAcceptor implements Runnable {
        public long workerCounter = 0;

        public boolean isStopped() {
            return acceptorStopped;
        }

        public void stop() {
            acceptorStopped = true;
            // getTalkLine().close();
        }

        public void run() {
            boolean isEvent = false;
            boolean isMail = false;
            Exception thrownException = null;
            try {
                // Tell the service that now the client is ready to accept mails
                // and events
                System.out.println("EventReceiveClient.MailAndEventAcceptor.    requestTo INFO_READY_FOR_EVENTS");
                if (MailTag.VAL_OK.name().equals(requestTo(MailTag.INFO_READY_FOR_EVENTS, MailTag.VAL_OK))) {
                    acceptorStopped = false;
                }

                while (!acceptorStopped) {
                    try {
                        String awaitMsg = readMsgTimedOut(1000);
                        if (!acceptorStopped && MailTag.REQ_READY_FOR_ACTION.name().equals(awaitMsg)) {
                            String action = readMsg();

                            // awaitRequest(MailTag.REQ_READY_FOR_ACTION);
                            if (!Util.isEmpty(action)) {
                                if (MailTag.VAL_ACTION_MAIL.name().equals(action)) {
                                    isMail = true;
                                    NotEOFMail mail = getTalkLine().receiveMail();
                                    MailWorker worker = new MailWorker(recipient, mail);
                                    Thread workerThread = new Thread(worker);
                                    workerThread.start();
                                }
                                if (MailTag.VAL_ACTION_EVENT.name().equals(action)) {
                                    System.out.println("Event BEGINN");
                                    if (workerCounter == Long.MAX_VALUE - 1)
                                        workerCounter = 0;
                                    isEvent = true;
                                    NotEOFEvent event = getTalkLine().receiveBaseEvent(ConfigurationManager.getApplicationHome());
                                    EventWorker worker = new EventWorker(recipient, event);
                                    Thread workerThread = new Thread(worker);
                                    worker.setId(++workerCounter);
                                    workerThread.start();
                                    System.out.println("Event ENDE");
                                }
                                if ("bla".equalsIgnoreCase(action)) {
                                    System.out.println("bla ist angekommen");
                                }
                            }
                        }
                    } catch (ActionFailedException a) {
                        if (24L == a.getErrNo()) {
                            // OK !!! Timeout expected here
                        } else {
                            thrownException = a;
                            acceptorStopped = true;
                            break;
                        }

                    } catch (Exception e) {
                        thrownException = e;
                        acceptorStopped = true;
                        break;
                    }

                    isMail = false;
                    isEvent = false;
                }
                System.out.println("Acceptor wurde gestoppt");
                // sendStopSignal();
                close();
            } catch (Exception e) {
                thrownException = e;
            }
            if (null != thrownException) {
                if (isMail) {
                    recipient.processMailException(thrownException);
                }
                if (isEvent) {
                    recipient.processEventException(thrownException);
                }
                if (!(isMail || isEvent)) {
                    recipient.processEventException(thrownException);
                }
            }
        }
    }

    /*
     * processing events by little worker - so the events are not lost. better
     * than a list...
     */
    private class EventWorker implements Runnable {
        protected EventRecipient recipient;
        protected NotEOFEvent event;
        protected long id;

        protected EventWorker(EventRecipient recipient, NotEOFEvent event) {
            this.recipient = recipient;
            this.event = event;
        }

        protected void setId(long id) {
            this.id = id;
        }

        protected long getId() {
            return this.id;
        }

        public void run() {
            while (getId() - 1 > workerPointer) {
                if (acceptorStopped) {
                    System.out.println("EventReceiveClient$EventWorker.run");
                    return;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            if (event.equals(EventType.EVENT_APPLICATION_STOP)) {
                recipient.processStopEvent(event);
            } else {
                recipient.processEvent(event);
            }
            workerPointer = getId();
        }
    }

    /*
     * processing events by little worker - so the events are not lost. better
     * than a list...
     */
    private class MailWorker implements Runnable {
        protected EventRecipient recipient;
        protected NotEOFMail mail;

        protected MailWorker(EventRecipient recipient, NotEOFMail mail) {
            this.recipient = recipient;
            this.mail = mail;
        }

        public void run() {
            recipient.processMail(mail);
        }
    }

    /**
     * Add events which the client is interested in.
     * <p>
     * If this is not done the client doesn't receives any event...
     * 
     * @param events
     *            The list that contains events.
     * @throws ActionFailedException
     */
    public void addInterestingEvents(List<NotEOFEvent> events) throws ActionFailedException {
        System.out.println("EventReceiveClient.addInterestingEvents. ");
        if (MailTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_EVENTLIST, MailTag.RESP_READY_FOR_EVENTLIST))) {
            System.out.println("EventReceiveClient.addInterestingEvents. Service ist bereit für Übertragung");
            DataObject dataObject = new DataObject();
            List<String> eventTypeNames = new ArrayList<String>();
            for (NotEOFEvent event : events) {
                String eventTypeName = event.getEventType().name();
                eventTypeNames.add(eventTypeName);
            }
            dataObject.setList(eventTypeNames);
            sendDataObject(dataObject);
        }
    }

    /**
     * Add terms which this client is interested in.
     * <p>
     * If a mail reaches the central server the services are informed about
     * this. <br>
     * To get a mail it is important to set destinations or headers which the
     * client waits for.
     * 
     * @param expressions
     *            MailMatchExpressions is an interface which must be implemented
     *            by expressions. This Object must contain a list with terms as
     *            Strings. The terms can be headers or other (combined) words.
     * @throws ActionFailedException
     *             If the list couldn't be transmitted to the service.
     */
    public void addInterestingMailExpressions(MailMatchExpressions expressions) throws ActionFailedException {
        if (null == expressions || null == expressions.getExpressions() || 0 == expressions.getExpressions().size())
            throw new ActionFailedException(1102L, "Liste der erwarteten Ausdruecke ist leer.");
        if (MailTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_EXPRESSIONS, MailTag.RESP_READY_FOR_EXPRESSIONS))) {
            awaitRequestAnswerImmediate(MailTag.REQ_EXPRESSION_TYPE, MailTag.RESP_EXPRESSION_TYPE, expressions.getClass().getName());
            DataObject dataObject = new DataObject();
            dataObject.setList(expressions.getExpressions());
            sendDataObject(dataObject);
        }
    }

    /**
     * Add client net id to ignore self send mails.
     * <p>
     * Normally own mails are not interesting to receive them... perhaps there
     * are mails of other clients which must be ignored. Ignored netClientId's
     * are stored in a list. That means that there can be more than one id to be
     * ignored.
     * 
     * @param clientNetId
     *            One more id to be ignored.
     * 
     */
    public void addIgnoredClientNetId(String clientNetId) throws ActionFailedException {
        this.ignoredClientNetIds.add(clientNetId);
        if (MailTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_IGNORED_CLIENTS, MailTag.RESP_READY_FOR_IGNORED_CLIENTS))) {
            DataObject dataObject = new DataObject();
            dataObject.setList(ignoredClientNetIds);
            sendDataObject(dataObject);
        }
    }

}
