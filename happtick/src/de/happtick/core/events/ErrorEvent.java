package de.happtick.core.events;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that an error has occured while the
 * application was running. <br>
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>description -> Exact description of the Alarm. </>
 * <li>errorId -> Unique identifier which is maintained in the application. </>
 * <li>level -> Numeric value which indicates the level of the error. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ErrorEvent extends HapptickEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_ERROR;

    protected void initDescriptions() {
        descriptions.put("description", "Exact description of the Alarm.");
        descriptions.put("errorId", "Unique identifier which is maintained in the application.");
        descriptions.put("level", "Numeric value which indicates the level of the error.");
    }
}
