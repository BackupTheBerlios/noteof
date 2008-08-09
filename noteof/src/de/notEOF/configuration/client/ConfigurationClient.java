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

    public ConfigurationClient(Socket socketToServer, TimeOut timeout, boolean activateLifeSignSystem, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, activateLifeSignSystem, args);
    }

    public ConfigurationClient(String ip, int port, TimeOut timeout, boolean activateLifeSignSystem, String... args) throws ActionFailedException {
        super(ip, port, timeout, activateLifeSignSystem, args);
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

    public DataObject getConfigurationValue(String xmlConfKey) throws ActionFailedException {
        writeMsg(ConfigurationTag.REQ_KEY);
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH, xmlConfKey);
        if (ConfigurationTag.INFO_OK.name().equals(requestTo(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND))) {
            System.out.println("Service antwortet mit ok");
            return receiveDataObject();
        }
        return null;
    }

}
