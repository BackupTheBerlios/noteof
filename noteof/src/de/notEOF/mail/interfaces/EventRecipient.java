package de.notEOF.mail.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.mail.NotEOFMail;

public interface EventRecipient {

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
     * @param exception
     *            Any exception...
     */
    public void processMailException(Exception exception);

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
     * @param exception
     *            Any exception...
     */
    public void processEventException(Exception exception);

    /**
     * Callback method if this application has to be stopped.
     * 
     * @param event
     */
    public void processStopEvent(NotEOFEvent event);

}
