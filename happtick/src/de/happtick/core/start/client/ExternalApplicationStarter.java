package de.happtick.core.start.client;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.event.ApplicationStartErrorEvent;
import de.happtick.core.event.ApplicationStartedEvent;
import de.happtick.core.event.ApplicationStoppedEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.ExternalCalls;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;

/**
 * This class starts external applications and tries to monitor them.
 * <p>
 * Monitoring means sending events to central !EOF server when application was
 * started and stopped.
 * 
 * @author Dirk
 * 
 */
public class ExternalApplicationStarter extends HapptickApplication {

    /**
     * Constructor for this class...
     * 
     * @param applicationId
     * @param applicationPath
     * @param serverAddress
     * @param serverPort
     * @param applArgs
     * @throws HapptickException
     */
    public ExternalApplicationStarter(long applicationId, String startId, String applicationPath, String serverAddress, int serverPort,
            String startIgnitionTime, boolean windowsSupport, String applArgs) throws HapptickException {
        super(applicationId, serverAddress, serverPort, applArgs);

        if (Util.isEmpty(applicationId))
            throw new HapptickException(650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new HapptickException(650L, "applicationPath");

        WorkerProcess worker = new WorkerProcess(applicationId, applicationPath, startId, startIgnitionTime, windowsSupport, applArgs);
        Thread workerThread = new Thread(worker);
        workerThread.start();

        // wait till worker has finished
        while (worker.processIsActive) {
            // time isn't real relevant because the callback method sends the
            // event to the server
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                try {
                    LocalLog.warn("Unerwarteter Abbruch des ExternalApplicationStarter.", e);
                } catch (Exception ex) {
                }
            }
        }
    }

    // This callback is called by worker. So the sleep within the loop above can
    // use a relative long time.
    private synchronized void callBackForWorker(NotEOFEvent event) {
        try {
            sendEvent(event);
        } catch (HapptickException e) {
            LocalLog.warn("Event konnte nicht versendet werden. EventType: " + event.getEventType() + "; ApplicationId: " + super.getApplicationId(), e);
        }
    }

    private class WorkerProcess implements Runnable {
        private boolean processIsActive = true;
        private String applicationPath = null;
        private Long applicationId = null;
        private String startId = null;
        private String applArgs = null;
        private String startIgnitionTime = null;
        private boolean windowsSupport = false;

        protected WorkerProcess(Long applicationId, String applicationPath, String startId, String startIgnitionTime, boolean windowsSupport, String applArgs) {
            this.applicationPath = applicationPath;
            this.applArgs = applArgs;
            this.startId = startId;
            this.applicationId = applicationId;
            this.startIgnitionTime = startIgnitionTime;
            this.windowsSupport = windowsSupport;
        }

        protected boolean isActive() {
            return processIsActive;
        }

        /**
         * Worker starts process and waits for it's termination
         */
        public void run() {
            boolean started = false;
            String errMsg = "";
            long errNo = 0;
            Process process = null;
            Throwable th = null;

            try {
                // create process
                ExternalCalls calls = new ExternalCalls();
                process = calls.startApplication(applicationPath, applArgs, windowsSupport);
                started = true;
            } catch (HapptickException he) {
                errNo = he.getErrNo();
                errMsg = he.getMessage();
                th = he;
            } catch (Throwable e) {
                errMsg = e.getMessage();
                th = e;
            }

            if (!started) {
                LocalLog.error("Applikation konnte nicht gestartet werden.", th);
                try {
                    ApplicationStartErrorEvent errorEvent = new ApplicationStartErrorEvent();
                    errorEvent.addAttribute("workApplicationId", String.valueOf(applicationId));
                    errorEvent.addAttribute("clientNetId", getClientNetId());
                    errorEvent.addAttribute("startId", startId);
                    errorEvent.addAttribute("errorDescription", errMsg);
                    errorEvent.addAttribute("errorId", String.valueOf(errNo));
                    errorEvent.addAttribute("errorLevel", "0");
                    errorEvent.addAttribute("startIgnitionTime", startIgnitionTime);
                    // main class now will send the event to the server
                    callBackForWorker(errorEvent);
                } catch (Exception e) {
                    LocalLog.warn("StartErrorEvent konnte nicht versendet werden.", e);
                }

            } else if (null != process) {
                try {
                    // send StartedEvent
                    ApplicationStartedEvent startedEvent = new ApplicationStartedEvent();
                    startedEvent.addAttribute("workApplicationId", String.valueOf(applicationId));
                    startedEvent.addAttribute("clientNetId", getClientNetId());
                    startedEvent.addAttribute("startId", startId);
                    // main class now will send the event to the server
                    callBackForWorker(startedEvent);

                    int exitCode = 0;
                    // exit code perhaps is used for event configuration and
                    // evaluated to raise other actions
                    try {
                        exitCode = process.waitFor();
                    } catch (InterruptedException ix) {
                        ix.printStackTrace();
                        if (null != process) {
                            // maybe process was killed or something else
                            exitCode = process.exitValue();
                            LocalLog.warn("ExternalApplicationStarter.run. Interrupted... exitValue = " + exitCode);
                        }
                    }

                    // create Event for inform other processes (clients,
                    // services)
                    ApplicationStoppedEvent stoppedEvent = new ApplicationStoppedEvent();
                    stoppedEvent.addAttribute("workApplicationId", String.valueOf(applicationId));
                    stoppedEvent.addAttribute("exitCode", String.valueOf(exitCode));
                    stoppedEvent.addAttribute("serviceId", "");
                    stoppedEvent.addAttribute("clientNetId", getClientNetId());
                    stoppedEvent.addAttribute("startId", startId);

                    // main class now will send the event to the server
                    callBackForWorker(stoppedEvent);

                } catch (Exception ex) {
                    LocalLog.warn("Anwendung wurde mit Fehler beendet. Anwendung: " + applicationPath, ex);
                }
            }
            processIsActive = false;
        }
    }

