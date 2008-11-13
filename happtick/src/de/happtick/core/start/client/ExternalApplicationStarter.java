package de.happtick.core.start.client;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.events.StoppedEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.ExternalCalls;
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
     * Constructor for the
     * 
     * @param applicationId
     * @param applicationPath
     * @param serverAddress
     * @param serverPort
     * @param applArgs
     * @throws HapptickException
     */
    public ExternalApplicationStarter(long applicationId, String startId, String applicationPath, String serverAddress, int serverPort, String applArgs)
            throws HapptickException {
        super(applicationId, serverAddress, serverPort, applArgs);

    }

    private class WorkerProcess implements Runnable {
        private final boolean processIsActive = false;
        private final String applicationPath;
        private final String applicationId;
        private final String applArgs;

        protected WorkerProcess(String applicationPath, String startId, String applArgs) {
            this.applicationPath = applicationPath;
            this.applArgs = applArgs;

        }

        @Override
        public void run() {
            try {
                processIsActive = true;
                Process process = ExternalCalls.startApplication(applicationPath, applArgs);

                StoppedEvent event = new StoppedEvent();
                event.addAttribute("exitCode", String.valueOf(process.exitValue()));
                event.addAttribute("serviceId", "");
                event.addAttribute("applicationId", String.valueOf(applicationId));
                event.addAttribute("clientNetId", getClientNetId());
                event.addAttribute("startId", "");

            } catch (Exception ex) {
                LocalLog.warn("Anwendung wurde mit Fehler beendet. Anwendung: " + applicationPath, ex);
            } finally {
                processIsActive = false;
            }
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

        // Remove Happtick arguments
        argsParser.removeParameterAll("--applicationId");
        argsParser.removeParameterAll("--startId");
        argsParser.removeParameterAll("--applicationPath");
        argsParser.removeParameterAll("--serverAddress");
        argsParser.removeParameterAll("--serverPort");

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
        new ExternalApplicationStarter(Util.parseLong(applicationId, 0), startId, applicationPath, serverAddress, Util.parseInt(serverPort, 0), arguments);
    }
}
