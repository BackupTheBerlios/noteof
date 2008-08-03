package de.notEOF.configuration.client;

import java.util.List;

import de.notIOC.configuration.ConfigurationManager;
import de.notIOC.exception.NotIOCException;

/**
 * This client supports the access to a local configuration.
 * 
 * @author Dirk
 * 
 */
public class LocalConfigurationClient {

    /**
     * Delivers a list of the local configuration. <br>
     * The list entries always are from type String. <br>
     * Please take notice of the double quotation marks.
     * 
     * @param xmlPath
     *            The 'key' of the configuration value. <br>
     *            Sample: <br>
     *            < friendsList name="Berti Bottrop" age="45" > Berti <
     *            /friendsList > <br>
     *            < friendsList name="Elli Eldorado" age="28" > Elli <
     *            /friendsList > <br>
     *            < friendsList name="Ferdi Flop" age="15" > Ferdi <
     *            /friendsList > <br>
     *            < friendsList name="Justus Jenau" age="77" > Justus <
     *            /friendsList >
     *            <p>
     *            Access to the list of short names (Berti, Elli, Ferdi,
     *            Justus): <br>
     *            getList("friendsList");
     *            <p>
     *            Access to their names: <br>
     *            getList("friendsList.[@name]");
     *            <p>
     *            Access to the years they live: <br>
     *            getList("friendsList.[@age]");
     * 
     * @return A list out of the xml configuration file.
     */
    public static List<String> getList(String xmlPath) {
        return ConfigurationManager.getProperty(xmlPath).getList();
    }

    // private static List<String> getList(String xmlPath, String listTag) {
    // return ConfigurationManager.getProperty(xmlPath).getList(listTag);
    // }

    /**
     * Delivers the String value of a configuration key.
     * 
     * @param xmlPath
     *            The key for the value.
     * @param defaultValue
     *            If the key was not found or the value is empty (null or
     *            length=0) this value will be returned.
     * @return The value for the key as a String.
     */
    public static String getString(String xmlPath, String defaultValue) {
        return ConfigurationManager.getProperty(xmlPath).getStringValue(defaultValue);
    }

    /**
     * Delivers the String value of a configuration key.
     * 
     * @param xmlPath
     *            The key for the value.
     * @return The value for the key as a String.
     * @throws NotIOCException
     *             If the key was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public static String getString(String xmlPath) throws NotIOCException {
        return ConfigurationManager.getProperty(xmlPath).getStringValue();
    }

    /**
     * Delivers the int value of a configuration key.
     * 
     * @param xmlPath
     *            The key for the value.
     * @param defaultValue
     *            If the key was not found or the value is empty (null or
     *            length=0) this value will be returned.
     * @return The value for the key as an int.
     */
    public static int getIntValue(String xmlPath, int defaultValue) {
        return ConfigurationManager.getProperty(xmlPath).getIntValue(defaultValue);
    }

    /**
     * Delivers the int value of a configuration key.
     * 
     * @param xmlPath
     *            The key for the value.
     * @return The value for the key as an int.
     * @throws NotIOCException
     *             If the key was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public static int getIntValue(String xmlPath) throws NotIOCException {
        return ConfigurationManager.getProperty(xmlPath).getIntValue();
    }

    /**
     * Delivers the boolean value of a configuration key.
     * 
     * @param xmlPath
     *            The key for the value.
     * @param defaultValue
     *            If the key was not found or the value is empty (null or
     *            length=0) this value will be returned.
     * @return The value for the key as a boolean.
     */
    public static boolean getBooleanValue(String xmlPath, boolean defaultValue) {
        return ConfigurationManager.getProperty(xmlPath).getBooleanValue(defaultValue);
    }

    /**
     * Delivers the boolean value of a configuration key.
     * 
     * @param xmlPath
     *            The key for the value.
     * @return The value for the key as an boolean.
     * @throws NotIOCException
     *             If the key was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public static boolean getBooleanValue(String xmlPath) throws NotIOCException {
        return ConfigurationManager.getProperty(xmlPath).getBooleanValue();
    }
}
