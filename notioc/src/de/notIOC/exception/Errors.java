package de.notIOC.exception;

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

        // Basic problemes with configuration
        errorList.put(5L, "Konfigurationsdatei konnte nicht verarbeitet werden.");
        errorList.put(6L, "Unzulässige mehrfache Initialisierung der zentralen Konfigurationsdatei.");

        // Configuration
        errorList.put(10L, "Konfigurationseintrag konnte nicht gespeichert werden.");
        errorList.put(11L, "Konfigurationseintrag konnte nicht hinzugefügt werden.");
        errorList.put(12L, "Element konnte nicht ermittelt werden.");
        errorList.put(13L, "Konfigurationswert konnte nicht entschlüsselt werden.");
        errorList.put(14L, "Konfigurationswert ist leer oder existiert nicht.");
        errorList.put(15L, "Sichtbarkeit des Konfigurationswertes konnte nicht ermittelt werden.");
        errorList.put(16L, "Option zu Schlüssel wurde nicht gefunden.");
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
