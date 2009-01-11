package de.notEOF.core.interfaces;

import de.notEOF.core.brokerage.EventQueue;

public interface EventBroker {

    /**
     * Used to let the broker accept a new incoming event.
     * 
     * @param service
     *            The service which sends the event. If the calling class is not
     *            of type Service this value may be NULL.
     * @param event
     *            The posted / raised / fired new event.
     * @see Service
     * @see NotEOFEvent
     */
    public void postEvent(Service service, NotEOFEvent event);

    /**
     * Used to register an observer to the broker for receiving events.
     * 
     * @param eventObserver
     *            The observer which must implement the Interface EventObserver.
     * @param lastReceivedQueueId
     *            Is the last received queue id. If this object is not null and
     *            the long value is >= 0 the broker should try to find the next
     *            message with the following queue id. This option is nice to
     *            have. There is no guarantee that the broker supports this
     *            feature. At the moment the Implementation {@link EventQueue}
     *            supports it.
     * @see EventObserver
     */
    public void registerForEvents(EventObserver eventObserver, Long lastReceivedQueueId);

    /**
     * Used to unregister an observer from the broker.
     * 
     * @param eventObserver
     *            The observer which must implement the Interface EventObserver
     *            and normally has registered before.
     * @see EventObserver
     */
    public void unregisterFromEvents(EventObserver eventObserver);
}
