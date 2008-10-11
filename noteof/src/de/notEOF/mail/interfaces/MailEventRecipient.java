package de.notEOF.mail.interfaces;

import de.notEOF.mail.NotEOFMail;

public interface MailEventRecipient {

    public void processMail(NotEOFMail mail);

    public void processMailException(Exception e);

}
