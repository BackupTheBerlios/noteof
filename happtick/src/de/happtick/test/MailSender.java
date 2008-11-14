package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.events.StartEvent;
import de.notEOF.core.exception.ActionFailedException;

public class MailSender {

    public static void main(String... args) throws ActionFailedException {

        HapptickApplication appl = new HapptickApplication(2, "localhost", 3000, args);
        // NotEOFMail mail;
        // try {

        // appl.sendActionEvent("965", "Eine tolle Action");
        // appl.sendAlarm("55", "Schlimmer Alarm", "2");

        StartEvent event = new StartEvent();
        event.addAttribute("clientIp", "192.168.0.2");
        event.addAttribute("applicationId", "99");
        // event.addAttribute("applicationPath",
        // "C:\\Projekte\\workspace\\noteof\\util\\test.bat");
        event.addAttribute("applicationPath", "calc.exe");
        event.addAttribute("applicationType", "UNKNOWN");
        event.addAttribute("arguments", "--bla=blubb");

        System.out.println("Kontrolle applicationPath: " + event.getAttribute("applicationPath"));
        appl.sendEvent(event);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
