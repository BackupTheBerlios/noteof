package de.notEOF.mail.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xpath.internal.Expression;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;
import de.notEOF.mail.interfaces.MailEventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;

public abstract class MailEventClient extends BaseClient {

    private MailEventAcceptor acceptor;
    private MailEventRecipient recipient;

    public MailEventClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public MailEventClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
    }

    /**
     * This method is called by the basic class when close() is called.
     */
    public void implementationLastSteps() {
        acceptor.stop();
    }

    public void awaitMailEvent(MailEventRecipient recipient) throws ActionFailedException {
        this.recipient = recipient;

        acceptor = new MailEventAcceptor();
        Thread acceptorThread = new Thread(acceptor);
        acceptorThread.start();
    }

    private class MailEventAcceptor implements Runnable {
        private boolean stopped = false;

        public boolean stopped() {
            return stopped;
        }

        public void stop() {
            stopped = true;
        }

        public void run() {
            try {
                while (!stopped) {
                    // wake up!
                    awaitRequestAnswerImmediate(MailTag.REQ_READY_FOR_MAIL, MailTag.RESP_READY_FOR_MAIL, BaseCommTag.VAL_OK.name());
                    NotEOFMail mail = getTalkLine().receiveMail();
                    recipient.processMail(mail);
                }
            } catch (Exception e) {

            }
        }
    }

    public void addInterestingEvents(List<NotEOFEvent> events) throws ActionFailedException {
        if (BaseCommTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_EVENTS, MailTag.RESP_READY_FOR_EVENTS))) {
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
     * Add words (destinations) which this client is interested for.
     * <p>
     * If a mail reaches the central server the services are informed about
     * this. <br>
     * To get a mail it is important to set destinations or headers which the
     * client waits for.
     * 
     * @param destinations
     *            A list with words.
     * @throws ActionFailedException
     *             If the list couldn't be transmitted to the service.
     */
    public void addInterestingMailExpressions(MailMatchExpressions expressions) throws ActionFailedException {
        if (BaseCommTag.VAL_OK.name().equals(requestTo(MailTag.REQ_READY_FOR_EXPRESSIONS, MailTag.RESP_READY_FOR_EXPRESSIONS))) {
            System.out.println(this.getClass().getName() + ": vor awaitRequest...");
            awaitRequestAnswerImmediate(MailTag.REQ_EXPRESSION_TYPE, MailTag.RESP_EXPRESSION_TYPE, Expression.class.getName());
            System.out.println(this.getClass().getName() + ": nach awaitRequest...");
            DataObject dataObject = new DataObject();
            dataObject.setList(expressions.getExpressions());
            sendDataObject(dataObject);
            System.out.println("Nach sendDataObject");
        }
    }
}
