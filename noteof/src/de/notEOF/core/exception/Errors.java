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
        errorList.put(11L, "Timeout der Kommunikation konnte nicht verändert werden.");
        errorList.put(12L, "Timeout der Kommunikation konnte nicht gelesen werden.");

        // Communication problems between client and server
        errorList.put(20L, "Request an Partner ohne Erfolg. Nicht erwartete Response.");
        errorList.put(21L, "Unerwartete Response von Partner eingetroffen.");
        errorList.put(22L, "Registrierung am Server ist fehlgeschlagen.");

        // Configuration
        errorList.put(30L, "Konfigurationseintrag konnte nicht gespeichert werden.");
        errorList.put(31L, "Konfigurationseintrag konnte nicht hinzugefügt werden.");
        errorList.put(32L, "Element konnte nicht ermittelt werden.");
        errorList.put(33L, "Konfigurationswert konnte nicht entschlüsselt werden.");
        errorList.put(34L, "Konfigurationswert ist leer oder existiert nicht.");
        errorList.put(35L, "Sichtbarkeit des Konfigurationswertes konnte nicht ermittelt werden.");

        // Central !EOF server
        errorList.put(100L, "Initialisierung des zentralen Servers ist fehlgeschlagen.");

        // Services
        errorList.put(150L, "Service konnte nicht dynamisch geladen werden.");
        errorList.put(151L, "Service kennt empfangene Nachricht nicht.");

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
