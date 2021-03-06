package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... stop an application.
 * <p>
 * This event is initialized by the central Happtick scheduler and can be sent
 * by any part of the framework.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>clientIp -> Ip where the application must be started.</>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ApplicationStopEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("workApplicationId", "Unique identifier for the application which has to be stopped. Fix set in happtick configuration.");
        descriptions.put("clientIp", "Ip where the application must be started.");
        descriptions.put("kill", "If kill = 'TRUE' the application should stop without do something else. Else the application can finish it's work.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_APPLICATION_STOP;
    }
}
