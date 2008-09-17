package de.notEOF.configuration;

import java.util.List;

import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;
import de.notIOC.configuration.ConfigurationManager;
import de.notIOC.util.Util;

/**
 * This client supports the access to a local configuration (file).
 * 
 * @see ConfigurationClient
 * @author Dirk
 * 
 */
public class LocalConfiguration implements NotEOFConfiguration {

    public static String getApplicationHome() {
        return ConfigurationManager.getApplicationHome();
    }

    public List<String> getAttributeList(String xmlPath, String attributeName) throws ActionFailedException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getAttributeList(attributeName);
        } catch (Exception ex) {
            throw new ActionFailedException(34L, "Element: " + xmlPath);
        }
    }

    public List<String> getTextList(String xmlPath) throws ActionFailedException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getTextList();
        } catch (Exception ex) {
            throw new ActionFailedException(34L, "Element: " + xmlPath);
        }
    }

    public String getAttribute(String xmlPath, String attributeName, String defaultValue) throws ActionFailedException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName, defaultValue);
        } catch (Exception ex) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlPath + "; Default Wert wird verwendet: " + defaultValue);
            return defaultValue;
        }
    }

    public String getAttribute(String xmlPath, String attributeName) throws ActionFailedException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName);
        } catch (Exception ex) {
            throw new ActionFailedException(34L, "Element: " + xmlPath);
        }
    }

    public int getAttributeInt(String xmlPath, String attributeName, int defaultValue) throws ActionFailedException {
        try {
            return Util.parseInt(ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName), defaultValue);
        } catch (Exception ex) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlPath + "; Default Wert wird verwendet: " + defaultValue);
        }
        return defaultValue;
    }

    public int getAttributeInt(String xmlPath, String attributeName) throws ActionFailedException {
        try {
            return Util.parseInt(ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName), 0);
        } catch (Exception ex) {
            throw new ActionFailedException(34L, "Element: " + xmlPath);
        }
    }

    public String getText(String xmlPath) throws ActionFailedException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getText();
        } catch (Exception ex) {
            throw new ActionFailedException(34L, "Element: " + xmlPath);
        }
    }

    public String getText(String xmlPath, String defaultValue) throws ActionFailedException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getText(defaultValue);
        } catch (Exception ex) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlPath + "; Default Wert wird verwendet: " + defaultValue);
            return "";
        }
    }

}
