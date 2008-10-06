package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;

public class NewMailEvent implements NotEOFEvent {

    private NotEOFMail mail;

    public NewMailEvent(NotEOFMail mail) {
        this.mail = mail;
    }

    public EventType getEventType() {
        return EventType.EVENT_MAIL;
    }

    public NotEOFMail getMail() {
        return mail;
    }
}