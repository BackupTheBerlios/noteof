package de.notEOF.configuration.service;

import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.util.Util;
import de.notIOC.exception.NotIOCException;

public class ConfigurationService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        return ConfigurationTag.class;
    }

    @Override
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        if (incomingMsgEnum.equals(ConfigurationTag.REQ_KEY)) {
            String xmlPath = requestTo(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH);
            Enum<ConfigurationTag> valueFound = ConfigurationTag.INFO_FAULT;
            String value = "";
            try {
                value = LocalConfigurationClient.getString(xmlPath);
                valueFound = ConfigurationTag.INFO_OK;
            } catch (NotIOCException e) {
                // nothing to do...
            }

            System.out.println("warte auf Request von Client");
            awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND, valueFound.name());

            System.out.println("value " + value);
            if (!Util.isEmpty(value)) {
                DataObject configObject = new DataObject();
                configObject.setConfigurationValue(value);
                sendDataObject(configObject);
            }
        }
    }
}
