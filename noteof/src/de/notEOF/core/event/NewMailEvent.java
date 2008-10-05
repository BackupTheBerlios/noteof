package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class NewMailEvent implements NotEOFEvent {

    private String destination;
    private String mailId;

    public NewMailEvent(String mailId, String destination) {
        this.destination = destination;
        this.mailId = mailId;
    }

    public EventType getEventType() {
        return EventType.EVENT_NEW_MSG;
    }

    public String getDestination() {
        return destination;
    }

    public String getMailId() {
        return mailId;
    }
}
