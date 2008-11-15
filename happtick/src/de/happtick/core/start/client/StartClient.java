package de.happtick.core.start.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.core.client.HapptickBaseClient;
import de.happtick.core.events.StartEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.ExternalCalls;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.MailAndEventRecipient;
import de.notIOC.logging.LocalLog;

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
public class StartClient extends HapptickBaseClient implements MailAndEventRecipient {

    public StartClient(String serverAddress, int port, String[] args) throws HapptickException {
        // must be called before useMailsAndEvents()
        connect(serverAddress, port, args);

        // Activate EventSystem
        useMailsAndEvents(this, false);

        // Catching important events is defined here
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new StartEvent());
        addInterestingEvents(events);
        // Before this the method useMailsAndEvents() must be called
        startAcceptingMailsEvents();

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("StartClient wurde beendet.\n" + e);
                break;
            }
        }
    }

    // TODO Pruefen, warum bei nicht Zurueckkehren des
    // ExternalApplicationStarter sich der hier aufhהngt... Nur interessehalber
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
                        applId = startEvent.getAttribute("applicationId");
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

            // if type is 'java' the application start the application itself
            // if type is 'unknown' start the special Happtick application which
            // controls 'foreign' processess
            if ("JAVA".equalsIgnoreCase(applicationType)) {
                new HapptickApplicationStarter(client, getServerAddress(), getServerPort(), startId, startEvent);
            } else if ("UNKNOWN".equalsIgnoreCase(applicationType)) {
                ExternalCalls calls = new ExternalCalls();
                calls.call(ExternalApplicationStarter.class.getCanonicalName(), getServerAddress(), getServerPort(), startId, startEvent);
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
        // process start request
        // try {
        // System.out.println(
        // "ההההההההההההההההההההההה   Event ist eingetroffen... ההההההההההההההההההההההההההההההה"
        // );
        // System.out.println("event applicationPath = " +
        // event.getAttribute("applicationPath"));
        // System.out.println(
        // "ההההההההההההההההההההההה   Event wurde eingetroffen... ההההההההההההההההההההההההההההההה"
        // );
        // } catch (ActionFailedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (event.getEventType().equals(EventType.EVENT_APPLICATION_START)) {
            startStarter(event);
        }
    }

    /**
     * Logging Errors which happen on the Event-Interface.
     */
    @Override
    public void processEventException(Exception e) {
        LocalLog.error("Fehler wurde durch die Event-Schnittstelle ausgelצst.", e);

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
        LocalLog.error("Fehler wurde durch die Mail-Schnittstelle ausgelצst.", e);

    }

    // @Override
    // protected void initHapptickBaseClient(String serverAddress, int
    // serverPort, String[] args, NotEOFClient notEofClient) throws
    // HapptickException {
    // super.init(serverAddress, serverPort, args, notEofClient);
    // }

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
