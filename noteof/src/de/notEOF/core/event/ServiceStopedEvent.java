package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class ServiceStopedEvent extends NotEOFBaseEvent implements NotEOFEvent {

    public EventType getEventType() {
        return EventType.EVENT_SERVICE_STOPPED;
    }

    @Override
    protected void initDescriptions() {
        descriptions.put("stopDate", "[Type: Date] Date when the service stopped. Milliseconds of Java.Date.");
        descriptions.put("serviceId", "[Type: String] Is the service id of the service which has stopped.");
    }
}
