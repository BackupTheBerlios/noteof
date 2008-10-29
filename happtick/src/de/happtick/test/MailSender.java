package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.exception.HapptickException;

public class MailSender {

    public static void main(String... args) throws HapptickException {

        HapptickApplication appl = new HapptickApplication(2, "localhost", 3000, args);
        // NotEOFMail mail;
        // try {
        appl.sendActionEvent("965", "Eine tolle Action");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        appl.sendAlarm("55", "Schlimmer Alarm", "2");

        // int counter = 0;
        // while (true) {
        // mail = new NotEOFMail("Kopf", "Counter: " + counter++,
        // String.valueOf(counter));
        // appl.sendMail(mail);
        // }
        // } catch (ActionFailedException e) {
        // throw new HapptickException(600L, "Anlegen einer Mail.", e);
        // }
    }
}
