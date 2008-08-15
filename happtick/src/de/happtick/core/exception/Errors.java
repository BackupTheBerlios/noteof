package de.happtick.core.exception;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class stores all errors which are used when throwing an
 * HapptickException. <br>
 * 
 * @author Dirk
 * 
 */
public class Errors {

    protected static Map<Long, String> errorList;
    protected static Errors errors;

    /**
     * Well defined keys deliver well formed statements
     * 
     * @param errNo
     *            Key for the message text
     * @return Text assigned to the key
     */
    public static String getMsg(long errNo) {
        initializeErrorList();

        String msg = errorList.get(errNo);
        if (null == msg)
            msg = errorList.get(0L);
        return msg;
    }

    /*
     * Sometimes the list must be filled up
     */
    private static void initializeErrorList() {
        errorList = new Hashtable<Long, String>();

        // The list elements
        errorList.put(0L, "Nicht erwarteter Fehler.");
        errorList.put(1L, "Nicht erwarteter Fehler.");

        // Initializing the happtick client
        errorList.put(50L, "Unvollständige Initialisierung des Happtick Client.");
        
        // Establishing the connection to service
        errorList.put(100L, "Verbindungsaufbau zum Service fehlgeschlagen.");
        
        // Lifecycle faults
        errorList.put(200l, "Beenden der Verbindung mit Service ist fehlgeschlagen.");
        errorList.put(201L, "Anfrage der Start-Erlaubnis beim Service ist fehlgeschlagen.");
        errorList.put(202L, "Senden einer Fehler-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(203L, "Senden einer Event-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(204L, "Senden einer Alarm-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(205L, "Senden einer Log-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(206L, "Senden der Client-ID an den Service ist fehlgeschlagen.");
    }

    /*
     * private because is a Singleton
     */
    private Errors() {
        initializeErrorList();
    }

    /**
     * Errors is a Singleton...
     */
    public static Errors getInstance() {
        if (errors == null) {
            errors = new Errors();
        }
        return errors;
    }
}
