package de.happtick.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.events.ActionEvent;
import de.happtick.core.events.AlarmEvent;
import de.happtick.core.events.LogEvent;
import de.happtick.core.events.StartErrorEvent;
import de.happtick.core.events.StartedEvent;
import de.happtick.core.events.StoppedEvent;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.mail.MailHeaders;
import de.notEOF.mail.MailToken;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.MailAndEventRecipient;

public class MailRecipient extends HapptickApplication implements MailAndEventRecipient {

    private int counter = 0;
    private int complete = 0;
    private Date lastStamp;
    private long stampDiff = 0;
    private boolean ready = false;

    public MailRecipient(long applicationId, String serverAddress, int serverPort, String... args) throws HapptickException {
        super(applicationId, serverAddress, serverPort, args);

        // Anwendung will selbst mails oder events verarbeiten
        useMailsAndEvents(this);

        // Hinzufuegen von interessanten Nachrichteninhalten
        MailToken tokens = new MailToken();
        tokens.add("Begriff");
        addInterestingMailExpressions(tokens);

        // Hinzufuegen von interessanten Nachrichtenheadern
        MailHeaders headers = new MailHeaders();
        headers.add("Kopf");
        addInterestingMailExpressions(headers);

        // Hinzufuegen von interessanten Events
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new ActionEvent());
        events.add(new AlarmEvent());
        events.add(new StartedEvent());
        events.add(new StoppedEvent());
        events.add(new LogEvent());
        events.add(new StartErrorEvent());
        addInterestingEvents(events);

        // jetzt geht's los
        startAcceptingMailsEvents();

        System.out.println("Jetzt gilts!");
        ready = true;
        // testMail();
        // processEvent(null);
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
        try {
            if (null != event) {
                System.out.println("=====================================");
                System.out.println("Event ist eingetroffen: " + event.getEventType());
                Map<String, String> attributeMap = event.getAttributes();
                Set<Entry<String, String>> set = attributeMap.entrySet();
                Iterator<Entry<String, String>> it = set.iterator();
                System.out.println("________________________________");
                System.out.println("*QueueId: " + event.getQueueId());
                while (it.hasNext()) {
                    Entry<String, String> entry = it.next();
                    System.out.println("________________________________");
                    System.out.println("Attribute: " + entry.getKey());
                    System.out.println("Value:     " + entry.getValue());
                    System.out.println("________________________________");
                }
                System.out.println("=====================================");
            }
        } catch (Exception e) {
            LocalLog.error("Fehler bei Anlegen oder Versand des Events.", e);
        }
    }

    public void processEventException(Exception e) {
        LocalLog.error("Event-Empfang verursachte Fehler: ", e);
    }

    public static void main(String... args) throws HapptickException {

        // MailRecipient x = new MailRecipient(args);
        new MailRecipient(0, "localhost", 3000, args);

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                System.out.println("HUPS - jetzt bin ich aber am Ende...");
                System.out.println("HUPS - jetzt bin ich aber am Ende...");
                e.printStackTrace();
                break;
            }
        }
        System.out.println("HUPS - jetzt bin ich aber am Ende...");
    }

}
