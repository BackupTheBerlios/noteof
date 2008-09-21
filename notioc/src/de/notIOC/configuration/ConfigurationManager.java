package de.notIOC.configuration;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.Configuration;

import de.notIOC.exception.NotIOCException;
import de.notIOC.logging.LocalLog;
import de.notIOC.util.Util;

/**
 * ConfigurationManager is a Singleton...
 * <p>
 * ... and used for get the configuration. <br>
 * He delivers the central configuration path and objects of type
 * ConfigProperty.
 * <p>
 * The ConfigurationManager should be initialized by the method
 * setInitialEnvironment(). If not he uses default values.
 * 
 * @see setInitialEnvironment().
 */
public class ConfigurationManager {

    private static String notEOFHome = null;
    private static String homeVarName = "NOT_EOF_HOME";
    private static String configFile = "noteof_master.xml";
    private static String configPath = "conf";

    protected static ConfigurationManager configManager;

    private ConfigurationManager() {
        try {
            loadConfiguration();
        } catch (NotIOCException e) {
            LocalLog.error("ConfigurationManager kann nicht geladen werden.", e);
        }
    }

    /**
     * The configuration manager must know where the configuration is stored. <br>
     * Typically there must be set a central HOME variable, a path (folder)
     * where the configuration file is stored and a configuration file. <br>
     * The configuration is searched in this path:
     * $NOT_EOF_HOME/conf/noteof_master.xml
     * 
     * @param homeVariableName
     *            e.g. NOT_EOF_HOME (default)
     * @param baseConfPath
     *            e.g. conf (default)
     * @param baseConfFile
     *            e.g. noteof_master.xml (default)
     */
    public static void setInitialEnvironment(String homeVariableName, String baseConfPath, String baseConfFile) {
        if (!Util.isEmpty(homeVariableName))
            homeVarName = homeVariableName;
        if (!Util.isEmpty(baseConfPath))
            configPath = baseConfPath;
        if (!Util.isEmpty(baseConfFile))
            configFile = baseConfFile;
        getInstance();
    }

    public static ConfigurationManager getInstance() {
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
                notEOFHome = (String) new InitialContext().lookup("java:comp/env/" + homeVarName);
                if (notEOFHome != null)
                    LocalLog.info("Home '" + homeVarName + "' found as initial context: " + notEOFHome);
            } catch (NamingException e) {
            }
        }
        // CFGROOT as VM-environment variable (-DCFGROOT)
        if (notEOFHome == null) {
            notEOFHome = System.getProperty(homeVarName);
            if (notEOFHome != null)
                LocalLog.info("Home '" + homeVarName + "' found in VM variable: " + notEOFHome);
        }
        // NOTEOF_HOME as system variable
        if (notEOFHome == null) {
            notEOFHome = System.getenv(homeVarName);
            if (notEOFHome != null)
                LocalLog.info("Home '" + homeVarName + "' found in system variable: " + notEOFHome);
        }
        if (notEOFHome == null) {
            throw new RuntimeException("Could not determine home variable: " + homeVarName);
        }
        System.out.println("Project environment home variable: '" + homeVarName + "'; Value: " + notEOFHome);
        return notEOFHome;
    }

    /**
     * @return The central configuration path of the server application (CFGROOT
     *         or NOTEOF_HOME) like $NOTEOF_HOME/conf
     */
    public String getConfigRoot() {
        return new File(getApplicationHome(), configPath).getAbsolutePath();
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
        getInstance();
        return new ConfigProperty(nodeName);
    }
}
