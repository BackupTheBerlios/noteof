package de.happtick.core.exception;

import java.util.Hashtable;
import java.util.Map;

/**
 * Class stores all errors which are used when throwing an HapptickException. <br>
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

        // Initializing the happtick service
        errorList.put(60L, "Unvollständige Initialisierung des Happtick Service.");

        // Establishing the connection to service
        errorList.put(100L, "Verbindungsaufbau zum Service fehlgeschlagen.");

        // Client lifecycle faults
        errorList.put(200L, "Beenden der Verbindung mit Service ist fehlgeschlagen.");
        errorList.put(201L, "Anfrage der Start-Erlaubnis beim Service ist fehlgeschlagen.");
        errorList.put(202L, "Senden einer Fehler-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(203L, "Senden einer Event-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(204L, "Senden einer Alarm-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(205L, "Senden einer Log-Meldung an den Service ist fehlgeschlagen.");
        errorList.put(206L, "Senden der Client-ID an den Service ist fehlgeschlagen.");
        errorList.put(207L, "Senden der Beendigung des Clients an den Service ist fehlgeschlagen.");

        // Service lifecycle faults
        errorList.put(300L, "");

        // MasterTable errors / Configuration
        errorList.put(400L, "Fehler bei Hinzuf�gen eines Service. Falscher Service-Typ.");
        errorList.put(401L, "Fehler bei Lesen der Client-Konfiguration.");
        errorList.put(402L, "Fehler bei Entfernen eines Service.");

        // Scheduler faults
        errorList.put(500L, "Ermitteln des Startservice zur Anwendung ist fehlgeschlagen.");

        // Mail and Event errors
        errorList.put(600L, "Senden einer Mail ist fehlgeschlagen.");
        errorList.put(601L, "Empfangen einer Mail oder eines Events ist fehlgeschlagen.");
        errorList.put(602L, "Begriffe fuer akzeptierte mails konnten nicht festgelegt werden.");
        errorList.put(603L, "Klassen fuer akzeptierte events konnten nicht festgelegt werden.");
        errorList.put(604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");

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
