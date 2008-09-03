package de.notEOF.configuration.service;

import java.util.List;

import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
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
            String attributeName = requestTo(ConfigurationTag.REQ_ATTRIBUTE_NAME, ConfigurationTag.RESP_ATTRIBUTE_NAME);

            Enum<ConfigurationTag> valueFound = ConfigurationTag.INFO_FAULT;
            List<String> values = null;
            try {
                values = LocalConfigurationClient.getAttributeList(xmlPath, attributeName);
                valueFound = ConfigurationTag.INFO_OK;
            } catch (NotIOCException e) {
                LocalLog.warn("Gesuchter Konfigurationswert konnte nicht ermittelt werden: " + xmlPath + "/" + attributeName, e);
            }
            awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND, valueFound.name());

            if (null != values) {
                DataObject configObject = new DataObject();
                if (values.size() > 1) {
                    // list found
                    configObject.setList(values);
                } else {
                    configObject.setConfigurationValue(values.get(0));
                }
                sendDataObject(configObject);
            }
        }
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }

    public List<EventType> getObservedEvents() {
        return null;
    }
}
