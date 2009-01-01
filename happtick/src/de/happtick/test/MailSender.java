package de.happtick.test;

import java.util.Date;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.event.ApplicationStartEvent;
import de.notEOF.core.event.GenericEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;

public class MailSender extends HapptickApplication {

    public MailSender(long applicationId, String serverAddress, int serverPort, String... args) throws ActionFailedException {
        super(applicationId, serverAddress, serverPort, args);

        try {
            ApplicationStartEvent event = new ApplicationStartEvent();
            event.addAttribute("clientIp", "192.168.0.2");

            System.gc();

            // for (int i = 0; i < 10; i++) {
            // event.addAttribute("workApplicationId", "3");
            // event.addAttribute("applicationPath",
            // "C:/Projekte/workspace/noteof/util/mail_recipient.bat");
            // event.addAttribute("windowsSupport", "true");
            // event.addAttribute("applicationType", "JAVA");
            // System.out.println("EVENT APPLICATIONID = " +
            // event.getApplicationId());
            // sendEvent(event);
            // try {
            // Thread.sleep(500);
            // } catch (InterruptedException e) {
            // }
            // }
            //
            // try {
            // Thread.sleep(10000);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            long startTime = new Date().getTime();
            int numberOfEvents = 1000;
            for (int i = 0; i < numberOfEvents; i++) {
                System.out.println("Versende Event Nr. " + i);
                NotEOFEvent gEvent = new GenericEvent();
                gEvent.addAttributeDescription("counter", "bla");
                gEvent.addAttribute("counter", String.valueOf(i));
                sendEvent(gEvent);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            long duration = (new Date().getTime() - startTime) / numberOfEvents;
            System.out.println("Duration: " + duration + " Sekunden.");
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (Exception e) {
        }

    }

    public static void main(String... args) throws ActionFailedException {
        new MailSender(0, "localhost", 3000, args);
    }

    public void processEventException(Exception e) {
        LocalLog.error("Fehler wurde durch die Event-Schnittstelle ausgeloest.", e);
        try {
            reconnect();
        } catch (ActionFailedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
