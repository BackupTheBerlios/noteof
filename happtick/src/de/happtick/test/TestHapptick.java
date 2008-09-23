package de.happtick.test;

import java.util.List;

import de.happtick.application.client.ApplicationTimeOut;
import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.client.HapptickConfigurationClient;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notIOC.configuration.ConfigurationManager;

public class TestHapptick {

    public static void main(String... args) {

        try {
            // Umgebungsvariablen sind durch Server-Start-Einstellungen bekannt
            NotEOFConfiguration conf1 = new ConfigurationClient("localhost", 3000, null);
            NotEOFConfiguration conf2 = new LocalConfiguration();

            List<String> serviceTypes = conf1.getAttributeList("serviceTypes", "simpleName");
            if (null != serviceTypes) {
                for (String type : serviceTypes) {
                    System.out.println("conf: serviceType = " + type);
                }
            }

            // Für lokale Konfiguration Umgebungsvariablen festlegen
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

            HapptickConfigurationClient hConfClient = new HapptickConfigurationClient("localhost", 3000, new ApplicationTimeOut());
            List<ApplicationConfiguration> list = hConfClient.getApplicationConfigurations();
            if (null != list && list.size() > 0) {
                for (ApplicationConfiguration appConf : list) {
                    System.out.println("appConf... " + appConf.getNodeNameApplication());
                }
            }
            hConfClient.close();

        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
