package de.happtick.core.start.client;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.exception.HapptickException;

public class ExternalApplicationStarter extends HapptickApplication {

    public ExternalApplicationStarter(long applicationId, String serverAddress, int serverPort, String[] args) throws HapptickException {
        super(applicationId, serverAddress, serverPort, args);
        // TODO Auto-generated constructor stub
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
