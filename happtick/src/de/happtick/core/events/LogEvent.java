package de.happtick.core.events;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... append an log entry to the logging mechanism. <br>
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>information -> Any Log-Text.</>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class LogEvent extends HapptickEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_LOG;

    protected void initDescriptions() {
        descriptions.put("information", "Any Log-Text.");
    }
}
