package de.notEOF.mail.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;
import de.notEOF.mail.interfaces.MailAndEventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;
import de.notIOC.configuration.ConfigurationManager;

public abstract class MailAndEventReceiveClient extends BaseClient {

    private MailAndEventAcceptor acceptor;
    private MailAndEventRecipient recipient;
    private long workerPointer = 0;

    private List<String> ignoredClientNetIds = new ArrayList<String>();

    public MailAndEventReceiveClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public MailAndEventReceiveClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
    }

    /**
     * This method is called by the basic class when close() is called.
     */
    public void implementationLastSteps() {
        acceptor.stop();
    }

    public void awaitMailOrEvent(MailAndEventRecipient recipient) throws ActionFailedException {
        if (null == this.recipient)
            this.recipient = recipient;
        activateAccepting();
    }

    private void activateAccepting() throws ActionFailedException {
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
        private boolean stopped = true;
        public long workerCounter = 0;

        public boolean isStopped() {
            return stopped;
        }

        public void stop() {
            stopped = true;
        }

        public void run() {
            boolean isEvent = false;
            int errCounter = 0;
            try {
                // Tell the service that now the client is ready to accept mails
                // and events
                if (MailTag.VAL_OK.name().equals(requestTo(MailTag.INFO_READY_FOR_EVENTS, MailTag.VAL_OK))) {
                    stopped = false;
                }

                while (!stopped) {
                    try {
                        awaitRequest(MailTag.REQ_READY_FOR_ACTION);
                        String action = readMsg();
                        if (MailTag.VAL_ACTION_MAIL.name().equals(action)) {
                            NotEOFMail mail = getTalkLine().receiveMail();
                            MailWorker worker = new MailWorker(recipient, mail);
                            Thread workerThread = new Thread(worker);
                            workerThread.start();
                        }
                        if (MailTag.VAL_ACTION_EVENT.name().equals(action)) {
                            if (workerCounter == 999999999)
                                workerCounter = 0;
                            isEvent = true;
                            NotEOFEvent event = getTalkLine().receiveBaseEvent(ConfigurationManager.getApplicationHome());
                            EventWorker worker = new EventWorker(recipient, event);
                            Thread workerThread = new Thread(worker);
                            worker.setId(++workerCounter);
                            workerThread.start();
                        }
                    } catch (Exception e) {
                        if (5 < errCounter++)
                            stopped = true;
                        if (!isEvent) {
                            recipient.processMailException(e);
                        } else {
                            recipient.processEventException(e);
                        }
                    }
                }
                close();
            } catch (Exception e) {
                if (!stopped) {
                    if (!isEvent) {
                        recipient.processMailException(e);
                    } else {
                        recipient.processEventException(e);
                    }
                }
            }
        }
    }

    /*
     * processing events by little worker - so the events are not lost. better
     * than a list...
     */
    private class EventWorker implements Runnable {
        protected MailAndEventRecipient recipient;
        protected NotEOFEvent event;
        protected long id;

        protected EventWorker(MailAndEventRecipient recipient, NotEOFEvent event) {
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
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                }
            }
            System.out.println("_______________________________________________________________");
            System.out.println(" MailAndReceiveClien.Worker: ID = " + getId());
            recipient.processEvent(event);
            workerPointer = getId();
        }
    }

    /*
     * processing events by little worker - so the events are not lost. better
     * than a list...
     */
    private class MailWorker implements Runnable {
        protected MailAndEventRecipient recipient;
        protected NotEOFMail mail;

        protected MailWorker(MailAndEventRecipient recipient, NotEOFMail mail) {
            this.recipient = recipient;
            this.mail = mail;
        }

        public void run() {
            recipient.processMail(mail);
        }
    }

    /**
     * Add events which the client is interested in.
     * 
     * @param events
     *            The list that contains events.
     * @throws ActionFailedException
     */
    public void addInterestingEvents(List<NotEOFEvent> events) throws ActionFailedException {
        if (MailTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_EVENTLIST, MailTag.RESP_READY_FOR_EVENTLIST))) {
            DataObject dataObject = new DataObject();
            List<String> eventClassNames = new ArrayList<String>();
            for (NotEOFEvent event : events) {
                String className = event.getClass().getName();
                eventClassNames.add(className);
            }
            dataObject.setList(eventClassNames);
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
