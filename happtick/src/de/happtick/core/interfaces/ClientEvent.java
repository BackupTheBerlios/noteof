package de.happtick.core.interfaces;

import de.happtick.core.enumeration.EventType;

public interface ClientEvent {

    /**
     * Returns the event type.
     * 
     * @see EventType
     */
    public EventType getEventType();
}
