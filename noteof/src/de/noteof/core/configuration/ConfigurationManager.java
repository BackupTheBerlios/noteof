package de.noteof.core.configuration;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.ConfigurationException;

import de.iccs.core.conf.CompositeConfigurationIccs;
import de.iccs.logging.LOG;
import de.noteof.core.logging.LocalLog;

public class ConfigurationManager {

    private String iccsHome = null;
    private CompositeConfigurationNotEof notEofConfiguration = new CompositeConfigurationNotEof();


    /**
     * ConfigurationManager is a Singleton...
     */

    protected static ConfigurationManager configManager;
    
    private ConfigurationManager () throws ConfigurationException  {
        LocalLog.debug("ConfigManager Initialisierung");

        File cfgRoot = new File(getConfigRoot());
        configFilePath = new File(cfgRoot, configFile).getAbsolutePath();
        iccsBasicConfiguration.setFileName(configFile);

        LocalLog.info("ConfigManager ConfigFile: " + configFilePath);

        try {
            iccsBasicConfiguration.setURL(new File(configFilePath).toURL());
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }

        LocalLog.info("ConfigManager BasePath: " + iccsBasicConfiguration.getBasePath());
        LocalLog.info("ConfigManager FileName: " + iccsBasicConfiguration.getFileName());
        LocalLog.info("ConfigManager URL: " + iccsBasicConfiguration.getURL());

        loadConfiguration(false);

        LocalLog.debug("ConfigManager Initialisierung abgeschlossen");
    }

    public static ConfigurationManager getInstance() {
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
     * Liefert die Konfiguration des gesamten ICCS zurueck. Der Zugriff auf bestimmte Schluessel/Werte erfolgt, wie in der Dokumentation zu CompositeConfigurations beschrieben (http://jakarta.apache.org/commons/configuration/index.html)
     * @return Configuration mit Zugriff auf die Konfiguration. <br>
     *         Die Klasse {@link CompositeConfigurationIccs} ueberschreibt die Methode 'setProperty()', die zum Speichern einer geaenderten Konfiguration genutzt werden sollte.
     * @see org.apache.commons.configuration.CompositeConfiguration
     */
    public CompositeConfigurationIccs getConfiguration() {
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
        if (iccsHome == null) {
            try {
                iccsHome = (String) new InitialContext().lookup("java:comp/env/ICCS_HOME");
                if (iccsHome != null)
                    LocalLog.info("ICCS_HOME found as InitialContext: " + iccsHome);
            } catch (NamingException e) {
            }
        }
        // CFGROOT als VM-Umgebungsvariable (-DCFGROOT)
        if (iccsHome == null) {
            iccsHome = System.getProperty("ICCS_HOME");
            if (iccsHome != null)
                LocalLog.info("ICCS_HOME found in VM variable: " + iccsHome);
        }
        // ICCS_HOME als Systemvariable
        if (iccsHome == null) {
            iccsHome = System.getenv("ICCS_HOME");
            if (iccsHome != null)
                LocalLog.info("ICCS_HOME found in system variable: " + iccsHome);
        }
        if (iccsHome == null) {
            throw new RuntimeException("Could not determine ICCS_HOME");
        }
        return iccsHome;
    }

    public String getDefaultConfigurationFile() {
        return this.getConfigRoot() + "/" + iccsConfiguration.getString("defaultConfigurationFile");
    }

    /**
     * @return Der zentrale Konfigurationspfad (CFGROOT bzw. ICCS_HOME)
     */
    public String getConfigRoot() {
        return new File(getIccsHome(), "conf").getAbsolutePath();
    }

}
