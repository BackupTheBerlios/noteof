package de.notEOF.application.service;

import de.notEOF.application.client.ApplicationTimeOut;
import de.notEOF.application.enumeration.ApplicationTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.service.BaseService;

public class ApplicationService extends BaseService implements Service {

    public void init() throws ActionFailedException {
    }

    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processMsg(Enum receivedEnum) throws ActionFailedException {
        if (receivedEnum.equals(ApplicationTag.REQ_CONNECT_ADDITIONAL_NAME)) {
            responseTo(ApplicationTag.RESP_CONNECT_ADDITIONAL_NAME, "UIUIIUUI");
        }
    }

    protected TimeOut getTimeOutObject(TimeOut timeOut) {
        if (null != timeOut)
            return timeOut;
        return new ApplicationTimeOut();
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

}
