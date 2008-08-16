package de.notEOF.core.interfaces;

public interface EventObserver {
    /**
     * Callback method to inform the observer about incoming events.
     * 
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public void update(ClientEvent event);
}
