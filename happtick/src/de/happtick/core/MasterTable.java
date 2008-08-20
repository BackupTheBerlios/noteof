package de.happtick.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.LocalConfigurationClient;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.events.ApplicationStopEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.start.service.StartService;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.ServiceStopEvent;
import de.notEOF.core.interfaces.EventObservable;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.interfaces.StopEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;
import de.notIOC.exception.NotIOCException;

/**
 * This static class contains central informations
 * <p>
 * The services and other objects of happtick use this class to get informations
 * about applications and processes.
 * 
 * @author Dirk
 * 
 */
public class MasterTable implements EventObservable {

    private static Map<Long, ApplicationConfiguration> applicationConfigurations = new HashMap<Long, ApplicationConfiguration>();
    private static Map<String, ApplicationService> applicationServices = new HashMap<String, ApplicationService>();
    private static List<EventObserver> eventObservers = new ArrayList<EventObserver>();
    private static Map<String, StartService> startServices = new HashMap<String, StartService>();
    private static List<Long> processChain = new ArrayList<Long>();

    private static boolean inAction = false;
    private static boolean confUpdated = false;

    /*
     * Liest die Konfiguration, initialisiert processChain und
     * applicationConfigurations
     */
    private static void updateConfiguration() {
        if (!confUpdated) {
            try {
                // processChain
                // aktiv?
                Boolean useChain = Util.parseBoolean(LocalConfigurationClient.getAttribute("scheduler.use", "chain", "false"), false);
                if (useChain) {
                    // Liste der nodes
                    List<String> nodes = LocalConfigurationClient.getTextList("scheduler.chain.application");
                    if (null != nodes) {
                        // for every node search applicationId and put into
                        // local list
                        for (String node : nodes) {
                            // looks like scheduler.application1
                            node = "scheduler." + node;
                            // attribute applicationId
                            Long applicationId = Util.parseLong(LocalConfigurationClient.getAttribute(node, "applicationId", "-1"), -1);
                            processChain.add(applicationId);
                        }
                    }
                }

                // ApplicationConfigurations
                // timer gesteuert?
                Boolean useTimer = Util.parseBoolean(LocalConfigurationClient.getAttribute("scheduler.use", "timer", "false"), false);
                if (useTimer) {
                    // Liste der nodes
                    List<String> nodes = LocalConfigurationClient.getTextList("scheduler.applications.application");
                    if (null != nodes) {
                        // for every node create Object ApplicationConfiguration
                        // the Objects read the configuration by themselve
                        for (String node : nodes) {
                            ApplicationConfiguration applConf = new ApplicationConfiguration(node);
                            applicationConfigurations.put(applConf.getApplicationId(), applConf);
                        }
                    }
                }

            } catch (NotIOCException e) {
                LocalLog.warn("Konfiguration der Prozesskette konnte nicht gelesen werden.");
            }

            confUpdated = true;
        }
    }

    /**
     * Delivers the configuration of applications
     * 
     * @return Map with configurations. applicationId is the key like used in
     *         configuration file and in the implementations of
     *         ApplicationClients.
     */
    public synchronized static Map<Long, ApplicationConfiguration> getApplicationConfigurations() {
        updateConfiguration();
        return applicationConfigurations;
    }

    /**
     * Delivers a flat list of ApplicationConfiguration
     * 
     * @return The list.
     */
    public static List<ApplicationConfiguration> getApplicationConfigurationsAsList() {
        List<ApplicationConfiguration> confList = new ArrayList<ApplicationConfiguration>();
        confList.addAll(getApplicationConfigurations().values());
        return confList;
    }

    /**
     * Delivers the process chain.
     * 
     * @return A list with application id's.
     */
    public synchronized static List<Long> getProcessChain() {
        updateConfiguration();
        return processChain;
    }

    /**
     * Delivers the active Services for processes (running applications).
     * 
     * @return Map with ApplicationServices. Key ist the serviceId.
     */
    public synchronized static Map<String, ApplicationService> getApplicationServices() {
        return applicationServices;
    }

    /**
     * Delivers all services which serve a client that has the specified
     * applicationId.
     * 
     * @param applicationId
     *            Id of application like used in configuration and
     *            implementation.
     * @return A list with found services.
     */
    public synchronized static List<ApplicationService> getApplicationServicesByApplicationId(Long applicationId) {
        Collection<ApplicationService> services = applicationServices.values();
        List<ApplicationService> serviceList = null;
        if (services.size() > 0) {
            serviceList = new ArrayList<ApplicationService>();
            for (ApplicationService service : services) {
                if (service.getApplicationId().longValue() == applicationId.longValue())
                    serviceList.add(service);
            }
        }

        return serviceList;
    }

    /**
     * Returns the ApplicationService with the serviceId.
     * 
     * @param serviceId
     *            Created by server. Is unique.
     * @return An ApplicationService or null if not found in master tables.
     */
    public synchronized static ApplicationService getApplicationService(String serviceId) {
        return applicationServices.get(serviceId);
    }

    /**
     * Returns the ApplicationService with given startId.
     * <p>
     * The start id was genereated by StartService. The StartService told it the
     * ApplicationClient. The ApplicationClient send it to the
     * ApplicationService. Here we are.
     * 
     * @param startId
     *            Created by StartClient. Is unique in the whole happtick
     *            environment as long as only one happtick server is runnnig.
     * @return An ApplicationService or null if not found in master tables.
     */
    public synchronized static ApplicationService getApplicationServiceByStartId(String startId) {
        Collection<ApplicationService> services = applicationServices.values();
        if (services.size() > 0) {
            for (ApplicationService service : services) {
                if (service.getStartId().equals(startId))
                    return service;
            }
        }
        return null;
    }

