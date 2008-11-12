package de.notEOF.mail.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;

public interface MailAndEventRecipient {

    /**
     * Callback method to process received mails. You should synchronize this.
     * 
     * @param mail
     *            The incoming mail.
     */
    public void processMail(NotEOFMail mail);

    /**
     * Callback method if an exception is raised while receiving a mail.
     * 
     * @param e
     *            Any exception...
     */
    public void processMailException(Exception e);

    /**
     * Callback method to process received events. You should synchronize this.
     * 
     * @param event
     *            The incoming event.
     */
    public void processEvent(NotEOFEvent event);

    /**
     * Callback method if an exception is raised while receiving an event.
     * 
     * @param e
     *            Any exception...
     */
    public void processEventException(Exception e);

}
