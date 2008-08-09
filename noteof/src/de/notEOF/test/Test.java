package de.notEOF.test;

import de.notEOF.application.client.ApplicationClient;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.communication.SimpleSocketData;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.distribute.client.DistributedClient;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        try {
            ConfigurationClient cfgClient = new ConfigurationClient("127.0.0.1", 3000, null, true, args);
            DataObject confObject = cfgClient.getConfigurationValue("serviceTypes");
            System.out.println("Type = " + confObject.getDataType());
            System.out.println("Wert = " + confObject.getConfigurationValue());

        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.exit(0);

        // try {
        // TestClient testClient = new TestClient("127.0.0.1", 3000, null,
        // args);
        // testClient.blubb();
        // } catch (ActionFailedException e2) {
        // e2.printStackTrace();
        // }
        // System.exit(0);

        try {
            ApplicationClient client = new ApplicationClient(true, args);
            SimpleSocketData socketData = new SimpleSocketData("127.0.0.1", 3000);
            @SuppressWarnings("unused")
            DistributedClient distClient = new DistributedClient(client, socketData, null);

            System.out.println("ServiceId 1 = " + client.getServiceId());
            while (client.isLinkedToService()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    System.out.println("Fehler im Test... " + ex);
                }
            }
        } catch (ActionFailedException afx) {
            afx.printStackTrace();
        }

        // Socket socketToApplService = null;
        // try {
        // BaseTimeOut baseTimeOut = new BaseTimeOut(1000, 60000);
        //
        // DispatchClient dispatchClient = new DispatchClient("127.0.0.1", 3000,
        // baseTimeOut, false, args);
        // // socketToApplService =
        // // dispatchClient.getServiceConnection(ApplicationService
        // // .class.getCanonicalName());
        // socketToApplService = dispatchClient.getServiceConnection(
        // "de.notEOF.application.service.ApplicationServiceXXX", 0);
        // // socketToApplService = dispatchClient.getServiceConnection(
        // // "de.notEOF.application.service.ApplicationServiceXXX", 30000);
        // } catch (ActionFailedException e1) {
        // e1.printStackTrace();
        // return;
        // }
        //

        // ApplicationClient applClient = null;
        // try {
        // applClient = new ApplicationClient(socketToApplService, null, true,
        // args);
        // System.out.println("ServiceId 2 = " + applClient.getServiceId());
        // } catch (ActionFailedException e) {
        // throw new RuntimeException(e);
        // }
        //
        // System.out.println("... und macht hier weiter... ");
        // try {
        // applClient.blabla();
        // } catch (ActionFailedException e) {
        // e.printStackTrace();
        // }
        // while (applClient.isLinkedToService()) {
        // try {
        // Thread.sleep(1000);
        // } catch (Exception ex) {
        // System.out.println("Fehler im Test... " + ex);
        // }
        // }
        // try {
        // applClient.close();
        // } catch (ActionFailedException e) {
        // e.printStackTrace();
        // }
    }
}
