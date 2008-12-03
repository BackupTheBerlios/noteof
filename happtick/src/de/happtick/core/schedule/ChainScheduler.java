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
            // Hinzuf�gen der events, die als condition oder prevent in
            // den chain-konfigurationen sind
            for (ChainLink link : conf.getChainLinkList()) {
                Scheduling.updateObservedEventsForChain(this.observedEvents, expectedEventActions, link);
            }
        } catch (ActionFailedException e) {
            LocalLog.warn("Scheduling f�r Chain konnte nicht aktiviert werden.", e);
        }
        // Events are filtered - now register as observer
        Server.getInstance().registerForEvents(this);

    }

    protected synchronized void setEvent(NotEOFEvent event) {
        // Sonderfall StopEvent
        if (EventType.EVENT_CHAIN_STOP.equals(event.getEventType()) && //
                String.valueOf(conf.getChainId()).equals(event.getAttribute("addresseeId"))) {
            stop();
        }

        if (!stopped) {
            // Sonderfall StoppedEvent
            // Pruefen, ob das der Vorgaenger war
            if (EventType.EVENT_APPLICATION_STOPPED.equals(event.getEventType()) || //
                    EventType.EVENT_CHAIN_STOPPED.equals(event.getEventType())) {
                boolean executeNext = false;
                int lastStartedIndex = nextStartConfIndex - 1;
                if (lastStartedIndex < 0)
                    lastStartedIndex = conf.getChainLinkList().size() - 1;

                if (EventType.EVENT_APPLICATION_STOPPED.equals(event.getEventType()) && //
                        "application".equalsIgnoreCase(conf.getChainLinkList().get(lastStartedIndex).getAddresseeType())) {
                    if (conf.getChainLinkList().get(lastStartedIndex).getAddresseeId().equals(event.getApplicationId())) {
                        executeNext = true;
                    }
                }
                if (EventType.EVENT_CHAIN_STOPPED.equals(event.getEventType()) && //
                        "chain".equalsIgnoreCase(conf.getChainLinkList().get(lastStartedIndex).getAddresseeType())) {
                    if (conf.getChainLinkList().get(lastStartedIndex).getAddresseeId().equals(Util.parseLong(event.getAttribute("chainId"), -1))) {
                        executeNext = true;
                    }
                }

                if (executeNext) {
                    // application or sub-chain of this chain was stopped
                    try {
                        // if sub-chain or loop is not allowed and the stopped
                        // application/ chain was the last of the chain then
                        // stop this chain
                        if ((conf.isDepends() || !conf.isLoop()) && chainEndReached) {
                            // Ende-Event losschicken
                            ChainStoppedEvent stoppedEvent = new ChainStoppedEvent();
                            Server.getInstance().unregisterFromEvents(this);
                            try {
                                stoppedEvent.addAttribute("chainId", String.valueOf(conf.getChainId()));
                                Server.getInstance().updateObservers(null, stoppedEvent);
                                this.stop();
                            } catch (ActionFailedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!stopped) {
                            // OK - next chain or application please
                            startChainAddressee();
                        }
                    } catch (HapptickException e) {
                        LocalLog.error("Fehler bei Start nach Erhalt eines Stopp-Events des Vorgaengers.", e);
                    }
                }
            }
        }

        if (!stopped) {
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
        try {
            // Ignition
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
        return hashCode() + ":" + this.getClass().getSimpleName();
    }

    @Override
    public List<EventType> getObservedEvents() {
        return this.observedEvents;
    }

    @Override
    public void update(Service arg0, NotEOFEvent event) {
        this.setEvent(event);
    }

    private synchronized void startChainAddressee() throws HapptickException {
        Long addresseeId = conf.getChainLinkList().get(nextStartConfIndex).getAddresseeId();
        String addresseeType = conf.getChainLinkList().get(nextStartConfIndex).getAddresseeType();
        ChainLink link = conf.getChainLinkList().get(nextStartConfIndex);

        try {
            // application
            if ("application".equalsIgnoreCase(addresseeType)) {
                // Start application
                ApplicationConfiguration applConf = MasterTable.getApplicationConfiguration(addresseeId);
                while (!conditionsValid(link)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Scheduling.startApplication(applConf);
            }
            // chain
            if ("chain".equalsIgnoreCase(addresseeType)) {
                // Start application
                ChainConfiguration chainConf = MasterTable.getChainConfiguration(addresseeId);
                while (!conditionsValid(link)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                scheduler.startChainScheduler(chainConf);
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
        String reason = "";
        boolean conditionEventFound = true;
        boolean preventEventFound = false;
        if (!Util.isEmpty(link.getConditionKey())) {
            conditionEventFound = false;
            reason = "Condition Event wurde bisher nicht gefeuert.";
            for (EventType type : observedEvents) {
                String typeName = type.name();
                String actionKey = typeName + link.getConditionKey() + link.getConditionValue();

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

        if (!Util.isEmpty(link.getPreventKey())) {
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

        // alle Bedingungen erfuellt
        return true;
    }

}
