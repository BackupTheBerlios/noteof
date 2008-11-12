package de.happtick.core.start.client;

import de.happtick.application.client.HapptickApplication;
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

    public ExternalApplicationStarter(long applicationId, String applicationPath, String serverAddress, int serverPort, String applArgs)
            throws HapptickException {
        super(applicationId, serverAddress, serverPort, applArgs);
        ExternalCalls.startApplication(applicationPath, applArgs);

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
        ArgsParser argsParser = new ArgsParser(args);
        String applicationId = argsParser.getValue("applicationId");
        String startId = argsParser.getValue("startId");
        String applicationPath = argsParser.getValue("applicationPath");
        String serverAddress = argsParser.getValue("serverAddress");
        String serverPort = argsParser.getValue("serverPort");
        String applArgs = argsParser.getValue("arguments");

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

        new ExternalApplicationStarter(Util.parseLong(applicationId, 0), applicationPath, serverAddress, Util.parseInt(serverPort, 0), applArgs);
    }
}
