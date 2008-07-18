package de.notEOF.application.service;

import java.net.Socket;

import de.notEOF.application.client.ApplicationTimeOut;
import de.notEOF.application.enumeration.ApplicationTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.TimeOut;
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
        System.out.println(bla);
        throw new ActionFailedException(1,"");
    }

    protected TimeOut getTimeOutObject(TimeOut timeOut) {
    	if (null != timeOut) return timeOut;
        return new ApplicationTimeOut();
    }
}
