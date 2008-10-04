package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class NewMailEvent implements NotEOFEvent {

    private String toServiceId;
    private String destination;
    private String mailId;

    public NewMailEvent(String mailId, String toServiceId, String destination) {
        this.toServiceId = toServiceId;
        this.destination = destination;
        this.mailId = mailId;
    }

    public EventType getEventType() {
        return EventType.EVENT_NEW_MSG;
    }

    public String getToServiceId() {
        return toServiceId;
    }

    public String getDestination() {
        return destination;
    }

    public String getMailId() {
        return mailId;
    }
}
