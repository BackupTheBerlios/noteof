package de.notEOF.core.event;

import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class TransportEvent extends NotEOFBaseEvent implements NotEOFEvent {
    public EventType EVENT_TYPE = EventType.EVENT_DEFAULT;

    public TransportEvent(EventType eventType, Map<String, String> attributes, Map<String, String> descriptions) {
        this.EVENT_TYPE = eventType;
        this.descriptions = descriptions;
        this.attributes = attributes;
    }

    @Override
    protected void initDescriptions() {
        // nothing to do for this implementation
    }
}
