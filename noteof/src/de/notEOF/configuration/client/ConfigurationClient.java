package de.notEOF.configuration.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.DataObjectDataTypes;
import de.notEOF.core.enumeration.DataObjectListTypes;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.logging.LocalLog;
import de.notIOC.util.Util;

/**
 * This client enables the access to single configuration objects via the
 * network.
 * <p>
 * Only the socket to the server or the ip and the port of the server must be
 * known. <br>
 * Don't forget to establish the connection to the service by using the connect
 * method or (maybe better) make the instance by using the constructor with
 * connection data.
 * 
 * @author Dirk
 * 
 */
public class ConfigurationClient extends BaseClient implements NotEOFConfiguration {

    /**
     * If this constructor is used it is recommended to call the method
     * connect() at a later time to establish the communication to the
     * ConfigurationService.
     */
    // public ConfigurationClient() {
    // }
    /**
     * Standard construction of the client. <br>
     * Within the super constructors of BaseClient the connection with server
     * and service will be established. If the 'generic' constructor is used the
     * connection must be established later by the method connect().
     * 
     * @throws ActionFailedException
     *             If the connection with server and service couldn't be
     *             established successfull an ActionFailedException will be
     *             thrown.
     */
    public ConfigurationClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    /**
     * Standard construction of the client. <br>
     * Within the super constructors of BaseClient the connection with server
     * and service will be established.
     * 
     * @throws ActionFailedException
     *             If the connection with server and service couldn't be
     *             established successfull an ActionFailedException will be
     *             thrown.
     */
    public ConfigurationClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
        // TODO activateLifeSignSystem mit Zeit aus timeout
    }

    /**
     * This method is used by the framework to find the correct service.
     */
    @Override
    public Class<?> serviceForClientByClass() {
        // return ConfigurationService.class;
        return null;
    }

    /**
     * This method is used by the framework to find the correct service.
     */
    @Override
    public String serviceForClientByName() {
        return "de.notEOF.configuration.service.ConfigurationService";
    }

    public String getAttribute(String xmlConfKey, String attributeName) throws ActionFailedException {
        List<String> newList = getAttributeList(xmlConfKey, attributeName);
        if (null == newList)
            throw new ActionFailedException(34L, "Element: " + xmlConfKey + "; Attribute: " + attributeName);

        return newList.get(0);
    }

    public String getAttribute(String xmlConfKey, String attributeName, String defaultValue) throws ActionFailedException {
        try {
            return getAttribute(xmlConfKey, attributeName);
        } catch (ActionFailedException afx) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlConfKey + "; Default Wert wird verwendet: " + defaultValue);
            return defaultValue;
        }
    }

    public String getText(String xmlConfKey) throws ActionFailedException {
        List<String> newList = getTextList(xmlConfKey);
        if (null == newList)
            throw new ActionFailedException(34L, "Element: " + xmlConfKey);

        return newList.get(0);
    }

    public String getText(String xmlConfKey, String defaultValue) throws ActionFailedException {
        try {
            return getText(xmlConfKey);
        } catch (ActionFailedException afx) {
            LocalLog.warn("Konfigurationswert fehlt: " + xmlConfKey + "; Default Wert wird verwendet: " + defaultValue);
            return defaultValue;
        }
    }

    public int getAttributeInt(String xmlPath, String attributeName, int defaultValue) throws ActionFailedException {
        String stringVal = getAttribute(xmlPath, attributeName);
        return Util.parseInt(stringVal, defaultValue);
    }

    public int getAttributeInt(String xmlPath, String attributeName) throws ActionFailedException {
        String stringVal = getAttribute(xmlPath, attributeName);
        return Util.parseInt(stringVal, 0);
    }

    public List<String> getAttributeList(String xmlPath, String attributeName) throws ActionFailedException {
        writeMsg(ConfigurationTag.REQ_ATTRIBUTE);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH, xmlPath);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_ATTRIBUTE_NAME, ConfigurationTag.RESP_ATTRIBUTE_NAME, attributeName);
        // only if the service has found values he sends a data object
        if (ConfigurationTag.INFO_OK.name().equals(requestTo(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND))) {
            DataObject dataObject = receiveDataObject();
            if (null != dataObject) {
                List<String> newList = new ArrayList<String>();
                if (dataObject.getDataType() == DataObjectDataTypes.LIST && //
                        dataObject.getListObjectType() == DataObjectListTypes.STRING) {
                    for (Object obj : dataObject.getList()) {
                        newList.add((String) obj);
                    }
                } else if (dataObject.getDataType() == DataObjectDataTypes.CONFIGURATION_VALUE) {
                    newList.add(dataObject.getConfigurationValue());
                }
                return newList;
            }
        }
        LocalLog.warn("Konfigurationswert fehlt: " + xmlPath + "; Attribut: " + attributeName);
        return null;
    }

    public List<String> getTextList(String xmlPath) throws ActionFailedException {
        writeMsg(ConfigurationTag.REQ_TEXT);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH, xmlPath);
        // only if the service has found values he sends a data object
        if (ConfigurationTag.INFO_OK.name().equals(requestTo(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND))) {
            DataObject dataObject = receiveDataObject();
            if (null != dataObject) {
                List<String> newList = new ArrayList<String>();
                if (dataObject.getDataType() == DataObjectDataTypes.LIST && //
                        dataObject.getListObjectType() == DataObjectListTypes.STRING) {
                    for (Object obj : dataObject.getList()) {
                        newList.add((String) obj);
                    }
                } else if (dataObject.getDataType() == DataObjectDataTypes.CONFIGURATION_VALUE) {
                    newList.add(dataObject.getConfigurationValue());
                }
                return newList;
            }
        }
        LocalLog.warn("Konfigurationswert fehlt: " + xmlPath);
        return null;
    }

}
