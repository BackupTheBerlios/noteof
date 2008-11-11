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
        initHapptickBaseClient(serverAddress, port, args, null);
        connect();

        // Activate EventSystem
        useMailsAndEvents(this, false);

        // Catching important events is defined here
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.add(new StartEvent());
        addInterestingEvents(events);

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("StartClient wurde beendet.\n" + e);
                break;
            }
        }
    }

    private synchronized void startStarter(NotEOFEvent event) {
        ApplStarter starter = new ApplStarter(event);
        Thread starterThread = new Thread(starter);
        starterThread.run();
    }

    /*
     * Starts applications. If applicationType is 'unknown' the special internal
     * ApplicationClient is started here.
     */
    private class ApplStarter implements Runnable {
        private NotEOFEvent event;

        protected ApplStarter(NotEOFEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            try {
                startApplication(event);
            } catch (Exception e) {
                String applId = null;
                if (!Util.isEmpty(event))
                    try {
                        applId = event.getAttribute("applicationId");
                    } catch (Exception ex) {
                        LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
                    }

                LocalLog.warn("Start einer Anwendung nicht moeglich. Id: " + applId, e);
            }
        }

        private void startApplication(NotEOFEvent event) throws HapptickException {
            String applicationId = null;
            String applicationPath = null;
            String arguments = null;
            String applicationType = null;
            try {
                applicationId = event.getAttribute("applicationId");
                applicationPath = event.getAttribute("applicationPath");
                arguments = event.getAttribute("arguments");
                applicationType = event.getAttribute("applicationType");
            } catch (Exception ex) {
                LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
            }

            if (Util.isEmpty(applicationId))
                throw new HapptickException(650L, "applicationId");
            if (Util.isEmpty(applicationPath))
                throw new HapptickException(650L, "applicationPath");
            if (Util.isEmpty(applicationType))
                throw new HapptickException(650L, "applicationType");

            String startId = serverAddress + String.valueOf(Thread.currentThread().getId()) + String.valueOf(new Date().getTime());

            String[] applArgs = null;
            if (!Util.isEmpty(arguments)) {
                List<String> applArgsList = Util.stringToList(arguments, "");
                applArgs = (String[]) applArgsList.toArray();
            }

            // if type is 'java' the application start the application itself
            // if type is 'unknown' start the special Happtick application which
            // controls 'foreign' processess
            LocalLog.info("Starting Application. ApplicationId: " + applicationId + "; ApplicationPath: " + applicationPath + "; Arguments: " + arguments);
            if ("JAVA".equalsIgnoreCase(applicationType)) {
                ExternalCalls.startHapptickApplication(applicationPath, startId, serverAddress, String.valueOf(serverPort), applArgs);
            } else if ("UNKNOWN".equalsIgnoreCase(applicationType)) {
                ExternalCalls.call(ExternalApplicationStarter.class.getCanonicalName(), applicationPath, applicationId, startId, serverAddress, String
                        .valueOf(serverPort), arguments);
            } else
                throw new HapptickException(1L, "Type: " + applicationType);

            LocalLog.info("Application started.  ApplicationId: " + applicationId + "; ApplicationPath: " + applicationPath + "; Arguments: " + arguments);
        }
    }

    /**
     * This method works on the StartEvent. It is the central point for
     * scheduling jobs.
     */
    @Override
    public void processEvent(NotEOFEvent event) {
        // process start request
        if (event.getEventType().equals(EventType.EVENT_APPLICATION_START)) {
            startStarter(event);
        }
    }

    /**
     * Logging Errors which happen on the Event-Interface.
     */
    @Override
    public void processEventException(Exception e) {
        LocalLog.error("Fehler wurde durch die Event-Schnittstelle ausgelöst.", e);

    }

    /**
     * This client doesn't await mails
     */
    @Override
    public void processMail(NotEOFMail mail) {
    }

    /**
     * Should not be raised...
     */
    @Override
    public void processMailException(Exception e) {
        LocalLog.error("Fehler wurde durch die Mail-Schnittstelle ausgelöst.", e);

    }

    @Override
    protected void initHapptickBaseClient(String serverAddress, int serverPort, String[] args, NotEOFClient notEofClient) throws HapptickException {
        super.init(serverAddress, serverPort, args, notEofClient);
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
