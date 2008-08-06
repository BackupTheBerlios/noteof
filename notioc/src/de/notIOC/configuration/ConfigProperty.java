package de.notIOC.configuration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

import de.notIOC.exception.NotIOCException;
import de.notIOC.logging.LocalLog;
import de.notIOC.util.Krypto;
import de.notIOC.util.Util;

/**
 * Hilfsklasse zum Auslesen und Schreiben von Werten. <br>
 * Soll vor allem den Zugriff auf optionale Werte vereinfachen. <br>
 * Wird zum Schreiben von Properties der ICCS-Konfiguration genutzt. <br>
 * Bedient sich des {@link ConfigManagers}.
 * <p>
 * Achtung! ConfigProperty unterstuetzt keine XPath-Ausdruecke! <br>
 * Zum Lesen von XPath-Ausdruecken kann der ConfigManager verwendet werden. Die
 * Werte koennen dann aber nicht zurueckgeschrieben werden.
 * 
 * @see ConfigManager
 */
public class ConfigProperty {

    private static CompositeConfigurationNotIOC propConf = null;

    private String answerType = null;
    private String attributeTag; // = "value";
    private String dependency_tag = "";
    private String dependency_value = "";
    private String visible_value = "";
    private String value = "";
    private String nodeNameConf = "";
    private String nodeNameTemplate = "";
    private String description = "";
    private String defaultValue = "";
    private List<String> valueList = null;
    private boolean valueUpdated = false;
    private boolean tagUpdated = false;
    private boolean abortedByUser = false;
    private int optionCount = -1;

    /**
     * Zur Konstruktion der Klasse werden die beiden Pfade fuer Konfiguration
     * und Template benoetigt.
     * <p>
     * 
     * @param confSection
     *            Enum-Klasse, die die 'Konstanten' fuer Konfigurations- und
     *            Template-Pfad kennt. <br>
     *            Dazu muss ein ConfigSection mit dem korrekten Pfad uebergeben
     *            werden.
     */
    public ConfigProperty(ConfigSection confSection) {
        this.nodeNameConf = confSection.getName();
        this.nodeNameTemplate = ConfigSection.TEMPLATE.getName() + "." + confSection.getName() + ".";

        initPropConf();
    }

    /**
     * Zur Konstruktion der Klasse werden die beiden Pfade fuer Konfiguration
     * und Template benoetigt.
     * <p>
     * 
     * @param nodeName
     *            Der Pfad fuer die Werte, ohne den uebergeordneten Pfad. D.h.
     *            'iccs_configuration' und 'iccs_template' werden automatisch
     *            eingefuegt.
     */
    public ConfigProperty(String nodeName) {
        this.nodeNameConf = nodeName;
        this.nodeNameTemplate = ConfigSection.TEMPLATE.getName() + "." + nodeNameConf + ".";

        initPropConf();
    }

    public ConfigProperty(String nodeName, String defaultValue) {
        this.defaultValue = defaultValue;

        this.nodeNameConf = nodeName;
        this.nodeNameTemplate = ConfigSection.TEMPLATE.getName() + "." + nodeNameConf + ".";

        initPropConf();
    }

    /*
     * Initialisierung der Konfigurationswerte
     */
    private void initPropConf() {
        try {
            propConf = ConfigurationManager.getInstance().getConfiguration();
        } catch (ConfigurationException cex) {
            // } catch (Exception cex) {
            LocalLog.error("Gesamtkonfiguration konnte nicht geladen werden.", cex);
            //LocalLog.error("Gesamtkonfiguration konnte nicht geladen werden.")
            // ;
        }
    }

    /**
     * Liefert die Anzahl verfuegbarer Optionen.
     * 
     * @return Die Anzahl.
     */
    @SuppressWarnings("unchecked")
    public int countOptions() {
        if (-1 == optionCount) {
            // Anzahl Optionen
            try {
                Object obj = propConf.getProperty(nodeNameTemplate + "options.option.index");
                if (obj != null)
                    optionCount = ((Collection) obj).size();
            } catch (Exception ex) {
                LocalLog.error("Error in initPropConf(): " + nodeNameTemplate, ex);
            }
        }
        return optionCount;
    }

    /**
     * Delivers the decrypted value of the property
     */
    public String getDecryptedValue() throws NotIOCException {
        if (Util.isEmpty(value)) {
            value = propConf.getString(getTag());
            if (Util.isEmpty(value)) {
                throw new NotIOCException(13L, nodeNameConf);
            }
        }

        Krypto cryptoClass = Krypto.getInstance();
        try {
            return cryptoClass.decrypt(value);
        } catch (Exception ex) {
            throw new NotIOCException(13L, nodeNameConf);
        }
    }

