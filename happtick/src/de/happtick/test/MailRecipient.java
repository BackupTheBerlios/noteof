package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.mail.MailDestinations;
import de.notEOF.mail.MailHeaders;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.MailAndEventRecipient;

public class MailRecipient implements MailAndEventRecipient {

    private HapptickApplication appl;

    public MailRecipient(String... args) throws HapptickException {
        appl = new HapptickApplication(0, "localhost", 3000, args);
        appl.useMailsAndEvents(this);
        MailDestinations destinations = new MailDestinations();
        destinations.add("Begriff");
        appl.addInterestingMailExpressions(destinations);

        MailHeaders headers = new MailHeaders();
        headers.add("Kopf");
        appl.addInterestingMailExpressions(headers);
        appl.startAcceptingMailsEvents();
    }

    public void processMail(NotEOFMail mail) {
        System.out.println("================================================");
        System.out.println("Mail ist angekommen...");
        System.out.println("________________________________________________");
        System.out.println("Header: " + mail.getHeader());
        System.out.println("Body Text: " + mail.getBodyText());
        System.out.println("Destination: " + mail.getDestination());
        System.out.println("ClientNetId: " + mail.getToClientNetId());
        System.out.println("================================================");
    }

    public void processMailException(Exception e) {
        LocalLog.error("Mail-Empfang wurde mit einem Fehler unterbrochen: ", e);
    }

    public static void main(String... args) throws HapptickException {

        new MailRecipient(args);

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void processEvent(NotEOFEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processEventException(Exception e) {
        // TODO Auto-generated method stub

    }
}
