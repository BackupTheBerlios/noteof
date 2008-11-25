package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class GenericEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("null", "null");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_GENERIC;
    }
}