    /**
     * Normally the values are read only one time out of the configuration. <br>
     * This method enables to reread the property.
     */
    public void clearValue() {
        value = null;
    }

    /**
     * Liefert den Wert zum Property.
     * <p>
     * Wurde der Wert verschluesselt, liefert diese Methode den Klartext. <br>
     * Ist nicht der Klartext, sondern der verschluesselte Wert erwuenscht, muss
     * die Methode getEncryptedValue() verwendet werden.
     * 
     * @return Der Wert
     */
    public String getValue() throws NotIOCException {
        if (Util.isEmpty(value)) {
            value = propConf.getString(getTag());
            if (Util.isEmpty(value)) {
                throw new NotIOCException(14L, nodeNameConf);
            }
        }
        if (isCrypted()) {
            Krypto cryptoClass = Krypto.getInstance();
            try {
                return cryptoClass.decrypt(value);
            } catch (Exception ex) {
                return "";
            }
        }
        return value;
    }

    public String getStringValue() throws NotIOCException {
        return getValue();

    }

    public String getStringValue(String def) {
        try {
            return getStringValue();
        } catch (NotIOCException ex) {
            return def;
        }
    }

    /**
     * Liefert den Wert zum Property.
     * <p>
     * Der Wert darf nicht verschluesselt vorliegen. Dazu muss die Methode
     * getValue() verwendet werden.
     * 
     * @return Wert des Properties
     */
    public int getIntValue() throws NotIOCException {
        try {
            return Integer.parseInt(getValue());
        } catch (Exception ex) {
            throw new NotIOCException(14L, nodeNameConf);
        }
    }

    /**
     * Returns the int value of the property
     * 
     * @param defaultValue
     *            Default value if no value has been retrieved while reading the
     *            configuration.
     * @return An int value
     */
    public int getIntValue(int defaultValue) {
        try {
            return getIntValue();
        } catch (NotIOCException ex) {
            return defaultValue;
        }
    }

    /**
     * Liefert den Wert zum Property.
     * <p>
     * Der Wert darf nicht verschluesselt vorliegen. Dazu muss die Methode
     * getValue() verwendet werden.
     * 
     * @return Wert des Properties
     */
    public boolean getBooleanValue() throws NotIOCException {
        String bValue = getValue(); // wirft die richtige NotIOCException
        try {
            bValue = bValue.replaceAll("\"", "");
            return Boolean.parseBoolean(bValue);
        } catch (Exception ex) {
            throw new NotIOCException(14L, nodeNameConf);
        }
    }

    public boolean getBooleanValue(boolean def) {
        try {
            return getBooleanValue();
        } catch (NotIOCException ex) {
            return def;
        }
    }

    /**
     * Liefert den Wert des Property.
     * <p>
     * Wenn der Wert verschluesselt ist, wird der kryptisierte Wert
     * zurueckgeliefert, handelt es sich um einen nicht verschluesselten Wert,
     * wird dieser im Klartext zurueckgegeben.
     * 
     * @return Verschluesseltes Datum oder Klartext.
     */
    public String getEncryptedValue() {
        return value;
    }

    /**
     * Delivers the value of the property or - if it is encrypted - asterisks
     * which cover the really content.
     * 
     * @return The value or asterisks
     */
    public String getValueOrAsteriskIfCrypted() {
        String returnValue = "";
        try {
            returnValue = getValue();
        } catch (NotIOCException ex) {
        }
        if (isCrypted()) {
            if (!Util.isEmpty(returnValue)) {
                return Krypto.ASTERISKS;
            }
        }
        return returnValue;
    }

    /**
     * Liefert den Wert des Properties. Wenn null, dann wird der Default-Wert
     * geliefert.
     * 
     * @param forceValue
     *            'true' ermoeglicht alternativ die Rueckgabe des
     *            Default-Wertes, wenn kein Wert in der Konfiguration vorliegt.
     *            'false' bedeutet, dass nur der konfigurierte Wert
     *            zurueckgeliefert werden soll.
     * @return Der konfigurierte Wert oder der Default oder null oder was
     */
    public String getValue(boolean forceValue) throws NotIOCException {
        if (!forceValue)
            return getValue();

        String localValue = null;
        try {
            localValue = getValue();
        } catch (NotIOCException ex) {
            // It is really good possible that an exception is thrown
            // Than the function resume it's work with the next lines
        }

        if (null == localValue || "".equals(localValue))
            localValue = getDefault();

        return localValue;
    }

