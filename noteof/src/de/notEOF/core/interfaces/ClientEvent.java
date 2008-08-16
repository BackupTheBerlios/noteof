package de.notEOF.core.interfaces;

import de.notEOF.core.enumeration.EventType;

public interface ClientEvent {

    /**
     * Returns the event type.
     * 
     * @see EventType
     */
    public EventType getEventType();
}
