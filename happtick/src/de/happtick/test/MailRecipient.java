package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.MailEventRecipient;

public class MailRecipient implements MailEventRecipient {

    private HapptickApplication appl;

    public MailRecipient(String... args) throws ActionFailedException {
        appl = new HapptickApplication(0, "localhost", 3000, args);
        appl.acceptMailsAndEvents(this);
    }

    public void processMail(NotEOFMail mail) {
        System.out.println("Mail ist angekommen...");
        System.out.println("Header: " + mail.getHeader());
        System.out.println("Body Text: " + mail.getBodyText());
        System.out.println("Destination: " + mail.getDestination());
        System.out.println("ClientNetId: " + mail.getToClientNetId());
    }

    public static void main(String... args) throws ActionFailedException {

        new MailRecipient(args);

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