    public String getListItemValue(String itemIdentifier) {
        initList();
        if (null != valueList && valueList.size() > 0) {
            int index = 0;
            String cuttedNodeName = nodeNameConf.substring(0, nodeNameConf.indexOf("[@"));
            Iterator<String> it = valueList.iterator();
            while (it.hasNext()) {
                String nextItem = (String) it.next();
                if (nextItem.equals(itemIdentifier)) {
                    return propConf.getString(cuttedNodeName + "(" + index + ")");
                }
                index++;
            }
        }
        return null;
    }

    private void initList() {
        // 1. Variante
        if (null == valueList) {
            try {
                @SuppressWarnings("unchecked")
                List<String> dummyList = (List<String>) propConf.getList(nodeNameConf);
                if (dummyList.size() > 0) {
                    this.valueList = dummyList;
                }
            } catch (Throwable th) {
                LocalLog.error("Error in initPropConf(): " + nodeNameTemplate, th);
            }
        }
    }

    /**
     * Liefert eine Liste, wenn das Property aus mehreren Werten besteht.
     * <p>
     * Hier handelt es sich um eine ICCS-konfigurationsspezifische Liste, mit
     * der Annahme, <br>
     * dass die Items durch list gekennzeichnet sind. <br>
     * Beispiel: <br>
     * <addOn list="de.iccs.core.conf.ConfigManager -d -v"/> <br>
     * <addOn list="de.iccs.core.conf.ConfigManager -d"/>
     * 
     * @return Eine Liste mit Werten
     */
    public List<String> getList() {
        return getList("list");
    }

    /**
     * Liefert eine Liste mit mehreren Werten zu einem Schluessel.
     * <p>
     * 
     * @param listIdentifier
     *            Kennzeichnet die Liste.
     * @return Eine Liste mit Werten
     */
    @SuppressWarnings("unchecked")
    public List<String> getList(String listIdentifier) {
        initList();
        // 2. Variante
        if (null == valueList) {
            valueList = (List<String>) propConf.getList(nodeNameConf + ".[@" + listIdentifier + "]");
        }
        return valueList;
    }

    public void add(String xmlFileName) throws NotIOCException {
        propConf.addProperty(getTag(), getValue(true), xmlFileName);
    }

    /**
     * Zurueckschreiben des Properties in die Konfigurationsdatei.
     */
    public void save() throws NotIOCException {
        if (valueUpdated) {
            try {
                if (!isCrypted())
                    propConf.setProperty(getTag(), getValue());
                if (isCrypted())
                    propConf.setProperty(getTag(), getEncryptedValue());
            } catch (NotIOCException afx) {
                if (14L == afx.getErrNo()) {
                    // OK - wenn kein Wert, gibt's auch nix zu speichern
                } else {
                    throw afx;
                }
            }
        }
        valueUpdated = false;
    }

    /**
     * Aendern des Wertes im Speicher mit optionalem Zurueckschreiben in die
     * Konfigurationsdatei.
     * 
     * @param newValue
     *            Der aktualisierte Wert.
     * @param storeImmediately
     *            'true' Erzwingt das Schreiben in die Konfigurationsdatei.
     */
    public void setValue(String newValue, boolean storeImmediately) throws NotIOCException {
        setValue(newValue);
        if (storeImmediately) {
            save();
        }
    }

    public void setValue(String newValue, String xmlFileName) throws NotIOCException {
        setValue(newValue);
        if (!isCrypted())
            propConf.setProperty(getTag(), getValue(), xmlFileName);
        if (isCrypted())
            propConf.setProperty(getTag(), getEncryptedValue(), xmlFileName);
    }

    /**
     * Aendern des Wertes im Speicher mit optionalem Zurueckschreiben in die
     * Konfigurationsdatei.
     * 
     * @param newValue
     *            Der aktualisierte Wert.
     */
    public void setValue(String newValue) {
        if (null == this.value || !this.value.equals(newValue)) {
            valueUpdated = true;
            if (isCrypted()) {
                Krypto cryptoClass = Krypto.getInstance();
                this.value = cryptoClass.encrypt(newValue);
            } else
                this.value = newValue;
        }
    }

    /**
     * Fuer den Komfort der Benutzerschnittstelle muss bekannt sein, welcher Typ
     * von Frage und Antwort vorliegen. Z.B. kann ein Wert oder Entscheidung
     * (Ja/Nein) abgefragt werden. <br>
     * Der AnswerType wird benutzt, um die Abfragen entsprechend zu gestalten.
     * 
     * @return Kann sein 'choice' (Eine Zahl von bis), 'input' (konkreter Wert),
     *         'secureInput' (verschluesselter Wert), 'boolean' (Ja/Nein)
     */
    public String getAnswerType() {
        if (null == answerType || "".equals(answerType)) {
            answerType = propConf.getString(nodeNameTemplate + "type");
        }
        return answerType;
    }

