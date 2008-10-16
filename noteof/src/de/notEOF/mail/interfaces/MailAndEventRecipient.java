package de.notEOF.mail.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;

public interface MailAndEventRecipient {

    public void processMail(NotEOFMail mail);

    public void processMailException(Exception e);

    public void processEvent(NotEOFEvent event);

    public void processEventException(Exception e);

}
