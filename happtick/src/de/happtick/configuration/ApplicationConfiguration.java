package de.happtick.configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.Scheduling;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

/**
 * This class represents configuration entries of applications.
 * 
 * @author Dirk
 * 
 */
public class ApplicationConfiguration {

    private Long applicationId;
    private String nodeNameApplication;
    private String clientIp;
    private String executableType;
    private String executablePath;
    private boolean multipleStart;
    private boolean enforce;
    private boolean windowsSupport;
    private boolean partOfChain;
    private String executableArgs;
    private List<Integer> timePlanSeconds;
    private List<Integer> timePlanMinutes;
    private List<Integer> timePlanHours;
    private List<Integer> timePlanWeekdays;
    private List<Integer> timePlanMonthdays;
    private List<Long> applicationsWaitFor;
    private List<Long> applicationsStartAfter;
    private List<Long> applicationsStartSync;
    private int maxStartStop;
    private int maxStepStep;
    private Date nextStartDate = null;

    /**
     * Simple constructor
     */
    public ApplicationConfiguration(Long applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Using this constructor the class reads the configuration by itself.
     * 
     * @param nodeNameApplication
     *            Reference within the configuration file to the configuration
     *            of this client.
     */
    public ApplicationConfiguration(String nodeNameApplication, NotEOFConfiguration conf) throws ActionFailedException {

        try {
            this.nodeNameApplication = nodeNameApplication;
            String node = "";

            // applicationId
            node = "scheduler." + nodeNameApplication;
            applicationId = Util.parseLong(conf.getAttribute(node, "applicationId", "-1"), -1);

            // clientIp
            node = "scheduler." + nodeNameApplication + ".client";
            clientIp = conf.getAttribute(node, "clientIp", "localhost");

            // executable type
            node = "scheduler." + nodeNameApplication + ".executable";
            executableType = conf.getAttribute(node, "type", "UNKNOWN");
            // executable path
            executablePath = conf.getAttribute(node, "path", "");
            // executable windowsSupport
            windowsSupport = Util.parseBoolean(conf.getAttribute(node, "windows"), false);
            // executable partOfChain
            partOfChain = Util.parseBoolean(conf.getAttribute(node, "partOfChain"), false);

            // arguments of executable
            node = "scheduler." + nodeNameApplication + ".executable.args";
            executableArgs = conf.getText(node, "");

            // option multiple start
            node = "scheduler." + nodeNameApplication + ".option";
            multipleStart = Util.parseBoolean(conf.getAttribute(node, "multiplestart"), false);
            // option enforce
            enforce = Util.parseBoolean(conf.getAttribute(node, "enforce"), false);

            // time plan
            String nodeTime = "scheduler." + nodeNameApplication + ".timeplan";
            // seconds
            // * or 0 means one time per minute
            node = nodeTime + ".seconds";
            String seconds = conf.getText(node);
            if ("".equals(seconds) || "*".equals(seconds) || "0".equals(seconds)) {
                timePlanSeconds = new ArrayList<Integer>();
                timePlanSeconds.add(0);
            } else {
                timePlanSeconds = Util.getElementsOfStringAsInt(node, conf);
                Collections.sort(timePlanSeconds);
            }

            // minutes
            // * or 0 means one time per hour
            node = nodeTime + ".minutes";
            String minutes = conf.getText(node);
            if ("".equals(minutes) || "*".equals(minutes)) {
                timePlanMinutes = new ArrayList<Integer>();
                for (int i = 0; i < 60; i++) {
                    timePlanMinutes.add(i);
                }
            } else {
                timePlanMinutes = Util.getElementsOfStringAsInt(node, conf);
                Collections.sort(timePlanMinutes);
            }

            // hours
            node = nodeTime + ".hours";
            String hours = conf.getText(node);
            if ("".equals(hours) || "*".equals(hours)) {
                timePlanHours = new ArrayList<Integer>();
                for (int i = 0; i < 24; i++) {
                    timePlanHours.add(i);
                }
            } else {
                timePlanHours = Util.getElementsOfStringAsInt(node, conf);
                Collections.sort(timePlanHours);
            }

            // days of week
            timePlanWeekdays = new ArrayList<Integer>();
            node = nodeTime + ".weekdays";
            String days = conf.getText(node);
            if ("".equals(days) || "*".equals(days)) {
                for (int i = 0; i < 7; i++) {
                    timePlanWeekdays.add(i);
                }
            } else {
                for (String element : (List<String>) Util.getElementsOfString(node, conf)) {
                    if (element.equalsIgnoreCase("MO"))
                        timePlanWeekdays.add(Calendar.MONDAY);
                    if (element.equalsIgnoreCase("TU"))
                        timePlanWeekdays.add(Calendar.TUESDAY);
                    if (element.equalsIgnoreCase("WE") || element.equalsIgnoreCase("MI"))
                        timePlanWeekdays.add(Calendar.WEDNESDAY);
                    if (element.equalsIgnoreCase("TH") || element.equalsIgnoreCase("DO"))
                        timePlanWeekdays.add(Calendar.THURSDAY);
                    if (element.equalsIgnoreCase("FR"))
                        timePlanWeekdays.add(Calendar.FRIDAY);
                    if (element.equalsIgnoreCase("SA"))
                        timePlanWeekdays.add(Calendar.SATURDAY);
                    if (element.equalsIgnoreCase("SU") || element.equalsIgnoreCase("SO"))
                        timePlanWeekdays.add(Calendar.SUNDAY);
                }
            }
            Collections.sort(timePlanWeekdays);

            // days of month
            node = nodeTime + ".monthdays";
            String months = conf.getText(node);
            if ("".equals(months) || "*".equals(months)) {
                timePlanMonthdays = new ArrayList<Integer>();
                timePlanMonthdays.add(0);
            } else {
                timePlanMonthdays = Util.getElementsOfStringAsInt(node, conf);
                Collections.sort(timePlanMonthdays);
            }

            // applications to wait for
            node = "scheduler." + nodeNameApplication + ".dependencies.waitfor";
            List<String> ids = null;
            try {
                ids = conf.getAttributeList(node, "applicationId");
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Lesen der Applikationen auf die gewartet werden soll.", e);
            }
            applicationsWaitFor = Util.stringListToLongList(ids);

            // applications to start after
            node = "scheduler." + nodeNameApplication + ".dependencies.startafter";
            ids = null;
            try {
                ids = conf.getAttributeList(node, "applicationId");
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Lesen der Applikationen die anschließend gestartet werden sollen.", e);
            }
            applicationsStartAfter = Util.stringListToLongList(ids);

            // applications to start synchronously
            node = "scheduler." + nodeNameApplication + ".dependencies.startsync";
            ids = null;
            try {
                ids = conf.getAttributeList(node, "applicationId");
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Lesen der Applikationen die gleichzeitig gestartet werden sollen.", e);
            }
            applicationsStartAfter = Util.stringListToLongList(ids);

            // maxStartStop
            node = "scheduler." + nodeNameApplication + ".runtime";
            maxStartStop = conf.getAttributeInt(node, "maxStartStop", 0);
            // maxStepStep
            maxStepStep = conf.getAttributeInt(node, "maxStepStep", 0);

        } catch (Exception ex) {
            LocalLog.error("Konfiguration der Applikation konnte nicht fehlerfrei gelesen werden. Applikation: " + nodeNameApplication, ex);
            throw new ActionFailedException(401, "Initialisierung ApplicationConfiguration", ex);
        }
    }

    /**
     * Looks if the application has to start now.
     * <p>
     * 
     * @return 0 if start is allowed or the time to start in millis
     * @throws HapptickException
     */
    public long startAllowed() throws HapptickException {
        long waitTime = 0;
        Calendar cal = new GregorianCalendar();

        // if program drops by here for first time calculate next start point
        if (null == nextStartDate) {
            nextStartDate = Scheduling.calculateNextStart(this, 0);
        }

        cal.setTime(nextStartDate);
        Util.formatCal("ApplicationConfiguration.startAllowed: Naechster Start ", cal);

        // if no instance of application is running and enforce is set to true
        // the application must start immediately
        // but don't look to often...
        if (!Scheduling.isEqualApplicationActive(this) && enforce) {
            return 0;
        }
        System.out.println("ApplicationConfiguration.startAllowed... enforce: " + enforce);

        long timeNow = new Date().getTime();
        waitTime = nextStartDate.getTime() - timeNow - 500;
        if (waitTime < 0) {
            waitTime = 0;
        }

        if (waitTime > 1000 || waitTime < -1000) {
            return waitTime;
        }

        // Perhaps this application has to wait till other application processes
        // are stopped
        if (Scheduling.mustWaitForOtherApplication(this)) {
            return 5000;
        }
        System.out.println("ApplicationConfiguration.startAllowed... mustWaitForOthers: NEIN");

        // Some millis as tolerance value...
        // +- 1000 millis
        System.out.println("ApplicationConfiguration.startAllowed... Differenz jetzt und startTime " + (waitTime / 1000));

        if (nextStartDate.getTime() > timeNow - 1000 && //
                nextStartDate.getTime() < timeNow + 1000) {

            // check if other instances of application are running
            // and if multiple start is allowed
            if (!isMultipleStart() && Scheduling.isEqualApplicationActive(this)) {
                // wait for ending other instances (next time point is
                // calculated above)
                return 300;
            }

            // ok time point reached, application may be started now
            // calculate the next start point
            nextStartDate = Scheduling.calculateNextStart(this, 2);

            // ok - time point reached, no more other instances are running or
            // multiple
            // start allowed
            System.out.println("ApplicationConfiguration.startAllowed... SSSSSSSSSTTTTTTTTTTTTTTAAAAAAAAAAAAAAARRRRRRRRRRRRRRRRTTTTTTTTTTTT");
            return 0;
        }

        // please wait
        System.out.println("ApplicationConfiguration.startAllowed... B�h");
        return nextStartDate.getTime();
    }

    public Date getNextStartDate() {
        return nextStartDate;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public boolean isMultipleStart() {
        return multipleStart;
    }

    public boolean isEnforce() {
        return enforce;
    }

    public String getExecutableArgs() {
        return executableArgs;
    }

    public List<Integer> getTimePlanSeconds() {
        return timePlanSeconds;
    }

    public List<Integer> getTimePlanMinutes() {
        return timePlanMinutes;
    }

    public List<Integer> getTimePlanHours() {
        return timePlanHours;
    }

    /**
     * List of days in weeks.
     * <p>
     * Use Object Calendar to map the values to a weekday (e.g.
     * Calendar.MONDAY).
     * 
     * @return
     */
    public List<Integer> getTimePlanWeekdays() {
        return timePlanWeekdays;
    }

    /**
     * List of days in month.
     * 
     * @return
     */
    public List<Integer> getTimePlanMonthdays() {
        return timePlanMonthdays;
    }

    public List<Long> getApplicationsWaitFor() {
        return applicationsWaitFor;
    }

    public List<Long> getApplicationsStartAfter() {
        return applicationsStartAfter;
    }

    public List<Long> getApplicationsStartSync() {
        return applicationsStartSync;
    }

    public String getExecutableType() {
        return executableType;
    }

    public int getMaxStartStop() {
        return maxStartStop;
    }

    public int getMaxStepStep() {
        return maxStepStep;
    }

    public String getNodeNameApplication() {
        return nodeNameApplication;
    }

    public void setNodeNameApplication(String nodeNameApplication) {
        this.nodeNameApplication = nodeNameApplication;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setExecutableType(String executableType) {
        this.executableType = executableType;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public void setMultipleStart(boolean multipleStart) {
        this.multipleStart = multipleStart;
    }

    public void setEnforce(boolean enforce) {
        this.enforce = enforce;
    }

    public void setExecutableArgs(String executableArgs) {
        this.executableArgs = executableArgs;
    }

    public void setTimePlanSeconds(List<Integer> timePlanSeconds) {
        this.timePlanSeconds = timePlanSeconds;
    }

    public void setTimePlanMinutes(List<Integer> timePlanMinutes) {
        this.timePlanMinutes = timePlanMinutes;
    }

    public void setTimePlanHours(List<Integer> timePlanHours) {
        this.timePlanHours = timePlanHours;
    }

    public void setTimePlanWeekdays(List<Integer> timePlanWeekdays) {
        this.timePlanWeekdays = timePlanWeekdays;
    }

    public void setTimePlanMonthdays(List<Integer> timePlanMonthdays) {
        this.timePlanMonthdays = timePlanMonthdays;
    }

    public void setApplicationsWaitFor(List<Long> applicationsWaitFor) {
        this.applicationsWaitFor = applicationsWaitFor;
    }

    public void setApplicationsStartAfter(List<Long> applicationsStartAfter) {
        this.applicationsStartAfter = applicationsStartAfter;
    }

    public void setApplicationsStartSync(List<Long> applicationsStartSync) {
        this.applicationsStartSync = applicationsStartSync;
    }

    public void setMaxStartStop(int maxStartStop) {
        this.maxStartStop = maxStartStop;
    }

    public void setMaxStepStep(int maxStepStep) {
        this.maxStepStep = maxStepStep;
    }

    public void setWindowsSupport(boolean windowsSupport) {
        this.windowsSupport = windowsSupport;
    }

    public boolean isWindowsSupport() {
        return this.windowsSupport;
    }

    public void setPartOfChain(boolean partOfChain) {
        this.partOfChain = partOfChain;
    }

    public boolean isPartOfChain() {
        return this.partOfChain;
    }
}
