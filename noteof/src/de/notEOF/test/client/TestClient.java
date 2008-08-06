package de.notEOF.test.client;

import java.net.Socket;
import java.nio.CharBuffer;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.test.enumeration.TestTag;
import de.notEOF.test.service.TestService;

public class TestClient extends BaseClient {

    public TestClient(String ip, int port, TimeOut timeout, String[] args) throws ActionFailedException {
        super(ip, port, timeout, args);
        // TODO Auto-generated constructor stub
    }

    public TestClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Class<?> serviceClassForClient() {
        // TODO Auto-generated method stub
        return TestService.class;
    }

    public void blubb() throws ActionFailedException {
        String requestValue = requestTo(TestTag.REQ_AWAIT_DATA_OBJECT, TestTag.RESP_SEND_DATA_OBJECT);
        System.out.println("Request erfolgreich? " + requestValue);
        DataObject dataObject = receiveDataObject();
        System.out.println("Datentype: " + dataObject.getDataType());
        CharBuffer cbuf = CharBuffer.allocate(dataObject.getCharArrayValue().length);
        cbuf.put(dataObject.getCharArrayValue());
        System.out.println("Wert:      " + cbuf.toString());
    }
}