    /**
     * Ein Eintrag kann abhaengig von einem anderen Eintrag sein. <br>
     * Wird vom yanic genutzt, um Konfigurationswerte nur dann abzufragen, wenn
     * sie an anderer Stelle <br>
     * vorgeschrieben sind
     */
    public String getDependencyTag() {
        if (null == dependency_tag || "".equals(dependency_tag)) {
            dependency_tag = propConf.getString(nodeNameTemplate + "dependency_tag");
        }
        return dependency_tag;
    }

    public String getDependencyValue() {
        if (null == dependency_value || "".equals(dependency_value)) {
            dependency_value = propConf.getString(nodeNameTemplate + "dependency_value");
        }
        return dependency_value;
    }

    public boolean isVisible() throws NotIOCException {
        if (null == visible_value || "".equals(visible_value)) {
            try {
                visible_value = propConf.getString(nodeNameTemplate + "visible");
            } catch (Exception ex) {
                visible_value = "true";
            }
            if (null == visible_value)
                visible_value = "true";
        }
        String bValue = visible_value;
        try {
            bValue = bValue.replaceAll("\"", "");
            return Boolean.parseBoolean(bValue);
        } catch (Exception ex) {
            throw new NotIOCException(15L, "Template-Tag <visible>");
        }
    }

    private String getTag() {
        if (!tagUpdated) {
            String tag = propConf.getString(nodeNameTemplate + "tag");
            if (Util.isEmpty(tag)) {
                tag = nodeNameConf;
                int pos = tag.indexOf(":");
                if (pos > -1) {
                    String attributeSearchKey = tag.substring(pos + 1);
                    tag = tag.substring(0, pos);
                    for (int i = 0; i < 99; i++) {
                        String anyKey = propConf.getString(tag.replaceFirst("#", String.valueOf(i)));
                        if (anyKey.equals(attributeSearchKey)) {
                            tag = tag.replaceFirst("#", String.valueOf(i));
                            pos = tag.indexOf("[@");
                            tag = tag.substring(0, pos);
                            return tag;
                        }
                    }
                }

                attributeTag = nodeNameConf;
            } else {
                /*
                 * Der Tag kann ein Verweis auf einen anderen XPath- oder
                 * Configuration-Ausdruck sein. Dadurch lassen sich die
                 * Nodenamen 'verbiegen', d.h. in der iccs_template kann
                 * explizit ein beliebiger Nodename angegeben werden.
                 */
                attributeTag = tag;

                /*
                 * Bsp: session-factory.property($)[@name]:dialect Bei Listen
                 * (wie in diesem Fall) es gilt, den Index zu ermitteln... vor
                 * dem Doppelpunkt - Zugriff auf die namen der properties nach
                 * Doppelpunkt - Das Attribute das gesucht wird <br> jetzt kann
                 * der Wert ausgelesen werden mit session-factory.property(i)
                 */
                int pos = tag.indexOf(":");
                if (pos > -1) {
                    String attributeSearchKey = tag.substring(pos + 1);
                    tag = tag.substring(0, pos);
                    for (int i = 0; i < 99; i++) {
                        String anyKey = propConf.getString(tag.replaceFirst("#", String.valueOf(i)));
                        if (anyKey.equals(attributeSearchKey)) {
                            tag = tag.replaceFirst("#", String.valueOf(i));
                            pos = tag.indexOf("[@");
                            tag = tag.substring(0, pos);
                            return tag;
                        }
                    }
                }
            }
        }
        tagUpdated = true;
        return attributeTag;
    }

    /**
     * Liefert den Default-Wert aus dem Template.
     * 
     * @return Ein Standardwert.
     */
    public String getDefault() {
        if (null == defaultValue || "".equals(defaultValue)) {
            defaultValue = propConf.getString(nodeNameTemplate + "default");
        }
        return defaultValue;
    }

    /**
     * Liefert eine generelle Beschreibung zum Konfigurationsabschnitt.
     * 
     * @return Ein beschreibender Text. Wird z.B. vom yanic fuer die
     *         Konsolen-Ausgaben verwendet.
     */
    public String getDescription() {
        if (null == description || "".equals(description)) {
            description = propConf.getString(nodeNameTemplate + "description");
        }
        return description;
    }

