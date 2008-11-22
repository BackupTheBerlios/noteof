package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class EmptyEvent extends NotEOFBaseEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_EMPTY;

    @Override
    protected void initDescriptions() {
        descriptions.put("null", "null");
    }
}
