package de.notEOF.client.test;

import de.notEOF.application.client.ApplicationClient;
import de.notEOF.core.exception.ActionFailedException;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");
        
        try {
			ApplicationClient applClient = new ApplicationClient("127.0.0.1", 2512, null, args);
			System.out.println(applClient.getServiceId());
		} catch (ActionFailedException e) {
			throw new RuntimeException(e);
		}
        
    }

}
