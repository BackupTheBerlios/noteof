package de.notEOF.core.event;

import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class TransportEvent extends NotEOFBaseEvent implements NotEOFEvent {

    public TransportEvent(EventType eventType, Map<String, String> attributes, Map<String, String> descriptions) {
        this.eventType = eventType;
        this.descriptions = descriptions;
        this.attributes = attributes;
    }

    @Override
    protected void initDescriptions() {
        // nothing to do for this implementation
    }

    @Override
    protected void initEventType() {
        // nothing to do for this implementation
    }
}
