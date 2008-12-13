package de.happtick.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.event.ActionEvent;
import de.happtick.core.event.AlarmEvent;
import de.happtick.core.event.ApplicationStartErrorEvent;
import de.happtick.core.event.ApplicationStartedEvent;
import de.happtick.core.event.ApplicationStoppedEvent;
import de.happtick.core.event.LogEvent;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.GenericEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.mail.MailHeaders;
import de.notEOF.mail.MailToken;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.EventRecipient;

public class MailRecipient extends HapptickApplication implements EventRecipient {

    private int counter = 0;
    private int complete = 0;
    private Date lastStamp;
    private long stampDiff = 0;
    private boolean ready = false;

    public MailRecipient(long applicationId, String serverAddress, int serverPort, String... args) throws HapptickException {
        super(applicationId, serverAddress, serverPort, args);
        setEventRecipient(this);

        System.out.println("MailRecipient.Construction. applicationId = " + this.getApplicationId());

        reconnection();
    }

    private void reconnection() throws HapptickException {
        // Hinzufuegen von interessanten Nachrichteninhalten
        MailToken tokens = new MailToken();
        tokens.add("Begriff");
        // addInterestingMailExpressions(tokens);

        // Hinzufuegen von interessanten Nachrichtenheadern
        MailHeaders headers = new MailHeaders();
        headers.add("Kopf");
        // addInterestingMailExpressions(headers);

        // Hinzufuegen von interessanten Events
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new ActionEvent());
        events.add(new AlarmEvent());
        events.add(new ApplicationStartedEvent());
        events.add(new ApplicationStoppedEvent());
        events.add(new LogEvent());
        events.add(new ApplicationStartErrorEvent());
        events.add(new GenericEvent());
        try {
            addInterestingEvents(events);
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Starte AcceptingEvents!");
        startAcceptingEvents();

        System.out.println("Jetzt gilts!");
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
        ready = true;

    }

    public boolean isReady() {
        return ready;
    }

    public synchronized void processMail(NotEOFMail mail) {
        if (null == lastStamp)
            lastStamp = new Date();
        if (null != mail) {
            Date newStamp = new Date();
            stampDiff += (newStamp.getTime() - lastStamp.getTime());
            lastStamp = new Date();
            // System.out.println("COUNTER: " + counter);
            if (500 <= counter++) {
                System.out.println("COUNTER: " + (complete += counter));
                System.out.println("COUNTER: " + counter);

                long millis = stampDiff / 500;
                System.out.println("Durchschnitt je mail in millis = " + millis);
                stampDiff = 0;
                counter = 0;
            }

        }
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
        LocalLog.error("Mail-Empfang verursachte Fehler: ", e);
    }

    public synchronized void processEvent(NotEOFEvent event) {
        System.out.println("HEY!  " + event.getEventType());

        try {
            if (null != event && EventType.EVENT_GENERIC.equals(event.getEventType())) {
                System.out.println("Aktuell: " + event.getAttribute("counter"));
            }
        } catch (Exception e) {
            LocalLog.error("Fehler bei Anlegen oder Versand des Events.", e);
        }
    }

    public void processEventException(Exception e) {
        LocalLog.error("Event-Empfang verursachte Fehler: ", e);
        // boolean retry = true;
        // while (retry) {
        // try {
        // reconnection();
        // retry = false;
        // } catch (HapptickException e1) {
        // try {
        // Thread.sleep(5000);
        // } catch (InterruptedException e2) {
        // }
        // }
        // }
    }

    public static void main(String... args) throws HapptickException {

        new MailRecipient(3, "localhost", 3000, args);

    }

    @Override
    public void processStopEvent(NotEOFEvent event) {

    }

}
