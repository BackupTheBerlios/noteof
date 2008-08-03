package de.notEOF.test;

import java.util.List;

import de.notEOF.application.client.ApplicationClient;
import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        ApplicationClient applClient = null;
        try {
            applClient = new ApplicationClient("127.0.0.1", 3000, null, args);
            System.out.println(applClient.getServiceId());
        } catch (ActionFailedException e) {
            throw new RuntimeException(e);
        }

        List<String> serviceTypesA = LocalConfigurationClient.getList("serviceTypes.[@type]");
        List<String> serviceTypesB = LocalConfigurationClient.getList("serviceTypes.[@maxClients]");
        List<String> serviceTypesC = LocalConfigurationClient.getList("serviceTypes.[@no]");
        List<String> serviceTypesD = LocalConfigurationClient.getList("serviceTypes");

        if (null != serviceTypesA) {
            for (String x : serviceTypesA) {
                System.out.println("Liste A: " + x);
            }
        }
        if (null != serviceTypesB) {
            for (String x : serviceTypesB) {
                System.out.println("Liste B: " + x);
            }
        }

        if (null != serviceTypesC) {
            for (String x : serviceTypesC) {
                System.out.println("Liste C: " + x);
            }
        }

        if (null != serviceTypesD) {
            for (String x : serviceTypesD) {
                System.out.println("Liste D: " + x);
            }
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
    }
}
