package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class EmptyEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("null", "null");
    }

    public EventType getEventType() {
        return EventType.EVENT_EMPTY;
    }

}
