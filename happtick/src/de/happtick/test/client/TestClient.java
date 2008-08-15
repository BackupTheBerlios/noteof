package de.happtick.test.client;

import de.happtick.test.enumeration.TestTag;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;

public class TestClient extends ConfigurationClient {

    @Override
    public Class<?> serviceForClientByClass() {
        return null;
    }

    @Override
    public String serviceForClientByName() {
        return "de.happtick.test.service.TestService";
    }

    public void triggerService() throws ActionFailedException {
        writeMsg(TestTag.REQ_SERVICES);
    }
}
