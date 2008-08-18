package de.happtick.core.start.service;

import java.util.List;

import de.happtick.core.MasterTable;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;

public class StartService extends BaseService {

    private String clientIp;

    /**
     * This method is called by BaseService directly when the connection with
     * client is established.
     */
    public void implementationFirstSteps() {
        // register at master tables
        MasterTable.addService(this);
    }

    public void implementationLastSteps() {
        // deregister from master tables
        MasterTable.removeService(this);
    }

    public String getClientIp() {
        return this.clientIp;
    }

    @Override
    public Class<?> getCommunicationTagClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void processMsg(Enum<?> arg0) throws ActionFailedException {
        // TODO Auto-generated method stub

        // anmelden an MasterTable
        // nach initialisierung durch client...
    }

    public List<EventType> getObservedEvents() {
        // TODO Auto-generated method stub
        return null;
    }

}
