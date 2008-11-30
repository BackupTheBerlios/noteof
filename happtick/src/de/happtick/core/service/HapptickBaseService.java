package de.happtick.core.service;

import de.happtick.core.MasterTable;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.service.BaseService;

/**
 * Basic class for Happtick Services.
 * <p>
 * This class mainly exists not to forget the implementationFirstSteps() and
 * implementationLastSteps(). <br>
 * This steps register and remove Happtick Services to or from the MasterTable.
 * <p>
 * If a derived class overwrites this methods it should call this both functions
 * by super.impl...()
 * 
 * @author Dirk
 * 
 */
public abstract class HapptickBaseService extends BaseService implements Service {

    public void implementationFirstSteps() {
        // register at master tables
        MasterTable.addService(this);
    }

    public void implementationLastSteps() {
        // deregister from master tables
        MasterTable.removeService(this);
    }
}
