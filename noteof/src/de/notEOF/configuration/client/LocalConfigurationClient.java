package de.notEOF.configuration.client;

import java.util.List;

import de.notEOF.core.logging.LocalLog;
import de.notIOC.configuration.ConfigurationManager;
import de.notIOC.exception.NotIOCException;
import de.notIOC.util.Util;

/**
 * This client supports the access to a local configuration.
 * 
 * @author Dirk
 * 
 */
public class LocalConfigurationClient {

    public static String getApplicationHome() {
        return ConfigurationManager.getApplicationHome();
    }

    /**
     * Delivers a list of the local configuration. <br>
     * The list entries always are from type String. <br>
     * Please take notice of the double quotation marks.
     * 
     * @param xmlPath
     *            Is the xml node to the element.
     * @param attributeName
     *            The name of the attribute for which the list must be created.
     * 
     * @return A list out of the xml configuration file.
     */
    public static List<String> getAttributeList(String xmlPath, String attributeName) throws NotIOCException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getAttributeList(attributeName);
        } catch (Exception ex) {
            throw new NotIOCException(34L, "Element: " + xmlPath);
        }
    }

    // private static List<String> getList(String xmlPath, String listTag) {
    // return ConfigurationManager.getProperty(xmlPath).getList(listTag);
    // }

    /**
     * Delivers the String value of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @param attributeName
     *            The name of the attribute for which the value is required.
     * @param defaultValue
     *            If the key was not found or the value is empty (null or
     *            length=0) this value will be returned.
     * @return The value for the key as a String.
     */
    public static String getAttribute(String xmlPath, String attributeName, String defaultValue) {
        try {
            return ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName, defaultValue);
        } catch (Exception ex) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlPath + "; Default Wert wird verwendet: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Delivers the String value of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @return The value for the key as a String.
     * @param attributeName
     *            The name of the attribute for which the value is required.
     * @throws NotIOCException
     *             If the key was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public static String getAttribute(String xmlPath, String attributeName) throws NotIOCException {
        try {
            return ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName);
        } catch (Exception ex) {
            throw new NotIOCException(34L, "Element: " + xmlPath);
        }
    }

    /**
     * Delivers the int value of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @param attributeName
     *            The name of the attribute for which the value is required.
     * @param defaultValue
     *            If the key was not found or the value is empty (null or
     *            length=0) this value will be returned.
     * @return The value for the key as an int.
     */
    public static int getAttributeInt(String xmlPath, String attributeName, int defaultValue) {
        try {
            return Util.parseInt(ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName), defaultValue);
        } catch (Exception ex) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlPath + "; Default Wert wird verwendet: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Delivers the int value of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @param attributeName
     *            The name of the attribute for which the value is required.
     * @return The value for the key as an int.
     * @throws NotIOCException
     *             If the node was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public static int getAttributeInt(String xmlPath, String attributeName) throws NotIOCException {
        try {
            return Util.parseInt(ConfigurationManager.getProperty(xmlPath).getAttribute(attributeName), 0);
        } catch (Exception ex) {
            throw new NotIOCException(34L, "Element: " + xmlPath);
        }
    }
}
