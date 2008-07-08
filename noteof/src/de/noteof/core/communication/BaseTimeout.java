package de.noteof.core.communication;

import de.noteof.core.interfaces.Timeout;

/**
 * Class to deliver timeOuts for establishing a connection and for the read
 * write actions.
 * 
 * @author Dirk
 */
public class BaseTimeout implements Timeout {

    private int millisConnection = 3000;
    private int millisCommunication = 0;

    /**
     * Construction without intial values is allowed. <br>
     * In this case default values are available.
     */
    public BaseTimeout() {

    }

    /**
     * Constructor which overwrites the default values.
     * 
     * @param millisConnection
     *            Timeout for the max. time to establish the connection to the
     *            server/service.
     * @param millisCommunication
     *            Timeout for read and write actions.
     */
    public BaseTimeout(int millisConnection, int millisCommunication) {
        this.setMillisCommunication(millisCommunication);
        this.setMillisConnection(millisConnection);
    }

    public void setMillisConnection(int millisConnection) {
        this.millisConnection = millisConnection;
    }

    public int getMillisConnection() {
        return millisConnection;
    }

    public void setMillisCommunication(int millisCommunication) {
        this.millisCommunication = millisCommunication;
    }

    public int getMillisCommunication() {
        return millisCommunication;
    }

}