    /**
     * Returns the configuration object for an application.
     * 
     * @param applicationId
     *            Should be unique. Created by human.
     * @return The ApplicationConfiguration object or null if not found in
     *         master tables.
     */
    public synchronized static ApplicationConfiguration getApplicationConfiguration(Long applicationId) {
        return applicationConfigurations.get(applicationId);
    }

    /**
     * Delivers a StartService by the client ip address.
     * 
     * @param clientIp
     *            The ip of the client host.
     * @return A StartService or null.
     */
    public synchronized static StartService getStartServiceByIp(String clientIp) {
        List<StartService> services = new ArrayList<StartService>();
        services.addAll(startServices.values());
        for (StartService service : services) {
            if (service.getClientIp().equals(clientIp)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Put a service into the list of ApplicationServices OR StartService.
     * 
     * @param service
     *            The service that must be added. May be of type
     *            ApplicationService or StartService.
     * @throws HapptickException
     */
    public synchronized static void addService(Service service) {
        // TODO prï¿½fen, ob isAssignableFrom() so funktioniert...
        while (inAction)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        inAction = true;
        if (service.getClass().isAssignableFrom(ApplicationService.class)) {
            applicationServices.put(service.getServiceId(), (ApplicationService) service);
        } else if (service.getClass().isAssignableFrom(StartService.class)) {
            startServices.put(service.getServiceId(), (StartService) service);
        } else {
            LocalLog.warn("Service konnte nicht in die MasterTable eingefï¿½gt werden. Type = " + service.getClass().getName());
        }
        inAction = false;
    }

    /**
     * Remove a service from the list of ApplicationServices .
     * 
     * @param serviceId
     *            Is the key of the service what must be removed.
     */
    public synchronized static void removeService(Service service) {
        while (inAction)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        inAction = true;

        // try for ApplicationServices
        if (service.getClass().isAssignableFrom(ApplicationService.class)) {
            Long applicationId = ((ApplicationService) service).getApplicationId();
            StopEvent stopEvent = (ApplicationStopEvent) ((ApplicationService) service).getLastEvent(EventType.EVENT_STOP);
            if (null == stopEvent) {
                stopEvent = new ApplicationStopEvent(service.getServiceId(), applicationId, 0);
            }
            applicationServices.remove(service.getServiceId());
            Util.updateAllObserver(eventObservers, null, stopEvent);
        }

        // try all StartServices
        if (service.getClass().isAssignableFrom(StartService.class)) {
            StopEvent stopEvent = new ServiceStopEvent(service.getServiceId());
            startServices.remove(service.getServiceId());
            Util.updateAllObserver(eventObservers, null, stopEvent);
        }

        inAction = false;
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
    public void registerForEvents(EventObserver eventObserver) {
        eventObservers.add(eventObserver);
    }

    /**
     * This function delivers a list with the Events for which the Observer is
     * interested in.
     * <p>
     * If the Observer doesn't initialized this list he get's no event.
     * 
     * @return A list with the Events which the observer wants to get.
     */
    public synchronized List<EventType> getObservedEvents() {
        List<EventType> observedEvents = new ArrayList<EventType>();
        observedEvents.add(EventType.EVENT_STOP);
        observedEvents.add(EventType.EVENT_SERVICE_CHANGE);
        return observedEvents;
    }

    /**
     * Returns the delay for starting applications (+ -) in milliseconds.
     * <p>
     * The delay is used for the time which goes by between request for and
     * response of the next start point of an application. So the delay is a
     * buffer for system performance 'problems'.
     * 
     * @return The delay. At the time the delay is a static hard coded value. In
     *         a later version this should be configurable.
     */
    public static int getTimeDelayForStart() {
        return 1000;
    }

    /**
     * After an application start was activated there can elapse some time until
     * the ApplicationServie is alive.
     * 
     * @return This method delivers the maximum waiting time in milliseconds. At
     *         the time the value is static hard coded. In a later version this
     *         should be configurable.
     */
    public static int waitTimeForStartApplicationService() {
        return 60000;
    }

    /**
     * Calculates the Date until an ApplicationService should be active after
     * the start of the application was activated.
     * 
     * @param startDate
     *            The Date from when the waiting begins. If startDate is null
     *            the actual system time will taken.
     * @return The new calculated Date when the ApplicationService must be
     *         running.
     */
    public static Date dateToWaitForApplicationService(Date startDate) {
        if (null == startDate)
            startDate = new Date();
        long endTime = startDate.getTime() + waitTimeForStartApplicationService();
        return new Date(endTime);
    }

    /**
     * Checks if the application may be started now.
     * <p>
     * The calculation regards the timeplan of the application and other
     * (active) processes.
     * 
     * @param applicationConfiguration
     *            The configuration which contains informations about the time
     *            plan.
     * @param startService
     *            The service which holds connection to the StartClient where
     *            the application would be started.
     * @return True if start is now allowed. False if not.
     */
    public boolean isStartAllowed(ApplicationConfiguration applicationConfiguration, StartService startService) {

        // durchsuche alle StartServices. wenn startservice = startservice ok.
        // ansonsten vergleiche konfiguration und applikationsid
        // berücksichtigen applicationservices

        return false;
    }

    /**
     * Looks if there is any application running for which the asking
     * application has to wait for.
     * 
     * @param applicationConfiguration
     *            Configuration of the application that is asking here.
     * @return True if the application has to wait.
     */
    public static boolean mustWaitForApplication(ApplicationConfiguration applicationConfiguration) {
        // iterate over the list of applications to wait for
        for (Long applicationId : applicationConfiguration.getApplicationsWaitFor()) {
            // if list with found services > 0 there exists one or more service
            if (getApplicationServicesByApplicationId(applicationId).size() > 0)
                return true;
        }
        return false;
    }
}
