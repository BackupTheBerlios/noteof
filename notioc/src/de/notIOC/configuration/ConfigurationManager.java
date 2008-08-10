package de.notIOC.configuration;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.Configuration;

import de.notIOC.exception.NotIOCException;
import de.notIOC.logging.LocalLog;

/**
 * ConfigurationManager is a Singleton...
 */
public class ConfigurationManager {

    private static String notEOFHome = null;
    private String configFile = "noteof_master.xml";

    protected static ConfigurationManager configManager = new ConfigurationManager();

    private ConfigurationManager() {
        try {
            loadConfiguration();
        } catch (NotIOCException e) {
            LocalLog.error("ConfigurationManager kann nicht geladen werden.", e);
        }
    }

    public static ConfigurationManager getInstance() throws NotIOCException {
        if (configManager == null) {
            configManager = new ConfigurationManager();
        }
        return configManager;
    }

    /*
     * Creates the singleton...
     */
    private ConfigurationManager retrieveSingleConfigManager() throws NotIOCException {
        if (null == configManager) {
            configManager = new ConfigurationManager();
        }
        return configManager;
    }

    /*
     * Initialization of complete configuration.
     */
    private void loadConfiguration() throws NotIOCException {
        File cfgRoot = new File(getConfigRoot());
        String configFilePath = new File(cfgRoot, configFile).getAbsolutePath();
        LocalLog.info("ConfigManager ConfigFile: " + configFilePath);

        // try {
        // File x = new File(configFilePath);
        // URI uri = x.toURI();
        // URL url = uri.toURL();
        // basicConfiguration.setURL(url);
        // } catch (Exception ex) {
        // throw new ConfigurationException(ex);
        // }

        System.out.println("ConfigurationManager loadConfiguration mit " + configFilePath);
        ConfigurationStore.setMasterXmlFile(configFilePath);
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
    public ConfigurationManager getConfigurationManager() throws NotIOCException {
        return retrieveSingleConfigManager();
    }

    /**
     * @return Central configuration path (CFGROOT, NOTEOF_HOME)
     */
    public static String getApplicationHome() {
        // Configuration Root depends to Tomcat variable
        if (notEOFHome == null) {
            try {
                notEOFHome = (String) new InitialContext().lookup("java:comp/env/NOTEOF_HOME");
                if (notEOFHome != null)
                    LocalLog.info("NOTEOF_HOME found as initial context: " + notEOFHome);
            } catch (NamingException e) {
            }
        }
        // CFGROOT as VM-environment variable (-DCFGROOT)
        if (notEOFHome == null) {
            notEOFHome = System.getProperty("NOTEOF_HOME");
            if (notEOFHome != null)
                LocalLog.info("NOTEOF_HOME found in VM variable: " + notEOFHome);
        }
        // NOTEOF_HOME as system variable
        if (notEOFHome == null) {
            notEOFHome = System.getenv("NOTEOF_HOME");
            if (notEOFHome != null)
                LocalLog.info("NOTEOF_HOME found in system variable: " + notEOFHome);
        }
        if (notEOFHome == null) {
            throw new RuntimeException("Could not determine NOTEOF_HOME");
        }
        return notEOFHome;
    }

    /**
     * @return The central configuration path of the server application (CFGROOT
     *         or NOTEOF_HOME) like $NOTEOF_HOME/conf
     */
    public String getConfigRoot() {
        return new File(getApplicationHome(), "conf").getAbsolutePath();
    }

    /**
     * Delivers the value to a configuration key ignoring the
     * ConfigurationManager environment.
     * <p>
     * 
     * @param nodeName
     *            Complete path of key.
     * @return A configuration object which can be part of the whole project
     *         configuration.
     * @throws NotIOCException
     */
    public static ConfigProperty getProperty(String nodeName) throws NotIOCException {
        return new ConfigProperty(nodeName);
    }
}
