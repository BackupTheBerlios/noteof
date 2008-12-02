package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.ChainLink;
import de.happtick.core.MasterTable;
import de.happtick.core.event.ChainStoppedEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.Scheduling;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.Util;

public class ChainScheduler implements EventObserver, Runnable {
    private ChainConfiguration conf;
    private Map<String, ChainAction> raisedEventActions = new HashMap<String, ChainAction>();
    private Map<String, ChainAction> expectedEventActions = new HashMap<String, ChainAction>();
    private boolean stopped = false;
    private List<EventType> observedEvents = new ArrayList<EventType>();
    private int nextStartConfIndex = 0;
    private boolean chainEndReached = false;
    private Scheduler scheduler;

    /*
     * Register events, store actions for fast detection and starting of
     * applications or chains. Start first application.
     */
    protected ChainScheduler(ChainConfiguration conf, Scheduler scheduler) throws HapptickException {
        if (Util.isEmpty(conf))
            throw new HapptickException(403L, "Configuration des Chain ist NULL oder leer.");

        this.scheduler = scheduler;
        this.conf = conf;
        // check if there are links
        if (Util.isEmpty(conf.getChainLinkList().size())) {
            LocalLog.warn("Chain-Konfiguration ohne Link-Angaben. ChainId: " + conf.getChainId());
            throw new HapptickException(403L, "ChainId: " + conf.getChainId());
        }

        try {
            // standard event 'stopped'
            this.observedEvents.add(EventType.EVENT_APPLICATION_STOPPED);
            this.observedEvents.add(EventType.EVENT_CHAIN_STOPPED);

            // filter events and configurations which the chain is
            // interested in
            Scheduling.filterObservedEventsForChain(conf.getChainId(), observedEvents, expectedEventActions, MasterTable.getEventConfigurationsAsList());
            // Hinzufügen der events, die als condition oder prevent in
            // den chain-konfigurationen sind
            for (ChainLink link : conf.getChainLinkList()) {
                Scheduling.updateObservedEventsForChain(this.observedEvents, expectedEventActions, link);
            }
        } catch (ActionFailedException e) {
            LocalLog.warn("Scheduling für Chain konnte nicht aktiviert werden.", e);
        }
        // Events are filtered - now register as observer
        Server.getInstance().registerForEvents(this);

    }

