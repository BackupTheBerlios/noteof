package de.noteof.core.configuration;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.noteof.core.logging.LocalLog;

public class ConfigurationManager {

    private String iccsHome = null;
    private CompositeConfigurationNotEof iccsConfiguration = new CompositeConfigurationNotEof();


    /**
     * ConfigurationManager is a Singleton...
     */

    protected static ConfigurationManager configManager;

    public static ConfigurationManager getInstance() {
        if (configManager == null) {
            configManager = new ConfigurationManager();
        }
        return configManager;
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
