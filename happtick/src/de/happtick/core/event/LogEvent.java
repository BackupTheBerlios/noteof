package de.happtick.core.event;

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

    protected void initDescriptions() {
        descriptions.put("information", "Any Log-Text.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_LOG;
    }
}
