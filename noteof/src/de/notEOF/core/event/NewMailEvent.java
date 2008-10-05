package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class NewMailEvent implements NotEOFEvent {

    private String toClientNetId;
    private String destination;
    private String header;
    private String mailId;

    public NewMailEvent(String mailId, String toClientNetId, String destination, String header) {
        this.toClientNetId = toClientNetId;
        this.destination = destination;
        this.header = header;
        this.mailId = mailId;
    }

    public EventType getEventType() {
        return EventType.EVENT_MAIL;
    }

    public String getDestination() {
        return destination;
    }

    public String getMailId() {
        return mailId;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @return the toClientNetId
     */
    public String getToClientNetId() {
        return toClientNetId;
    }
}
