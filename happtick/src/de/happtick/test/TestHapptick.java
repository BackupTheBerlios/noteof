package de.happtick.test;

import java.util.List;

import de.happtick.test.client.TestClient;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.BaseClientOrService;
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
            NotEOFConfiguration conf2 = new LocalConfiguration();

            List<String> serviceTypes = conf1.getTextList(xmlPath);

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
            conf1.close();
            // testClient.close();
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
