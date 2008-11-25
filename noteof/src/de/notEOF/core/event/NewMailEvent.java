package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;

public class NewMailEvent extends NotEOFBaseEvent implements NotEOFEvent {

    private NotEOFMail mail;

    public NewMailEvent(NotEOFMail mail) {
        this.mail = mail;
    }

    public NotEOFMail getMail() {
        return mail;
    }

    /**
     * The NewMailEvent is an exception of other events!
     */
    protected void initDescriptions() {
        descriptions.put("", "");
    }

    @Override
    protected void initEventType() {
        this.eventType = EventType.EVENT_MAIL;
    }
}
