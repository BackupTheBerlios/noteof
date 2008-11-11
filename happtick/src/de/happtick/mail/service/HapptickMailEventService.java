package de.happtick.mail.service;

import de.happtick.core.MasterTable;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.service.MailAndEventReceiveService;

/**
 * Dummy class to simplify the handling...
 * 
 * @author Dirk
 * 
 */
public class HapptickMailEventService extends MailAndEventReceiveService {

    @Override
    public void implementationFirstSteps() throws ActionFailedException {
        MasterTable.addService(this);
    }

    @Override
    public void implementationLastSteps() throws ActionFailedException {
        MasterTable.removeService(this);
    }
}
