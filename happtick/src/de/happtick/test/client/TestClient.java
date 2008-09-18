package de.happtick.test.client;

import java.net.Socket;

import de.happtick.test.enumeration.TestTag;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;

public class TestClient extends ConfigurationClient {

    public TestClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
        // TODO Auto-generated constructor stub
    }

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
