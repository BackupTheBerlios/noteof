package de.notEOF.core.interfaces;

import java.util.List;

import de.notEOF.core.enumeration.EventType;

public interface EventObserver {

    /**
     * Callback method to inform the observer about incoming events.
     * 
     * @param service
     *            The service which fires the event. If there is a chain of
     *            services which fire the event her always the last of them is
     *            referenced. <br>
     *            The service can be NULL if the Observable is not of type
     *            Service!
     * @param event
     *            The incoming event that the client has fired or which was
     *            detected by the service.
     */
    public void update(Service service, NotEOFEvent event);

    /**
     * This function delivers a list with the Events for which the Observer is
     * interested in.
     * <p>
     * If the Observer doesn't initialized this list he get's no event.
     * 
     * @return A list with the Events which the observer wants to get.
     */
    public List<EventType> getObservedEvents();
}
