package de.notEOF.core.interfaces;

import java.util.Date;

/**
 * Interface to fire event when any process has started.
 * 
 * @author Dirk
 * 
 */
public interface StartEvent extends NotEOFEvent {

    public Date getStartDate();
}
