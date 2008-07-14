package de.notEOF.application.service;

import java.net.Socket;

import de.notEOF.application.client.ApplicationTag;
import de.notEOF.application.client.ApplicationTimeout;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;

public class ApplicationService extends BaseService {

    public ApplicationService(Socket socketToClient) throws ActionFailedException {
        super(socketToClient, new ApplicationTimeout());
    }

    @Override
    protected Class<?> getCommunicationTags() {
        // TODO Auto-generated method stub
        return ApplicationTag.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleMsg(Enum receivedEnum) {
        // TODO Auto-generated method stub
        String bla = receivedEnum.name();
    }

}
