package de.notEOF.configuration.service;

import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;

public class ConfigurationService extends BaseService {

    /**
     * Used by the framework.
     */
    @Override
    public Class<?> getCommunicationTagClass() {
        return ConfigurationTag.class;
    }

    @Override
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        NotEOFConfiguration conf = new LocalConfiguration();

        List<String> values = null;
        Enum<ConfigurationTag> valueFound = ConfigurationTag.INFO_FAULT;

        // look for attribute value
        if (incomingMsgEnum.equals(ConfigurationTag.REQ_ATTRIBUTE)) {
            String xmlPath = requestTo(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH);
            String attributeName = requestTo(ConfigurationTag.REQ_ATTRIBUTE_NAME, ConfigurationTag.RESP_ATTRIBUTE_NAME);
            try {
                values = conf.getAttributeList(xmlPath, attributeName);
                valueFound = ConfigurationTag.INFO_OK;
            } catch (ActionFailedException e) {
                LocalLog.warn("Gesuchter Konfigurationswert konnte nicht ermittelt werden: " + xmlPath + "/" + attributeName, e);
            }
        }

        // look for text value
        if (incomingMsgEnum.equals(ConfigurationTag.REQ_TEXT)) {
            String xmlPath = requestTo(ConfigurationTag.REQ_KEY_PATH, ConfigurationTag.RESP_KEY_PATH);
            try {
                values = conf.getTextList(xmlPath);
                valueFound = ConfigurationTag.INFO_OK;
            } catch (ActionFailedException e) {
                LocalLog.warn("Gesuchter Konfigurationswert konnte nicht ermittelt werden: " + xmlPath, e);
            }
        }

        // tell client if value was found
        awaitRequestAnswerImmediate(ConfigurationTag.REQ_KEY_FOUND, ConfigurationTag.RESP_KEY_FOUND, valueFound.name());

        // if values found send object
        if (null != values) {
            DataObject configObject = new DataObject();
            if (values.size() > 1) {
                // list found
                configObject.setList(values);
            } else {
                // single value
                configObject.setConfigurationValue(values.get(0));
            }
            System.out.println("Aufruf sendDataObject im ConfigurationService");
            sendDataObject(configObject);
        }
    }

    /**
     * Used by framework.
     */
    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }

    /**
     * Used by framework.
     */
    public List<EventType> getObservedEvents() {
        return null;
    }
}
