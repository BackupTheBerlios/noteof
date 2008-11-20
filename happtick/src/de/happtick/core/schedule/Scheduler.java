package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.ChainLink;
import de.happtick.configuration.EventConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.util.Scheduling;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EventFinder;
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

    /*
     * Initialize...
     */
    private Scheduler() {
        NotEOFConfiguration conf = new LocalConfiguration();

        // Start Event Handler
        SchedulerObserver observer = new SchedulerObserver();
        Thread observerThread = new Thread(observer);
        observerThread.start();

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
                    conf.getEventClassName();
                    NotEOFEvent event = EventFinder.getNotEOFEvent(Server.getApplicationHome(), conf.getEventClassName());
                    observedEvents.add(event.getEventType());
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

            // TODO
            // rufe funktion auf, die ALLE events auswertet und entspr. der
            // Konfiguration handhabt
            // Evtl. in eigenem Thread...
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
                startChainScheduler(conf);
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Start der Chain-Scheduler.", afx);
        }
    }

    private ChainScheduler startChainScheduler(ChainConfiguration conf) {
        ChainScheduler chainSched = new ChainScheduler(conf);
        Thread thread = new Thread(chainSched);
        thread.start();
        return chainSched;
    }

    // TODO ChainScheduler evtl. als Observer, weil der doch jede Menge events
    // abfangen muss
    // Wenn ein Event rein kommt, prufen, ob prevent oder condition und ob auch
    // die uebrigen Parameter passen (key, value). Wenn ja mit setEvent in die
    // interne Liste...
    private class ChainScheduler implements Runnable, EventObserver {
        private ChainConfiguration conf;
        private Map<EventType, NotEOFEvent> uniqueEvents = new HashMap<EventType, NotEOFEvent>();
        // TODO String: applicationType + application id +
        private Map<String, Long> conditionEvents;
        private Map<String, Long> preventEvents;
        private boolean stopped = false;
        private List<EventType> observedEvents;
        private List<Long> observedConfigurationIds;
        // TODO lastStartedListIndex -> so weiss ich, welche appl. als
        // naechste
        // dran ist.
        private int lastStartedListIndex = -1;

        protected ChainScheduler(ChainConfiguration conf) {
            this.conf = conf;
            try {
                // filter events and configurations which the chain is
                // interested in
                this.observedEvents = Scheduling.filterObservedEvents("chain", conf.getChainId(), MasterTable.getEventConfigurationsAsList());
                //TODO hinzufügen der events, die als condition oder prevent in den chain-konfigurationen sind
                for (ChainLink link :  conf.getChainLinkList()){
                    if (link.getConditionEventId())
                }
                this.observedConfigurationIds = Scheduling.filterObservedConfigurations("chain", conf.getChainId(), MasterTable.getEventConfigurationsAsList());
            } catch (ActionFailedException e) {
                LocalLog.warn("Scheduling für Chain konnte nicht aktiviert werden.", e);
            }
            // Events are filtered - now register as observer
            Server.getInstance().registerForEvents(this);
        }

        protected void setEvent(NotEOFEvent event) {
            String action = Scheduling.filterActionOfEvent(event, this.observedConfigurationIds);

            uniqueEvents.put(event.getEventType(), event);
        }

        protected void clear() {
            uniqueEvents.clear();
        }

        /*
         * Check what to do with the next application
         */
        private boolean conditionsValid() {
            // TODO pruefen, ob kein prevent, und ob condition ok
            // dann das event loeschen
            // condition: warten bis condition mit entspr. Werten eintritt;
            // bleibt bis clear erhalten
            // prevent: verhindert den Start der Anwendung bis zum clear
            return false;
        }

        protected void stop() {
            stopped = true;
        }

        // TODO
        // vor dem Start jeder anwendung prï¿½fen, ob conditionevent oder
        // preventevent vorliegt
        // erste anwendung starten
        // mit jedem einer hier gestarteten anwendung erhaltenen stopp die
        // nï¿½chste anwendung starten, wenn bedingungen (events) erfï¿½llt

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
            if (EventType.EVENT_ACTION)
            
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
