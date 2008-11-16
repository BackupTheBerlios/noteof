package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.events.StartEvent;
import de.notEOF.core.exception.ActionFailedException;

public class MailSender {

    public static void main(String... args) throws ActionFailedException {

        HapptickApplication appl = new HapptickApplication(2, "localhost", 3000, args);
        // NotEOFMail mail;
        // try {

        StartEvent event = new StartEvent();
        event.addAttribute("clientIp", "192.168.0.2");
        // int counter = -1;
        // while (true) {
        // counter++;
        // appl.sendActionEvent(String.valueOf(counter),
        // String.valueOf(counter));
        // appl.sendAlarm(String.valueOf(counter), String.valueOf(counter),
        // String.valueOf(counter));
        // try {
        // Thread.sleep(50);
        // } catch (InterruptedException e) {
        // }
        // if (false)
        // break;

        // event.addAttribute("applicationId", "99");
        // event.addAttribute("applicationPath", "calc.exe");
        // event.addAttribute("applicationType", "UNKNOWN");
        // event.addAttribute("arguments", "123 :m");
        // System.out.println("Kontrolle applicationPath: " +
        // event.getAttribute("applicationPath"));
        // appl.sendEvent(event);
        // }

        // try {
        // Thread.sleep(150);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        event.setApplicationId(new Long(77));
        event.addAttribute("applicationId", "77");
        // event.addAttribute("applicationPath",
        // "cmd /c start/wait C:\\Projekte\\workspace\\noteof\\util\\applStartTest.bat"
        // );
        event.addAttribute("applicationPath", "C:\\Projekte\\workspace\\noteof\\util\\applStartTest.bat");
        event.addAttribute("windowsSupport", "true");
        // event.addAttribute("applicationPath",
        // "cmd /c start/wait C:\\Projekte\\workspace\\noteof\\util\\applStartTest.bat"
        // );
        event.addAttribute("applicationType", "JAVA");
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
