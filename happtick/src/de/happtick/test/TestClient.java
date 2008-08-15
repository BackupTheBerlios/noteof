package de.happtick.test;

import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;

public class TestClient  {

    public static void main(String...args) throws ActionFailedException {
    ConfigurationClient confClient = new ConfigurationClient("localhost", 3000, null, true, args);
    confClient.getConfigurationValue("test.element", "data");
    }
    
}
