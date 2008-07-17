package de.notEOF.core.configuration;

/**
 * Klasse zur Vereinheitlichung und eindeutigen Zuordnung von 'Konstanten'.
 * <p>
 * Zur Unterscheidung zwischen Vorlage und Konfiguration dienen die Typen
 * 'Template' und 'Configuration'. <br>
 * Die �brigen Typen werden fuer den Zugriff auf die Sektionen verwendet.
 */
public enum ConfigSection {

    // Unterscheidung auf 'oberster' Ebene, d.h., Konfiguration oder Vorlage
    /**
     * Template und Configuration dienen der Unterscheidung, ob ein Wert aus der
     * Konfiguration oder aus der Vorlage gelesen werden soll.
     */
    TEMPLATE("iccs_template"),
    /**
     * Template und Configuration dienen der Unterscheidung, ob ein Wert aus der
     * Konfiguration oder aus der Vorlage gelesen werden soll.
     */
    // CONFIGURATION("iccs_configuration"),
    /**
     * Zentrale Konfiguration
     * <p>
     * Zugriff auf Liste der XML-Dateien und <br>
     * Zugriff auf Liste der Sectionsnamen.
     * <p>
     * Achtung - Eintraege dieser Liste korrespondieren mit Liste aus
     * XMLFileSections.
     */
    XML_FILE_NAMES("xml.[@fileName]"),
    /**
     * Zentrale Konfiguration
     * <p>
     * Zugriff auf Liste der Sectionsnamen.
     * <p>
     * Achtung - Eintraege dieser Liste korrespondieren mit Liste aus
     * XMLFileNames.
     */
    // XML_FILE_SECTIONS("section.[@sectionName]"),
    /**
     * Alle konfigurierten Parameter
     */
    ICCS_SECTIONS("iccsSections"),
    INDEX("index"),
    DESCRIPTION("description"),

    /**
     * Liste der AddOns
     */
    ADDON_LIST("addOns.addOn");

    // Hierum geht's eigentlich
    private String enumName;

    private ConfigSection(String enumName) {
        this.enumName = enumName;
    }

    /**
     * @return Name der Instanz. Kann benutzt werden, wie eine Konstante.
     */
    public String getName() {
        return enumName;
    }

}