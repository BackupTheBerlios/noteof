package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class ServiceStopEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("serviceId", "[Type: String] Is the service id of the service which has to be stopped.");
        descriptions.put("allServices", "If this is set to true all services have to stop immediately.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_SERVICE_STOP;
    }
}
