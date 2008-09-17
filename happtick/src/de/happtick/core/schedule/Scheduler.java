package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.HapptickAlarmLevel;
import de.happtick.core.enumeration.HapptickAlarmType;
import de.happtick.core.events.ApplicationAlarmEvent;
import de.happtick.core.events.ApplicationStopEvent;
import de.happtick.core.start.service.StartService;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.ServiceStopEvent;
import de.notEOF.core.interfaces.EventObservable;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notIOC.configuration.ConfigurationManager;
import de.notIOC.exception.NotIOCException;

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
        Boolean useTimer = Util.parseBoolean(conf.getAttribute("scheduler.use", "timer", "false"), false);
        if (useTimer) {
            startAllApplicationSchedulers();

            SchedulerGarbage garbage = new SchedulerGarbage();
            Thread garbageThread = new Thread(garbage);
            garbageThread.start();
        }

        // process chain is active
        Boolean useChain = Util.parseBoolean(conf.getAttribute("scheduler.use", "chain", "false"), false);
        if (useChain) {

        }
    }

    /**
     * The only interface...
     */
    public static void start() {
        if (null == scheduler) {
            scheduler = new Scheduler();
        }
    }

    // TODO Prozesskette ebenfalls in runnern, die die Anwendungen abarbeiten.
    // Allerdings werden die runner nacheinander gestartet...

    // TODO mehrere chains, die z.B. auf unterschiedlichen Rechnern laufen...
    // Version 1.2 ???

    // TODO Startet für jede Anwendung einen Thread. Die Threads überwachen
    // die Startzeiten
    // Runner beenden sich selbst, wenn ihr Service sich beendet...

    // TODO Service über den der Scheduler gestoppt werden kann... der Service
    // schickt ein observe an MasterTable? ???
    // Das event wird hier zyklisch abgefragt ???

    /*
     * For each configured application start a runner. The runner observes the
     * start point. Put the runner into a list.
     */
    private void startAllApplicationSchedulers() {
        for (ApplicationConfiguration conf : MasterTable.getApplicationConfigurationsAsList()) {
            startApplicationScheduler(conf);
        }
    }

    /*
     * Start the runner as thread
     */
    private ApplicationScheduler startApplicationScheduler(ApplicationConfiguration configuration) {
        ApplicationScheduler appSched;
        StartService startService = MasterTable.getStartServiceByIp(configuration.getClientIp());
        if (null == startService) {
            LocalLog.warn("Nicht aktiver StartService. applicationId: " + configuration.getApplicationId() + "; clientIp: " + configuration.getClientIp());
            return null;
        }
        appSched = new ApplicationScheduler(configuration, startService, false);
        applicationSchedulers.add(appSched);
        Thread thread = new Thread(appSched);
        appSched.setThread(thread);
        thread.start();
        return appSched;
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

    /*
     * This class initializes the start of application by talking with
     * StartServices. The StartService gets a startId from Client. This id is
     * used to identify the ApplicationService in the master tables. If this
     * scheduler is not working in chain-mode after start the next start point
     * will be calculated.
     */
    private class ApplicationScheduler implements Runnable, EventObserver, EventObservable {
        private Thread thread;
        private boolean stopped = false;
        private boolean appServiceStopped = false;
        private boolean chainMode = false;
        private ApplicationConfiguration applicationConfiguration;
        private StartService startService;
        private List<EventObserver> eventObservers;

        protected ApplicationScheduler(ApplicationConfiguration applicationConfiguration, StartService startService, boolean chainMode) {
            this.applicationConfiguration = applicationConfiguration;
            this.startService = startService;
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
            // TODO offen

            try {
                while (!stopped) {
                    // check modes
                    if (chainMode) {
                        // This runner is part of a chain
                        // wait for other applications
                        while (MasterTable.mustWaitForApplication(applicationConfiguration)) {
                            Thread.sleep(300);
                        }
                        // tell StartService to start the application
                        // when the method startService.startApplication returns
                        // the client has started. After that there can
                        // time goes by until the ApplicationService exists.
                        String startId = startService.startApplication(applicationConfiguration);

                        // after starting the application check other
                        // dependencies
                        // applications to start syncronously
                        for (Long applId : applicationConfiguration.getApplicationsStartSync()) {
                            ApplicationConfiguration syncConf = MasterTable.getApplicationConfiguration(applId);
                            if (null != syncConf) {
                                startApplicationScheduler(syncConf);
                            }
                        }

                        // wait for activated ApplicationService
                        Date endDate = MasterTable.dateToWaitForApplicationService(null);
                        ApplicationService applicationService = null;
                        while (null == (applicationService = MasterTable.getApplicationServiceByStartId(startId)) && //
                                endDate.getTime() < System.currentTimeMillis()) {
                            Thread.sleep(500);
                        }

                        // Start of application not checkable
                        if (null == applicationService) {
                            // write log entry
                            LocalLog
                                    .warn("Anwendung sollte als Teil einer Kette gestartet werden. Start der Anwendung konnte nicht �berpr�ft werden. Kette wird unterbrochen. ApplicationId: "
                                            + applicationConfiguration.getApplicationId());
                            stopped = true;
                            // send to all observer an alarm event
                            updateAllObserver(eventObservers, null,
                                              new ApplicationAlarmEvent(HapptickAlarmType.INTERNAL_CHAIN_ALARM.ordinal(),
                                                      HapptickAlarmLevel.ALARM_LEVEL_SIGNIFICANT.ordinal(),
                                                      "Anwendung als Teil einer Kette wurde evtl. nicht gestartet. ApplicationId: "
                                                              + applicationConfiguration.getApplicationId()));
                            // stop working
                            break;
                        }

                        // start was successfull - the ApplicationService is
                        // running
                        // register at the ApplicationService and wait for
                        // application exit by event
                        applicationService.registerForEvents(this);
                        while (!appServiceStopped) {
                            Thread.sleep(500);
                        }

                        // Fire event to all observer
                        // probably a chain runner is waiting...
                        updateAllObserver(eventObservers, null, new ApplicationStopEvent(startService.getServiceId(), applicationConfiguration
                                .getApplicationId(), applicationService.getExitCode()));

                        // start dependent applications
                        for (Long applId : applicationConfiguration.getApplicationsStartAfter()) {
                            ApplicationConfiguration afterConf = MasterTable.getApplicationConfiguration(applId);
                            if (null != afterConf) {
                                startApplicationScheduler(afterConf);
                            }
                        }

                        // this runner was in chain mode and has done it's work
                        stopped = true;

                    } else {
                        // Time Plan Mode

                        // 2. ist enforce und keiner l�uft
                        // 3. ist startzeitpunkt erreicht und multiple
                        // 4. ist startzeitpunkt erreicht und nicht multiple
                        // 5. ist startzeitpunkt erreicht, dependencies
                        // ber�cksichtigen

                        // laeuft bereits? hier selbst gestartet?
                    }

                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            stopped = true;
        }

        public List<EventType> getObservedEvents() {
            List<EventType> list = new ArrayList<EventType>();
            // list.add(EventType.EVENT_ALARM);
            // list.add(EventType.EVENT_START);
            list.add(EventType.EVENT_STOP);
            return list;
        }

        public void registerForEvents(EventObserver observer) {
            if (null == eventObservers)
                eventObservers = new ArrayList<EventObserver>();
            eventObservers.add(observer);

        }

        public void update(Service service, NotEOFEvent event) {
            // normally here only the stop signal from application service may
            // come in.
            if (event.getEventType().equals(EventType.EVENT_STOP))
                appServiceStopped = true;
        }

        public void updateAllObserver(List<EventObserver> eventObserver, Service service, NotEOFEvent event) {
            Util.updateAllObserver(eventObserver, service, event);
        }
    }

    private class SchedulerGarbage implements Runnable {
        // TODO !!!
        private boolean stopped = false;

        protected void stop() {
            stopped = true;
        }

        private void blabla() {
            // StartService from MasterTable by clientIp
            ApplicationConfiguration applConf = MasterTable.getApplicationConfiguration(new Long(100));
            String clientIp = applConf.getClientIp();
            StartService startService = MasterTable.getStartServiceByIp(clientIp);

        }

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

    private class chainStarter implements Runnable, EventObserver {

        // List of applicationId's decides the order of application starts
        private boolean stopped = false;
        private boolean loopChain;
        private String stoppedServiceId;

        public chainStarter() {
            try {
                loopChain = Util.parseBoolean(LocalConfigurationClient.getAttribute("scheduler.chain", "loop"), true);
            } catch (NotIOCException e) {
                LocalLog.warn("Attribut 'loop' f�r chain-Konfiguration konnte nicht ermittelt werden.", e);
            }
        }

        // TODO chain startet runner. runner nicht vergessen in die runnerliste
        // einzutragen, da sonst der M�llmann nicht aufr�umen kann.

        public void run() {
            // TODO Noch offen...
            do {
                for (Long applicationId : MasterTable.getProcessChain()) {
                    ApplicationConfiguration applConf = MasterTable.getApplicationConfiguration(applicationId);
                    if (null != applConf) {
                        stoppedServiceId = "";
                        // Runner for application must deliver service
                        // so chainStarter can observe the service for stop
                        // event
                        // when stop event is raised and the service id is the
                        // same like here the next application may run.

                        ApplicationService applicationService = null;
                        while (!stopped && !stoppedServiceId.equals(applicationService.getServiceId())) {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
            } while (!stopped && loopChain);
        }

        public List<EventType> getObservedEvents() {
            List<EventType> types = new ArrayList<EventType>();
            types.add(EventType.EVENT_STOP);
            return types;
        }

        /**
         * May only act when StopEvent comes in! Normally the service is an
         * ApplicationService.
         */
        public void update(Service service, NotEOFEvent event) {
            if (!event.getEventType().equals(EventType.EVENT_STOP))
                return;
            stoppedServiceId = ((ServiceStopEvent) event).getServiceId();
        }

    }

}
