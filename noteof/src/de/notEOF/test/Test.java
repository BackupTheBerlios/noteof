package de.notEOF.test;

import de.notEOF.application.client.ApplicationClient;
import de.notEOF.core.exception.ActionFailedException;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        ApplicationClient applClient = null;
        try {
            applClient = new ApplicationClient("127.0.0.1", 2512, null, args);
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
    }
}
