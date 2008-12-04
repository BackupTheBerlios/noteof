package de.happtick.mail.client;

import java.net.Socket;

import de.happtick.mail.service.HapptickEventService;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.mail.client.EventReceiveClient;

public class HapptickEventClient extends EventReceiveClient {

    public HapptickEventClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public HapptickEventClient(String serverAddress, int port, TimeOut timeout, String[] args) throws ActionFailedException {
        super(serverAddress, port, timeout, args);
    }

    @Override
    public Class<?> serviceForClientByClass() {
        return HapptickEventService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }
}
