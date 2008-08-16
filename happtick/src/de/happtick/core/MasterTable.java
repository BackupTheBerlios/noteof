package de.happtick.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.events.ApplicationStopEvent;
import de.notEOF.core.enumeration.EventType;
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
        service.getLastEvent(EventType.EVENT_STOP);

        applicationServices.remove(serviceId);
        Util.updateAllObserver(eventObservers, null, new ApplicationStopEvent(applicationId, 0));
    }

    @Override
    public void registerForEvents(EventObserver eventObserver) {
        eventObservers.add(eventObserver);
    }

    @Override
    public List<EventType> getObservedEvents() {
        // TODO Auto-generated method stub
        List<EventType> observedEvents = new ArrayList<EventType>();
        observedEvents.add(EventType.EVENT_STOP);
        observedEvents.add(EventType.EVENT_SERVICE_CHANGE);
        return observedEvents;
    }

    @Override
    public void update(Service arg0, NotEOFEvent arg1) {
        // TODO Auto-generated method stub

    }
}
