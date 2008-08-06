package de.notEOF.test.service;

import java.nio.CharBuffer;

import de.notEOF.core.communication.DataObject;
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

            char[] characters = new char[18];
            CharBuffer cbuf = CharBuffer.allocate(36);
            cbuf.put("Hottelditotteldidö");
            cbuf.get(characters, 0, 18);

            dataObject.setCharArrayValue(characters);
            sendDataObject(dataObject);
            System.out.println("Objekt gesendet.");
        }
    }
}
