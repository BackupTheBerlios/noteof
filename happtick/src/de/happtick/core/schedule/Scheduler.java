package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.events.StartEvent;
import de.happtick.core.util.Scheduling;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObservable;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
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

    private List<ApplicationScheduler> applicationSchedulers = new ArrayList<ApplicationScheduler>();
    private static Scheduler scheduler = new Scheduler();

    /*
     * Initialize...
     */
    private Scheduler() {
        NotEOFConfiguration conf = new LocalConfiguration();
        // Standard via timer
        try {
            Boolean useTimer = Util.parseBoolean(conf.getAttribute("scheduler.use", "timer"), false);
            if (useTimer) {
                startAllApplicationSchedulers();

                SchedulerGarbage garbage = new SchedulerGarbage();
                Thread garbageThread = new Thread(garbage);
                garbageThread.start();
            }

            // process chain is active
            Boolean useChain = Util.parseBoolean(conf.getAttribute("scheduler.use", "chain"), false);
            if (useChain) {

            }
            // events are active
            Boolean useEvents = Util.parseBoolean(conf.getAttribute("scheduler.use", "event"), false);
            if (useEvents) {
                // TODO
                /*
                 * Alle events in der konfiguration - auch die der chains nehmen
                 * als observer an util klasse anmelden
                 */
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Lesen der Konfiguration für Anwendungsscheduler.", afx);
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

    private class SchedulerObserver implements Runnable, EventObserver {

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        // TODO Events ergaenzen start, stopp, start error...
        public List<EventType> getObservedEvents() {
            List<EventType> observedEvents = new ArrayList<EventType>();
            observedEvents.add(EventType.EVENT_START_ERROR);
            observedEvents.add(EventType.EVENT_CLIENT_STOPPED);
            return null;
        }

        @Override
        public void update(Service service, NotEOFEvent event) {
            // TODO Auto-generated method stub

        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

        }

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
     * @return True if start is now allowed. False if not.
     */
    public boolean isStartAllowed(ApplicationConfiguration applicationConfiguration) {

        // durchsuche alle StartServices. wenn startservice = startservice ok.
        // ansonsten vergleiche konfiguration und applikationsid
        // berücksichtigen applicationservices

        return false;
    }

    // TODO Prozesskette werden einfach nur einmalig angestossen
    // dann werden die Anwendungen einfach gestartet

    // TODO mehrere chains, die z.B. auf unterschiedlichen Rechnern laufen...
    // Version 1.2 ???

    // TODO Startet fÃ¼r jede Anwendung einen Thread. Die Threads Ã¼berwachen
    // die Startzeiten
    // Runner beenden sich selbst, wenn ihr Service sich beendet...

    // TODO Service Ã¼ber den der Scheduler gestoppt werden kann... der Service
    // schickt ein observe an MasterTable? ???
    // Das event wird hier zyklisch abgefragt ???

    /*
     * For each configured application start a runner. The runner observes the
     * start point. Put the runner into a list.
     */
    private void startAllApplicationSchedulers() {
        try {
            for (ApplicationConfiguration conf : MasterTable.getApplicationConfigurationsAsList()) {
                startApplicationScheduler(conf);
            }
        } catch (ActionFailedException afx) {
            LocalLog.error("Fehler bei Start der Anwendungsscheduler.", afx);
        }
    }

    /*
     * Start the runner as thread
     */
    private ApplicationScheduler startApplicationScheduler(ApplicationConfiguration configuration) {
        ApplicationScheduler appSched;
        LocalLog.warn("Nicht aktiver StartService. Id: " + configuration.getApplicationId() + "; clientIp: " + configuration.getClientIp());
        // return null;
        appSched = new ApplicationScheduler(configuration, false);
        applicationSchedulers.add(appSched);
        Thread thread = new Thread(appSched);
        appSched.setThread(thread);
        thread.start();
        return appSched;
    }

    private class ApplicationScheduler implements Runnable, EventObserver, EventObservable {
        private Thread thread;
        private boolean stopped = false;
        // private boolean appServiceStopped = false;
        private boolean chainMode = false;
        private ApplicationConfiguration applicationConfiguration;

        protected ApplicationScheduler(ApplicationConfiguration applicationConfiguration, boolean chainMode) {
            this.applicationConfiguration = applicationConfiguration;
            this.chainMode = chainMode;
        }

        protected boolean hasStopped() {
            return stopped;
        }

        protected void setThread(Thread thread) {
            this.thread = thread;
        }

        protected Thread getThread() {
            return this.thread;
        }

        public void run() {
            try {
                while (!stopped) {
                    // check if start allowed
                    if (applicationConfiguration.startAllowed(chainMode)) {
                        // start application
                        Scheduling.startApplication(applicationConfiguration);

                        // after starting the application check other
                        // dependencies
                        // applications to start syncronously
                        for (Long applId : applicationConfiguration.getApplicationsStartSync()) {
                            ApplicationConfiguration syncConf = MasterTable.getApplicationConfiguration(applId);
                            if (null != syncConf) {
                                startApplicationScheduler(syncConf);
                            }
                        }

                        // TODO die start after werden mit events ausgelöst
                        // start dependent applications
                        // for (Long applId :
                        // applicationConfiguration.getApplicationsStartAfter())
                        // {
                        // ApplicationConfiguration afterConf =
                        // MasterTable.getApplicationConfiguration(applId);
                        // if (null != afterConf) {
                        // startApplicationScheduler(afterConf);
                        // }
                        // }

                    }
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            stopped = true;
        }

        // TODO hier fehlt wohl einiges...
        public List<EventType> getObservedEvents() {
            List<EventType> list = new ArrayList<EventType>();
            list.add(EventType.EVENT_ALARM);
            list.add(EventType.EVENT_START_ERROR);
            return list;
        }

        public void registerForEvents(EventObserver observer) {
            // if (null == eventObservers)
            // eventObservers = new HashMap<String, EventObserver>();
            // Util.registerForEvents(eventObservers, observer);
        }

        public void unregisterFromEvents(EventObserver observer) {
            // Util.unregisterFromEvents(eventObservers, observer);
        }

        public void update(Service service, NotEOFEvent event) {
            // normally here only the stop signal from application service may
            // come in.
            // if (event.getEventType().equals(EventType.EVENT_CLIENT_STOPPED))
            // appServiceStopped = true;
        }

        public void updateAllObserver(Map<String, EventObserver> eventObservers, Service service, NotEOFEvent event) {
            Util.updateAllObserver(eventObservers, service, event);
        }

        public String getName() {
            return "Class: " + this.getClass().getName() + "; This is part of the central Scheduler class.";
        }

    }

    private class SchedulerGarbage implements Runnable {
        // TODO !!!
        private boolean stopped = false;

        protected void stop() {
            ChainStarter bla = new ChainStarter();
            bla.getObservedEvents();
            stopped = true;
        }

        // private void blabla() {
        // // StartService from MasterTable by clientIp
        // ApplicationConfiguration applConf =
        // MasterTable.getApplicationConfiguration(new Long(100));
        // String clientIp = applConf.getClientIp();
        // StartService startService =
        // MasterTable.getStartServiceByIp(clientIp);
        //
        // }

        public void run() {
            boolean breaked = false;
            try {
                while (!stopped) {
                    // if breaked the list must be read once more...
                    while (breaked) {
                        breaked = false;
                        for (ApplicationScheduler appSched : applicationSchedulers) {
                            if (appSched.hasStopped()) {
                                applicationSchedulers.remove(appSched);
                                breaked = true;
                                break;
                            }
                        }
                    }
                    Thread.sleep(2000);
                }
            } catch (Exception ex) {
                stopped = true;
            }
        }
    }

    private class ChainStarter implements Runnable, EventObserver {

        // List of application id's decides the order of application starts
        private boolean stopped = false;
        private boolean loopChain;

        private String stoppedServiceId;

        public ChainStarter() {
            NotEOFConfiguration conf = new LocalConfiguration();
            try {
                loopChain = Util.parseBoolean(conf.getAttribute("scheduler.chain", "loop"), true);
            } catch (ActionFailedException e) {
                LocalLog.warn("Attribut 'loop' für chain-Konfiguration konnte nicht ermittelt werden.", e);
            }
        }

        public String getStoppedServiceId() {
            return stoppedServiceId;
        }

        // TODO chain startet runner. runner nicht vergessen in die runnerliste
        // einzutragen, da sonst der Müllmann nicht aufräumen kann.

        public void run() {
            try {
                // TODO Noch offen...
                do {
                    for (ChainConfiguration chainConf : MasterTable.getChainConfigurationsAsList()) {
                        System.out.println(chainConf.getChainId());
                    }
                } while (!stopped && loopChain);
            } catch (ActionFailedException afx) {
                LocalLog.error("Fehler in der run Methode des ChainStarter.", afx);
            }
        }

        public List<EventType> getObservedEvents() {
            List<EventType> types = new ArrayList<EventType>();
            types.add(EventType.EVENT_CLIENT_STOPPED);
            return types;
        }

        /**
         * May only act when StopEvent comes in! Normally the service is an
         * ApplicationService.
         */
        public void update(Service service, NotEOFEvent event) {
            if (!event.getEventType().equals(EventType.EVENT_CLIENT_STOPPED))
                return;
            try {
                stoppedServiceId = event.getAttribute("serviceId");
                String stopDate = event.getAttribute("stopDate");
                System.out.println(stopDate);
            } catch (Exception ex) {
                LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
            }
        }

        public String getName() {
            return "Class: " + this.getClass().getName() + "; This is part of the central Scheduler class.";
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