    /**
     * Sagt aus, ob der konfigurierte Wert in verschluesselter Form vorliegt.
     * 
     * @return Je nachdem...
     */
    public boolean isCrypted() {
        try {
            if (getAnswerType().equalsIgnoreCase("secureInput")) {
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * Liefert den Index zu einer Option anhand des Schluessels.
     * 
     * @param optionKey
     *            Der Schluessel, ueber den gesucht wird.
     * @return Der Index. Wird fuer den Zugriff auf andere Werte der Option
     *         verwendet.
     * @throws NotIOCException
     */
    public int getOptionIndex(String optionKey) throws NotIOCException {
        try {
            for (int i = 0; i < optionCount; i++) {
                if (optionKey.equals(getOption(optionKey, i))) {
                    return i;
                }
            }
        } catch (Throwable th) {
            LocalLog.error("Fehler bei Zugriff auf Schl�ssel einer Option: " + optionKey, th);
            throw new NotIOCException(16L, "Schl�ssel: " + optionKey);
        }
        return -1;
    }

    /**
     * Liefert einen optionalen Wert. Das kann z.B. die Beschreibung, oder auch
     * ein moeglicher konfigurierbarer Wert sein.
     * 
     * @param optionKey
     *            Name des Keys (z.B. description oder index)
     * @param optionIndex
     *            Der index, beginnend mit 0
     * @return Der optionale Wert
     * @throws NotIOCException
     */
    public String getOption(String optionKey, int optionIndex) throws NotIOCException {
        try {
            String key = nodeNameTemplate + "options.option(" + String.valueOf(optionIndex).trim() + ")." + optionKey;
            return (String) propConf.getProperty(key);
        } catch (Exception th) {
            LocalLog.error("Fehler bei Zugriff auf eine Option: " + optionKey + " Index = " + optionIndex, th);
            throw new NotIOCException(16L, "Key: " + optionKey + ", Index: " + optionIndex);
        }
    }

    /**
     * Liefert ein Array mit den Beschreibungen der Optionen
     * 
     * @return Das Array mit den Beschreibungen
     * @throws NotIOCException
     */
    public String[] getOptionDescriptionArray() throws NotIOCException {
        String descriptions[] = new String[countOptions()];
        for (int i = 0; i < countOptions(); i++) {
            descriptions[i] = getOptionDescription(i);
        }
        return descriptions;
    }

    /**
     * Liefert die Beschreibung zu einer Option.
     * 
     * @param optionIndex
     *            Der Index der Option.
     * @return Die Beschreibung
     * @throws NotIOCException
     */
    public String getOptionDescription(int optionIndex) throws NotIOCException {
        return getOption("description", optionIndex);
    }

    public String getOptionDependencyValue(int optionIndex) throws NotIOCException {
        return getOption("dependency_value", optionIndex);
    }

    public String getOptionDependencyTag(int optionIndex) throws NotIOCException {
        return getOption("dependency_tag", optionIndex);
    }

    /**
     * Liefert den Index (den Namen) einer Option. <br>
     * Index, weil dieser Bezeichner als Referenz in der Konfiguration verwendet
     * wird.
     * 
     * @param optionIndex
     *            Der Name bzw. der Index der Option.
     * @return Der Name der Option.
     * @throws NotIOCException
     */
    public String getOptionIndex(int optionIndex) throws NotIOCException {
        return getOption("index", optionIndex);
    }

    public String getNodeName() {
        return nodeNameConf;
    }

    public String toString() {
        String ret = this.getClass().getName();
        ret += "; answerType = " + answerType;
        ret += "; attributeTag = " + attributeTag;
        ret += "; dependency_tag = " + dependency_tag;
        ret += "; dependency_value = " + dependency_value;
        ret += "; visible_value = " + visible_value;
        ret += "; value = " + value;
        ret += "; nodeNameConf = " + nodeNameConf;
        ret += "; nodeNameTemplate = " + nodeNameTemplate;
        ret += "; description = " + description;
        ret += "; defaultValue = " + defaultValue;
        ret += "; valueUpdated = " + valueUpdated;
        ret += "; tagUpdated = " + tagUpdated;
        ret += "; optionCount = " + optionCount;

        if (!Util.isEmpty(valueList)) {
            for (String listValue : valueList) {
                ret += "; valueList.item = " + listValue;
            }
        }

        return ret;
    }

    public boolean isAbortedByUser() {
        return abortedByUser;
    }

    public void setAbortedByUser(boolean abortedByUser) {
        this.abortedByUser = abortedByUser;
    }
}