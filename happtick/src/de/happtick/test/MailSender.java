package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.NotEOFMail;

public class MailSender {

    public static void main(String... args) throws HapptickException {

        HapptickApplication appl = new HapptickApplication(2, "localhost", 3000, args);
        NotEOFMail mail;
        try {
            int counter = 0;
            while (true) {

                mail = new NotEOFMail("Kopf", "Counter: " + counter++, String.valueOf(counter));
                appl.sendMail(mail);
            }
        } catch (ActionFailedException e) {
            throw new HapptickException(600L, "Anlegen einer Mail.", e);
        }
    }
}
