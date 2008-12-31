package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell scheduler if a StartClient has started or stopped.
 * <p>
 * This event is initialized by the class StartClient.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>clientIp -> Ip where the application must be started.</>
 * <li>state -> START or STOP</>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class InternalClientStarterEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("clientIp", "Ip where the StartClient runs.");
        descriptions.put("state", "START or STOP.");
    }

    protected void initEventType() {
        this.eventType = EventType.INTERNAL_CLIENT_STARTER_EVENT;
    }
}
