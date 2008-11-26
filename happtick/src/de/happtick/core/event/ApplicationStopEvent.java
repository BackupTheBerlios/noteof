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
 * <li>applicationId -> Unique identifier which is fix given by the happtick
 * configuration. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ApplicationStopEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("clientIp", "Ip where the application must be started.");
        descriptions.put("applicationId", "Unique identifier which is fix given by the happtick configuration.");
        descriptions.put("kill", "If kill = 'TRUE' the application should stop without do something else. Else the application can finish it's work.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_APPLICATION_START;
    }
}
