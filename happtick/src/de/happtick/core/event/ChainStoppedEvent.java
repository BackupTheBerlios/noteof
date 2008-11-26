package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that a chain has been stopped or is
 * stopping now. <br>
 * This event can be used e.g. for chain execution or if the start of another
 * service depends to the stop of the chain which raised this stop event.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>chainId -> Unique identifier which is fix given by the happtick
 * configuration. </>
 * <li>exitCode -> Result of a process. Normally 0. Used for raising actions.
 * </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ChainStoppedEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("chainId", "Unique identifier which is fix given by the happtick configuration.");
        descriptions.put("exitCode", "Result of a process. Normally 0. Used for raising actions.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_CHAIN_STOPPED;
    }
}
