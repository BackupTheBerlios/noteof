package de.notEOF.core.interfaces;

import java.util.List;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;

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
     * @throws ActionFailedException
     */
    public void update(Service service, NotEOFEvent event) throws ActionFailedException;

    /**
     * This function delivers a list with the Events for which the Observer is
     * interested in.
     * <p>
     * If the Observer doesn't initialized this list he get's no event.
     * 
     * @return A list with the Events which the observer wants to get.
     */
    public List<EventType> getObservedEvents();

    /**
     * The name of the observer (needed e.g. for error logging).
     * <p>
     * Can be the name of a service, it's serviceId or something else.
     * 
     * @return A name which is clearly enough to identify the observer.
     */
    public String getName();
}
