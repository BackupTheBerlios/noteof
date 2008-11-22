package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;

public class NewMailEvent extends NotEOFBaseEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_MAIL;

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
}
