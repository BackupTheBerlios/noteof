package de.happtick.core.start.client;

import de.notEOF.core.client.BaseClient;

public class StartClient extends BaseClient {

    /*
     * suffix will sent by service later. Makes the startId's unique in the
     * whole system.
     */
    @SuppressWarnings("unused")
    private String startIdSuffix;

    @Override
    public Class<?> serviceForClientByClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serviceForClientByName() {
        return "de.happtick.core.start.service.StartService";
    }
}
