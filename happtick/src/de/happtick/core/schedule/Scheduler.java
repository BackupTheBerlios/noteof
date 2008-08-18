package de.happtick.core.schedule;

import java.util.ArrayList;
import java.util.List;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.start.service.StartService;
import de.notEOF.core.util.ArgsParser;
import de.notIOC.configuration.ConfigurationManager;

/**
 * The scheduler is not available from outside exept the start method.
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
        startAllApplicationRunners();
        
        SchedulerGarbage garbage = new SchedulerGarbage();
        Thread thread = new Thread(garbage);
        thread.start();
    }
    
    /**
     * The only interface...
     */
    public static void start() {
        if (null == scheduler) {
            scheduler = new Scheduler();
        }
    }
    
//    TODO Prozesskette ebenfalls in runnern, die die Anwendungen abarbeiten.
//    Allerdings werden die runner nacheinander gestartet... 

//    TODO mehrere chains, die z.B. auf unterschiedlichen Rechnern laufen... Version 1.2 ???
    
//    TODO Startet für jede Anwendung einen Thread. Die Threads überwachen die Startzeiten
//    Runner beenden sich selbst, wenn ihr Service sich beendet...
    
//    TODO Service über den der Scheduler gestoppt werden kann... der Service schickt ein observe an MasterTable? ???
//    Das event wird hier zyklisch abgefragt ???    
    
    /*
     * For each configured application start a runner. The runner observes the start point.
     */
    private void startAllApplicationRunners() {
        for (ApplicationConfiguration conf : MasterTable.getApplicationConfigurationsAsList()) {
            ApplicationScheduler appSched = new ApplicationScheduler(conf);
            Thread thread = new Thread(appSched);
            appSched.setThread(thread);
            thread.start();
            applicationSchedulers.add(appSched);
        }
    }
    
    /*
     * Start the runner as thread, put him into the list. 
     */
    private void startApplicationRunner(ApplicationConfiguration configuration) {
        
    }
    
    /*
     * The runner asks here and the allowance is verified with other services, configurations, runners
     */
    private boolean isStartAllowed(Long applicationId) {
        return false;
    }
    
    /*
     * Runner gets the StartService to tell him that the application must be started.
     * The Services are also stored in the MasterTable. 
     */
    private StartService getStartService(String clientId) {
       return null; 
    }
    
    /**
     * Start the scheduler.
     * @param args <br>
     * --homeVar is the basic environment variable for the system to find libs and configuration. Default is HAPPTICK_HOME <br>
     * --baseConfDir is the directory where the configuration files are stored (e.g. if value is set to 'conf' the system will search files in $HAPPTICK_HOME/conf/). <br>
     * --baseConfFile is the central configuration file. Default is happtick_master.xml <br>
     */
    public static void main(String... args) {
//        TODO hilfe einbasteln
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
     * This class really initializes the start of application by talking with StartServices.
     */
    private class ApplicationScheduler implements Runnable{
        private Thread thread;
        private boolean stopped = false;
        private ApplicationConfiguration conf;
        
        protected ApplicationScheduler(ApplicationConfiguration conf) {
            this.conf = conf;
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
            
            stopped = true;
        }
    }
    
    private class SchedulerGarbage implements Runnable {
        
        public void run() {
            while (true) {
            for (ApplicationScheduler appSched : applicationSchedulers) {
                if (appSched.hasStopped()) {
                    applicationSchedulers.remove(appSched);
                    break;
                }
            }
            }
        }
    }
}
