package de.happtick.test;

import de.happtick.test.client.TestClient;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;

public class TestHapptick {

    public static void main(String... args) {

        try {
            ConfigurationClient configClient = new ConfigurationClient();
            configClient.connect("localhost", 3000, null);
            DataObject dataObject = configClient.getConfigurationObject("serviceTypes", "simpleName");
            System.out.println("Erhalten: " + dataObject.getLine());

            TestClient testClient = new TestClient();
            testClient.connect("localhost", 3000, null);
            testClient.triggerService();

            System.out.println("Programm ist hier eigentlich zu ende....");

            configClient.close();
            // testClient.close();
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
