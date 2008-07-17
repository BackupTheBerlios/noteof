package de.notEOF.core.constants;

/**
 * Global constant definitions which are used from miscellaneous core classes of
 * this project.
 * 
 * @author Dirk
 * 
 */
public class NotEOFConstants {

    /**
     * Intervall in milliseconds which is used by BaseService and BaseClient for
     * max. time which may elapse between two life signs. <br>
     * Also used as max. time for reading on socket. Within this time the client
     * must send any message (lifetime messages is accepted too) or the service
     * assumes that the client is dead. <br>
     * The client interval must be a little shorter to avoid communication
     * problems.
     **/
    public static int LIFE_TIME_INTERVAL_SERVICE = 20000;
    public static int LIFE_TIME_INTERVAL_CLIENT = 10000;

}
