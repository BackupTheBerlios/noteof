package de.happtick.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainLink;
import de.happtick.configuration.EventConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.event.ApplicationStartEvent;
import de.happtick.core.event.ApplicationStopEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.schedule.ChainAction;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Util;

public class Scheduling {

    private static class ApplicationStarter implements Runnable {
        private ApplicationConfiguration applConf;
        private boolean startApp = true;

        protected ApplicationStarter(ApplicationConfiguration applConf) {
            this.applConf = applConf;
        }

        public void run() {
            // Check if the StartClient for the IP-Address is active
            while (Util.isEmpty(MasterTable.getStartClientEvent(applConf.getClientIp()))) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (startApp && (applConf.isMultipleStart() || !isEqualApplicationActive(applConf))) {
                    ApplicationStartEvent event = new ApplicationStartEvent();
                    try {
                        event.addAttribute("workApplicationId", String.valueOf(applConf.getApplicationId()));
                        event.addAttribute("clientIp", applConf.getClientIp());
                        event.addAttribute("applicationPath", applConf.getExecutablePath());
                        event.addAttribute("applicationType", applConf.getExecutableType());
                        event.addAttribute("windowsSupport", String.valueOf(applConf.isWindowsSupport()));
                        System.out.println("Scheduling windowsSupport? " + event.getAttribute("windowsSupport"));

                        // Arguments
                        if (!Util.isEmpty(applConf.getArguments())) {
                            int i = 0;
                            for (String arg : applConf.getArguments()) {
                                event.addAttributeDescription("internal:ARG(" + i + ")", "Argument: (" + i + ")");
                                event.addAttribute("internal:ARG(" + i + ")", arg);
                                i++;
                            }
                        }

                        // Environment
                        if (!Util.isEmpty(applConf.getEnvironment())) {
                            for (String key : applConf.getEnvironment().keySet()) {
                                event.addAttributeDescription("internal:ENV->" + key, "Environment: " + key);
                                event.addAttribute("internal:ENV->" + key, applConf.getEnvironment().get(key));
                            }
                        }

                    } catch (ActionFailedException e) {
                        LocalLog.error("Start einer Anwendung ist fehlgeschlagen.", e);
                    }

                    raiseEvent(event);
                }
            } catch (HapptickException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Uses the UpdateObserver functionality to inform the StartClients about
     * the start ignition.
     * 
     * @param applConf
     * @throws ActionFailedException
     */
    public static synchronized void startApplication(ApplicationConfiguration applConf) throws HapptickException {
        if (null == applConf)
            throw new HapptickException(503L, "Anwendungskonfiguration fehlt.");

        new Thread(new ApplicationStarter(applConf)).start();
    }

    public static synchronized void stopApplication(ApplicationConfiguration applConf) throws HapptickException {
        ApplicationStopEvent event = new ApplicationStopEvent();
        try {
            event.addAttribute("workApplicationId", String.valueOf(applConf.getApplicationId()));
            event.addAttribute("clientIp", applConf.getClientIp());
            event.addAttribute("kill", "FALSE");
        } catch (ActionFailedException e) {
            throw new HapptickException(504L, e);
        }

        raiseEvent(event);
    }

    /**
     * Raise (fire) event - all interested observers will get it.
     * 
     * @param event
     */
    public static synchronized void raiseEvent(NotEOFEvent event) {
        Server.getInstance().updateObservers(null, event);
    }

    /**
     * Looks if there is any application active for which the configured
     * application has to wait...
     * 
     * @param applConf
     * @return
     */
    public static synchronized boolean mustWaitForOtherApplication(ApplicationConfiguration applConf) {
        for (Long id : applConf.getApplicationsWaitFor()) {
            // if list with found services > 0 there exists one or more service
            if (!Util.isEmpty(MasterTable.getApplicationServicesByApplicationId(id)))
                // true => must wait!
                return true;

            if (null != MasterTable.getStartEvent(id))
                // true => must wait!
                return true;
        }
        // false => ok - has not to wait
        return false;
    }

    /**
     * Looks if there is any process of same application running
     * 
     * @param applicationConfiguration
     *            Configuration of the application that is asking here.
     * @return True if the application has to wait.
     */
    public static synchronized boolean isEqualApplicationActive(ApplicationConfiguration applConf) throws HapptickException {
        if (Util.isEmpty(applConf)) {
            throw new HapptickException(404L, "Pruefung auf aktive Anwendung im Scheduling.");
        }

        if (!(Util.isEmpty(MasterTable.getApplicationServicesByApplicationId(applConf.getApplicationId())))) {
            return true;
        }

        if (null != MasterTable.getStartEvent(applConf.getApplicationId())) {
            return true;
        }

        // nothing found
        return false;
    }

    public static synchronized boolean mustWaitForSameApplication(Long applId) throws HapptickException {
        ApplicationConfiguration conf = MasterTable.getApplicationConfiguration(applId);

        if (null == conf) {
            // no configuration for this application
            return false;
        }

        if (conf.isMultipleStart())
            // waiting is not required
            return false;

        if (Util.isEmpty(MasterTable.getApplicationServicesByApplicationId(applId).size()) && //
                null != MasterTable.getStartEvent(conf.getApplicationId())) {
            // no active process and no startEvent found
            return false;
        }

        // must wait
        return true;
    }

    /**
     * Calculates the next start point up from now
     * 
     * @return Date when the application should run only depending to the
     *         configuration, ignoring other active processes etc.
     * @throws ActionFailedException
     */
    public static synchronized Date calculateNextStart(ApplicationConfiguration applConf, int offsetSeconds) {

        // Mit aktueller Systemzeit beginnen...
        Calendar calcDate = new GregorianCalendar();
        calcDate.add(Calendar.SECOND, offsetSeconds);

        // ermittle den ersten gueltigen Tag, ohne die Wochentage zu
        // beruecksichtigen
        boolean timeValueFound = true;
        List<Integer> monthDays = ApplicationConfiguration.transformTimePlanMonthDays(applConf.getTimePlanMonthdays());
        if (!Util.isEmpty(monthDays)) {
            timeValueFound = monthDays.contains(calcDate.get(Calendar.DAY_OF_MONTH));

            if (!timeValueFound) {
                for (Integer day : monthDays) {
                    // Tag kommt noch in diesem Monat
                    if (calcDate.get(Calendar.DAY_OF_MONTH) < day) {
                        calcDate.set(Calendar.DAY_OF_MONTH, day);
                        calcDate.set(Calendar.HOUR_OF_DAY, 0);
                        calcDate.set(Calendar.MINUTE, 0);
                        calcDate.set(Calendar.SECOND, 0);
                        timeValueFound = true;
                        break;
                    }
                }
            }
            // Tag folgt in diesem Monat nicht mehr, also auf den kleinsten des
            // naechsten Monats setzen.
            if (!timeValueFound) {
                calcDate.set(Calendar.DAY_OF_MONTH, monthDays.get(0));
                calcDate.add(Calendar.MONTH, 1);
                calcDate.set(Calendar.HOUR_OF_DAY, 0);
                calcDate.set(Calendar.MINUTE, 0);
                calcDate.set(Calendar.SECOND, 0);
            }
        }

        // pruefe, ob Wochentag passt
        List<Integer> weekDays = ApplicationConfiguration.transformTimePlanWeekDays(applConf.getTimePlanWeekdays());
        if (!Util.isEmpty(weekDays)) {
            timeValueFound = weekDays.contains(calcDate.get(Calendar.DAY_OF_WEEK));

            // Tag passt nicht... Zeit erst mal auf 0
            if (!timeValueFound) {
                // ein anderer Tag ist es
                calcDate.set(Calendar.HOUR_OF_DAY, 0);
                calcDate.set(Calendar.MINUTE, 0);
                calcDate.set(Calendar.SECOND, 0);
            }
        }

        // Solange suchen, bis Wochentag und Tag im Monat passen...
        while (!timeValueFound) {
            // vergleiche Wochentage
            // wenn Wochentag nicht passt direkt naechsten Tag
            if (!Util.isEmpty(weekDays) && //
                    !weekDays.contains(calcDate.get(Calendar.DAY_OF_WEEK))) {
                calcDate.add(Calendar.DATE, 1);
                continue;
            }

            // vergleiche Tag des Monats
            timeValueFound = Util.isEmpty(monthDays) || //
                    (!Util.isEmpty(monthDays) && monthDays.contains(calcDate.get(Calendar.DAY_OF_MONTH)));

            if (!timeValueFound)
                calcDate.add(Calendar.DATE, 1);
        }

        // // afjklasfljköasfölsadfklsdfklöasdf
        // calcDate.add(Calendar.SECOND, 5);
        // if (true)
        // return calcDate.getTime();
        // // asfjasfjklö ljkasdfjklaslksafjklöasdfjklf

        // Jetzt auf Uhrzeit pruefen
        // Sekunden
        timeValueFound = false;
        List<Integer> seconds = ApplicationConfiguration.transformTimePlanSeconds(applConf.getTimePlanSeconds());
        for (int sec = calcDate.get(Calendar.SECOND); sec < 60; sec++) {
            if (seconds.contains(sec)) {
                calcDate.set(Calendar.SECOND, sec);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.MINUTE, 1);
            calcDate.set(Calendar.SECOND, seconds.get(0));
        }

        // Minuten
        timeValueFound = false;
        List<Integer> minutes = ApplicationConfiguration.transformTimePlanMinutes(applConf.getTimePlanMinutes());
        for (int minute = calcDate.get(Calendar.MINUTE); minute < 60; minute++) {
            if (minutes.contains(minute)) {
                calcDate.set(Calendar.MINUTE, minute);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.HOUR_OF_DAY, 1);
            calcDate.set(Calendar.MINUTE, minutes.get(0));
            calcDate.set(Calendar.SECOND, seconds.get(0));
        }

        // Stunden
        timeValueFound = false;
        List<Integer> hours = ApplicationConfiguration.transformTimePlanHours(applConf.getTimePlanHours());
        for (int hour = calcDate.get(Calendar.HOUR_OF_DAY); hour < 24; hour++) {
            if (hours.contains(hour)) {
                calcDate.set(Calendar.HOUR_OF_DAY, hour);
                timeValueFound = true;
                break;
            }
        }

        if (!timeValueFound) {
            calcDate.add(Calendar.DATE, 1);
            calcDate.set(Calendar.HOUR_OF_DAY, hours.get(0));
            calcDate.set(Calendar.MINUTE, minutes.get(0));
            calcDate.set(Calendar.SECOND, seconds.get(0));
        }
        return calcDate.getTime();
    }

    /**
     * Delivers a list with EventType which match to some conditions.
     * <p>
     * 
     * @param addresseeType
     *            'chain', 'application', 'event'
     * @param addresseeId
     *            id of chain, application or event
     * @param eventConfigurations
     *            Complete List of all EventConfigurations (MasterTable)
     * @return Filtered EventTypes - matching to the conditions above.
     */
    public static synchronized void filterObservedEventsForChain(Long addresseeId, List<EventType> typeList, Map<String, ChainAction> chainActions,
            List<EventConfiguration> eventConfigurations) {
        // only one entry for the different event types is needed...
        // so a map simplifies filtering that
        Map<EventType, EventType> types = new HashMap<EventType, EventType>();
        try {
            for (EventConfiguration conf : eventConfigurations) {
                if ("chain".equalsIgnoreCase(conf.getAddresseeType()) && //
                        (Util.isEmpty(conf.getAddresseeId()) || //
                                -1 == conf.getAddresseeId() || //
                        conf.getAddresseeId() == addresseeId)) {
                    EventType type = Util.lookForEventType(conf.getEventClassName());
                    types.put(type, type);

                    // action merken
                    ChainAction action = new ChainAction(conf.getAction(), conf.getAddresseeType(), conf.getAddresseeId(), true);
                    String typeName = type.name();
                    chainActions.put(typeName + conf.getKeyName() + conf.getKeyValue(), action);
                }
            }
        } catch (ActionFailedException e) {
            LocalLog.warn("Event konnte der Chain nicht zugeordnet werden.", e);
        }
        Set<EventType> typeSet = types.keySet();
        // List<EventType> typeList = new ArrayList<EventType>();

        typeList.addAll(typeSet);

        // return typeList;
    }

    /**
     * Updates a EventTypes list with EventTypes for chain configurations.
     * <p>
     * Takes ChainLinks and looks for prevent or condition events. If there is
     * an event and it is not yet stored in the list the EventType of the found
     * event will be added to the list.
     * 
     * @param observedEvents
     *            List with EventTypes
     * @param link
     *            The ChainLink contains perhaps a prevent or a condition event.
     */
    public static synchronized void updateObservedEventsForChain(List<EventType> observedEvents, Map<String, ChainAction> chainActions, ChainLink link)
            throws HapptickException {
        // Conditions
        if (null != link.getConditionEventId()) {
            try {
                EventConfiguration conf = MasterTable.getEventConfiguration(link.getConditionEventId());
                if (Util.isEmpty(conf)) {
                    throw new HapptickException(403L, "Link zeigt auf unbekannte Event-Konfiguration. ChainId: " + link.getChainId() + "; LinkId: "
                            + link.getLinkId() + "; EventId: " + link.getConditionEventId());
                }
                EventType type = Util.lookForEventType(conf.getEventClassName());
                boolean alreadyExists = false;
                for (EventType existingType : observedEvents) {
                    if (type.equals(existingType)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists)
                    observedEvents.add(type);

                // action merken
                ChainAction action = new ChainAction("condition", link.getAddresseeType(), link.getAddresseeId(), link.isSkip());
                String typeName = type.name();
                chainActions.put(typeName + link.getConditionKey() + link.getConditionValue(), action);
            } catch (ActionFailedException e) {
                LocalLog.warn("ChainLink konnte nicht auf Condition untersucht werden.", e);
            }

        }
        // Prevents
        if (null != link.getPreventEventId()) {
            try {
                EventConfiguration conf = MasterTable.getEventConfiguration(link.getPreventEventId());
                EventType type = Util.lookForEventType(conf.getEventClassName());
                boolean alreadyExists = false;
                for (EventType existingType : observedEvents) {
                    if (type.equals(existingType)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists)
                    observedEvents.add(type);

                // action merken
                ChainAction action = new ChainAction("prevent", link.getAddresseeType(), link.getAddresseeId(), link.isSkip());
                String typeName = type.name();
                chainActions.put(typeName + link.getPreventKey() + link.getPreventValue(), action);
            } catch (ActionFailedException e) {
                LocalLog.warn("ChainLink konnte nicht auf Prevent untersucht werden.", e);
            }
        }
    }

    /**
     * Delivers the event configurations for a EventType
     * 
     * @param eventType
     *            The type...
     * @return A list with the configurations or NULL
     */
    public synchronized static List<EventConfiguration> getEventConfigurationsForEventType(EventType eventType) {
        if (Util.isEmpty(eventType))
            return null;

        List<EventConfiguration> foundConfigurations = new ArrayList<EventConfiguration>();
        try {
            for (EventConfiguration conf : MasterTable.getEventConfigurationsAsList()) {
                if (eventType.equals(Util.lookForEventType(conf.getEventClassName()))) {
                    foundConfigurations.add(conf);
                }
            }
            return foundConfigurations;
        } catch (ActionFailedException e) {
        }
        return null;
    }

    /**
     * Delivers a event configuration for a event.
     * <p>
     * To find the correct configuration keyName is searched to the attributes
     * of the event. If the keyName matches with the configuration keyName or if
     * there is no keyname in the configuration but an action this are matches
     * ...
     * 
     * @param event
     *            A fired event that has some attributes.
     * @param configurations
     *            A List with configurations for this EventType.
     * @return A list mit some matching configurations or NULL.
     */
    public synchronized static List<EventConfiguration> getEventConfigurationsForEvent(NotEOFEvent event, List<EventConfiguration> configurations) {
        if (Util.isEmpty(event) || Util.isEmpty(configurations))
            return null;

        List<EventConfiguration> foundConfigurations = new ArrayList<EventConfiguration>();
        for (EventConfiguration conf : configurations) {
            // keyName found or no keyName configured but any action...
            // and verify applicationId's
            if ((Util.isEmpty(conf.getKeyName()) || !Util.isEmpty(event.getAttribute(conf.getKeyName())))
                    && (Util.isEmpty(conf.getApplicationId()) || conf.getApplicationId().equals(event.getApplicationId())))
                foundConfigurations.add(conf);
        }

        if (Util.isEmpty(foundConfigurations))
            return null;
        return foundConfigurations;
    }
}
