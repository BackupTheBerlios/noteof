package de.happtick.test;

import java.util.Date;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.event.ApplicationStartEvent;
import de.notEOF.core.exception.ActionFailedException;

public class MailSender {

    public static void main(String... args) throws ActionFailedException {

        HapptickApplication appl = new HapptickApplication(2, "localhost", 3000, args);
        // NotEOFMail mail;
        // try {

        ApplicationStartEvent event = new ApplicationStartEvent();
        event.addAttribute("clientIp", "192.168.0.2");

        System.gc();

        for (int i = 0; i < 25; i++) {
            event.setApplicationId(new Long(77));
            event.addAttribute("applicationId", "77");
            // event.addAttribute("applicationPath",
            // "cmd /c start/wait C:\\Projekte\\workspace\\noteof\\util\\applStartTest.bat"
            // );
            // event.addAttribute("applicationPath",
            // "C:/Projekte/workspace/noteof/util/applStartTest.bat");
            event.addAttribute("applicationPath", "C:/Projekte/workspace/noteof/util/mail_recipient.bat");
            event.addAttribute("windowsSupport", "true");
            // event.addAttribute("applicationPath",
            // "cmd /c start/wait C:\\Projekte\\workspace\\noteof\\util\\applStartTest.bat"
            // );
            event.addAttribute("applicationType", "JAVA");
            appl.sendEvent(event);
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        long startTime = new Date().getTime();
        for (int i = 0; i < 1000; i++) {
            System.out.println("Versende Event Nr. " + i);
            appl.sendActionEvent(String.valueOf(i), String.valueOf(i));
            appl.sendAlarm(String.valueOf(i), String.valueOf(i), String.valueOf(i));
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
            }
            if (false)
                break;
        }
        long duration = (new Date().getTime() - startTime) / 1000;
        System.out.println("Duration: " + duration);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
