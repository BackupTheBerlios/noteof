package de.noteof.core.configuration;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import de.noteof.core.exception.ActionFailedException;
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

    private ConfigurationManager() throws ConfigurationException {
        File cfgRoot = new File(getConfigRoot());
        configFilePath = new File(cfgRoot, configFile).getAbsolutePath();
        basicConfiguration.setFileName(configFile);

        LocalLog.info("ConfigManager ConfigFile: " + configFilePath);

        try {
            File x = new File(configFilePath);
            URI uri = x.toURI();
            URL url = uri.toURL();
            basicConfiguration.setURL(url);
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
     * Stellt sicher, dass es eine Instanz des ConfigManagers gibt und liefert
     * diese zurueck.
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
     * In Ausnahmesituationen kann es erforderlich sein, die
     * Konfigurationsdatei(en) neu zu laden. Z.B. wenn ein neuer Wert in
     * CompositeConfiguration generiert wurde.
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
        List<String> xmlFileList = (List<String>) basicConfiguration.getList(ConfigSection.XML_FILE_NAMES.getName());
        for (String fileName : xmlFileList) {
            // XMLConfiguration additionalXMLConf = new XMLConfiguration(new
            // File(pathName, fileName));
            XMLConfiguration additionalXMLConf = new XMLConfiguration(pathName + fileName);
            notEofConfiguration.addConfiguration(additionalXMLConf);
        }
    }

    /**
     * Delifers the whole configuration of !EOF. <br>
     * How access to certain keys respectively their values works is described
     * in the documentation of CompositeConfigurations <br>
     * (http://jakarta.apache.org/commons/configuration/index.html)
     * 
     * @return Complete configuration<br>
     *         Class {@link CompositeConfigurationNotEof} overwrites some
     *         methods (esp. setProperty()).
     * @see org.apache.commons.configuration.CompositeConfiguration
     */
    public CompositeConfigurationNotEof getConfiguration() {
        return notEofConfiguration;
    }

    /**
     * Delivers the manager itself. <br>
     * Normally the manager is not requested by other objects except for the
     * central configuration management. <br>
     * It is recommended to use the configuration object {@link Configuration}
     * (getConfiguration()).
     * 
     * @return Single instance of ConfigurationManager
     */
    public ConfigurationManager getConfigurationManager() {
        return retrieveSingleConfigManager();
    }

    /**
     * @return Central configuration path (CFGROOT, NOTEOF_HOME)
     */
    public String getNotEofHome() {
        // CFGROOT als contextabhaengige Tomcat-Umgebungsvariable
        if (noteofHome == null) {
            try {
                noteofHome = (String) new InitialContext().lookup("java:comp/env/NOTEOF_HOME");
                if (noteofHome != null)
                    LocalLog.info("NOTEOF_HOME found as InitialContext: " + noteofHome);
            } catch (NamingException e) {
            }
        }
        // CFGROOT als VM-Umgebungsvariable (-DCFGROOT)
        if (noteofHome == null) {
            noteofHome = System.getProperty("NOTEOF_HOME");
            if (noteofHome != null)
                LocalLog.info("ICCS_HOME found in VM variable: " + noteofHome);
        }
        // ICCS_HOME als Systemvariable
        if (noteofHome == null) {
            noteofHome = System.getenv("NOTEOF_HOME");
            if (noteofHome != null)
                LocalLog.info("ICCS_HOME found in system variable: " + noteofHome);
        }
        if (noteofHome == null) {
            throw new RuntimeException("Could not determine NOTEOF_HOME");
        }
        return noteofHome;
    }

    public String getDefaultConfigurationFile() {
        return this.getConfigRoot() + "/" + notEofConfiguration.getString("defaultConfigurationFile");
    }

    /**
     * @return The central configuration path of the server application (CFGROOT
     *         bzw. NOTEOF_HOME) like $NOTEOF_HOME/conf
     */
    public String getConfigRoot() {
        return new File(getNotEofHome(), "conf").getAbsolutePath();
    }

    /**
     * Liefert eine spezielle Konfiguration ueber einen Schluessel.
     * <p>
     * Das ermoeglicht dem Entwickler die vom ConfigManager 'unabhaengige'
     * Verwendung der Konfigurationsdateien, ohne Kenntnis von Dateinamen und
     * Pfaden.
     * 
     * @param confSection
     *            Ein enum-Objekt, das den die Konfiguration identifizierenden
     *            Schluessel beinhaltet.
     * @return Eine Konfiguration, die Teil der Gesamtkonfiguration sein kann.
     */
    public static ConfigProperty getProperty(String nodeName) {
        return new ConfigProperty(nodeName);
    }

    /*
     * Methode wird durch dynamisches Laden der Klasse vom marvin verwendet.
     * Daher findet der Compiler keine Referenz.
     */
    public static String getPropertyString(String nodeName, String defaultValue) throws ActionFailedException {
        ConfigProperty property = getProperty(nodeName);
        if (property == null)
            return null;
        return property.getStringValue();
    }

    public static String getPropertyString(String nodeName) throws ActionFailedException {
        return getPropertyString(nodeName, null);
    }
}
