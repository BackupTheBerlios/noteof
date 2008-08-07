package de.notEOF.test.service;

import java.io.File;

import de.notEOF.configuration.client.LocalConfigurationClient;
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

            // String testString = "abcdef";

            File testFile = new File(LocalConfigurationClient.getApplicationHome() + "/conf/noteof_services.xml");
            dataObject.setFile(testFile);
            // char[] fileChars = new char[(int) testFile.length()];
            // try {
            // BufferedReader bReader = new BufferedReader(new
            // FileReader(testFile));
            //
            // bReader.read(fileChars);
            // dataObject.setCharArray(fileChars);
            // } catch (FileNotFoundException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            // char[] characters = new char[testString.length()];
            // for (int i = 0; i < testString.length(); i++) {
            // characters[i] = testString.charAt(i);
            // }
            //
            // for (int i = 0; i < testString.length(); i++) {
            // System.out.println("char... " + characters[i]);
            // }
            // dataObject.setCharArray(characters);
            sendDataObject(dataObject);
            System.out.println("Objekt gesendet.");
        }
    }
}
