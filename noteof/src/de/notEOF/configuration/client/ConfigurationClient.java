package de.notEOF.configuration.client;

import java.net.Socket;

import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;

/**
 * This client enables the access to single configuration objects via the network. <p>
 * Only the socket to the server or the ip and the port of the server must be known.
 * @author Dirk
 *
 */
public class ConfigurationClient extends BaseClient {

    /**
     * If this constructor is used it is recommended to call the method connect() at a later time to establish the communication to the ConfigurationService.
     */
    public ConfigurationClient() {
    }

    /**
     * Standard construction of the client. <br>
     * Within the super constructors of BaseClient the connection with server and service will be established. If the 'generic' constructor is
     * used the connection must be established later by the method connect().
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
     * Within the super constructors of BaseClient the connection with server and service will be established.
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
//        return ConfigurationService.class;
        return null;
    }

    /**
     * This method is used by the framework to find the correct service.
     */
    @Override
    public String serviceForClientByName() {
        return "de.notEOF.configuration.service.ConfigurationService";
    }

    /**
     * Delivers a DataObject with a configuration attribute value (also can be a list of values).
     * @param xmlConfKey Configuration key used in xml format (e.g. chains.chain.used)
     * @param attributeName Attribute Name of the attribute.
     * @return A DataObject which holds the values or null if the xmlConfKey doesn't point to a valid xml path.  
     * @see DataObject
     * @throws ActionFailedException
     */
    public DataObject getAttribute(String xmlConfKey, String attributeName) throws ActionFailedException {
        writeMsg(ConfigurationTag.REQ_ATTRIBUTE);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH, xmlConfKey);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_ATTRIBUTE_NAME, ConfigurationTag.RESP_ATTRIBUTE_NAME, attributeName);
        // only if the service has found values he sends a data object
        if (ConfigurationTag.INFO_OK.name().equals(requestTo(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND))) {
            return receiveDataObject();
        }
        return null;
    }
    
    /**
     * Delivers a DataObject with a configuration text value (also can be a list of values).
     * @param xmlConfKey Configuration key used in xml format (e.g. chains.chain.used)
     * @return A DataObject which holds the values or null if the xmlConfKey doesn't point to a valid xml path.  
     * @see DataObject
     * @throws ActionFailedException
     */
    public DataObject getText(String xmlConfKey) throws ActionFailedException {
        writeMsg(ConfigurationTag.REQ_TEXT);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH, xmlConfKey);
        // only if the service has found values he sends a data object
        if (ConfigurationTag.INFO_OK.name().equals(requestTo(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND))) {
            return receiveDataObject();
        }
        return null;
    }

}
