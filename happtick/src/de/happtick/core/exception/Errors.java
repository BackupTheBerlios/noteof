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
        errorList.put(50L, "Unvollstaendige Initialisierung des Happtick Client.");

        // Initializing the happtick service
        errorList.put(60L, "Unvollstaendige Initialisierung des Happtick Service.");

        // Establishing the connection to service
        errorList.put(100L, "Fehler bei Verbindungsaufbau zum Service.");

        // Client lifecycle faults
        errorList.put(200L, "Fehler bei Beenden der Verbindung mit Service.");
        errorList.put(201L, "Fehler bei Anfrage der Start-Erlaubnis beim Service.");
        errorList.put(202L, "Fehelr bei Senden eines Events an den Service.");
        errorList.put(206L, "Fehler bei Senden der Client-ID an den Service.");
        errorList.put(207L, "Fehler bei Senden der Beendigung des Clients an den Service.");

        // Service lifecycle faults
        errorList.put(300L, "");

        // MasterTable errors / Configuration
        errorList.put(400L, "Fehler bei Hinzufügen eines Service. Falscher Service-Typ.");
        errorList.put(401L, "Fehler bei Lesen der Client-Konfiguration.");
        errorList.put(402L, "Fehler bei Entfernen eines Service.");

        // Scheduler faults
        errorList.put(500L, "Fehler bei Ermitteln des Startservice zur Anwendung.");

        // Mail and Event errors
        errorList.put(600L, "Fehler bei Senden einer Mail.");
        errorList.put(601L, "Fehler bei Empfangen einer Mail oder eines Events.");
        errorList.put(602L, "Fehler bei Festlegen der Begriffe fuer akzeptierte Mails.");
        errorList.put(603L, "Fehler bei Festlegen der Klassen fuer akzeptierte Events.");
        errorList.put(604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");
        errorList.put(605L, "Fehler bei einer vorbereitenden Massnahme fuer den Empfang von Mails.");

        // Start Client
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
