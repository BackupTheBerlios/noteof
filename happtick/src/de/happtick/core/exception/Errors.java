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
        errorList.put(10050L, "Unvollstaendige Initialisierung des Happtick Client.");

        // Initializing the happtick service
        errorList.put(10060L, "Unvollstaendige Initialisierung des Happtick Service.");

        // Establishing the connection to service
        errorList.put(10000L, "Fehler bei Verbindungsaufbau zum Service.");

        // Client lifecycle faults
        errorList.put(10200L, "Fehler bei Beenden der Verbindung mit Service.");
        errorList.put(10201L, "Fehler bei Anfrage der Start-Erlaubnis beim Service.");
        errorList.put(10202L, "Fehler bei Senden eines Events an den Service.");
        errorList.put(10206L, "Fehler bei Senden der Client-ID an den Service.");
        errorList.put(10207L, "Fehler bei Senden der Beendigung des Clients an den Service.");
        errorList.put(10208L, "Fehler bei Verwendung des Clients. Die Anwendung hat noch keine Starterlaubnis.");

        // Service lifecycle faults
        errorList.put(10300L, "");

        // MasterTable errors / Configuration
        errorList.put(10400L, "Fehler bei Hinzuf�gen eines Service. Falscher Service-Typ.");
        errorList.put(10401L, "Fehler bei Lesen der Client-Konfiguration.");
        errorList.put(10402L, "Fehler bei Entfernen eines Service.");
        errorList.put(10403L, "Fehlerhafte Konfiguration eines Links (Aktion) einer Chain.");
        errorList.put(10404L, "Fehlerhafte oder fehlende Konfiguration einer Applikation.");
        errorList.put(10405L, "Konfigurationsobjekt konnte nicht per Id gefunden werden.");
        errorList.put(10406L, "Fehler bei Zugriff auf selbstdefiniertes Event per Alias-Name.");

        // Scheduler faults
        errorList.put(10500L, "Fehler bei Ermitteln des Startservice zur Anwendung.");
        errorList.put(10501L, "Fehler bei Zuordnung eines Events zu einer Aktion.");
        errorList.put(10502L, "Start einer Anwendung oder einer Chain innerhalb einer Chain ist fehlgeschlagen.");
        errorList.put(10503L, "Start einer Anwendung ist fehlgeschlagen.");
        errorList.put(10504L, "Stoppen einer Anwendung ist fehlgeschlagen.");

        // Mail and Event errors
        errorList.put(10600L, "Fehler bei Senden einer Mail.");
        errorList.put(10601L, "Fehler bei Empfangen einer Mail oder eines Events.");
        errorList.put(10602L, "Fehler bei Festlegen der Begriffe fuer akzeptierte Mails.");
        errorList.put(10603L, "Fehler bei Festlegen der Klassen fuer akzeptierte Events.");
        errorList.put(10604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");
        errorList.put(10605L, "Fehler bei einer vorbereitenden Massnahme fuer den Empfang von Mails.");

        // Start Client
        errorList.put(10650L, "Fehler bei Starten einer Anwendung. Fehlender Wert im Start-Event: ");
        errorList.put(10651L, "Fehler bei Starten einer Anwendung.");

        // Connection faults
        errorList.put(10700L, "Fehler bei Schliessen einer Verbindung.");
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
