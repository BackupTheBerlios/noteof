package de.happtick.core.schedule;

import java.util.Date;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.core.MasterTable;
import de.happtick.core.util.Scheduling;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

public class ApplicationScheduler implements Runnable {
    private ApplicationConfiguration conf;

    protected ApplicationScheduler(ApplicationConfiguration conf) {
        this.conf = conf;
    }

    public void run() {
        try {
            long waitTime = 0;
            while (true) {
                // check if start allowed
                waitTime = conf.startAllowed();
                if (0 == waitTime) {
                    // start application
                    Scheduling.startApplication(conf);

                    // applications to start syncronously
                    if (!Util.isEmpty(conf.getApplicationsStartSync())) {
                        for (Long applId : conf.getApplicationsStartSync()) {
                            ApplicationConfiguration syncConf = MasterTable.getApplicationConfiguration(applId);
                            if (null != syncConf) {
                                Scheduling.startApplication(syncConf);
                            }
                        }
                    }
                    waitTime = conf.getNextStartDate().getTime() - new Date().getTime() - 100;
                    if (waitTime < 0)
                        waitTime = 0;
                }
                // Calendar cal = new GregorianCalendar();
                // cal.setTime(conf.getNextStartDate());
                // Util.formatCal("Scheduler.run Schlafe jetzt bis ", cal);
                Thread.sleep(waitTime);
            }
        } catch (Exception e) {
            LocalLog.error("Scheduling fuer Applikation mit Id " + conf.getApplicationId() + " ist ausgefallen.", e);
        }
    }
}
