package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class NewServiceEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("serviceId", "[Type: String] Is the service id of the service which was created.");
    }

    public EventType getEventType() {
        return EventType.EVENT_SERVICE_CHANGE;
    }

}
