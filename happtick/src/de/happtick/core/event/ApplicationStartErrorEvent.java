package de.happtick.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... tell other services (clients) that the client couldn't been started. <br>
 * Attributes: <br>
 * <ul>
 * <li>applicationId -> Unique identifier which is fix given by the happtick
 * configuration. </>
 * <li>clientNetId -> Unique identifier of the client during the complete
 * !NotEOF system is running. This id is generated when the client was started.
 * </>
 * <li>startId -> Identifier which the client gets by the process which started
 * the client. </>
 * <li>errorDescription -> Exact description of the Error.</li>
 * <li>errorId -> Id specified by implementations.</li>
 * <li>errorLevel -> Numeric value which indicates the level of the error.</li>
 * <li>startIgnitionTime -> Timepoint when the application start was initialized
 * in Millis like java.util.Date.</>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class ApplicationStartErrorEvent extends HapptickEvent implements NotEOFEvent {

    protected void initDescriptions() {
        descriptions.put("clientNetId",
                         "Unique identifier of the client during the complete !NotEOF system is running. This id is generated when the client was started.");
        descriptions.put("workApplicationId", "Unique identifier for the application which should to be started. Fix set in happtick configuration.");
        descriptions.put("startId", "Identifier which the client gets by the process which started the client.");
        descriptions.put("errorDescription", "Exact description of the Error.");
        descriptions.put("errorId", "Id specified by implementations.");
        descriptions.put("errorLevel", "Numeric value which indicates the level of the error.");
        descriptions.put("startIgnitionTime", "Timepoint when the application start was initialized in Millis like java.util.Date.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_START_ERROR;
    }
}
