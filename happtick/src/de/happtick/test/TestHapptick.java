package de.happtick.test;

import java.util.List;

import de.happtick.test.client.TestClient;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.DataObjectDataTypes;
import de.notEOF.core.enumeration.DataObjectListTypes;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;

public class TestHapptick {

    @SuppressWarnings("unchecked")
    public static void main(String... args) {

        try {
            NotEOFConfiguration conf1 = new ConfigurationClient("localhost", 3000, null);

            ConfigurationClient configClient = new ConfigurationClient();
            configClient.connect("localhost", 3000, null);
            // DataObject dataObject = configClient.getAttribute("serviceTypes",
            // "simpleName");

            // System.out.println("Datentyp: " + dataObject.getDataType());
            // System.out.println("Listentyp: " +
            // dataObject.getListObjectType());
            //
            // if (dataObject.getDataType() == DataObjectDataTypes.LIST) {
            // List<?> list = dataObject.getList();
            // if (dataObject.getListObjectType() == DataObjectListTypes.STRING)
            // {
            // for (String str : (List<String>) list) {
            // System.out.println("Wert: " + str);
            // }
            // }
            // }

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
