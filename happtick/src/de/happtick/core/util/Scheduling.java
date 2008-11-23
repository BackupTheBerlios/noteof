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
import de.happtick.core.events.ApplicationStartEvent;
import de.happtick.core.schedule.ChainAction;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Util;

public class Scheduling {

    /**
     * Uses the UpdateObserver functionality to inform the StartClients about
     * the start ignition.
     * 
     * @param applConf
     * @throws ActionFailedException
     */
    public static synchronized void startApplication(ApplicationConfiguration applConf) throws ActionFailedException {
        ApplicationStartEvent event = new ApplicationStartEvent();
        event.setApplicationId(applConf.getApplicationId());
        event.addAttribute("clientIp", applConf.getClientIp());
        event.addAttribute("applicationId", String.valueOf(applConf.getApplicationId()));
        event.addAttribute("applicationPath", applConf.getExecutablePath());
        event.addAttribute("windowsSupport", String.valueOf(applConf.isWindowsSupport()));
        event.addAttribute("applicationType", applConf.getExecutableType());
        event.addAttribute("arguments", applConf.getExecutableArgs());

        Server.getInstance().updateObservers(null, event);
    }

    /**
     * Looks if there is any application active for which the configured
     * application has to wait...
     * 
     * @param applConf
     * @return
     */
    public static boolean mustWaitForApplication(ApplicationConfiguration applConf) {
        for (Long id : applConf.getApplicationsWaitFor()) {
            // if list with found services > 0 there exists one or more service
            if (MasterTable.getApplicationServicesByApplicationId(id).size() > 0)
                return true;

            if (null != MasterTable.getStartEvent(id))
                return true;
        }
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
    public static boolean isEqualApplicationActive(ApplicationConfiguration applConf) {
        if (MasterTable.getApplicationServicesByApplicationId(applConf.getApplicationId()).size() > 0)
            return true;

        // 2. check: StartEvent was raised before?
        if (null != MasterTable.getStartEvent(applConf.getApplicationId()))
            return true;

        // nothing found
        return false;
    }

    /**
     * Calculates the next start point up from now
     * 
     * @return Date when the application should run only depending to the
     *         configuration, ignoring other active processes etc.
     */
    public static Date calculateNextStart(ApplicationConfiguration applConf) {

        // Mit aktueller Systemzeit beginnen...
        Calendar calcDate = new GregorianCalendar();

        // ermittle den ersten gueltigen Tag, ohne die Wochentage zu
        // beruecksichtigen
        boolean timeValueFound = true;
        if (!Util.isEmpty(applConf.getTimePlanMonthdays())) {
            timeValueFound = applConf.getTimePlanMonthdays().contains(calcDate.get(Calendar.DAY_OF_MONTH));

            if (!timeValueFound) {
                for (Integer day : applConf.getTimePlanMonthdays()) {
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
                calcDate.set(Calendar.DAY_OF_MONTH, applConf.getTimePlanMonthdays().get(0));
                calcDate.add(Calendar.MONTH, 1);
                calcDate.set(Calendar.HOUR_OF_DAY, 0);
                calcDate.set(Calendar.MINUTE, 0);
                calcDate.set(Calendar.SECOND, 0);
            }
        }

        // pruefe, ob Wochentag passt
        if (!Util.isEmpty(applConf.getTimePlanWeekdays())) {
            timeValueFound = applConf.getTimePlanWeekdays().contains(calcDate.get(Calendar.DAY_OF_WEEK));

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
            calcDate.add(Calendar.DATE, 1);

            // vergleiche Wochentage
            // wenn Wochentag nicht passt direkt naechsten Tag
            if (!Util.isEmpty(applConf.getTimePlanWeekdays()) && //
                    !applConf.getTimePlanWeekdays().contains(calcDate.get(Calendar.DAY_OF_WEEK)))
                continue;

            // vergleiche Tag des Monats
            timeValueFound = !Util.isEmpty(applConf.getTimePlanMonthdays()) && //
                    applConf.getTimePlanMonthdays().contains(calcDate.get(Calendar.DAY_OF_MONTH));
        }

        // Jetzt auf Uhrzeit pruefen
        // Sekunden
        timeValueFound = false;
        for (int sec = calcDate.get(Calendar.SECOND); sec < 60; sec++) {
            if (applConf.getTimePlanSeconds().contains(sec)) {
                calcDate.set(Calendar.SECOND, sec);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.MINUTE, 1);
            calcDate.set(Calendar.SECOND, applConf.getTimePlanSeconds().get(0));
        }

        // Minuten
        timeValueFound = false;
        for (int minute = calcDate.get(Calendar.MINUTE); minute < 60; minute++) {
            if (applConf.getTimePlanMinutes().contains(minute)) {
                calcDate.set(Calendar.MINUTE, minute);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.HOUR_OF_DAY, 1);
            calcDate.set(Calendar.MINUTE, applConf.getTimePlanMinutes().get(0));
            calcDate.set(Calendar.SECOND, applConf.getTimePlanSeconds().get(0));
        }

        // Stunden
        timeValueFound = false;
        for (int hour = calcDate.get(Calendar.HOUR_OF_DAY); hour < 24; hour++) {
            if (applConf.getTimePlanHours().contains(hour)) {
                calcDate.set(Calendar.HOUR_OF_DAY, hour);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.DATE, 1);
            calcDate.set(Calendar.HOUR_OF_DAY, applConf.getTimePlanHours().get(0));
            calcDate.set(Calendar.MINUTE, applConf.getTimePlanMinutes().get(0));
            calcDate.set(Calendar.SECOND, applConf.getTimePlanSeconds().get(0));
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
    public static List<EventType> filterObservedEventsForChain(Long addresseeId, Map<String, ChainAction> chainActions,
            List<EventConfiguration> eventConfigurations) {
        // only one entry for the different event types is needed...
        // so a map simplifies filtering that
        Map<EventType, EventType> types = new HashMap<EventType, EventType>();
        try {
            for (EventConfiguration conf : eventConfigurations) {
                if (Util.isEmpty(conf.getAddresseeId()) || //
                        conf.getAddresseeId().equals(addresseeId)) {
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
        List<EventType> typeList = new ArrayList<EventType>();
        typeList.addAll(typeSet);

        return typeList;
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
    public static void updateObservedEventsForChain(List<EventType> observedEvents, Map<String, ChainAction> chainActions, ChainLink link) {
        // Conditions
        if (null != link.getConditionEventId()) {
            try {
                EventConfiguration conf = MasterTable.getEventConfiguration(link.getConditionEventId());
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
}
