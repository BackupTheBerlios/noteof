package de.noteof.core.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import de.noteof.core.exception.ActionFailedException;
import de.noteof.core.logging.LocalLog;
import de.noteof.core.util.Util;

public class CompositeConfigurationNotEof extends org.apache.commons.configuration.CompositeConfiguration {

    public void setProperty(String key, String value) throws ActionFailedException {
        setProperty(key, value, null);
    }

    public void setProperty(String key, String value, String xmlFileName) throws ActionFailedException {
        Configuration anyConfig = retrieveConfiguration(key);

        if (null == anyConfig) {
            anyConfig = searchConfigurationParent(key);
        }
        if (null != anyConfig) {
            anyConfig.setProperty(key, value);
            try {
                if (anyConfig instanceof XMLConfiguration) {
                    ((XMLConfiguration) anyConfig).save();
                } else if (anyConfig instanceof PropertiesConfiguration) {
                    ((PropertiesConfiguration) anyConfig).save();
                }
            } catch (Exception ex) {
                throw new ActionFailedException(30L, "Update des Wertes", ex);
            }
        } else if (!Util.isEmpty(xmlFileName)) {
            try {
                XMLConfiguration config = new XMLConfiguration(xmlFileName);
                config.setProperty(key, value);
                config.save();
                config.reload();

            } catch (Exception ex) {
                throw new ActionFailedException(30L, "Neu Setzen (kein Hinzufügen)", ex);
            }
        } else {
            try {
                String fileName = ConfigurationManager.getInstance().getDefaultConfigurationFile();
                addProperty(key, value, fileName);

                System.out.println("Achtung! Der Key " + key + " ist neu in Ihrer Konfiguration und wurde in die Datei " + fileName + " eingetragen.");
                LocalLog.info("Der Key " + key + " existierte bisher nicht in der Kundenkonfiguration und wurde in die Datei " + fileName + " eingetragen.");
            } catch (ConfigurationException ex) {
                throw new ActionFailedException(30L, "Neues Element hinzufügen", ex);
            }
        }
    }

    /**
     * Problem war das Schreiben neuer Werte ohne zusaetzliche Angabe der
     * jeweiligen Konfiguration(-sdatei).
     * 
     * @param key
     *            Frei aus dem dt.: Schluessel
     * @param value
     *            Kann hier eine komplexe Struktur sein
     */
    public void addProperty(String key, String value, String xmlFileName) throws ActionFailedException {
        if (Util.isEmpty(xmlFileName)) {
            throw new ActionFailedException(31L, "Fehlende Angabe: xmlFileName");
        }

        try {
            XMLConfiguration config = new XMLConfiguration(xmlFileName);
            config.addProperty(key, value);
            config.save();
            config.reload();
        } catch (Exception ex) {
            throw new ActionFailedException(31L, "Konfigurations-Datei ist " + xmlFileName, ex);
        }
    }

    private Configuration searchConfigurationParent(String key) throws ActionFailedException {
        Configuration anyConfig = null;
        int pos = 0;
        while (pos != -1 && (!"".equals(key)) && null == anyConfig) {
            pos = key.lastIndexOf(".");
            if (pos != -1) {
                key = key.substring(0, pos);
                anyConfig = retrieveConfiguration(key);
            }
        }
        // System.out.println("AnyConfig " + anyConfig);
        return anyConfig;
    }

