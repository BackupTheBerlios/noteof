package de.happtick.core.events;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell a chain that it has to stop now<br>
 * This event can be used e.g. for chain execution.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>chainId -> Unique identifier which is fix given by the happtick
 * configuration. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ChainStopEvent extends HapptickEvent implements NotEOFEvent {
    public final static EventType EVENT_TYPE = EventType.EVENT_CHAIN_STOP;

    protected void initDescriptions() {
        descriptions.put("chainId", "Unique identifier which is fix given by the happtick configuration.");
    }
}
