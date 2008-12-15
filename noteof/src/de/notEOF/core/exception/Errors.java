package de.notEOF.core.exception;

import java.util.Hashtable;
import java.util.Map;

/**
 * Class stores all errors which are used when throwing an
 * ActionFailedException. <br>
 * This is the first step to be prepared for international use of the framework.
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

        // Communication connection and transport problems
        errorList.put(10L, "Basiskommunikation konnte nicht initialisiert werden.");
        errorList.put(11L, "Timeout der Kommunikation konnte nicht veraendert werden.");
        errorList.put(12L, "Timeout der Kommunikation konnte nicht gelesen werden.");
        errorList.put(13L, "Wert des DataObject konnte nicht korrekt belegt werden.");
        errorList.put(14L, "Wert des DataObject konnte nicht korrekt gelesen werden.");
        errorList.put(15L, "Datei des DataObject existiert bereits im System und darf nicht überschrieben werden.");
        errorList.put(16L, "Mehrfacher Empfang eines leeren Zeichens. Inhaltlose Nachrichten sind unter !EOF nicht zul�ssig.");

        // Communication problems between client and server
        errorList.put(20L, "Request an Partner ohne Erfolg. Nicht erwartete Response.");
        errorList.put(21L, "Unerwartete Response von Partner eingetroffen.");
        errorList.put(22L, "Registrierung am Server ist fehlgeschlagen.");
        errorList.put(23L, "Lesen auf Socketebene fehlgeschlagen.");
        errorList.put(24L, "Timeout bei Lesen auf Socketebene.");
        errorList.put(25L, "Schreiben auf Socketebene fehlgeschlagen.");
        errorList.put(26L, "Timeout bei Schreiben auf Socketebene.");

        // Missunderstoods between Client and Service
        errorList.put(27L, "Client nutzt nicht unterstützte Service-Funktion.");

        // Configuration
        errorList.put(30L, "Konfigurationseintrag konnte nicht gespeichert werden.");
        errorList.put(31L, "Konfigurationseintrag konnte nicht hinzugefuegt werden.");
        errorList.put(32L, "Element konnte nicht ermittelt werden.");
        errorList.put(33L, "Konfigurationswert konnte nicht entschluesselt werden.");
        errorList.put(34L, "Konfigurationswert ist leer oder existiert nicht.");
        errorList.put(35L, "Sichtbarkeit des Konfigurationswertes konnte nicht ermittelt werden.");
        errorList.put(36L, "Fehler bei Hinzufuegen einer Konfigurationsdatei.");

        // Central !EOF server
        errorList.put(100L, "Initialisierung des zentralen Servers ist fehlgeschlagen.");

        // Services
        errorList.put(150L, "Klasse konnte nicht dynamisch geladen werden.");
        errorList.put(151L, "Service kennt empfangene Nachricht nicht.");
        errorList.put(152L, "Service Klasse wurde nicht gefunden. Service muss mittels CLASSPATH auffindbar sein.");
        errorList.put(153L, "Eine Bibliothek konnte nicht nach einer Klasse durchsucht werden. Evtl. stimmt eine Pfadeinstellung nicht.");

        // Converting problems
        errorList.put(200L, "Fehler bei Umwandlung eines Strings in eine Zahl.");
        errorList.put(201L, "Fehler bei Umwandlung einer Zahl in einen String.");

        // Special client/service messages up from 1000L
        // Dispatching
        errorList.put(1000L, "Dispatch Service steht auf angefragtem Server nicht zur Verfügung.");
        errorList.put(1001L, "Dispatch Service konnte gesuchten Service nicht finden.");
        errorList.put(1002L, "Dispatch Service konnte keinen freien Service finden (max. Anzahl Clients erreicht).");
        errorList.put(1003L, "Dispatch Client konnte nicht auf mitgeteilte IP-Adresse und/oder Port zugreifen.");
        errorList.put(1004L, "Dispatch Client konnte Socket auf IP und/oder Port nicht initialisieren.");
        errorList.put(1005L, "Dispatch Client konnte Client Klasse nicht auflösen.");
        errorList.put(1006L, "Dispatch Service konnte Konfiguration der Services nicht lesen.");
        errorList.put(1007L, "Dispatch Service konnte Konfiguration der Server nicht lesen.");

        // Mail and Events
        errorList.put(1090L, "Mehrfacher Versuch, Client zum Empfang von Mails und Events zu aktivieren.");

        // Mail functions
        errorList.put(1100L, "Unvollständige Mail.");
        errorList.put(1101L, "Problem bei Zuordnung einer Mail zu einem Service.");
        errorList.put(1102L, "Problem bei Zuordnung erwarteter Mails an Client.");

        // Events
        errorList.put(1150L, "Unbekannter Schluessel bei Hinzufuegen eines Wertes. Schluessel: ");
        errorList.put(1151L, "Fehler bei Transport eines Events.");
        errorList.put(1152L, "Event konnte nicht dynamisch geladen werden.");
        errorList.put(1153L, "Event Klasse wurde nicht gefunden. Event muss mittels CLASSPATH auffindbar sein.");
        errorList.put(1154L, "Event Verarbeitung und Versand an Client nicht moeglich.");
        errorList.put(1155L, "Unbekannter Schluessel bei Lesen eines Wertes. Schluessel: ");

    
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
