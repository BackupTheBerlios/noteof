package de.notEOF.core.interfaces;

import java.util.Date;

import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Interface for fire an event when any process stopped.
 * 
 * @author Dirk
 * 
 */
public interface StopEvent extends NotEOFEvent {
    public Date getStopDate();

    public String getServiceId();
}