    /**
     * Ermittelt den Wert eines XML-Knotens.
     * <p>
     * Dazu wird ein zweistufiges Verfahren angewandt: <br>
     * 1. Suche ueber commons.configuration (da die Konfiguration bereits im
     * Speicher ist) <br>
     * 2. Bei Nichterfolg Suche ueber SAX und XPath-Ausdruecke.
     * <p>
     * Die Suche bezieht sich auf alle in iccs_master_conf.xml hinterlegten
     * XML-Dateien.
     * 
     * @param nodeName
     *            Ist der 'Schluessel'. Zulaessig sind Ausdruecke wie z.B.
     *            /hibernate
     *            -configuration/session-factory/property[@name="dialect"]
     * @return Der gesuchte Wert oder null
     */
    public String getString(String nodeName) {
        String value = null;
        // 1. commons.configuration
        try {
            value = super.getString(nodeName);

            if (null == value) {
                // 2. xpath
                int i = 0;
                int maxCounter = 99;

                Configuration anyConfig = null;
                while ((null == value || "".equals(value)) && i < maxCounter) {
                    try {
                        anyConfig = super.getConfiguration(i++);
                    } catch (IndexOutOfBoundsException ex) {
                        // Es gibt definitiv keine weitere Konfiguration mehr
                        // Abbrechen der Suche
                        // System.out.println(
                        // "Die Konfiguration der iccs_master_conf.xml wurde wahrscheinlich nicht vollstï¿½ndig durchgefï¿½hrt.\n"
                        // +
                        // "Evtl. existieren nicht alle notwendigen Konfigurationsdateien:\n"
                        // +
                        // " - sw/conf/iccs_conf.xml\n - sw/conf/iccs_template.xml\n - sw/conf/iccs_master.xml\n"
                        // +
                        // " - sw/conf/extensions.xml\n - sw/classes/xwork.xml\n - sw/classes/hibernate.cfg.xml"
                        // );
                        // System.out.println("Gesuchter Parameter: " +
                        // nodeName);
                        // LOG.warn(
                        // "Die Konfiguration der iccs_master_conf.xml wurde wahrscheinlich nicht vollstï¿½ndig durchgefï¿½hrt.\n"
                        // +
                        // "Evtl. existieren nicht alle notwendigen Konfigurationsdateien:\n"
                        // +
                        // " - sw/conf/iccs_conf.xml\n - sw/conf/iccs_template.xml\n - sw/conf/iccs_master.xml\n"
                        // +
                        // " - sw/conf/extensions.xml\n - sw/classes/xwork.xml\n - sw/classes/hibernate.cfg.xml"
                        // );
                        // LOG.warn("Gesuchter Parameter: " + nodeName);
                        i = maxCounter;
                    }
                    // if (null == anyConfig || anyConfig instanceof
                    // BaseConfiguration) {}
                    if (null != anyConfig && anyConfig instanceof XMLConfiguration) {
                        try {
                            String fileName = ((XMLConfiguration) anyConfig).getFileName();
                            int pos = fileName.indexOf("file:");
                            if (pos > -1)
                                fileName = fileName.substring(pos + "file:".length());

                            SAXReader reader = new SAXReader();
                            Document document = reader.read(fileName);
                            value = document.selectSingleNode(nodeName).getStringValue();
                            // System.out.println("NodeName: " + nodeName +
                            // "; Value: " + value);
                        } catch (NullPointerException npEx) {
                        } catch (Exception ex) {
                            // ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return value;
    }

    /**
     * Liefert einen Iterator ueber einen XPath-Node.
     * <p>
     * Die vom Iterator zur Verfuegung gestellten Objekte muessen in der
     * Anwendung auf (org.dom4j.Node) gecastet werden. <br>
     * Die Suche bezieht sich auf alle in iccs_master_conf.xml hinterlegten
     * XML-Dateien.
     * 
     * @param nodeName
     *            Der Name des Nodes entsprechend XPath-Konvention.
     * @return Iterator mit n Nodes oder null
     */
    // public Iterator getNodeIterator(String nodeName) {
    // int i = 0;
    // Iterator nodeIterator = null;
    // Configuration anyConfig = null;
    // while (null == nodeIterator && null != (anyConfig =
    // super.getConfiguration(i++)) && //
    // (anyConfig instanceof XMLConfiguration)) {
    // try {
    // String fileName = ((XMLConfiguration)anyConfig).getFileName();
    // int pos = fileName.indexOf("file:");
    // if (pos > -1) fileName = fileName.substring(pos + "file:".length());
    //
    // SAXReader reader = new SAXReader();
    // Document document = reader.read(fileName);
    // nodeIterator = document.selectNodes(nodeName).iterator();
    // } catch (Exception ex) {
    // }
    // }
    // return nodeIterator;
    // }
    /**
     * Sucht ueber den Key die 'richtige' Konfiguration(-sdatei).
     * <p>
     * 
     * @param elementName
     *            Ist der Name, der den Parameter identifiziert.
     * @return null oder eine passende Konfiguration. Existiert derselbe
     *         Schluessel in zwei Konfigurationsdateien, wird die zuerst
     *         gefundene Configuration zurueckgeliefert.
     */
    public Configuration retrieveConfiguration(String elementName) throws ActionFailedException {
        Configuration anyConfig = null;
        int i = 0;
        try {
            while (null != (anyConfig = super.getConfiguration(i++))) {
                if (anyConfig.containsKey(elementName)) {
                    return anyConfig;
                }
            }
        } catch (IndexOutOfBoundsException iEx) { // ok - passiert
        } catch (Exception ex) {
            LocalLog.error("Element konnte nicht ermittelt werden: " + elementName, ex);
            throw new ActionFailedException(32L, ": " + elementName, ex);
        }
        return null;
    }

    /**
     * Sucht eine passende Konfiguration(-sdatei).
     * <p>
     * 
     * @param confSection
     *            Ein Enum, dessen Name dem Parameter entspricht, nach dem
     *            gesucht wird.
     * @return null oder eine passende Konfiguration. Existiert derselbe
     *         Schluessel in zwei Konfigurationen, wird die zuerst gefundene
     *         Configuration zurueckgeliefert.
     */
    public Configuration retrieveConfiguration(ConfigSection confSection) throws ActionFailedException {
        return retrieveConfiguration(confSection.getName());
    }
}
