package de.happtick.mail.client;

import java.net.Socket;

import de.happtick.mail.service.HapptickMailEventService;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.mail.client.MailAndEventClient;

public class HapptickMailEventClient extends MailAndEventClient {

    public HapptickMailEventClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public HapptickMailEventClient(String serverAddress, int port, TimeOut timeout, String[] args) throws ActionFailedException {
        super(serverAddress, port, timeout, args);
    }

    @Override
    public Class<?> serviceForClientByClass() {
        return HapptickMailEventService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }
}
