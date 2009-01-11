package de.notEOF.core.interfaces;

public interface EventObservable {
    /**
     * This method enables the server to inform the services about events or
     * changes of the system.
     * 
     * @param eventObserver
     *            One or more Observers can register them here. This observers
     *            will be informed for events at a later moment.
     */
    public void registerForEvents(EventObserver eventObserver);

    /**
     * This method enables the server to inform the services about events or
     * changes of the system.
     * 
     * @param eventObserver
     *            One or more Observers can register them here. This observers
     *            will be informed for events at a later moment.
     * @param lastReceivedQueueId
     *            Is the queue id of the last received event.
     */
    public void registerForEvents(EventObserver eventObserver, Long lastReceivedQueueId);
}