    /**
     * Normally this main method is called by the Happtick class StartClient.
     * <p>
     * 
     * @param args
     *            To start external 'foreign' applications some arguments are
     *            needed.
     *            <ul>
     *            <li>--applicationId</> <br>
     *            <li>--startId</> <br>
     *            <li>--applicationPath</><br>
     *            <li>--serverAddress</> <br>
     *            <li>--serverPort</> <br>
     *            <li>--arguments</><br>
     *            </ul>
     * @throws HapptickException
     */
    public static void main(String[] args) throws HapptickException {
        // Scan special Happtick arguments
        ArgsParser argsParser = new ArgsParser(args);
        String applicationId = argsParser.getValue("applicationId");
        String startId = argsParser.getValue("startId");
        String applicationPath = argsParser.getValue("applicationPath");
        String serverAddress = argsParser.getValue("serverAddress");
        String serverPort = argsParser.getValue("serverPort");
        String startIgnitionTime = argsParser.getValue("startIgnitionTime");
        String windowsSupport = argsParser.getValue("windowsSupport");

        // Remove Happtick arguments
        argsParser.removeParameterAll("--applicationId");
        argsParser.removeParameterAll("--startId");
        argsParser.removeParameterAll("--applicationPath");
        argsParser.removeParameterAll("--serverAddress");
        argsParser.removeParameterAll("--serverPort");
        argsParser.removeParameterAll("--startIgnitionTime");
        argsParser.removeParameterAll("--windowsSupport");

        // build argument string for the external application
        String arguments = "";
        if (argsParser.getArgs().length > 0) {
            for (String arg : argsParser.getArgs()) {
                arguments += arg + " ";
            }
            arguments.trim();
        }

        // some checks to ensure that all required parameters are set
        if (Util.isEmpty(applicationId)) {
            LocalLog.warn("ExternalApplicationStarter wurde ohne gueltige Application Id aufgerufen.");
            return;
        }
        if (Util.isEmpty(startId)) {
            LocalLog.warn("ExternalApplicationStarter wurde ohne gueltige Start Id aufgerufen.");
            return;
        }
        if (Util.isEmpty(applicationPath)) {
            LocalLog.warn("ExternalApplicationStarter wurde ohne Phadangabe aufgerufen.");
            return;
        }
        if (Util.isEmpty(serverAddress)) {
            LocalLog.warn("ExternalApplicationStarter wurde ohne ServerAdresse aufgerufen.");
            return;
        }
        if (Util.isEmpty(serverPort)) {
            LocalLog.warn("ExternalApplicationStarter wurde ohne ServerPort aufgerufen.");
            return;
        }

        // use class to start application
        new ExternalApplicationStarter(Util.parseLong(applicationId, 0), startId, applicationPath, serverAddress, Util.parseInt(serverPort, 0),
                startIgnitionTime, Util.parseBoolean(windowsSupport, false), arguments);
    }
}
