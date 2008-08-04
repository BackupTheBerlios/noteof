package de.notEOF.test;

import java.net.Socket;

import de.notEOF.application.client.ApplicationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.dispatch.client.DispatchClient;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        Socket socketToApplClient = null;
        try {
            DispatchClient dispatchClient = new DispatchClient("127.0.0.1", 3000, null, args);
            socketToApplClient = dispatchClient.getServiceConnection("ApplicationService");
        } catch (ActionFailedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        ApplicationClient applClient = null;
        try {
            applClient = new ApplicationClient(socketToApplClient, null, args);
            System.out.println(applClient.getServiceId());
        } catch (ActionFailedException e) {
            throw new RuntimeException(e);
        }

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
