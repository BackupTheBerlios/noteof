package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that an action happened. <br>
 * Actions normally happen if a client has finished a step of it's work or the
 * client has fullfilled it's task.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>information -> Additional information(s) about the raised event.</>
 * <li>eventId -> Unique identifier which is maintained in the application. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ActionEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("information", "Additional information(s) about the raised event.");
        descriptions.put("eventId", "Unique identifier which is maintained in the application.");
    }

    protected void initEventType() {
        eventType = EventType.EVENT_ACTION;
    }

}
