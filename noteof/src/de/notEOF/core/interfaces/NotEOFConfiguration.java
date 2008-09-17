package de.notEOF.core.interfaces;

import java.util.List;

import de.notEOF.core.exception.ActionFailedException;

public interface NotEOFConfiguration {

    /**
     * Delivers an attribute list of the local configuration. <br>
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
    public List<String> getAttributeList(String xmlPath, String attributeName) throws ActionFailedException;

    /**
     * Delivers a text list of the local configuration. <br>
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
    public List<String> getTextList(String xmlPath) throws ActionFailedException;

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
    public String getAttribute(String xmlPath, String attributeName, String defaultValue) throws ActionFailedException;

    /**
     * Delivers the String value of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @return The value for the key as a String.
     * @param attributeName
     *            The name of the attribute for which the value is required.
     * @throws ActionFailedException
     *             If the key was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public String getAttribute(String xmlPath, String attributeName) throws ActionFailedException;

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
    public int getAttributeInt(String xmlPath, String attributeName, int defaultValue) throws ActionFailedException;

    /**
     * Delivers the int value of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @param attributeName
     *            The name of the attribute for which the value is required.
     * @return The value for the key as an int.
     * @throws ActionFailedException
     *             If the node was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public int getAttributeInt(String xmlPath, String attributeName) throws ActionFailedException;

    /**
     * Delivers the text of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @return The text of a configuration element.
     * @throws ActionFailedException
     *             If the node was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public String getText(String xmlPath) throws ActionFailedException;

    /**
     * Delivers the text of a configuration key.
     * 
     * @param xmlPath
     *            The node of the xml element.
     * @param defaultValue
     *            If the key was not found or the value is empty (null or
     *            length=0) this value will be returned.
     * @return The text of a configuration element.
     * @throws ActionFailedException
     *             If the node was not found or the value is empty (null or
     *             length=0) an exception is thrown. The exception tells for
     *             which key the value was empty.
     */
    public String getText(String xmlPath, String defaultValue) throws ActionFailedException;
}
