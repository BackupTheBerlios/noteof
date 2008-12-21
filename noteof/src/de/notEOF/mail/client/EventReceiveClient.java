package de.notEOF.mail.client;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.communication.DataObject;
import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;
import de.notEOF.mail.interfaces.EventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;
import de.notIOC.configuration.ConfigurationManager;
import de.notIOC.util.Util;

public class EventReceiveClient {

    private MailAndEventAcceptor acceptor;
    private EventRecipient recipient;
    TalkLine talkLine;
    private long workerPointer = 0;

    public EventReceiveClient(TalkLine talkLine, EventRecipient recipient) throws ActionFailedException {
        this.talkLine = talkLine;
        this.recipient = recipient;
    }

    public void stop() {
        System.out.println("EventReceiveClient.stop. Vor acceptor.stop");
        // acceptor = null;
        // acceptor.notifyAll();
        acceptor.stop();
        System.out.println("EventReceiveClient.stop. Nach acceptor.stop");
        while (!acceptor.isStopped()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("EventReceiveClient.stop. Nach warten auf acceptor stopped");
    }

    public void startAccepting() throws ActionFailedException {
        talkLine.writeMsg(MailTag.INFO_READY_FOR_EVENTS.name());

        String antwort = talkLine.readMsg();
        if ((MailTag.VAL_OK.name() + "=" + MailTag.VAL_OK.name()).equals(antwort)) {
            // if (MailTag.VAL_OK.name().equals(talkLine.readMsg())) {
            acceptor = new MailAndEventAcceptor();
            System.out.println("Ab jetzt sollte eigentlich der Acceptor laufen.");
            new Thread(acceptor).start();
        }
    }

    private class MailAndEventAcceptor implements Runnable {
        public long workerCounter = 0;
        private boolean acceptorStopped = false;
        private boolean acceptorToStop = false;

        public boolean isStopped() {
            return acceptorStopped;
        }

        public void stop() {
            acceptorToStop = true;
        }

        public void run() {
            boolean isEvent = false;
            boolean isMail = false;
            acceptorStopped = false;
            Exception thrownException = null;
            try {
                while (!acceptorToStop) {
                    System.out.println("EventReceiveClient.run " + recipient.getClass().getSimpleName());
                    try {
                        String awaitMsg = talkLine.readMsgTimedOut(1000);
                        System.out.println("EventReceiveClient$Acceptor.run  Client: " + recipient.getClass().getSimpleName());
                        if (acceptorToStop) {
                            break;
                        }
                        if (MailTag.REQ_READY_FOR_ACTION.name().equals(awaitMsg)) {
                            String action = talkLine.readMsgTimedOut(1000);

                            // awaitRequest(MailTag.REQ_READY_FOR_ACTION);
                            if (!Util.isEmpty(action)) {
                                if (MailTag.VAL_ACTION_MAIL.name().equals(action)) {
                                    isMail = true;
                                    NotEOFMail mail = talkLine.receiveMail();
                                    MailWorker worker = new MailWorker(recipient, mail);
                                    Thread workerThread = new Thread(worker);
                                    workerThread.start();
                                }
                                if (MailTag.VAL_ACTION_EVENT.name().equals(action)) {
                                    System.out.println("Event BEGINN");
                                    if (workerCounter == Long.MAX_VALUE - 1)
                                        workerCounter = 0;
                                    isEvent = true;
                                    NotEOFEvent event = talkLine.receiveBaseEvent(ConfigurationManager.getApplicationHome());
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
                acceptorStopped = true;
            } catch (Exception e) {
                thrownException = e;
            }
            if (null != thrownException) {
                if (isMail) {
                    System.out.println("EventReceiveClient.run. Muss jetzt die processMailException von recipient aufrufen.");
                    recipient.processMailException(thrownException);
                }
                if (isEvent) {
                    System.out.println("EventReceiveClient.run. Muss jetzt die processEventException von recipient aufrufen.");
                    recipient.processEventException(thrownException);
                }
                if (!(isMail || isEvent)) {
                    System.out.println("EventReceiveClient.run. Muss jetzt die processEventException von recipient aufrufen.");
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
                System.out.println("EventReceiveClient.EventWorker.run.   Rufe jetzt den Recipienten auf...");
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
        if (MailTag.VAL_OK.name().equals(talkLine.requestTo(MailTag.REQ_READY_FOR_EXPRESSIONS, MailTag.RESP_READY_FOR_EXPRESSIONS))) {
            talkLine.awaitRequestAnswerImmediate(MailTag.REQ_EXPRESSION_TYPE, MailTag.RESP_EXPRESSION_TYPE, expressions.getClass().getName());
            DataObject dataObject = new DataObject();
            dataObject.setList(expressions.getExpressions());
            talkLine.sendDataObject(dataObject);
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
        if (MailTag.VAL_OK.name().equals(talkLine.requestTo(MailTag.REQ_READY_FOR_EVENTLIST, MailTag.RESP_READY_FOR_EVENTLIST))) {
            DataObject dataObject = new DataObject();
            List<String> eventTypeNames = new ArrayList<String>();
            for (NotEOFEvent event : events) {
                String eventTypeName = event.getEventType().name();
                eventTypeNames.add(eventTypeName);
            }
            dataObject.setList(eventTypeNames);
            talkLine.sendDataObject(dataObject);
        }
    }
}
