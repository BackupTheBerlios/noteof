package de.notEOF.application.service;

import java.net.Socket;

import de.notEOF.application.client.ApplicationTimeout;
import de.notEOF.application.enumeration.ApplicationTag;
import de.notEOF.core.communication.BaseTimeout;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.service.BaseService;

public class ApplicationService extends BaseService implements Service{

    public void init (Socket socketToClient, String serviceId) throws ActionFailedException {
        super.init(socketToClient, serviceId);
    }

    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processMsg(Enum receivedEnum) throws ActionFailedException {
        String bla = receivedEnum.name();
        throw new ActionFailedException(1,"");
    }

    protected BaseTimeout getTimeOutObject() {
        return new ApplicationTimeout();
    }
}