    protected void setEvent(NotEOFEvent event) {
        conf.getChainLinkList().get(0).getAddresseeId();
        conf.getChainLinkList().get(0).getAddresseeType();

        System.out.println("Scheduler$ChainScheduler.setEvent. event: " + event.getEventType());
        // wenn gestoppt - keine weitere Verarbeitung von events
        if (stopped) {
            System.out.println("ChainScheduler.setEvent. chainId: " + conf.getChainId() + "STOPPED");
            return;
        }

        // Sonderfall StopEvent
        if (EventType.EVENT_CHAIN_STOP.equals(event.getEventType()) && //
                String.valueOf(conf.getChainId()).equals(event.getAttribute("addresseeId"))) {
            stop();
            // this chain may not do anything more...
            return;
        }

        // Sonderfall StoppedEvent
        // Pruefen, ob das der Vorgaenger war
        if (EventType.EVENT_APPLICATION_STOPPED.equals(event.getEventType()) || //
                EventType.EVENT_CHAIN_STOPPED.equals(event.getEventType())) {
            boolean executeNext = false;
            System.out.println("Scheduler$ChainScheduler.setEvent anzahl = " + conf.getChainLinkList().size());
            System.out.println("Scheduler$ChainScheduler.setEvent index  = " + nextStartConfIndex);
            int lastStartedIndex = nextStartConfIndex - 1;
            if (lastStartedIndex < 0)
                lastStartedIndex = conf.getChainLinkList().size() - 1;

            System.out.println("Scheduler$ChainScheduler.setEvent zuvor id:     "
                    + String.valueOf(conf.getChainLinkList().get(lastStartedIndex).getAddresseeId()));
            System.out.println("Scheduler$ChainScheduler.setEvent zuvor type:     " + conf.getChainLinkList().get(lastStartedIndex).getAddresseeType());
            System.out.println("Scheduler$ChainScheduler.setEvent event id: " + event.getApplicationId());
            System.out.println("Scheduler$ChainScheduler.setEvent event type: " + event.getEventType());

            if (EventType.EVENT_APPLICATION_STOPPED.equals(event.getEventType()) && //
                    "application".equalsIgnoreCase(conf.getChainLinkList().get(lastStartedIndex).getAddresseeType())) {
                System.out.println("Scheduler$ChainScheduler.setEvent EVENT_APPLICATION_STOPPED von applicationId: " + event.getApplicationId());
                if (conf.getChainLinkList().get(lastStartedIndex).getAddresseeId().equals(event.getApplicationId())) {
                    executeNext = true;
                }
            }
            if (EventType.EVENT_CHAIN_STOPPED.equals(event.getEventType()) && //
                    "chain".equalsIgnoreCase(conf.getChainLinkList().get(lastStartedIndex).getAddresseeType())) {
                System.out.println("Scheduler$ChainScheduler.setEvent EVENT_CHAIN_STOPPED von chainId: " + event.getAttribute("chainId"));
                if (conf.getChainLinkList().get(lastStartedIndex).getAddresseeId().equals(Util.parseLong(event.getAttribute("chainId"), -1))) {
                    executeNext = true;
                }
            }

            System.out.println("Scheduler$ChainScheduler.setEvent executeNex: " + executeNext);
            if (executeNext) {
                // application or sub-chain of this chain was stopped
                try {
                    // if sub-chain or loop is not allowed and the stopped
                    // application/ chain was the last of the chain then
                    // stop this chain
                    if ((conf.isDepends() || !conf.isLoop()) && chainEndReached) {
                        // Ende-Event losschicken
                        ChainStoppedEvent stoppedEvent = new ChainStoppedEvent();
                        try {
                            this.stop();
                            Server.getInstance().unregisterFromEvents(this);
                            stoppedEvent.addAttribute("chainId", String.valueOf(conf.getChainId()));
                            System.out.println("Scheduler$ChainScheduler.setEvent sende ChainStoppedEvent");
                            Server.getInstance().updateObservers(null, stoppedEvent);
                            // keine weitere Verarbeitung!
                            return;
                        } catch (ActionFailedException e) {
                        }
                    }

                    // OK - next chain or application please
                    startChainAddressee();
                    // RETURN!
                    // Code below will not be used!
                    return;
                } catch (HapptickException e) {
                    LocalLog.error("Fehler bei Start nach Erhalt eines Stopp-Events des Vorgaengers.", e);
                }
            }
        }

        // Next code only will be executed if before there was no exit by
        // return!
        // typ + alle key-value paare durchforsten
        String eventTypeName = event.getEventType().name();
        Set<String> keySet = event.getAttributes().keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = event.getAttribute(key);

            String actionKey = eventTypeName + key + value;
            ChainAction action = expectedEventActions.get(actionKey);
            if (null != action) {
                // was ist mit der action zu tun?
                if ("reset".equalsIgnoreCase(action.getAction())) {
                    // wieder mit der ersten Anwendung starten, alle
                    // eingetroffen events loeschen
                    reset();
                }
                if ("clear".equalsIgnoreCase(action.getAction())) {
                    clear();
                }
                if ("prevent".equalsIgnoreCase(action.getAction()) || //
                        "condition".equalsIgnoreCase(action.getAction())) {
                    raisedEventActions.put(actionKey, action);
                }
            }
        }
    }

    private void clear() {
        raisedEventActions.clear();
    }

    private void reset() {
        nextStartConfIndex = 0;
        clear();
    }

    protected void stop() {
        stopped = true;
    }

    public void run() {
        System.out.println("Scheduler$ChainScheduler.Run begonnen. ChainId: " + conf.getChainId());
        try {
            // Ignition
            System.out.println("Scheduler$ChainScheduler.Construction. Ignition!");
            startChainAddressee();
            while (!stopped) {
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            LocalLog.error("Scheduling fuer Chain mit Id " + conf.getChainId() + " ist ausgefallen.", e);
        }
    }

    @Override
    public String getName() {
        return hashCode() + String.valueOf(Thread.currentThread().getId());
    }

    @Override
    public List<EventType> getObservedEvents() {
        return this.observedEvents;
    }

    @Override
    public void update(Service arg0, NotEOFEvent event) {
        this.setEvent(event);
    }

    // TODO !!!
    // diesen teil in thread
    // dann kann hier auf das fehlende conditionevent gewartet werden
    // oder auf das loeschen des preventevent.

    private void startChainAddressee() throws HapptickException {
        Long addresseeId = conf.getChainLinkList().get(nextStartConfIndex).getAddresseeId();
        String addresseeType = conf.getChainLinkList().get(nextStartConfIndex).getAddresseeType();
        ChainLink link = conf.getChainLinkList().get(nextStartConfIndex);

        System.out.println("Scheduler$ChainScheduler.startAddressee. nextStartConfIndex = " + nextStartConfIndex);

        try {
            // application
            System.out.println("Scheduler$ChainScheduler.startChainAddressee. Typ: " + addresseeType);
            if ("application".equalsIgnoreCase(addresseeType)) {
                // Start application
                ApplicationConfiguration applConf = MasterTable.getApplicationConfiguration(addresseeId);
                // TODO auslagern der gesamten methode in thread
                while (!conditionsValid(link)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
                Scheduling.startApplication(applConf);
            }
            // chain
            if ("chain".equalsIgnoreCase(addresseeType)) {
                // Start application
                ChainConfiguration chainConf = MasterTable.getChainConfiguration(addresseeId);
                // TODO auslagern der gesamten methode in thread
                while (!conditionsValid(link)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
                System.out.println("Scheduler$ChainScheduler.startChainAddressee. Chain wird gestartet...");
                scheduler.startChainScheduler(chainConf);
                System.out.println("Scheduler$ChainScheduler.startChainAddressee. Chain wurde gestartet...");
            }
            if (++nextStartConfIndex >= conf.getChainLinkList().size()) {
                chainEndReached = true;
                nextStartConfIndex = 0;
            }
        } catch (ActionFailedException e) {
            LocalLog.warn("Start einer Anwendung oder einer Chain innerhalb einer Chain ist fehlgeschlagen. ChainId: " + conf.getChainId() + "; AddresseeId: "
                    + addresseeId + "; AddresseeType: " + addresseeType, e);
        }
    }

    /*
     * Check if prevent or condition (if required) are fired before start
     * application or chain
     */
    private boolean conditionsValid(ChainLink link) {
        System.out.println("Scheduler.conditionsValid. Beginn der Prüfung.");
        System.out.println("Link.. id: " + link.getAddresseeId());
        System.out.println("Link.. type: " + link.getAddresseeType());
        System.out.println("Link.. cKey: " + link.getConditionKey());
        System.out.println("Link.. pKey: " + link.getPreventKey());
        String reason = "";
        boolean conditionEventFound = true;
        boolean preventEventFound = false;
        if (!Util.isEmpty(link.getConditionKey())) {
            System.out.println("Scheduler.conditionsValid. ConditionKey ist gesetzt.");
            conditionEventFound = false;
            reason = "Condition Event wurde bisher nicht gefeuert.";
            for (EventType type : observedEvents) {
                String typeName = type.name();
                String actionKey = typeName + link.getConditionKey() + link.getConditionValue();
                System.out.println("Scheduler.conditionsValid. ActionKey: " + actionKey);

                ChainAction action = raisedEventActions.get(actionKey);
                if (!Util.isEmpty(action)) {
                    // suche starterlaubnis
                    if ("condition".equalsIgnoreCase(action.getAction())) {
                        conditionEventFound = true;
                        break;
                    }
                }
            }
        }

        System.out.println("Scheduler.conditionsValid. Vor Prüfen auf Prevent.");
        if (!Util.isEmpty(link.getPreventKey())) {
            System.out.println("Scheduler.conditionsValid. PreventKey ist gesetzt.");
            preventEventFound = false;
            for (EventType type : observedEvents) {
                String typeName = type.name();
                String actionKey = typeName + link.getPreventKey() + link.getPreventValue();

                ChainAction action = raisedEventActions.get(actionKey);
                if (!Util.isEmpty(action)) {
                    // pruefe auf startverbot
                    if ("prevent".equalsIgnoreCase(action.getAction())) {
                        reason = "Prevent Event wurde gefeuert.";
                        preventEventFound = true;
                        break;
                    }
                }
            }
        }

        if (preventEventFound) {
            LocalLog.info("Startbedingung wurde nicht erfuellt. " + reason);
            return false;
        }
        if (!conditionEventFound) {
            LocalLog.info("Startbedingung wurde nicht erfuellt. " + reason);
            return false;
        }

        System.out.println("Scheduler.conditionsValid. Und nü? " + (!preventEventFound && conditionEventFound));
        // alle Bedingungen erfuellt
        return true;
    }

}
