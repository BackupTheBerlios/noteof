package de.notEOF.core.interfaces;

import de.notEOF.core.enumeration.EventType;

public interface NotEOFEvent {

    /**
     * Returns the event type.
     * 
     * @see EventType
     */
    public EventType getEventType();
}
