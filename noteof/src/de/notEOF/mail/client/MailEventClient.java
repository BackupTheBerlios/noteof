package de.notEOF.mail.client;

import java.net.Socket;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;
import de.notEOF.mail.interfaces.MailEventRecipient;
import de.notEOF.mail.service.MailEventService;

public class MailEventClient extends BaseClient {

    private MailEventAcceptor acceptor;
    private MailEventRecipient recipient;

    public MailEventClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public MailEventClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
    }

    @Override
    public Class<?> serviceForClientByClass() {
        return MailEventService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
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

}
