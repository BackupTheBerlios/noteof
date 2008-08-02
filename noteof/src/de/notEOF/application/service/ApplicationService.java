package de.notEOF.application.service;

import java.net.Socket;

import de.notEOF.application.client.ApplicationTimeOut;
import de.notEOF.application.enumeration.ApplicationTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.service.BaseService;

public class ApplicationService extends BaseService implements Service {

    public void init(Socket socketToClient, String serviceId) throws ActionFailedException {
        super.init(socketToClient, serviceId);
        System.out.println("ApplicationService init activate...");
        super.activateLifeSignSystem();
    }

    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processMsg(Enum receivedEnum) throws ActionFailedException {
        System.out.println("ApplicationService empfangenes Enum: " + receivedEnum.name());

        if (receivedEnum.equals(ApplicationTag.REQ_CONNECT_ADDITIONAL_NAME)) {
            System.out.println("Treffer");
            responseTo(ApplicationTag.RESP_CONNECT_ADDITIONAL_NAME, "UIUIIUUI");
        }
    }

    protected TimeOut getTimeOutObject(TimeOut timeOut) {
        if (null != timeOut)
            return timeOut;
        return new ApplicationTimeOut();
    }
}
