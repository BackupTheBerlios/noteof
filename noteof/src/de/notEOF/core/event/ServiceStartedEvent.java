package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class ServiceStartedEvent extends NotEOFBaseEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_SERVICE_STARTED;

    protected void initDescriptions() {
        descriptions.put("startDate", "[Type: Date] Date when the service started. Milliseconds of Java.Date.");
        descriptions.put("serviceId", "[Type: String] Is the service id of the service which has started.");
    }
}
