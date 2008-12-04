package de.happtick.core.service;

import de.happtick.core.MasterTable;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.service.SimpleService;

public class HapptickSimpleService extends SimpleService {

    public void implementationFirstSteps() throws HapptickException {
        try {
            super.implementationFirstSteps();
        } catch (ActionFailedException e) {
            throw new HapptickException(60L, e);
        }
        super.addObservedEvent(EventType.EVENT_START_CLIENT);
        getServer().registerForEvents(this);
    }

    /**
     * The SimpleService is a good place for internal event processing...
     * <p>
     */
    public synchronized void processEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        if (event.equals(EventType.EVENT_START_CLIENT)) {
            MasterTable.updateStartClientEvent(event);
        }
    }
}
