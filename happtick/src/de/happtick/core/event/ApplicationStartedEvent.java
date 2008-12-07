package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that the client has been started. <br>
 * This event can be used e.g. for chain execution or if the start of another
 * service depends to the start of the client which raises the start event.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>clientNetId -> Unique identifier of the client during the complete
 * !NotEOF system is running. This id is generated when the client was started.
 * </>
 * <li>startId -> Identifier which the client gets by the process which started
 * the client. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ApplicationStartedEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("workApplicationId", "Unique identifier for the application which has to be started. Fix set in happtick configuration.");
        descriptions.put("clientNetId",
                         "Unique identifier of the client during the complete !NotEOF system is running. This id is generated when the client was started.");
        descriptions.put("startId", "Identifier which the client gets by the process which started the client.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_APPLICATION_STARTED;
    }
}
