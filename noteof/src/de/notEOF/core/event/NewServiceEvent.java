package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class NewServiceEvent extends NotEOFBaseEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_SERVICE_CHANGE;

    @Override
    protected void initDescriptions() {
        descriptions.put("serviceId", "[Type: String] Is the service id of the service which was created.");
    }
}
