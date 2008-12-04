package de.happdemo;

import java.util.ArrayList;
import java.util.List;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.EventRecipient;

public class Actor extends HapptickApplication implements EventRecipient {
    private String errorSound = "bla";

    public Actor(long applicationId, String serverAddress, int serverPort, String[] args) throws HapptickException {
        super(applicationId, serverAddress, serverPort, args);
        doWork();
    }

    private void doWork() throws HapptickException {

        SoundPlayer.playSound("C:\\Dokumente und Einstellungen\\Dirk\\Eigene Dateien\\Eigene Musik\\Sven01.wav");

        // Anwendung will selbst mails oder events verarbeiten
        useEvents(this);

        // Hinzufuegen von interessanten Events
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new SoundEvent());
        addInterestingEvents(events);

        // jetzt geht's los
        startAcceptingEvents();

        System.out.println("Wohlan!");
    }

    /**
     * Sendet ein Event an das System. <br>
     * Dieses Event beinhaltet:
     * <ul>
     * <li>Die ApplicationId, die aber fester Bestandteil aller Events ist</>
     * <li>Den Namen der zuletzt abgespielten Sounddatei</li>
     * <li>Einen Merker, der aussagt, dass es sich um einen abgespielten Sound
     * handelt</>
     * </ul>
     */
    private void sendSoundEvent(NotEOFEvent event) {
        try {
            sendEvent(event);
        } catch (HapptickException e) {
            e.printStackTrace();
        }
    }

    /**
     * Steuert das Abspielen der Sounddatei an und sorgt fuer die Rueckmeldung.
     */
    private void playSound(NotEOFEvent event) {
        // Wie lange bis zum Abspielen warten?
        long waitMillis = Util.parseLong(event.getAttribute("delay"), 0);
        String soundFile = event.getAttribute("soundFile");

        // Die verlangte Zeit bis zum Abspielen warten.
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e1) {
        }

        // Spiele Sound
        SoundPlayer.playSound(soundFile);

        // Response Event
        SoundEvent newSoundEvent = new SoundEvent();
        try {
            newSoundEvent.addAttribute("soundFile", soundFile);
            newSoundEvent.addAttribute("state", "played");
        } catch (ActionFailedException e) {
            e.printStackTrace();
        }

        // Antworte den uebrigen Akteuren
        sendSoundEvent(newSoundEvent);
    }

    /**
     * Hier werden die eingehenden Events verarbeitet.
     * <p>
     * Das sind vorrangig die Sound-Nachrichten, die durch die anderen Akteure
     * in diesem Spiel direkt oder indirekt ueber die Konfiguration ausgeloest
     * wurden. <br>
     * Sound-Nachrichten enthalten:
     * <ul>
     * <li>Die abzuspielende Sounddatei (Dateiname)</li>
     * <li>Die Zeitverzoegerung bis zum Abspielen (Millisekunden)</li>
     * </ul>
     */
    @Override
    public void processEvent(NotEOFEvent event) {
        if (event.equals(EventType.EVENT_SOUND)) {
            playSound(event);
        }
    }

    @Override
    public void processEventException(Exception arg0) {
        SoundPlayer.playSound(errorSound);
    }

    /**
     * Mails werden von dieser Anwendung nicht erwartet.
     */
    @Override
    public void processMail(NotEOFMail arg0) {
    }

    /**
     * Da keine Mails versendet werden, duerfen auch keine Mail-Fehler
     * auftauchen.
     */
    @Override
    public void processMailException(Exception arg0) {
    }

    @Override
    public void processStopEvent(NotEOFEvent arg0) {
        // TODO Auto-generated method stub
    }

    public static void main(String[] args) {
        try {
            new Actor(10, "localhost", 3000, args);
        } catch (HapptickException e) {
            e.printStackTrace();
        }
    }
}
