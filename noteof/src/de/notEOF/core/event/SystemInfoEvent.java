package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... transport informations (states, statistics, whatever) which are
 * calculated or recognized by core classes.
 * <p>
 * The attributes are build by the scheme: Classname:Kind:Name <br>
 * Kind can be e.g. Counter, State <br>
 * Name is the name of the variable <br>
 * Sample: Util:Counter:ActiveEventThreads
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>Util:Counter:SumEvents -> Number of events since server started. </>
 * <li>Util:Counter:CompletedEvents -> Number of done events since server
 * started. </>
 * <li>Util:Counter:ActiveEventThreads -> Active threads which feed observers.
 * </>
 * <li>Util:Counter:SumEventThreads -> Sum of all threads which feed(ed)
 * observers. </>
 * <li>Util:Counter:CompletedEventThreads -> Finished threads for feeding
 * observers. </>
 * <li>Util:State:LastEventProcessingTime -> Last time used for event
 * processing. </>
 * <li>Util:State:MinEventProcessingTime -> Best time for processing an event
 * since server started (millis). </>
 * <li>Util:State:MaxEventProcessingTime -> Slowest event processing (millis).
 * </>
 * <li>Server:Counter:SumServices -> Number of services since server started.
 * </>
 * <li>Server:Counter:ActiveServices -> Active services for clients. </>
 * <li>Server:Counter:FinishedServices -> Stopped Services. </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class SystemInfoEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("Util:Counter:SumEvents", "Number of events since server started.");
        descriptions.put("Util:Counter:CompletedEvents", "Number of done events since server started.");
        descriptions.put("Util:Counter:ActiveEventThreads", "Active threads which feed observers.");
        descriptions.put("Util:Counter:SumEventThreads", "Sum of all threads which feed(ed) observers.");
        descriptions.put("Util:Counter:CompletedEventThreads", "Finished threads for feeding observers.");
        descriptions.put("Util:State:LastEventProcessingTime", "Last time used for event processing.");
        descriptions.put("Util:State:MinEventProcessingTime", "Best time for processing an event since server started (millis).");
        descriptions.put("Util:State:MaxEventProcessingTime", "Slowest event processing (millis).");
        descriptions.put("Server:Counter:SumServices", "Number of services since server started.");
        descriptions.put("Server:Counter:ActiveServices", "Active services for clients.");
        descriptions.put("Server:Counter:FinishedServices", "Stopped Services.");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_SYSTEM_INFO;
    }
}
