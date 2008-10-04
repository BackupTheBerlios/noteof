package de.happtick.test.service;

import java.util.List;

import de.happtick.test.enumeration.TestTag;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.service.BaseService;

public class TestService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        // TODO Auto-generated method stub
        return TestTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void processClientMsg(Enum<?> arg0) throws ActionFailedException {
        System.out.println("processMsg");
        List<Service> services = getServiceListByTypeName("ConfigurationService");
        if (null != services) {
            for (Service service : services) {
                System.out.println("Service id = " + service.getServiceId());
            }
        }
    }

    public List<EventType> getObservedEvents() {
        // TODO Auto-generated method stub
        return null;
    }
}
