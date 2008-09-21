package de.happtick.test;

import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notIOC.configuration.ConfigurationManager;

public class TestHapptick {

    public static void main(String... args) {

        try {
            NotEOFConfiguration conf1 = new ConfigurationClient("localhost", 3000, null);
            NotEOFConfiguration conf2 = new LocalConfiguration();

            List<String> serviceTypes = conf1.getAttributeList("serviceTypes", "simpleName");
            if (null != serviceTypes) {
                for (String type : serviceTypes) {
                    System.out.println("conf: serviceType = " + type);
                }
            }

            ConfigurationManager.setInitialEnvironment("NOTEOF_HOME", "conf", "noteof_master.xml");
            System.out.println("bla... " + ConfigurationManager.getApplicationHome());
            serviceTypes = conf2.getAttributeList("serviceTypes", "simpleName");
            if (null != serviceTypes) {
                for (String type : serviceTypes) {
                    System.out.println("conf: serviceType = " + type);
                }
            }

            conf1.close();
            conf2.close();
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
