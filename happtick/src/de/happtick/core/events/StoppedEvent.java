package de.happtick.core.events;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that the client has been stopped or is
 * stopping now. <br>
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
 * <li>exitCode -> Result of a process. Normally 0. Used for raising actions.
 * </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class StoppedEvent extends HapptickEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_APPLICATION_STOPPED;

    protected void initDescriptions() {
        descriptions.put("applicationId", "Unique identifier which is fix given by the happtick configuration.");
        descriptions.put("clientNetId",
                         "Unique identifier of the client during the complete !NotEOF system is running. This id is generated when the client was started.");
        descriptions.put("startId", "Identifier which the client gets by the process which started the client.");
        descriptions.put("serviceId", "Id of the service. Probably not set.");
        descriptions.put("exitCode", "Result of a process. Normally 0. Used for raising actions.");
    }
}
