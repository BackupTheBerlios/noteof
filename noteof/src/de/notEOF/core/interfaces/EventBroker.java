package de.notEOF.core.interfaces;

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
     * @see EventObserver
     */
    public void registerForEvents(EventObserver eventObserver);

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
