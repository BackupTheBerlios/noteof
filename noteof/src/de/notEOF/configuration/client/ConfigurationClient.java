package de.notEOF.configuration.client;

import java.net.Socket;

import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.configuration.service.ConfigurationService;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;

public class ConfigurationClient extends BaseClient {

    public ConfigurationClient() {
    }

    public ConfigurationClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public ConfigurationClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
        // TODO activateLifeSignSystem mit Zeit aus timeout
    }

    @Override
    public Class<?> serviceForClientByClass() {
        return ConfigurationService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }

    public DataObject getConfigurationObject(String xmlConfKey, String attributeName) throws ActionFailedException {
        writeMsg(ConfigurationTag.REQ_KEY);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH, xmlConfKey);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_ATTRIBUTE_NAME, ConfigurationTag.RESP_ATTRIBUTE_NAME, attributeName);
        if (ConfigurationTag.INFO_OK.name().equals(requestTo(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND))) {
            return receiveDataObject();
        }
        return null;
    }

}