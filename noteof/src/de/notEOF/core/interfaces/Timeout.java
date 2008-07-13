package de.notEOF.core.interfaces;

/**
 * Timeouts are needed for communication. <br>
 * All clients have to use timeouts for communication and connection but there
 * can be variants in more timeouts.
 * 
 * @author Dirk
 */
public interface Timeout {

    public int millisConnection = 3000;
    public int millisCommunication = 0;

    /**
     * Timeout for establishing a connection to the server/service.
     * 
     * @param millisConnection
     *            Max. time in milliseconds which may exceed till the connection
     *            is build.
     */
    public void setMillisConnection(int millisConnection);

    /**
     * Timeout for establishing a connection to the server/service.
     * 
     * @param millisConnection
     *            Max. time in milliseconds which may exceed till the connection
     *            is build.
     */
    public int getMillisConnection();

    /**
     * Timeout for sending or receiving messages.
     * 
     * @param millisConnection
     *            Max. time in milliseconds which may exceed by sending or
     *            receiving a message. is build.
     */
    public void setMillisCommunication(int millisCommunication);

    /**
     * Timeout for sending or receiving messages.
     * 
     * @param millisConnection
     *            Max. time in milliseconds which may exceed by sending or
     *            receiving a message. is build.
     */
    public int getMillisCommunication();
}
