package de.happtick.core.events;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that an alarm has raised. <br>
 * Alarms are indications of the application clients that something unexptected
 * happened.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>description -> Exact description of the Alarm. </>
 * <li>type -> Numeric value which indicates the kind of the alarm. </>
 * <li>level -> Numeric value which indicates the level of the alarm. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class AlarmEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("description", "Exact description of the Alarm.");
        descriptions.put("type", "Numeric value which indicates the kind of the alarm.");
        descriptions.put("level", "Numeric value which indicates the level of the alarm.");
    }

    public EventType getEventType() {
        return EventType.EVENT_ALARM;
    }
}
