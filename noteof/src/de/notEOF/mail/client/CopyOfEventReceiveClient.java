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

public abstract class CopyOfEventReceiveClient extends BaseClient {

    private MailAndEventAcceptor acceptor;
    private EventRecipient recipient;
    private long workerPointer = 0;

    private List<String> ignoredClientNetIds = new ArrayList<String>();

    public CopyOfEventReceiveClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public CopyOfEventReceiveClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
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
        System.out.println("EventReceiveClient.stop() wurde aufgerufen");
        System.out.println("EventReceiveClient.stop() vor sendStopSignal()");
        sendStopSignal();
        System.out.println("EventReceiveClient.stop() nach sendStopSignal()");
        acceptor = null;
        acceptor.notifyAll();
        acceptor.stop();
        System.out.println("EventReceiveClient.stop() nach acceptor.stop()");
        while (!acceptor.isStopped()) {
            System.out.println("Warte darauf, dass sich der acceptor beendet");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("EventReceiveClient.stop() wurde abgearbeitet.");
        // try {
        // close();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    private void activateAccepting() throws ActionFailedException {
        // beware of multiple start!
        if (null == acceptor || acceptor.isStopped()) {
            System.out.println("EventReceiveClient.activateAccepting() acceptor null? " + (null == acceptor));
            if (null != acceptor)
                System.out.println("EventReceiveClient.activateAccepting() acceptor stopped? " + (acceptor.isStopped()));
            acceptor = new MailAndEventAcceptor();
            Thread acceptorThread = new Thread(acceptor);
            acceptorThread.start();

            while (acceptor.isStopped()) {
                System.out.println("EventReceiveClient.activateAccepting() rabäh");
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
        private boolean acceptorStopped = false;
        private boolean acceptorToStop = false;

        public boolean isStopped() {
            return acceptorStopped;
        }

        public void stop() {
            System.out.println("EventReceiveClient$MailAndEventAcceptor.stop()");
            acceptorToStop = true;
        }

        public void run() {
            boolean isEvent = false;
            boolean isMail = false;
            acceptorStopped = false;
            Exception thrownException = null;
            try {
                // Tell the service that now the client is ready to accept mails
                // and events
                if (MailTag.VAL_OK.name().equals(requestTo(MailTag.INFO_READY_FOR_EVENTS, MailTag.VAL_OK))) {
                    acceptorToStop = false;
                }

                while (!acceptorToStop) {
                    try {
                        String awaitMsg = readMsgTimedOut(1000);
                        if (acceptorToStop) {
                            break;
                        }
                        if (MailTag.REQ_READY_FOR_ACTION.name().equals(awaitMsg)) {
                            String action = readMsgTimedOut(1000);

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
                                    EventWorker worker = new EventWorker(this, recipient, event);
                                    Thread workerThread = new Thread(worker);
                                    worker.setId(++workerCounter);
                                    workerThread.start();
                                    System.out.println("Event ENDE");
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
                // sendStopSignal();
                close();
                acceptorStopped = true;
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
        MailAndEventAcceptor acceptor;

        protected EventWorker(MailAndEventAcceptor acceptor, EventRecipient recipient, NotEOFEvent event) {
            this.recipient = recipient;
            this.event = event;
            this.acceptor = acceptor;
        }

        protected void setId(long id) {
            this.id = id;
        }

        protected long getId() {
            return this.id;
        }

        public void run() {
            while (getId() - 1 > workerPointer) {
                if (acceptor.isStopped()) {
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
        System.out.println("EventReceiveClient.addIgnored.. Eigene ClientNetId ist: " + getClientNetId());
        this.ignoredClientNetIds.add(clientNetId);
        if (MailTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_IGNORED_CLIENTS, MailTag.RESP_READY_FOR_IGNORED_CLIENTS))) {
            DataObject dataObject = new DataObject();
            dataObject.setList(ignoredClientNetIds);
            sendDataObject(dataObject);
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
        if (MailTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_EVENTLIST, MailTag.RESP_READY_FOR_EVENTLIST))) {
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
}
