package de.notEOF.test;

import java.net.Socket;

import de.notEOF.application.client.ApplicationClient;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.dispatch.client.DispatchClient;
import de.notEOF.test.client.TestClient;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        try {
            TestClient testClient = new TestClient("127.0.0.1", 3000, null, args);
            testClient.blubb();
        } catch (ActionFailedException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        System.exit(0);

        Socket socketToApplService = null;
        try {
            BaseTimeOut baseTimeOut = new BaseTimeOut(0, 0);

            DispatchClient dispatchClient = new DispatchClient("127.0.0.1", 3000, baseTimeOut, args);
            // socketToApplService =
            // dispatchClient.getServiceConnection(ApplicationService
            // .class.getCanonicalName());
            socketToApplService = dispatchClient.getServiceConnection("de.bla.bla.SuperKlasse");
        } catch (ActionFailedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }

        ApplicationClient applClient = null;
        try {
            applClient = new ApplicationClient(socketToApplService, null, args);
            System.out.println(applClient.getServiceId());
        } catch (ActionFailedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("... und macht hier weiter... ");
        try {
            applClient.blabla();
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (applClient.isLinkedToService()) {
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.out.println("Fehler im Test... " + ex);
            }
        }
        try {
            applClient.close();
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
