package de.happtick.core.start.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.core.client.HapptickBaseClient;
import de.happtick.core.event.ApplicationStartEvent;
import de.happtick.core.event.StartClientEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.ExternalCalls;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.EventRecipient;

/**
 * This client awaits StartEvents by the StartService. It is needed to start
 * applications (executables).
 * <p>
 * If a StartEvent is raised there are two possibilities: <br>
 * <ul>
 * <li>The application is not using the HapptickApplication interface.<br>
 * This is when the application executable is configured as 'UNKNOWN'.<br>
 * In such cases the very special HapptickApplication HapptickSimpleApplication
 * is started with the name of the application as an argument.</>
 * <li>The application is using the HapptickApplication interface.<br>
 * This works if the application executable is configured as 'JAVA'.<br>
 * Then the application is started directly by the StartClient.</>
 * </ul>
 * 
 * @author Dirk
 */
public class StartClient extends HapptickBaseClient implements EventRecipient {

    private String serverAddress;
    private int port;
    private String[] args;
    private boolean stopped = false;

    public StartClient(String serverAddress, int port, String[] args) throws HapptickException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.args = args;
        doWork();
    }

    private void doWork() throws HapptickException {
        // must be called before useMailsAndEvents()
        connect(serverAddress, port, args, false);

        // Activate EventSystem
        useEvents(this, false);

        // Catching important events is defined here
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new ApplicationStartEvent());
        addInterestingEvents(events);
        // Before this the method useMailsAndEvents() must be called
        startAcceptingEvents();

        // tell scheduler that at this computer a StartClient is active
        NotEOFEvent event = new StartClientEvent();
        try {
            event.addAttribute("state", "START");
            event.addAttribute("clientIp", getSimpleClient().getTalkLine().getSocketToPartner().getLocalAddress().getHostName());
            sendEvent(event);
        } catch (ActionFailedException e) {
            LocalLog.error("Fehler bei Senden des StartEvent an den Scheduler. Scheduler kann Anwendungen fuer diesen Client nicht beruecksichtigen.", e);
        }

        while (!stopped) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("StartClient wird beendet.\n" + e);
                break;
            }
        }
        event = new StartClientEvent();
        try {
            event.addAttribute("state", "STOP");
            event.addAttribute("clientIp", getSimpleClient().getTalkLine().getLocalAddress());
            sendEvent(event);
        } catch (ActionFailedException e) {
            LocalLog.error("Fehler bei Senden des StopEvent an den Scheduler. Scheduler wird Anwendungen fuer diesen Client weiter beruecksichtigen.", e);
        }
    }

    private synchronized void startStarter(NotEOFEvent event) {
        ApplStarter starter = new ApplStarter(this.notEofClient, event);
        Thread starterThread = new Thread(starter);
        starterThread.start();
    }

    /*
     * Starts applications. If applicationType is 'unknown' the special internal
     * ApplicationClient is started here.
     */
    private class ApplStarter implements Runnable {
        private NotEOFClient client;
        private NotEOFEvent startEvent;

        protected ApplStarter(NotEOFClient client, NotEOFEvent startEvent) {
            this.startEvent = startEvent;
            this.client = client;
        }

        @Override
        public void run() {
            try {
                startApplication(startEvent);
            } catch (Exception e) {
                String applId = null;
                if (!Util.isEmpty(startEvent))
                    try {
                        applId = String.valueOf(startEvent.getApplicationId());
                    } catch (Exception ex) {
                        LocalLog.error("Fehler bei Verarbeitung eines StartEvents. Das Event ist nicht korrekt initialisiert.", ex);
                    }

                LocalLog.warn("Start einer Anwendung nicht moeglich. Id: " + applId, e);
            }
        }

        private void startApplication(NotEOFEvent startEvent) throws HapptickException {
            String applicationType = null;
            try {
                applicationType = startEvent.getAttribute("applicationType");
            } catch (Exception ex) {
                LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
            }

            if (Util.isEmpty(applicationType))
                throw new HapptickException(650L, "applicationType");

            // create unique identifier for the application process
            String startId = getServerAddress() + String.valueOf(Thread.currentThread().getId()) + String.valueOf(new Date().getTime());

            // if type is 'java' the application will be started 'directly' by
            // it's name
            // if type is 'unknown' an instance of the
            // ExternalApplicationStarter will be build and the he starts and
            // controls the 'foreign' process
            if ("JAVA".equalsIgnoreCase(applicationType)) {
                new HapptickApplicationStarter(client, getServerAddress(), getServerPort(), startId, startEvent);
            } else if ("UNKNOWN".equalsIgnoreCase(applicationType)) {
                ExternalCalls calls = new ExternalCalls();
                calls.callHapptickMain(ExternalApplicationStarter.class.getCanonicalName(), getServerAddress(), getServerPort(), startId, startEvent);
            } else
                throw new HapptickException(1L, "Type: " + applicationType);
        }
    }

    /**
     * This method works on the StartEvent. It is the central point for
     * scheduling jobs.
     */
    @Override
    public synchronized void processEvent(NotEOFEvent event) {
        if (event.equals(EventType.EVENT_APPLICATION_START)) {
            startStarter(event);
        }
    }

    /**
     * Logging Errors which happen on the Event-Interface.
     */
    @Override
    public void processEventException(Exception e) {
        LocalLog.error("Fehler wurde durch die Event-Schnittstelle ausgeloest.", e);
        boolean err = true;
        while (err)
            try {
                doWork();
                err = false;
            } catch (HapptickException e1) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                e1.printStackTrace();
            }

    }

    /**
     * This client doesn't await mails
     */
    @Override
    public synchronized void processMail(NotEOFMail mail) {
    }

    /**
     * Should not be raised...
     */
    @Override
    public void processMailException(Exception e) {
        LocalLog.error("Fehler wurde durch die Mail-Schnittstelle ausgeloest.", e);

    }

    @Override
    public void processStopEvent(NotEOFEvent event) {
        this.stopped = true;
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
