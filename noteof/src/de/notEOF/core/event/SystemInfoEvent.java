package de.notEOF.core.event;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Event used to...
 * <p>
 * ... transport informations (states, statistics, whatever) which are
 * calculated or recognized by core classes.
 * <p>
 * The attributes are build by the scheme: Kind:Name <br>
 * Kind can be e.g. Counter, State <br>
 * Name is the name of the variable <br>
 * Sample: Counter:ActiveEventThreads
 * <p>
 * Attributes: <br>
 * <ul>
 * <li>Counter:SumEvents -> Number of events since server started. </>
 * <li>Counter:CompletedEvents -> Number of done events since server started.
 * </>
 * <li>Counter:ActiveEventThreads -> Active threads which feed observers. </>
 * <li>Counter:SumEventThreads -> Sum of all threads which feed(ed) observers.
 * </>
 * <li>Counter:CompletedEventThreads -> Finished threads for feeding observers.
 * </>
 * <li>State:LastEventProcessingTime -> Last time used for event processing. </>
 * <li>State:AvgEventProcessingTime -> Average time for event processing
 * (millis). </>
 * <li>State:MaxEventProcessingTime -> Slowest event processing (millis). </>
 * <li>Counter:SumServices -> Number of services since server started. </>
 * <li>Counter:ActiveServices -> Active services for clients. </>
 * <li>Counter:FinishedServices -> Stopped Services. </>
 * <li>Counter:Observers -> Observers for Events or Mails. </>
 * <li>State:DispatcherWaitTime -> Time the dispatcher event buffer waits till
 * he fires the next event (millis). </>
 * </ul>
 * 
 * @see NotEOFEvent
 * @author Dirk
 * 
 */
public class SystemInfoEvent extends NotEOFBaseEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("Counter:SumEvents", "Number of events since server started.");
        descriptions.put("Counter:CompletedEvents", "Number of done events since server started.");
        descriptions.put("Counter:ActiveEventThreads", "Active threads which feed observers.");
        descriptions.put("Counter:SumEventThreads", "Sum of all threads which feed(ed) observers.");
        descriptions.put("Counter:CompletedEventThreads", "Finished threads for feeding observers.");
        descriptions.put("State:LastEventProcessingTime", "Last time used for event processing.");
        descriptions.put("State:AvgEventProcessingTime", "Average time for event processing (millis).");
        descriptions.put("State:MaxEventProcessingTime", "Slowest event processing (millis).");
        descriptions.put("Counter:SumServices", "Number of services since server started.");
        descriptions.put("Counter:ActiveServices", "Active services for clients.");
        descriptions.put("Counter:FinishedServices", "Stopped Services.");
        descriptions.put("Counter:Observers", "Observers for Events or Mails.");
        descriptions.put("State:DispatcherWaitTime", "Time the dispatcher event buffer waits till he fires the next event (millis).");
    }

    @Override
    protected void initEventType() {
        eventType = EventType.EVENT_SYSTEM_INFO;
    }
}
