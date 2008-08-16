package de.happtick.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.events.ApplicationStopEvent;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.ServiceStopEvent;
import de.notEOF.core.interfaces.EventObservable;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.util.Util;

/**
 * This static class contains two lists: <br>
 * 1. List of all ApplicationConfigurations <br>
 * 2. List of all active ApplicationServices (= active processes)
 * <p>
 * The services and other objects of happtick use this class to get informations
 * about applications and processes.
 * 
 * @author Dirk
 * 
 */
public class MasterTable implements EventObservable, EventObserver {

    private static Map<Long, ApplicationConfiguration> applicationConfigurations = new HashMap<Long, ApplicationConfiguration>();
    private static Map<String, ApplicationService> applicationServices = new HashMap<String, ApplicationService>();
    private static List<EventObserver> eventObservers = new ArrayList<EventObserver>();

    private static boolean inAction = false;

    public synchronized static Map<Long, ApplicationConfiguration> getApplicationConfigurations() {
        return applicationConfigurations;
    }

    public synchronized static Map<String, ApplicationService> getApplicationServices() {
        return applicationServices;
    }

    public synchronized static void addApplicationService(ApplicationService service) {
        while (inAction)
            ;
        inAction = true;
        applicationServices.put(service.getServiceId(), service);
        inAction = false;
    }

    public synchronized static void removeApplicationService(String serviceId) {
        while (inAction)
            ;
        inAction = true;
        ApplicationService service = applicationServices.get(serviceId);
        Long applicationId = service.getApplicationId();
        ApplicationStopEvent stopEvent = (ApplicationStopEvent) service.getLastEvent(EventType.EVENT_STOP);
        if (null == stopEvent) {
            stopEvent = new ApplicationStopEvent(serviceId, applicationId, 0);
        }
        applicationServices.remove(serviceId);
        Util.updateAllObserver(eventObservers, null, stopEvent);
    }

    /**
     * To observe what happens with the services (the clients) here one or more
     * observer of type EventObserver can register themself.
     * <p>
     * Whether a service or an extended class of type service really fires
     * events and which events are fired depends to the single business logic.
     * 
     * @param eventObserver
     *            The registered EventObservers.
     */
    @Override
    public void registerForEvents(EventObserver eventObserver) {
        eventObservers.add(eventObserver);
    }

    @Override
    public synchronized List<EventType> getObservedEvents() {
        List<EventType> observedEvents = new ArrayList<EventType>();
        observedEvents.add(EventType.EVENT_STOP);
        observedEvents.add(EventType.EVENT_SERVICE_CHANGE);
        return observedEvents;
    }

    @Override
    public void update(Service service, NotEOFEvent event) {
        // only two eventTypes are allowed (see method getObservedEvents()
        if (event.getClass().equals(EventType.EVENT_STOP)) {
            String serviceId = ((ApplicationStopEvent) event).getServiceId();
            removeApplicationService(serviceId);
        }

        if (event.getClass().equals(EventType.EVENT_START)) {
            // TODO ist noch nicht klar, wann das start event ausgelöst wird...
            // String serviceId = ((ApplicationStopEvent) event).getServiceId();
        }

    }
}
