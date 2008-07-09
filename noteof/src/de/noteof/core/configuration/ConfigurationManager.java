package de.noteof.core.configuration;

import java.io.File;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import de.iccs.core.conf.CompositeConfigurationIccs;
import de.iccs.core.conf.ConfigSection;
import de.noteof.core.logging.LocalLog;

public class ConfigurationManager {

    private String noteofHome = null;
    private CompositeConfigurationNotEof notEofConfiguration = new CompositeConfigurationNotEof();
    private XMLConfiguration basicConfiguration = new XMLConfiguration();
    private String configFile = "noteof_master_conf.xml";
    private String configFilePath;



    /**
     * ConfigurationManager is a Singleton...
     */

    protected static ConfigurationManager configManager;
    
    private ConfigurationManager () throws ConfigurationException  {
        File cfgRoot = new File(getConfigRoot());
        configFilePath = new File(cfgRoot, configFile).getAbsolutePath();
        basicConfiguration.setFileName(configFile);

        LocalLog.info("ConfigManager ConfigFile: " + configFilePath);

        try {
            basicConfiguration.setURL(new File(configFilePath).toURL());
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }

        LocalLog.info("ConfigManager BasePath: " + basicConfiguration.getBasePath());
        LocalLog.info("ConfigManager FileName: " + basicConfiguration.getFileName());
        LocalLog.info("ConfigManager URL: " + basicConfiguration.getURL());

        loadConfiguration(false);
    }

    public static ConfigurationManager getInstance() throws ConfigurationException {
        if (configManager == null) {
            configManager = new ConfigurationManager();
        }
        return configManager;
    }
    
    /*
     * Stellt sicher, dass es eine Instanz des ConfigManagers gibt und liefert diese zurueck.
     */
    private ConfigurationManager retrieveSingleConfigManager() {
        if (null == configManager) {
            try {
                configManager = new ConfigurationManager();
            } catch (ConfigurationException cfgEx) {
            }
        }

        return configManager;
    }
    
    /**
     * In Ausnahmesituationen kann es erforderlich sein, die Konfigurationsdatei(en) neu zu laden. Z.B. wenn ein neuer Wert in CompositeConfiguration generiert wurde.
     */
    private void loadConfiguration(boolean reload) throws ConfigurationException {
        if (reload) {
            notEofConfiguration = new CompositeConfigurationNotEof();
            basicConfiguration.reload();
        } else {
            basicConfiguration.load();
        }

        // Liste aller XML-Konfigurationen
        String pathName = basicConfiguration.getBasePath().replaceAll("file:", "");
        @SuppressWarnings("unchecked")
        List<String> xmlFileList = (List<String>)basicConfiguration.getList(ConfigSection.XML_FILE_NAMES.getName());
        for (String fileName : xmlFileList) {
            // XMLConfiguration additionalXMLConf = new XMLConfiguration(new File(pathName, fileName));
            XMLConfiguration additionalXMLConf = new XMLConfiguration(pathName + fileName);
            notEofConfiguration.addConfiguration(additionalXMLConf);
        }
    }


    
    /**
     * Liefert die Konfiguration des gesamten ICCS zurueck. Der Zugriff auf bestimmte Schluessel/Werte erfolgt, wie in der Dokumentation zu CompositeConfigurations beschrieben (http://jakarta.apache.org/commons/configuration/index.html)
     * @return Configuration mit Zugriff auf die Konfiguration. <br>
     *         Die Klasse {@link CompositeConfigurationIccs} ueberschreibt die Methode 'setProperty()', die zum Speichern einer geaenderten Konfiguration genutzt werden sollte.
     * @see org.apache.commons.configuration.CompositeConfiguration
     */
    public CompositeConfigurationNotEof getConfiguration() {
        return notEofConfiguration;
    }

    /**
     * Liefert den Konfigurationsmanager. Wird eigentlich ausserhalb des zentralen ICCS- Konfigurations-Managements nicht benoetigt. Stattdessen sollte die Konfiguration mit Hilfe des Objekts 'Configuration' ausgelesen werden (Methode getConfiguration()).
     * @return Der komplette Konfigurationsmanager
     */
    public ConfigurationManager getConfigurationManager() {
        return retrieveSingleConfigManager();
    }



    /**
     * @return Der zentrale Konfigurationspfad (CFGROOT bzw. ICCS_HOME)
     */
    public String getIccsHome() {
        // CFGROOT als contextabhaengige Tomcat-Umgebungsvariable
        if (noteofHome == null) {
            try {
                noteofHome = (String) new InitialContext().lookup("java:comp/env/ICCS_HOME");
                if (noteofHome != null)
                    LocalLog.info("ICCS_HOME found as InitialContext: " + noteofHome);
            } catch (NamingException e) {
            }
        }
        // CFGROOT als VM-Umgebungsvariable (-DCFGROOT)
        if (noteofHome == null) {
            noteofHome = System.getProperty("ICCS_HOME");
            if (noteofHome != null)
                LocalLog.info("ICCS_HOME found in VM variable: " + noteofHome);
        }
        // ICCS_HOME als Systemvariable
        if (noteofHome == null) {
            noteofHome = System.getenv("ICCS_HOME");
            if (noteofHome != null)
                LocalLog.info("ICCS_HOME found in system variable: " + noteofHome);
        }
        if (noteofHome == null) {
            throw new RuntimeException("Could not determine ICCS_HOME");
        }
        return noteofHome;
    }

    public String getDefaultConfigurationFile() {
        return this.getConfigRoot() + "/" + notEofConfiguration.getString("defaultConfigurationFile");
    }

    /**
     * @return Der zentrale Konfigurationspfad (CFGROOT bzw. ICCS_HOME)
     */
    public String getConfigRoot() {
        return new File(getIccsHome(), "conf").getAbsolutePath();
    }

}
