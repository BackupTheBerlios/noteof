package de.happdemo;

import java.util.ArrayList;
import java.util.List;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.event.ApplicationStopEvent;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.EventRecipient;

public class Actor extends HapptickApplication implements EventRecipient {
    private boolean stopped = false;

    public Actor(Long applicationId, String serverAddress, int serverPort, String[] args) throws HapptickException {
        super(null, serverAddress, serverPort, args);
        super.setEventRecipient(this);

        doWork(args);
    }

    /**
     * Startet die Anwendung.
     * <p>
     * Evtl. wird die Anwendung mit einer Sounddatei gestartet.
     * 
     * @param args
     *            Kann den Namen einer Sounddatei enthalten. Das waere ein
     *            Parameter --soundFile=soundFile
     * @throws HapptickException
     */
    private void doWork(String[] args) throws HapptickException {
        System.out.println("asljasdfjlasdflöasdfklöasdfljkös");

        // Anwendung will selbst mails oder events verarbeiten

        // TODO jetzt liste direkt übergeben...
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new SoundEvent());
        events.add(new ApplicationStopEvent());

        try {
            addInterestingEvents(events);
        } catch (ActionFailedException e1) {
            e1.printStackTrace();
        }
        System.out.println("Starte jetzt die Annahme von Events.");
        startAcceptingEvents();
        System.out.println("Annahme von Events abgeschlossen");

        // TODO jetzt liste direkt übergeben...
        // try {
        //super.notEofClient.getTalkLine().writeMsg(BaseCommTag.REQ_TEST.name())
        // ;
        // } catch (ActionFailedException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // new Thread(new Bla()).start();

        // Hinzufuegen von interessanten Events
        // List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        // events.add(new SoundEvent());
        // events.add(new ApplicationStopEvent());
        // addInterestingEvents(events);
        //
        // // jetzt geht's los
        // startAcceptingEvents();
        System.out.println("Wohlan!");

        // mal schauen, ob es direkt was zu tun gibt (Abspielen einer
        // Sound-Datei)
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.containsStartsWith("--soundFile")) {
            // Starten der Anwendung mit soundFile
            String soundFile = argsParser.getValue("soundFile");
            if (!Util.isEmpty(soundFile)) {
                playSound(soundFile, 0);
                stopped = true;
            }
        }
        System.out.println("Vor loop.");

        stopped = true;
        // wait for stop event
        while (!stopped) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        // close connection to service in usual way
        System.out.println("Anwendung wird beendet.");
        stop();
        System.out.println("Anwendung beendet.");
    }

    /**
     * Sendet ein Event an das System. <br>
     * Dieses Event beinhaltet:
     * <ul>
     * <li>Die ApplicationId, die aber fester Bestandteil aller Events ist</>
     * <li>Den Namen der zuletzt abgespielten Sounddatei</li> <li>ODER den Namen
     * der gerade aktiven Sounddatei</li> <li>Einen Merker, der aussagt, dass es
     * sich um einen abgespielten Sound handelt</> <li>ODER dass gerade ein
     * Sound gespielt wird</>
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
     * Steuert das Abspielen der Sounddatei an und sorgt fuer eine Rueckmeldung
     * an alle Akteure.
     */
    private void playSound(String soundFile, long waitMillis) {
        // Die verlangte Zeit bis zum Abspielen warten.
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e1) {
        }

        // Response Event
        SoundEvent newSoundEvent = new SoundEvent();
        try {
            newSoundEvent.addAttribute("soundFile", soundFile);
            newSoundEvent.addAttribute("state", "playing");
        } catch (ActionFailedException e) {
            e.printStackTrace();
        }

        // Spiele Sound
        SoundPlayer.playSound(soundFile);

        // Response Event
        newSoundEvent = new SoundEvent();
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
     * Loest das Abspielen der Sounddatei anhand eines Events aus.
     */
    private void playSound(NotEOFEvent event) {
        // Wie lange bis zum Abspielen warten?
        long waitMillis = Util.parseLong(event.getAttribute("delay"), 0);
        String soundFile = event.getAttribute("soundFile");
        playSound(soundFile, waitMillis);
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

    /**
     * Wenn beim Versand eines Events eine Exception geworfen wurde, koennte
     * hier ein Fehler angemeckert werden.
     */
    @Override
    public void processEventException(Exception arg0) {
        // SoundPlayer.playSound(errorSound);
    }

    /**
     * Mails werden von dieser Anwendung nicht erwartet.
     */
    @Override
    public void processMail(NotEOFMail arg0) {
    }

    @Override
    public void processMailException(Exception arg0) {
    }

    /**
     * Finish this process.
     */
    @Override
    public void processStopEvent(NotEOFEvent arg0) {
        this.stopped = true;
    }

    /**
     * Unumgaengliches main...
     * 
     * @param args
     *            Kann evtl. ein --soundFile enthalten
     */
    public static void main(String[] args) {
        try {
            new Actor(null, "localhost", 3000, args);
        } catch (HapptickException e) {
            e.printStackTrace();
        }
    }
}
