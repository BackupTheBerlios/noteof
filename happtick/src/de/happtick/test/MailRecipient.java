package de.happtick.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.events.ActionEvent;
import de.happtick.core.events.LogEvent;
import de.happtick.core.events.StartEvent;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.mail.MailHeaders;
import de.notEOF.mail.MailToken;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.MailAndEventRecipient;

public class MailRecipient implements MailAndEventRecipient {

    private HapptickApplication appl;
    private int counter = 0;
    private int complete = 0;
    private Date lastStamp;
    private long stampDiff = 0;

    public MailRecipient(String... args) throws HapptickException {
        appl = new HapptickApplication(0, "localhost", 3000, args);

        // Anwendung will selbst mails oder events verarbeiten
        appl.useMailsAndEvents(this);

        // Hinzufuegen von interessanten Nachrichteninhalten
        MailToken tokens = new MailToken();
        tokens.add("Begriff");
        appl.addInterestingMailExpressions(tokens);

        // Hinzufuegen von interessanten Nachrichtenheadern
        MailHeaders headers = new MailHeaders();
        headers.add("Kopf");
        appl.addInterestingMailExpressions(headers);

        // Hinzufuegen von interessanten Events
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new ActionEvent());
        events.add(new StartEvent());
        events.add(new LogEvent());
        appl.addInterestingEvents(events);

        // jetzt geht's los
        appl.startAcceptingMailsEvents();

        System.out.println("Jetzt gilts!");
        processMail(null);
        // processEvent(null);
    }

    public void processMail(NotEOFMail mail) {
        if (null == lastStamp)
            lastStamp = new Date();
        if (null != mail) {
            Date newStamp = new Date();
            stampDiff += (newStamp.getTime() - lastStamp.getTime());
            lastStamp = new Date();
            if (500 <= counter++) {
                System.out.println("COUNTER: " + (complete += counter));
                System.out.println("COUNTER: " + counter);

                long millis = stampDiff / 500;
                System.out.println("Durchschnitt je mail in millis = " + millis);
                stampDiff = 0;
                counter = 0;
            }

            // System.out.println(
            // "================================================");
            // System.out.println("Mail ist angekommen...");
            // System.out.println(
            // "________________________________________________");
            // System.out.println("Header: " + mail.getHeader());
            // System.out.println("Body Text: " + mail.getBodyText());
            // System.out.println("Destination: " + mail.getDestination());
            // System.out.println("ClientNetId: " + mail.getToClientNetId());
            // System.out.println(
            // "================================================");
        }

        try {
            Thread.sleep(5);
            NotEOFMail newMail;
            newMail = new NotEOFMail("Kopf", "xBegriff", "Wichtiger Inhalt");
            appl.sendMail(newMail);
        } catch (Exception e) {
            LocalLog.error("Fehler bei Anlegen oder Versand der Mail.", e);
        }

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
        int eventSwitch = 0;
        if (null != event) {
            EventType type = event.getEventType();
            if (type.equals(EventType.EVENT_LOG))
                eventSwitch = 1;
        }

        try {
            NotEOFEvent newEvent = new LogEvent();
            newEvent.addAttribute("information", "Holderidudödeldu");
            if (0 == eventSwitch) {
                System.out.println("?????????????????????");
                appl.sendEvent(newEvent);
            } else {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                appl.sendActionEvent("965", "Eine tolle Action");
            }
        } catch (Exception e) {
            LocalLog.error("Fehler bei Anlegen oder Versand des Events.", e);
        }
    }

    @Override
    public void processEventException(Exception e) {
        LocalLog.error("Event-Empfang wurde mit einem Fehler unterbrochen: ", e);
    }
}
