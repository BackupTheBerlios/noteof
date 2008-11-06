package de.happtick.core.start.client;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.exception.HapptickException;

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

    public ExternalApplicationStarter(long applicationId, String serverAddress, int serverPort, String[] args) throws HapptickException {
        super(applicationId, serverAddress, serverPort, args);
        connect();
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
     *            <li>--applicationPath</><br>
     *            <li>--serverAddress</> <br>
     *            <li>--serverPort</> <li> --arguments</><br>
     *            </ul>
     */
    public static void main(String[] args) {

    }
}
