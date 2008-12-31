package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.EventConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.event.ApplicationStartErrorEvent;
import de.happtick.core.util.ExternalCalls;
import de.happtick.core.util.Scheduling;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
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
    private static Scheduler scheduler; // = new Scheduler();

    /*
     * Initialize...
     */
    private Scheduler() {
        // Start Observer who gets all fired Events
        new Thread(new SchedulerObserver()).start();

        if (MasterTable.isSchedulerUsed()) {
            startAllApplicationSchedulers();
        }

        // process chain is active
        if (MasterTable.isChainsUsed()) {
            startAllChainSchedulers();
        }

        if (!(MasterTable.isSchedulerUsed() || MasterTable.isChainsUsed())) {
            LocalLog.warn("Happtick Scheduler: Weder applications noch chains sind aktiv. Scheduler wird beendet.");
            System.exit(1);
        }

        // start the EventChecker
        new Thread(new StartedEventChecker()).start();
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
     * This class looks if there are old ApplicationStartEvents. If old events
     * found, it raises an AlarmEvent. Old StartEvents are deleted by the
     * scheduler! so this class doesn't deletes Events.
     */
    private class StartedEventChecker implements Runnable {

        @Override
        public void run() {
            while (true) {
                for (NotEOFEvent event : MasterTable.getStartEventsAsList()) {
                    if (event.equals(EventType.EVENT_APPLICATION_START)) {
                        Long timeStamp = event.getTimeStampSend();
                        long maxWaitTime = MasterTable.getMaxDelay();
                        // older than 30 seconds?
                        if (maxWaitTime < new Date().getTime() - timeStamp) {
                            // older - fire StartError
                            NotEOFEvent startAlarm = new ApplicationStartErrorEvent();
                            try {
                                startAlarm.addAttribute("workApplicationId", event.getAttribute("workApplicationId"));
                                startAlarm.addAttribute("clientNetId", "Scheduler");
                                startAlarm.addAttribute("startId", "?");
                                startAlarm.addAttribute("errorDescription", "Scheduler Ueberpruefung: IgnitionZeit (" + Math.abs(maxWaitTime / 1000)
                                        + ") + ueberschritten.");
                                startAlarm.addAttribute("errorId", String.valueOf(0));
                                startAlarm.addAttribute("errorLevel", "0");
                                startAlarm.addAttribute("startIgnitionTime", String.valueOf(event.getTimeStampSend()));

                                // send alarm
                                Scheduling.raiseEvent(startAlarm);
                            } catch (ActionFailedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LocalLog.warn("Die Klasse StartedEventChecker zur Ueberwachung nicht gestarteter Applikationen ist ausgefallen.");
                }
            }
        }
    }

    /*
     * This class processes the events
     */
    private class EventHandler implements Runnable {
        private NotEOFEvent event;

        protected EventHandler(NotEOFEvent event) {
            this.event = event;
        }

        /*
         * some events are handled directly by chain runners or application
         * runners (s. below)
         */
        protected void handleEvent() {
            // search for the configuration to this event
            List<EventConfiguration> eventConfigurations = Scheduling.getEventConfigurationsForEventType(this.event.getEventType());
            List<EventConfiguration> actionEventConfigurations = Scheduling.getEventConfigurationsForEvent(this.event, eventConfigurations);

            // if configuration found look what to do...
            if (!Util.isEmpty(actionEventConfigurations)) {
                for (EventConfiguration eConf : actionEventConfigurations) {
                    // start, stop, ignite
                    String action = eConf.getAction();

                    try {
                        if ("application".equalsIgnoreCase(eConf.getAddresseeType())) {
                            // only application related events
                            ApplicationConfiguration applConf = MasterTable.getApplicationConfiguration(eConf.getAddresseeId());

                            if ("start".equals(action)) {
                                Scheduling.startApplication(applConf);
                            }
                            if ("stop".equals(action)) {
                                Scheduling.stopApplication(applConf);
                            }
                        }

                        if ("event".equalsIgnoreCase(eConf.getAddresseeType())) {
                            EventConfiguration eventConf = MasterTable.getEventConfiguration(eConf.getAddresseeId());
                            if ("ignite".equals(action)) {
                                String eventClassName = eventConf.getEventClassName();
                                if (eventClassName.startsWith("Alias:")) {
                                    // NotEOFEvent newEvent =
                                    // Util.getGenericEvent(eventClassName,
                                    // false);
                                    NotEOFEvent newEvent = MasterTable.getOwnEvent(eventClassName);
                                    Scheduling.raiseEvent(newEvent);
                                } else {
                                    LocalLog
                                            .warn("Nur konfigurierte Events koennen mit ignite ausgeloest werden. Konfigurierte Events sind am Prefix 'Alias:' erkennbar.");
                                }
                            }
                        }
                    } catch (ActionFailedException e) {
                        LocalLog.error("Fehler bei Bearbeitung eintreffender Events.", e);
                    }
                }
            }
        }

        @Override
        public void run() {
            handleEvent();
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
            observedEvents.add(EventType.EVENT_GENERIC);
            observedEvents.add(EventType.EVENT_APPLICATION_START);
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
            return hashCode() + ":" + this.getClass().getSimpleName();
        }

        public List<EventType> getObservedEvents() {
            return this.observedEvents;
        }

        public synchronized void update(Service service, NotEOFEvent event) {
            // Application started
            if (event.equals(EventType.EVENT_APPLICATION_START)) {
                System.out.println("Scheduler$SchedulerObserver. Füge StartEvent hinzu.");
                MasterTable.putStartEvent(event);
            }

            // Application process now running
            // Activate dependent applications
            if (event.equals(EventType.EVENT_APPLICATION_STARTED)) {
                System.out.println("Scheduler$SchedulerObserver. Füge StartED!Event hinzu.");
                MasterTable.replaceStartEvent(event);
            }

            // Application process stopped
            // Service removes itself from the MasterTable
            if (event.equals(EventType.EVENT_APPLICATION_STOPPED)) {
                Long stoppedApplId = Util.parseLong(event.getAttribute("workApplicationId"), -1);
                MasterTable.removeStartEvent(event);

                // start dependent applications
                try {
                    ApplicationConfiguration stoppedConf = MasterTable.getApplicationConfiguration(stoppedApplId);
                    for (Long applId : stoppedConf.getApplicationsStartAfter()) {
                        ApplicationConfiguration afterConf = MasterTable.getApplicationConfiguration(applId);
                        if (null != afterConf) {
                            Scheduling.startApplication(afterConf);
                        }
                    }
                } catch (ActionFailedException e) {
                    LocalLog.warn("Fehler bei Verarbeitung eines events: " + event.getEventType().name(), e);
                }
            }

            // Application process could not be started
            // Remove StartEvent
            if (event.equals(EventType.EVENT_START_ERROR)) {
                System.out.println("Scheduler$SchedulerObserver. Entferne StartEvent.");
                MasterTable.removeStartEvent(event);
            }

            // EventHandler Threads process all events independent to the
            // observer
            if (MasterTable.isEventsUsed()) {
                new Thread(new EventHandler(event)).start();
            }
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
     * For each configured application which is started by timeplan! start a
     * runner. The runner observes the start point.
     */
    private void startAllApplicationSchedulers() {
        try {
            for (ApplicationConfiguration conf : MasterTable.getApplicationConfigurationsAsList()) {
                if (conf.hasTimePlan())
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
                    if (!conf.isDepends()) {
                        startChainScheduler(conf);
                    }
                } catch (ActionFailedException afx) {
                    LocalLog.error("Fehler bei Start eines Chain-Scheduler.", afx);
                }
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Start der Chain-Scheduler.", afx);
        }
    }

    protected void startChainScheduler(ChainConfiguration conf) throws ActionFailedException {
        new Thread(new ChainScheduler(conf, this)).start();
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

    public String getName() {
        return hashCode() + ":" + this.getClass().getSimpleName();
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
            System.out.println("Scheduler.main: homeVar = " + homeVar);
        }
        if (argsParser.containsStartsWith("--baseConfFile")) {
            baseConfFile = argsParser.getValue("baseConfFile");
        }
        if (argsParser.containsStartsWith("--baseConfPath")) {
            baseConfDir = argsParser.getValue("baseConfPath");
        }

        ConfigurationManager.setInitialEnvironment(homeVar, baseConfDir, baseConfFile);

        ExternalCalls calls = new ExternalCalls();
        LocalLog.info("!EOF Server wird als Teil des Happtick gestartet.");
        calls.call("de.notEOF.core.server.Server", args);

        Scheduler.start();
    }
}
