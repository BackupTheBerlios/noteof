package de.happtick.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.EventConfiguration;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.start.service.StartService;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Util;

/**
 * This static class contains central informations
 * <p>
 * The services and other objects of happtick use this class to get informations
 * about applications and processes.
 * 
 * @author Dirk
 * 
 */
public class MasterTable {

    private static Map<Long, ApplicationConfiguration> applicationConfigurations = new HashMap<Long, ApplicationConfiguration>();
    private static Map<Long, ChainConfiguration> chainConfigurations = new HashMap<Long, ChainConfiguration>();
    private static Map<Long, EventConfiguration> eventConfigurations = new HashMap<Long, EventConfiguration>();
    // private static Map<String, ApplicationService> applicationServices = new
    // HashMap<String, ApplicationService>();
    // private static Map<String, StartService> startServices = new
    // HashMap<String, StartService>();

    private static Map<String, Service> services = new HashMap<String, Service>();

    // private static List<EventObserver> eventObservers = new
    // ArrayList<EventObserver>();

    private static boolean inAction = false;
    private static boolean confUpdated = false;

    private static Server server;

    /*
     * Liest die Konfiguration, initialisiert processChain und
     * applicationConfigurations
     */
    private static void updateConfiguration() throws ActionFailedException {
        if (!confUpdated) {
            NotEOFConfiguration conf = new LocalConfiguration();

            // processChain
            // aktiv?
            Boolean useChain = Util.parseBoolean(conf.getAttribute("scheduler.use", "chain"), false);
            if (useChain) {
                // Liste der nodes
                List<String> nodes = conf.getTextList("scheduler.chains.chain");
                if (null != nodes) {
                    // for every node create object of type ChainConfiguration
                    // the objects initialize themselve with configuration data
                    for (String node : nodes) {
                        ChainConfiguration chainConf = new ChainConfiguration(node, conf);
                        chainConfigurations.put(chainConf.getChainId(), chainConf);
                    }
                }
            }

            // ApplicationConfigurations
            // timer gesteuert?
            Boolean useTimer = Util.parseBoolean(conf.getAttribute("scheduler.use", "timer"), false);
            if (useTimer) {
                // Liste der nodes
                List<String> nodes = conf.getTextList("scheduler.applications.application");
                if (null != nodes) {
                    // for every node create Object ApplicationConfiguration
                    // the Objects read the configuration by themselve
                    for (String node : nodes) {
                        ApplicationConfiguration applConf = new ApplicationConfiguration(node, conf);
                        applicationConfigurations.put(applConf.getApplicationId(), applConf);
                    }
                }
            }

            // Events
            // Events nutzen?
            Boolean useEvent = Util.parseBoolean(conf.getAttribute("scheduler.use", "event"), false);
            if (useEvent) {
                // Liste der nodes
                List<String> nodes = conf.getTextList("scheduler.events.event");
                if (null != nodes) {
                    // for every node create object of type EventConfiguration
                    // the objects initialize themselve with configuration data
                    for (String node : nodes) {
                        EventConfiguration eventConf = new EventConfiguration(node, conf);
                        eventConfigurations.put(eventConf.getEventId(), eventConf);
                    }
                }
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
    public synchronized static Map<Long, ApplicationConfiguration> getApplicationConfigurations() throws ActionFailedException {
        updateConfiguration();
        return applicationConfigurations;
    }

    /**
     * Delivers a flat list of ApplicationConfiguration
     * 
     * @return The list.
     * @throws ActionFailedException
     */
    public static List<ApplicationConfiguration> getApplicationConfigurationsAsList() throws ActionFailedException {
        List<ApplicationConfiguration> confList = new ArrayList<ApplicationConfiguration>();
        confList.addAll(getApplicationConfigurations().values());
        return confList;
    }

    /**
     * Delivers the configuration object for one chain.
     * 
     * @param chainId
     *            The identifier of the chain.
     * @return The object if found or null.
     */
    public synchronized static ChainConfiguration getChainConfiguration(Long chainId) {
        return chainConfigurations.get(chainId);
    }

    /**
     * Delivers the configuration of chains
     * 
     * @return Map with configurations. chainId is the key like used in
     *         configuration file and in the implementations of
     *         ChainConfigurations.
     */
    public synchronized static Map<Long, ChainConfiguration> getChainConfigurations() throws ActionFailedException {
        updateConfiguration();
        return chainConfigurations;
    }

    /**
     * Delivers the process chain.
     * 
     * @return A list with chains.
     * @throws ActionFailedException
     */
    public synchronized static List<ChainConfiguration> getChainConfigurationsAsList() throws ActionFailedException {
        List<ChainConfiguration> confList = new ArrayList<ChainConfiguration>();
        confList.addAll(getChainConfigurations().values());
        return confList;
    }

    /**
     * Delivers the configuration object for one event.
     * 
     * @param eventId
     *            The identifier of the event.
     * @return The object if found or null.
     */
    public synchronized static EventConfiguration getEventConfiguration(Long eventId) {
        return eventConfigurations.get(eventId);
    }

    /**
     * Delivers the configuration of events
     * 
     * @return Map with configurations. eventId is the key like used in
     *         configuration file and in the implementations of
     *         EventConfigurations.
     */
    public synchronized static Map<Long, EventConfiguration> getEventConfigurations() throws ActionFailedException {
        updateConfiguration();
        return eventConfigurations;
    }

    /**
     * Delivers the events.
     * 
     * @return A list with events.
     * @throws ActionFailedException
     */
    public synchronized static List<EventConfiguration> getEventConfigurationsAsList() throws ActionFailedException {
        List<EventConfiguration> confList = new ArrayList<EventConfiguration>();
        confList.addAll(getEventConfigurations().values());
        return confList;
    }

    /**
     * Delivers the active Services for processes (running applications).
     * 
     * @return Map with ApplicationServices. Key ist the serviceId.
     */
    public synchronized static List<ApplicationService> getApplicationServicesAsList() {
        List<ApplicationService> applicationList = new ArrayList<ApplicationService>();
        for (Service service : getServicesAsList()) {
            if (service.getClass().isAssignableFrom(ApplicationService.class)) {
                applicationList.add((ApplicationService) service);
            }
        }
        return applicationList;
    }

    private static List<Service> getServicesAsList() {
        List<Service> serviceList = new ArrayList<Service>();
        serviceList.addAll(services.values());
        return serviceList;
    }

    /**
     * Delivers all services which serve a client that has the specified
     * applicationId.
     * 
     * @param applicationId
     *            Id of application like used in configuration and
     *            implementation.
     * @return A list with found services or NULL.
     */
    public synchronized static List<ApplicationService> getApplicationServicesByApplicationId(Long applicationId) {
        List<ApplicationService> completeList = getApplicationServicesAsList();
        if (completeList.size() > 0) {
            List<ApplicationService> byIdList = new ArrayList<ApplicationService>();
            for (ApplicationService service : completeList) {
                if (service.getApplicationId().longValue() == applicationId.longValue())
                    byIdList.add(service);
            }
            if (byIdList.size() > 0)
                return byIdList;
        }
        return null;
    }

    /**
     * Returns the ApplicationService with the serviceId.
     * 
     * @param serviceId
     *            Created by server. Is unique.
     * @return An ApplicationService or null if not found in master tables.
     */
    public synchronized static ApplicationService getApplicationService(String serviceId) {
        return (ApplicationService) services.get(serviceId);
    }

    /**
     * Delivers a Base Service by serviceId.
     * 
     * @param serviceId
     * @return
     */
    public synchronized static Service getService(String serviceId) {
        return services.get(serviceId);
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
        List<ApplicationService> completeList = getApplicationServicesAsList();
        if (completeList.size() > 0) {
            for (ApplicationService service : completeList) {
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
     * Put a service into the list of ApplicationServices OR StartService.
     * 
     * @param service
     *            The service that must be added. May be of type
     *            ApplicationService or StartService.
     * @throws HapptickException
     */
    public synchronized static void addService(Service service) {
        LocalLog.info("MasterTable registering service: " + service.getClass().getCanonicalName() + " (clientNetId = " + service.getClientNetId() + ")");
        // TODO Kommt man so an den Server???
        if (null == server)
            server = service.getServer();

        // TODO pruefen, ob isAssignableFrom() so funktioniert...
        while (inAction)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        inAction = true;
        services.put(service.getServiceId(), service);
        inAction = false;
    }

    /**
     * Remove a service from the internal service lists. There exists two lists:
     * One for ApplicationServices, one for StartServices.
     * 
     * @param serviceId
     *            Is the key of the service what must be removed.
     */
    public synchronized static void removeService(Service service) {
        LocalLog.info("MasterTable releasing service: " + service.getClass().getCanonicalName() + " (clientNetId = " + service.getClientNetId() + ")");
        while (inAction)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        inAction = true;
        services.remove(service.getServiceId());
        inAction = false;
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
        observedEvents.add(EventType.EVENT_SERVICE_STOPPED);
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
        for (Long id : applicationConfiguration.getApplicationsWaitFor()) {
            // if list with found services > 0 there exists one or more service
            if (getApplicationServicesByApplicationId(id).size() > 0)
                return true;
        }
        return false;
    }
}
