package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.ServiceChangeEvent;

public class NewServiceEvent implements ServiceChangeEvent {

    private Service service;

    public NewServiceEvent(Service service) {
        this.service = service;
    }

    public Service getService() {
        return this.service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public EventType getEventType() {
        return EventType.EVENT_SERVICE_CHANGE;
    }

}
