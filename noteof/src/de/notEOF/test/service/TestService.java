package de.notEOF.test.service;

import java.io.File;
import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.service.BaseService;
import de.notEOF.test.enumeration.TestTag;

public class TestService extends BaseService implements Service {

    @Override
    public Class<?> getCommunicationTagClass() {
        // TODO Auto-generated method stub
        return TestTag.class;
    }

    @Override
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        // TODO Auto-generated method stub
        if (incomingMsgEnum.equals(TestTag.REQ_AWAIT_DATA_OBJECT)) {
            responseTo(TestTag.RESP_SEND_DATA_OBJECT, "ok");
            System.out.println("geantwortet...");
            DataObject dataObject = new DataObject();

            // String testString = "abcdef";

            File testFile = new File(LocalConfiguration.getApplicationHome() + "/conf/noteof_services.xml");
            dataObject.setFile(testFile);
            sendDataObject(dataObject);
            System.out.println("Objekt gesendet.");
        }
    }

    @Override
    public boolean isLifeSignSystemActive() {
        // TODO Auto-generated method stub
        return false;
    }

    public List<EventType> getObservedEvents() { 
        // TODO Auto-generated method stub
        return null;
    }
}
