package de.happtick.core.events;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that the client has stopped or is stopping
 * now. <br>
 * This event can be used e.g. for chain execution or if the start of another
 * service depends to the stop of the client which raises the stop event.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>serviceId -> Id of the service. Probably not set.</>
 * <li>applicationId -> Unique identifier which is fix given by the happtick
 * configuration. </>
 * <li>clientNetId -> Unique identifier of the client during the complete
 * !NotEOF system is running. This id is generated when the client was started.
 * </>
 * <li>startId -> Identifier which the client gets by the process which started
 * the client. </>
 * <li>exitCode -> Exit code which the application fires. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class StopEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("applicationId", "Unique identifier which is fix given by the happtick configuration.");
        descriptions.put("clientNetId",
                         "Unique identifier of the client during the complete !NotEOF system is running. This id is generated when the client was started.");
        descriptions.put("startId", "Identifier which the client gets by the process which started the client.");
        descriptions.put("exitCode", "Exit code which the application fires.");
        descriptions.put("serviceId", "Id of the service. Probably not set.");
    }

    public EventType getEventType() {
        return EventType.EVENT_STOP;
    }
}
