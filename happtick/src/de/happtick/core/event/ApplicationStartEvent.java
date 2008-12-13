package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... start an application.
 * <p>
 * This event is initialized by the central Happtick scheduler and sent by a
 * StartService to the StartClient.
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>clientIp -> Ip where the application must be started.</>
 * <li>applicationPath -> Physical path of the executable application. Normally
 * stored in the configuration.</>
 * <li>arguments -> All arguments and their values are transported within this
 * one string.</>
 * <li>applicationType -> The type of application. Options are 'INTERNAL' or
 * 'EXTERNAL'. </>
 * <li>windowsSupport -> Option to automatically support Windows start
 * scripts.</>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ApplicationStartEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("clientIp", "Ip where the application must be started.");
        descriptions.put("workApplicationId", "Unique identifier for the application which has to be started. Fix set in happtick configuration.");
        descriptions.put("applicationPath", "Physical path of the executable application. Normally stored in the configuration.");
        descriptions.put("arguments", "All arguments and their values are transported within this one string.");
        descriptions.put("applicationType", "The type of application. Options are 'INTERNAL' or 'EXTERNAL'.");
        descriptions.put("windowsSupport", "Option to automatically support Windows start scripts.");
    }

    protected void initEventType() {
        this.eventType = EventType.EVENT_APPLICATION_START;
    }
}
