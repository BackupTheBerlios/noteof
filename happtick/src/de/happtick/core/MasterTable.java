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
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EventFinder;
import de.notEOF.core.event.GenericEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Util;
import de.notIOC.configuration.ConfigurationManager;

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
    private static Map<String, Service> services = new HashMap<String, Service>();
    private static Map<Long, NotEOFEvent> startEvents = new HashMap<Long, NotEOFEvent>();
    private static Map<String, NotEOFEvent> startClientEvents = new HashMap<String, NotEOFEvent>();

    private static Map<String, NotEOFEvent> ownEvents = new HashMap<String, NotEOFEvent>();

    private static boolean inAction = false;
    private static boolean confUpdated = false;
    private static long maxDelay = 10000;

    private static Server server;

    private static boolean chainsUsed = false;
    private static boolean eventsUsed = false;
    private static boolean schedulerUsed = false;

    /*
     * Liest die Konfiguration, initialisiert processChain und
     * applicationConfigurations
     */
    private static void updateConfiguration() throws ActionFailedException {
        if (!confUpdated) {
            NotEOFConfiguration conf = new LocalConfiguration();

            // Configuration must be read in this order:
            // 0. Global settings
            // 1. Applications
            // 2. Events
            // 3. Chains
            // 4. Self Build Events

            // Global settings
            maxDelay = Util.parseLong(conf.getText("scheduler.maxDelay"), 10000);
            eventsUsed = Util.parseBoolean(conf.getAttribute("scheduler.use", "event"), false);
            schedulerUsed = Util.parseBoolean(conf.getAttribute("scheduler.use", "timer"), false);
            chainsUsed = Util.parseBoolean(conf.getAttribute("scheduler.use", "chain"), false);

            // Events
            // Liste der nodes
            List<String> eventNodes = conf.getTextList("scheduler.events.event");
            if (null != eventNodes) {
                // for every node create object of type EventConfiguration
                // the objects initialize themselve with configuration data
                for (String node : eventNodes) {
                    EventConfiguration eventConf = new EventConfiguration(node, conf);
                    eventConfigurations.put(eventConf.getEventId(), eventConf);
                }
            }

            // processChain
            // Liste der nodes
            List<String> chainNodes = conf.getTextList("scheduler.chains.chain");
            if (null != chainNodes) {
                // for every node create object of type ChainConfiguration
                // the objects initialize themselve with configuration data
                for (String node : chainNodes) {
                    ChainConfiguration chainConf = new ChainConfiguration(node, conf);
                    chainConfigurations.put(chainConf.getChainId(), chainConf);
                }
            }

            // ApplicationConfigurations
            // Liste der nodes
            List<String> applNodes = conf.getTextList("scheduler.applications.application");
            if (null != applNodes) {
                // for every node create Object ApplicationConfiguration
                // the Objects read the configuration by themselve
                for (String node : applNodes) {
                    ApplicationConfiguration applConf = new ApplicationConfiguration(node, conf);
                    applicationConfigurations.put(applConf.getApplicationId(), applConf);
                }
            }

            // Self build events
            List<String> ownEventNodes = conf.getTextList("ownEvents.event");
            if (null != ownEventNodes) {
                // for every node create Object ApplicationConfiguration
                // the Objects read the configuration by themselve
                for (String eventNode : ownEventNodes) {
                    String node = "ownEvents." + eventNode;
                    System.out.println("node " + node);
                    // derived?
                    String derivedClassName = conf.getAttribute(node, "derived");
                    System.out.println("derivedClassName " + derivedClassName);
                    NotEOFEvent event;
                    if (!Util.isEmpty(derivedClassName)) {
                        event = EventFinder.getNotEOFEvent(ConfigurationManager.getApplicationHome(), derivedClassName);
                    } else {
                        event = new GenericEvent();
                    }
                    System.out.println("ClassType: " + event.getEventType());

                    // attributes
                    List<String> attributes = conf.getTextList(node + ".attribute");
                    if (null != attributes) {
                        // for every node create Object ApplicationConfiguration
                        // the Objects read the configuration by themselve
                        for (String attribute : attributes) {
                            System.out.println("attribute " + attribute);
                            String attNode = node + "." + attribute;
                            String descriptionKey = conf.getAttribute(attNode, "descriptionKey");
                            String descriptionValue = conf.getAttribute(attNode, "descriptionValue");
                            String keyName = conf.getAttribute(attNode, "keyName");
                            String keyValue = conf.getAttribute(attNode, "keyValue");

                            System.out.println("descKey " + descriptionKey);
                            System.out.println("descVal " + descriptionValue);
                            System.out.println("keyName " + keyName);
                            System.out.println("keyValue " + keyValue);

                            event.addAttributeDescription(descriptionKey, descriptionValue);
                            event.addAttribute(keyName, keyValue);
                        }
                    }
                    // add generated event to list
                    ownEvents.put("Alias:" + derivedClassName, event);
                }
                System.out.println("Anzahl generische Events: " + ownEvents.size());
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
     *            The identifier of the event like stored in the configuration.
     * @return The object if found or null.
     */
    public synchronized static EventConfiguration getEventConfiguration(Long eventId) throws HapptickException {
        EventConfiguration ret = eventConfigurations.get(eventId);
        if (Util.isEmpty(ret))
            throw new HapptickException(405L, "EventConfiguration. Id: " + eventId);

        return ret;
    }

    // /**
    // * Delivers the configuration object for one event.
    // *
    // * @param raiseId
    // * The identifier of the event like stored in the configuration.
    // * @return The object if found or null.
    // */
    // public synchronized static RaiseConfiguration
    // getRaiseConfiguration(String raiseId) {
    // return raiseConfigurations.get(raiseId);
    // }
    //
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

    // /**
    // * Delivers the configuration of raising
    // */
    // public synchronized static Map<String, RaiseConfiguration>
    // getRaiseConfigurations() throws ActionFailedException {
    // updateConfiguration();
    // return raiseConfigurations;
    // }
    //
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

    // /**
    // * Delivers the raise configurations as list.
    // *
    // * @return A list with raise entries.
    // * @throws ActionFailedException
    // */
    // public synchronized static List<RaiseConfiguration>
    // getRaiseConfigurationsAsList() throws ActionFailedException {
    // List<RaiseConfiguration> confList = new ArrayList<RaiseConfiguration>();
    // confList.addAll(getRaiseConfigurations().values());
    // return confList;
    // }
    //
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
    public synchronized static ApplicationService getApplicationServiceByStartId(String startId) throws HapptickException {
        List<ApplicationService> completeList = getApplicationServicesAsList();
        if (!Util.isEmpty(completeList)) {
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
    public synchronized static ApplicationConfiguration getApplicationConfiguration(Long applicationId) throws HapptickException {
        ApplicationConfiguration ret = applicationConfigurations.get(applicationId);
        if (Util.isEmpty(ret) && applicationId >= -1)
            throw new HapptickException(405L, "ApplicationConfiguration. Id: " + applicationId);

        return ret;
    }

    /**
     * Delivers a self described (configured) event.
     * <p>
     * Selfdescribed events are raised by the prefix 'Alias:' + configuration
     * name.
     * 
     * @param eventClassName
     *            The complete Aliasname like Alias:EventName.
     * @return A new NotEOFEvent.
     * @throws HapptickException
     *             Is thrown if the event cannot be identified by the
     *             eventClassName.
     */
    public synchronized static NotEOFEvent getOwnEvent(String eventClassName) throws HapptickException {
        NotEOFEvent event = ownEvents.get(eventClassName);
        if (Util.isEmpty(event)) {
            throw new HapptickException(406L, "Aliasname: " + eventClassName);
        }
        return event;
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

        while (inAction) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        inAction = true;

        // if (service.getClass().isAssignableFrom(ApplicationService.class)) {
        // System.out.println(
        // "MasterTable.addService: applicationId bei Registrierung: " +
        // ((ApplicationService) service).getApplicationId());
        // System.out.println("MasterTable.addService: ClassName = " +
        // service.getClass());
        // }

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
        while (inAction) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        inAction = true;
        services.remove(service.getServiceId());
        if (service.getClass().isAssignableFrom(ApplicationService.class)) {
            removeStartEvent(((ApplicationService) service).getApplicationId());
        }
        inAction = false;
    }

    /**
     * If the StoppedEvent was raised the StartEvent then must be removed.
     * <p>
     * It is possible that the map doesn't contains the event because it was
     * removed before by a service or another event. <br>
     * This is no problem because the map is only important to avoid the
     * multiple start of applications which are not allowed for this.
     * 
     * @param startedEvent
     */
    public static synchronized void removeStartEvent(NotEOFEvent event) {
        startEvents.remove(event.getApplicationId());
    }

    public static synchronized void replaceStartEvent(NotEOFEvent event) {
        startEvents.remove(Util.parseLong(event.getAttribute("workApplicationId"), -1));
        putStartEvent(event);
    }

    /**
     * Remove StartEvent by applicationId. Normally done by ApplicationServices.
     * <p>
     * It is possible that the map doesn't contains the event because it was
     * removed before by a service or another event. <br>
     * This is no problem because the map is only important to avoid the
     * multiple start of applications which are not allowed for this.
     * 
     * @param applicationId
     */
    public static synchronized void removeStartEvent(Long applicationId) {
        startEvents.remove(applicationId);
    }

    /**
     * Add StartEvent
     * 
     * @param event
     */
    public static synchronized void putStartEvent(NotEOFEvent event) {
        startEvents.put(Util.parseLong(event.getAttribute("workApplicationId"), -1), event);
    }

    /**
     * Delivers a StartEvent for an application
     * 
     * @param applicationId
     * @return
     */
    public static NotEOFEvent getStartEvent(Long applicationId) {
        return startEvents.get(applicationId);
    }

    public static List<NotEOFEvent> getStartEventsAsList() {
        List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        events.addAll(startEvents.values());
        return events;

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

    public static long getMaxDelay() {
        return maxDelay;
    }

    /**
     * StartClients send START and STOP signals by StartClienEvent.
     * <p>
     * This signals are used by the scheduler to allow or prevent the start of
     * applications.
     * 
     * @param ipAddress
     *            The server-address of the StartClient
     * @return The last fired event of the StartClient or NULL
     */
    public static NotEOFEvent getStartClientEvent(String ipAddress) {
        return startClientEvents.get(ipAddress);
    }

    /**
     * StartClients send START and STOP signals by StartClienEvent.
     * <p>
     * This START signal must be stored in the MasterTables. When the STOP
     * signal arrives the START must be deleted.
     * 
     * @param event
     *            The last fired StartClienEvent of the StartClient
     */
    public static void updateStartClientEvent(NotEOFEvent event) {
        if (event.equals(EventType.EVENT_START_CLIENT)) {
            if (event.getAttribute("state").equals("START")) {
                System.out.println("MasterTable.updateStartClientEvent. clientIp ist: " + event.getAttribute("clientIp"));
                startClientEvents.put(event.getAttribute("clientIp"), event);
            }
            if (event.getAttribute("state").equals("STOP")) {
                startClientEvents.remove(event.getAttribute("clientIp"));
            }
        }
    }

    /**
     * @return the chainsUsed
     */
    public static boolean isChainsUsed() {
        return chainsUsed;
    }

    /**
     * @return the eventsUsed
     */
    public static boolean isEventsUsed() {
        return eventsUsed;
    }

    /**
     * @return the schedulerUsed
     */
    public static boolean isSchedulerUsed() {
        return schedulerUsed;
    }
}
