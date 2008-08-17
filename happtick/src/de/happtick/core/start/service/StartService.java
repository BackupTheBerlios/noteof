package de.happtick.core.start.service;

import java.util.List;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;

public class StartService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void processMsg(Enum<?> arg0) throws ActionFailedException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<EventType> getObservedEvents() {
        // TODO Auto-generated method stub
        return null;
    }

}
