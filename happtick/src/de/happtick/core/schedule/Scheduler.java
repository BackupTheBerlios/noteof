package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.EventConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.util.Scheduling;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EventFinder;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObservable;
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
                startAllChainSchedulers(observer.getObservedEvents());
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Lesen der Konfiguration f�r Anwendungsscheduler.", afx);
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
                LocalLog.warn("Fehler bei Registrierung der Events. Evtl. werden nicht alle Events im Scheduling korrekt verarbeitet oder ber�cksichtigt.", e);
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

    // TODO Service über den der Scheduler gestoppt werden kann... der Service
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
    private void startAllChainSchedulers(List<EventType>observedEvents) {
        try {
            for (ChainConfiguration conf : MasterTable.getChainConfigurationsAsList()) {
                startChainScheduler(conf, observedEvents);
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Start der Chain-Scheduler.", afx);
        }
    }

    private ChainScheduler startChainScheduler(ChainConfiguration conf, List<EventType>observedEvents) {
        ChainScheduler chainSched = new ChainScheduler(conf, observedEvents);
        Thread thread = new Thread(chainSched);
        thread.start();
        return chainSched;
    }

    // TODO ChainScheduler evtl. als Observer, weil der doch jede Menge events
    // abfangen muss
    // Wenn ein Event rein kommt, prufen, ob prevent oder condition und ob auch
    // die �brigen Parameter passen (key, value). Wenn ja mit setEvent in die
    // interne Liste...
    private class ChainScheduler implements Runnable, EventObserver {
        private ChainConfiguration conf;
        private Map<EventType, NotEOFEvent> uniqueEvents = new HashMap<EventType, NotEOFEvent>();
        private boolean stopped = false;
        private List<EventType>observedEvents;
        // TODO lastStartedListIndex -> so wei� ich, welche appl. als n�chste
        // dran ist.
        private int lastStartedListIndex = -1;

        protected ChainScheduler(ChainConfiguration conf, List<EventType>observedEvents) {
            this.conf = conf;
            this.observedEvents = observedEvents;
            Server.getInstance().registerForEvents(this);
        }

        protected void setEvent(NotEOFEvent event) {
            if (event.getAttribute("actionType))
            
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
        // vor dem Start jeder anwendung pr�fen, ob conditionevent oder
        // preventevent vorliegt
        // erste anwendung starten
        // mit jedem einer hier gestarteten anwendung erhaltenen stopp die
        // n�chste anwendung starten, wenn bedingungen (events) erf�llt

        
        public void run() {
            try {
                while (!stopped) {
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                LocalLog.error("Scheduling f�r Chain mit Id " + conf.getChainId() + " ist ausgefallen.", e);
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
                LocalLog.error("Scheduling f�r Applikation mit Id " + conf.getApplicationId() + " ist ausgefallen.", e);
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
