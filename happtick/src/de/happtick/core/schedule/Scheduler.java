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
import de.happtick.configuration.EventConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.events.ChainStoppedEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.Scheduling;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notIOC.configuration.ConfigurationManager;

/**
 * The scheduler is not available from outside exept the start method.
 * 
 * @author dirk
 * 
 */
public class Scheduler {
    private static Scheduler scheduler = new Scheduler();
    private EventHandler eventHandler;

    /*
     * Initialize...
     */
    private Scheduler() {
        NotEOFConfiguration conf = new LocalConfiguration();

        // Start Observer who gets all fired Events
        new Thread(new SchedulerObserver()).start();

        // Start Event Handler which decides what to do with the events by
        // regarding the configuration
        eventHandler = new EventHandler();
        new Thread(eventHandler).start();

        // Standard via timer
        try {
            Boolean useTimer = Util.parseBoolean(conf.getAttribute("scheduler.use", "timer"), false);
            if (useTimer) {
                startAllApplicationSchedulers();
            }

            // process chain is active
            Boolean useChain = Util.parseBoolean(conf.getAttribute("scheduler.use", "chain"), false);
            if (useChain) {
                startAllChainSchedulers();
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Lesen der Konfiguration fï¿½r Anwendungsscheduler.", afx);
        }
    }

    /**
     * Start from outer side - normally not needed
     */
    public static void start() {
        if (null == scheduler) {
            scheduler = new Scheduler();
        }
    }

    private class EventHandler implements Runnable {

        protected void handleEvent(NotEOFEvent event) {
            // TODO wenn Generic reinkommt, attribut 'aliasName' auswerten
            // bei Versand von Generic aliasName setzen

        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /*
     * Observes the events for starting applications or to avoid the starts.
     */
    private class SchedulerObserver implements Runnable, EventObserver {
        List<EventType> observedEvents = new ArrayList<EventType>();;

        protected SchedulerObserver() {
            // before registering add events
            observedEvents.add(EventType.EVENT_START_ERROR);
            observedEvents.add(EventType.EVENT_APPLICATION_STARTED);
            observedEvents.add(EventType.EVENT_APPLICATION_STOPPED);

            // all events from configuration are important
            try {
                for (EventConfiguration conf : MasterTable.getEventConfigurationsAsList()) {
                    observedEvents.add(Util.lookForEventType(conf.getEventClassName()));
                }
            } catch (ActionFailedException e) {
                LocalLog
                        .warn("Fehler bei Registrierung der Events. Evtl. werden nicht alle Events im Scheduling korrekt verarbeitet oder berï¿½cksichtigt.", e);
            }

            // list completed - now register
            Server.getInstance().registerForEvents(this);
        }

        public String getName() {
            return this.getClass().getSimpleName();
        }

        public List<EventType> getObservedEvents() {
            return this.observedEvents;
        }

        public void update(Service service, NotEOFEvent event) {
            // Application process now running
            // Activate dependent applications
            if (EventType.EVENT_APPLICATION_STARTED.equals(event.getEventType())) {
                MasterTable.replaceStartEvent(event);
            }

            // Application process stopped
            // Service removes itself from the MasterTable
            if (EventType.EVENT_APPLICATION_STOPPED.equals(event.getEventType())) {
                MasterTable.removeStartEvent(event);

                // start dependent applications
                Long stoppedApplId = event.getApplicationId();
                ApplicationConfiguration stoppedConf = MasterTable.getApplicationConfiguration(stoppedApplId);
                for (Long applId : stoppedConf.getApplicationsStartAfter()) {
                    ApplicationConfiguration afterConf = MasterTable.getApplicationConfiguration(applId);
                    if (null != afterConf) {
                        startApplicationScheduler(afterConf);
                    }
                }
            }

            // Application process could not be started
            // Remove StartEvent
            if (EventType.EVENT_START_ERROR.equals(event.getEventType())) {
                MasterTable.removeStartEvent(event);
            }

            // EventHandler processes all events additional to the observer
            eventHandler.handleEvent(event);
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    // TODO mehrere chains, die z.B. auf unterschiedlichen Rechnern laufen...
    // Version 1.2 ???

    // TODO Service Ã¼ber den der Scheduler gestoppt werden kann... der Service
    // schickt ein observe an MasterTable? ???

    /*
     * For each configured application start a runner. The runner observes the
     * start point.
     */
    private void startAllApplicationSchedulers() {
        try {
            for (ApplicationConfiguration conf : MasterTable.getApplicationConfigurationsAsList()) {
                startApplicationScheduler(conf);
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Start der Anwendungs-Scheduler.", afx);
        }
    }

    /*
     * For each configured chain start a runner. The runner observes the start
     * points. Put the runner into a list.
     */
    private void startAllChainSchedulers() {
        try {
            for (ChainConfiguration conf : MasterTable.getChainConfigurationsAsList()) {
                // start only if this chain is not part of another chain
                try {
                    if (!conf.isDepends())
                        startChainScheduler(conf);
                } catch (ActionFailedException afx) {
                    LocalLog.error("Fehler bei Start eines Chain-Scheduler.", afx);
                }
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Start der Chain-Scheduler.", afx);
        }
    }

    private ChainScheduler startChainScheduler(ChainConfiguration conf) throws HapptickException {
        ChainScheduler chainSched = new ChainScheduler(conf);
        Thread thread = new Thread(chainSched);
        thread.start();
        return chainSched;
    }

    private class ChainScheduler implements Runnable, EventObserver {
        private ChainConfiguration conf;
        private Map<String, ChainAction> raisedEventActions = new HashMap<String, ChainAction>();
        private Map<String, ChainAction> expectedEventActions;
        private boolean stopped = false;
        private List<EventType> observedEvents;
        private int nextStartConfIndex = 0;

        /*
         * Register events, store actions for fast detection and starting of
         * applications or chains. Start first application.
         */
        protected ChainScheduler(ChainConfiguration conf) throws HapptickException {
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
                this.observedEvents = Scheduling.filterObservedEventsForChain(conf.getChainId(), expectedEventActions, MasterTable
                        .getEventConfigurationsAsList());
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

            // Ignition
            try {
                startAddressee();
            } catch (HapptickException e) {
                LocalLog.error("Fehler bei Initialisierung eines ChainSchedulers.", e);
            }
        }

        protected void setEvent(NotEOFEvent event) {
            // wenn gestoppt - keine weitere Verarbeitung von events
            if (stopped)
                return;

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
                boolean chainEndReached = false;
                int index = nextStartConfIndex - 1;
                if (index < 0) {
                    index = conf.getChainLinkList().size() - 1;
                    chainEndReached = true;
                }

                if (EventType.EVENT_APPLICATION_STOPPED.equals(event.getEventType())) {
                    if (String.valueOf(conf.getChainLinkList().get(index).getAddresseeId()).equals(event.getAttribute("applicationId"))) {
                        executeNext = true;
                    }
                }
                if (EventType.EVENT_CHAIN_STOPPED.equals(event.getEventType())) {
                    if (String.valueOf(conf.getChainLinkList().get(index).getAddresseeId()).equals(event.getAttribute("chainId"))) {
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
                            try {
                                this.stop();
                                stoppedEvent.addAttribute("chainId", String.valueOf(conf.getChainId()));
                                Server.getInstance().updateObservers(null, stoppedEvent);
                                // keine weitere Verarbeitung!
                                return;
                            } catch (ActionFailedException e) {
                            }
                        }

                        // OK - next chain or application please
                        startAddressee();
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

        /*
         * Startet eine Anwendung oder eine Chain
         */
        private void startAddressee() throws HapptickException {
            Long addresseeId = conf.getChainLinkList().get(nextStartConfIndex).getAddresseeId();
            String addresseeType = conf.getChainLinkList().get(nextStartConfIndex).getAddresseeType();
            ChainLink link = conf.getChainLinkList().get(nextStartConfIndex);

            try {
                // application
                if ("application".equalsIgnoreCase(addresseeType)) {
                    // Start application
                    ApplicationConfiguration applConf = MasterTable.getApplicationConfiguration(addresseeId);
                    if (conditionsValid(link)) {
                        Scheduling.startApplication(applConf);
                    }
                }
                // chain
                if ("chain".equalsIgnoreCase(addresseeType)) {
                    // Start application
                    ChainConfiguration chainConf = MasterTable.getChainConfiguration(addresseeId);
                    if (conditionsValid(link)) {
                        startChainScheduler(chainConf);
                    }
                }
                ++nextStartConfIndex;
                if (nextStartConfIndex >= conf.getChainLinkList().size()) {
                    nextStartConfIndex = 0;
                }
            } catch (ActionFailedException e) {
                throw new HapptickException(502L, addresseeType, e);
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
                for (EventType type : this.observedEvents) {
                    String typeName = type.name();
                    String actionKey = typeName + link.getConditionKey() + link.getConditionValue();

                    ChainAction action = raisedEventActions.get(actionKey);
                    if (!Util.isEmpty(action)) {
                        // suche starterlaubnis
                        if ("prevent".equalsIgnoreCase(action.getAction())) {
                            reason = "Condition Event wurde bisher nicht gefeuert.";
                            conditionEventFound = true;
                        }
                    }
                }
            }

            if (!Util.isEmpty(link.getPreventKey())) {
                preventEventFound = false;
                for (EventType type : this.observedEvents) {
                    String typeName = type.name();
                    String actionKey = typeName + link.getPreventKey() + link.getPreventValue();

                    ChainAction action = raisedEventActions.get(actionKey);
                    if (!Util.isEmpty(action)) {
                        // pruefe auf startverbot
                        if ("prevent".equalsIgnoreCase(action.getAction())) {
                            reason = "Prevent Event wurde gefeuert.";
                            preventEventFound = true;
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

        protected void stop() {
            stopped = true;
        }

        public void run() {
            try {
                while (!stopped) {
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                LocalLog.error("Scheduling fï¿½r Chain mit Id " + conf.getChainId() + " ist ausgefallen.", e);
            }
        }

        @Override
        public String getName() {
            return this.getClass().getCanonicalName();
        }

        @Override
        public List<EventType> getObservedEvents() {
            return this.observedEvents;
        }

        @Override
        public void update(Service arg0, NotEOFEvent arg1) {

        }
    }

    /*
     * Start the runner as thread
     */
    private ApplicationScheduler startApplicationScheduler(ApplicationConfiguration conf) {
        ApplicationScheduler appSched = new ApplicationScheduler(conf);
        Thread thread = new Thread(appSched);
        thread.start();
        return appSched;
    }

    private class ApplicationScheduler implements Runnable {
        private ApplicationConfiguration conf;

        protected ApplicationScheduler(ApplicationConfiguration conf) {
            this.conf = conf;
        }

        public void run() {
            try {
                while (true) {
                    // check if start allowed
                    if (conf.startAllowed()) {
                        // start application
                        Scheduling.startApplication(conf);

                        // applications to start syncronously
                        for (Long applId : conf.getApplicationsStartSync()) {
                            ApplicationConfiguration syncConf = MasterTable.getApplicationConfiguration(applId);
                            if (null != syncConf) {
                                startApplicationScheduler(syncConf);
                            }
                        }
                    }
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                LocalLog.error("Scheduling fï¿½r Applikation mit Id " + conf.getApplicationId() + " ist ausgefallen.", e);
            }
        }
    }

    public String getName() {
        return "Class: " + this.getClass().getName() + "; This is one of the central elements of Happtick.";
    }

    /**
     * Start the scheduler.
     * 
     * @param args
     * <br>
     *            --homeVar is the basic environment variable for the system to
     *            find libs and configuration. Default is HAPPTICK_HOME <br>
     *            --baseConfDir is the directory where the configuration files
     *            are stored (e.g. if value is set to 'conf' the system will
     *            search files in $HAPPTICK_HOME/conf/). <br>
     *            --baseConfFile is the central configuration file. Default is
     *            happtick_master.xml <br>
     */
    public static void main(String... args) {
        // TODO hilfe einbasteln
        String homeVar = "HAPPTICK_HOME";
        String baseConfFile = "happtick_master.xml";
        String baseConfDir = "conf";
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.containsStartsWith("--homeVar")) {
            homeVar = argsParser.getValue("homeVar");
        }
        if (argsParser.containsStartsWith("--baseConfFile")) {
            baseConfFile = argsParser.getValue("baseConfFile");
        }
        if (argsParser.containsStartsWith("--baseConfPath")) {
            baseConfDir = argsParser.getValue("baseConfPath");
        }
        ConfigurationManager.setInitialEnvironment(homeVar, baseConfDir, baseConfFile);

        Scheduler.start();
    }
}
