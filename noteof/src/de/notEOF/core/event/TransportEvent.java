package de.notEOF.core.event;

import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class TransportEvent extends NotEOFEventBase implements NotEOFEvent {

    private EventType eventType;

    public TransportEvent(EventType eventType, Map<String, String> attributes, Map<String, String> descriptions) {
        this.eventType = eventType;
        this.descriptions = descriptions;
        this.attributes = attributes;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    protected void initDescriptions() {
        // nothing to do for this implementation
    }

}
