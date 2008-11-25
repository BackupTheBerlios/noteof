package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class ServiceStoppedEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("stopDate", "[Type: Date] Date when the service stopped. Milliseconds of Java.Date.");
        descriptions.put("serviceId", "[Type: String] Is the service id of the service which has stopped.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_SERVICE_STOPPED;
    }
}
