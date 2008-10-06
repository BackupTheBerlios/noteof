package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.NotEOFMail;

public class MailSender {

    public static void main(String... args) throws ActionFailedException {

        HapptickApplication appl = new HapptickApplication(2, "localhost", 3000, args);
        NotEOFMail mail = new NotEOFMail("Kopf", "Blabla", "Bestimmung");
        appl.sendMail(mail);
    }
}
