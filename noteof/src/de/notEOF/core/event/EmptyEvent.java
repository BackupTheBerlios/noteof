package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class EmptyEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("null", "null");
    }

    protected void initEventType() {
        this.eventType = EventType.EVENT_EMPTY;
    }
}
