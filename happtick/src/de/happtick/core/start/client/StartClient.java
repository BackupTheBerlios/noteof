package de.happtick.core.start.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.event.ApplicationStartEvent;
import de.happtick.core.event.InternalClientStarterEvent;
import de.happtick.core.util.ExternalCalls;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.interfaces.EventRecipient;

/**
 * This client awaits StartEvents by the StartService. It is needed to start
 * applications (executables).
 * <p>
 * If a StartEvent is raised there are two possibilities: <br>
 * <ul>
 * <li>The application is not using the HapptickApplication interface.<br>
 * This is when the application executable is configured as 'EXTERNAL'.<br>
 * In such cases the very special HapptickApplication HapptickSimpleApplication
 * is started with the name of the application as an argument.</>
 * <li>The application is using the HapptickApplication interface.<br>
 * This works if the application executable is configured as 'JAVA'.<br>
 * Then the application is started directly by the StartClient.</>
 * </ul>
 * 
 * @author Dirk
 */
public class StartClient extends HapptickApplication implements EventRecipient {

    private boolean stopped = false;

    public StartClient(String serverAddress, int port, String[] args) throws ActionFailedException {
        super(new Long(-99), serverAddress, port, args);
        super.setEventRecipient(this);

        doWork();
    }

    private void initEventAccepting() {
        // Catching important events is defined here
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new ApplicationStartEvent());
        try {
            addInterestingEvents(events);
            startAcceptingEvents();
        } catch (ActionFailedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        sendStartEvent();
    }

    private void doWork() throws ActionFailedException {
        initEventAccepting();

        // tell scheduler that at this computer a StartClient is active
        System.out.println("Vor SendStartEvent");
        System.out.println("Nach SendStartEvent");

        while (!stopped) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("StartClient wird beendet.\n" + e);
                break;
            }
        }
        sendStopEvent();
    }

    private void sendStartEvent() {
        NotEOFEvent event = new InternalClientStarterEvent();
        try {
            event.addAttribute("state", "START");
            event.addAttribute("clientIp", super.getLocalAddress());
            sendEvent(event);
        } catch (ActionFailedException e) {
            LocalLog.error("Fehler bei Senden des StartEvent an den Scheduler. Scheduler kann Anwendungen fuer diesen Client nicht beruecksichtigen.", e);
        }
    }

    private void sendStopEvent() {
        NotEOFEvent event = new InternalClientStarterEvent();
        try {
            event.addAttribute("state", "STOP");
            event.addAttribute("clientIp", super.getLocalAddress());
            sendEvent(event);
        } catch (ActionFailedException e) {
            LocalLog.error("Fehler bei Senden des StopEvent an den Scheduler. Scheduler wird Anwendungen fuer diesen Client weiter beruecksichtigen.", e);
        }
    }

    private synchronized void startStarter(NotEOFEvent event) {
        ApplStarter starter = new ApplStarter(this, event);
        Thread starterThread = new Thread(starter);
        starterThread.start();
    }

    /*
     * Starts applications. If applicationType is 'EXTERNAL' the special
     * internal ApplicationClient is started here.
     */
    private class ApplStarter implements Runnable {
        private StartClient starter;
        private NotEOFEvent startEvent;

        protected ApplStarter(StartClient starter, NotEOFEvent startEvent) {
            this.startEvent = startEvent;
            this.starter = starter;
        }

        @Override
        public void run() {
            try {
                startApplication(startEvent);
            } catch (Exception e) {
                String applId = null;
                if (!Util.isEmpty(startEvent))
                    try {
                        applId = String.valueOf(startEvent.getAttribute("workApplicationId"));
                    } catch (Exception ex) {
                        LocalLog.error("Fehler bei Verarbeitung eines StartEvents. Das Event ist nicht korrekt initialisiert.", ex);
                    }

                LocalLog.warn("Start einer Anwendung nicht moeglich. Id: " + applId, e);
            }
        }

        private void startApplication(NotEOFEvent startEvent) throws ActionFailedException {
            String applicationType = null;
            try {
                applicationType = startEvent.getAttribute("applicationType");
            } catch (Exception ex) {
                LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
            }

            if (Util.isEmpty(applicationType))
                throw new ActionFailedException(10650L, "applicationType");

            // create unique identifier for the application process
            String startId = getServerAddress() + String.valueOf(Thread.currentThread().getId()) + String.valueOf(new Date().getTime());

            // if type is 'INTERNAL' the application will be started 'directly'
            // by
            // it's name
            // if type is 'EXTERNAL' an instance of the
            // ExternalApplicationStarter will be build and the he starts and
            // controls the 'foreign' process
            if ("INTERNAL".equalsIgnoreCase(applicationType)) {
                System.out.println("StartClient.startApplication. Vor Internal Starten.");
                new HapptickApplicationStarter(starter, getServerAddress(), getServerPort(), startId, startEvent);
                System.out.println("StartClient.startApplication. Nach Internal Starten.");
            } else if ("EXTERNAL".equalsIgnoreCase(applicationType)) {
                ExternalCalls calls = new ExternalCalls();
                calls.callHapptickApplMain(ExternalApplicationStarter.class.getCanonicalName(), getServerAddress(), getServerPort(), startId, startEvent);
            } else
                throw new ActionFailedException(1L, "Type: " + applicationType);
        }
    }

    /**
     * This method works on the StartEvent. It is the central point for
     * scheduling jobs.
     */
    @Override
    public synchronized void processEvent(NotEOFEvent event) {
        if (event.equals(EventType.EVENT_APPLICATION_START)) {
            System.out.println("StartClient.processEvent: EVENT_APPLICATION_START mit ApplId: " + event.getAttribute("workApplicationId"));
            startStarter(event);
            System.out.println("StartClient.processEvent: EVENT_APPLICATION_STARTED mit ApplId: " + event.getAttribute("workApplicationId"));
        }
    }

    /**
     * Logging Errors which happen on the Event-Interface.
     */
    @Override
    public void processEventException(Exception e) {
        LocalLog.error("Fehler wurde durch die Event-Schnittstelle ausgeloest.", e);
        try {
            reconnect();
            initEventAccepting();
        } catch (ActionFailedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * This application is used to start other applications.
     * <p>
     * 
     * @param args
     *            For connect to one of the central server it must be called
     *            with some arguments:
     *            <ul>
     *            <li>--serverIp=address</> <li>--serverPort=port</>
     *            </ul>
     */
    public static void main(String... args) throws Exception {
        int port = 0;
        String address = "";
        ArgsParser parser = new ArgsParser(args);
        if (parser.containsStartsWith("--serverIp")) {
            address = parser.getValue("serverIp");
        }
        if (parser.containsStartsWith("--serverPort")) {
            port = Util.parseInt(parser.getValue("serverPort"), 0);
        }

        if (!Util.isEmpty(address) && 0 != port) {
            new StartClient(address, port, args);
        } else {
            System.out.println("This application must be started as: ");
            System.out.println(" StartClient --serverIp=ip --serverPort=port");
            System.out.println("FINISHED.");
        }
    }
}
