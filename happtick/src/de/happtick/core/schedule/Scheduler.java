package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.LocalConfigurationClient;
import de.happtick.core.MasterTable;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.start.service.StartService;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.EventObserver;
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
        // Standard via timer
        Boolean useTimer = Util.parseBoolean(LocalConfigurationClient.getAttribute("scheduler.use", "timer", "false"), false);
        if (useTimer) {
            startAllApplicationRunners();

            SchedulerGarbage garbage = new SchedulerGarbage();
            Thread garbageThread = new Thread(garbage);
            garbageThread.start();
        }

        // process chain is active
        Boolean useChain = Util.parseBoolean(LocalConfigurationClient.getAttribute("scheduler.use", "chain", "false"), false);
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
    private void startAllApplicationRunners() {
        for (ApplicationConfiguration conf : MasterTable.getApplicationConfigurationsAsList()) {
            ApplicationScheduler appSched = startApplicationRunner(conf);
            if (null != appSched)
                applicationSchedulers.add(appSched);
        }
    }

    /*
     * Start the runner as thread
     */
    private ApplicationScheduler startApplicationRunner(ApplicationConfiguration configuration) {
        ApplicationScheduler appSched = new ApplicationScheduler(configuration);
        Thread thread = new Thread(appSched);
        appSched.setThread(thread);
        thread.start();
        return appSched;
    }

    /*
     * The runner asks here and the allowance is verified with other services,
     * configurations, runners
     */
    private boolean isStartAllowed(Long applicationId) {
        return false;
    }

    /*
     * Runner gets the StartService to tell him that the application must be
     * started. The Services are also stored in the MasterTable.
     */
    private StartService getStartService(String clientId) {
        return null;
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
     * used to identify the ApplicationService in the master tables.
     */
    private class ApplicationScheduler implements Runnable {
        private Thread thread;
        private boolean stopped = false;
        private ApplicationConfiguration conf;

        // TODO Wenn diese Klasse mit 'chain' aufgerufen wird, muss kein
        // scheduling durchgeführt werden.
        // Dann wird direkt der StartService benachrichtigt, der wiederum den
        // client verständigt...

        protected ApplicationScheduler(ApplicationConfiguration conf) {
            this.conf = conf;
            StartService startService = MasterTable.getStartServiceByIp(conf.getClientIp());
        }

        // TODO Die startId wird vom StartClient generiert. Der teilt die dem
        // StartService mit. Der 'richtige' Service ist derjenige, der auch die
        // startId hat...
        protected ApplicationService getApplicationService() {
            // TODO !!!
            return null;
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

            while (true) {
                // checkStartAllowed();
                break;
            }
            stopped = true;
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
                LocalLog.warn("Attribut 'loop' für chain-Konfiguration konnte nicht ermittelt werden.", e);
            }
        }

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
            stoppedServiceId = service.getServiceId();
        }

    }

    /*
     * prooves if the
     */
    private boolean checkStart(ApplicationConfiguration applConf) {
        Date nextStart = applConf.calculateNextStart();

        return false;
    }

}
