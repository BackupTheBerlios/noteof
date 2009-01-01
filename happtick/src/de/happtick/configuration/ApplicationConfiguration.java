package de.happtick.configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.core.MasterTable;
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
    private String timePlanSeconds;
    private String timePlanMinutes;
    private String timePlanHours;
    private String timePlanWeekdays;
    private String timePlanMonthdays;
    private List<Long> applicationsWaitFor;
    private List<Long> applicationsStartAfter;
    private List<Long> applicationsStartSync;
    private List<String> arguments;
    private Map<String, String> environment;
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
            executableType = conf.getAttribute(node, "type", "external");
            // executable path
            executablePath = conf.getAttribute(node, "path", "");
            // executable windowsSupport
            windowsSupport = Util.parseBoolean(conf.getAttribute(node, "windows"), false);
            // executable partOfChain
            partOfChain = Util.parseBoolean(conf.getAttribute(node, "partOfChain"), false);

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
            timePlanSeconds = conf.getText(node);

            // minutes
            // * or 0 means one time per hour
            node = nodeTime + ".minutes";
            timePlanMinutes = conf.getText(node);

            // hours
            node = nodeTime + ".hours";
            timePlanHours = conf.getText(node);

            // days of week
            node = nodeTime + ".weekdays";
            timePlanWeekdays = conf.getText(node);

            // days of month
            node = nodeTime + ".monthdays";
            timePlanMonthdays = conf.getText(node);

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
            applicationsStartSync = Util.stringListToLongList(ids);

            // maxStartStop
            node = "scheduler." + nodeNameApplication + ".runtime";
            maxStartStop = conf.getAttributeInt(node, "maxStartStop", 0);
            // maxStepStep
            maxStepStep = conf.getAttributeInt(node, "maxStepStep", 0);

            node = "scheduler." + nodeNameApplication + ".executable.arg";
            arguments = conf.getTextList(node);

            setEnvironment(readEnv(conf));

        } catch (Exception ex) {
            LocalLog.error("Konfiguration der Applikation konnte nicht fehlerfrei gelesen werden. Applikation: " + nodeNameApplication, ex);
            throw new ActionFailedException(401, "Initialisierung ApplicationConfiguration", ex);
        }
    }

    private Map<String, String> readEnv(NotEOFConfiguration conf) throws ActionFailedException {
        Map<String, String> env = new HashMap<String, String>();
        String node = "scheduler." + this.nodeNameApplication + ".executable.env";
        List<String> envKeys = conf.getAttributeList(node, "var");
        List<String> envVals = conf.getAttributeList(node, "val");

        if (!Util.isEmpty(envKeys)) {
            for (int i = 0; i < envKeys.size(); i++) {
                String value = replaceVar(envVals.get(i), env);
                env.put(envKeys.get(i), value);
            }

            return env;
        }
        return null;
    }

    private String replaceVar(String str, Map<String, String> values) {

        String original = "";
        str = str.trim();

        while (str.indexOf("$") > -1) {
            int pos1 = str.indexOf("$");
            int pos2 = str.indexOf("$", pos1 + 1);
            original += str.substring(0, pos1);
            String key = str.substring(pos1 + 1, pos2);
            original += values.get(key);
            str = str.substring(pos2 + 1);
        }
        original += str;
        original = original.trim();

        return original;
    }

    /**
     * Applications can exist without a time plan. For example to use only in
     * chains or in dependency to other applications.
     * 
     * @return TRUE if there is a plan, otherwise FALSE
     */
    public boolean hasTimePlan() {
        return (!(Util.isEmpty(timePlanHours) && //
                Util.isEmpty(timePlanMinutes) && //
                Util.isEmpty(timePlanSeconds) && //
                Util.isEmpty(timePlanMonthdays) && //
        Util.isEmpty(timePlanWeekdays)));
    }

    /**
     * Looks if the application has to start now.
     * <p>
     * 
     * @return 0 if start is allowed or the time to start in millis
     * @throws ActionFailedException
     */
    public long startAllowed() throws ActionFailedException {
        long waitTime = 0;
        long timeNow = new Date().getTime();

        // if program drops by here for first time calculate next start point
        if (null == nextStartDate) {
            nextStartDate = Scheduling.calculateNextStart(this, 0);
        }

        Calendar cal = new GregorianCalendar();
        cal.setTime(nextStartDate);

        // check if the max. delay between the calculated time and the time now
        // has exceeded
        // it last starttime is to old calc new one
        if (timeNow - MasterTable.getMaxDelay() > nextStartDate.getTime()) {
            nextStartDate = Scheduling.calculateNextStart(this, 0);
        }

        // if no instance of application is running and enforce is set to true
        // the application must start immediately
        // but don't look to often...
        if ((!Scheduling.isEqualApplicationActive(this)) && enforce) {
            return 0;
        }

        waitTime = nextStartDate.getTime() - timeNow - 300;
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

        // Some millis as tolerance value...
        // +- 500 millis
        if (waitTime > -100 && //
                waitTime < +500) {

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
            // System.out.println(
            // "ApplicationConfiguration.startAllowed... SSSSSSSSSTTTTTTTTTTTTTTAAAAAAAAAAAAAAARRRRRRRRRRRRRRRRTTTTTTTTTTTT"
            // );
            return 0;
        }

        // please wait
        return waitTime;
    }

    public static List<Integer> transformTimePlanSeconds(String seconds) {
        List<Integer> timePlanSeconds = null;
        if ("".equals(seconds) || "*".equals(seconds) || "0".equals(seconds)) {
            timePlanSeconds = new ArrayList<Integer>();
            timePlanSeconds.add(0);
        } else {
            try {
                timePlanSeconds = Util.getElementsOfStringAsInt(seconds);
                Collections.sort(timePlanSeconds);
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Aufsplitten des Zeitplans: " + "Sekunden. Konfiguration: " + seconds);
            }
        }
        return timePlanSeconds;
    }

    public static List<Integer> transformTimePlanMinutes(String minutes) {
        List<Integer> timePlanMinutes = null;
        if ("".equals(minutes) || "*".equals(minutes)) {
            timePlanMinutes = new ArrayList<Integer>();
            for (int i = 0; i < 60; i++) {
                timePlanMinutes.add(i);
            }
        } else {
            try {
                timePlanMinutes = Util.getElementsOfStringAsInt(minutes);
                Collections.sort(timePlanMinutes);
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Aufsplitten des Zeitplans: " + "Minuten. Konfiguration: " + minutes);
            }
        }
        return timePlanMinutes;
    }

    public static List<Integer> transformTimePlanHours(String hours) {
        List<Integer> timePlanHours = null;
        if ("".equals(hours) || "*".equals(hours)) {
            timePlanHours = new ArrayList<Integer>();
            for (int i = 0; i < 24; i++) {
                timePlanHours.add(i);
            }
        } else {
            try {
                timePlanHours = Util.getElementsOfStringAsInt(hours);
                Collections.sort(timePlanHours);
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Aufsplitten des Zeitplans: " + "Stunden. Konfiguration: " + hours);
            }
        }
        return timePlanHours;
    }

    public static List<Integer> transformTimePlanWeekDays(String weekdays) {
        List<Integer> timePlanWeekdays = null;
        if ("".equals(weekdays) || "*".equals(weekdays)) {
            timePlanWeekdays = new ArrayList<Integer>();
            for (int i = 1; i < 8; i++) {
                timePlanWeekdays.add(i);
            }
        } else {
            timePlanWeekdays = new ArrayList<Integer>();
            try {
                for (String element : (List<String>) Util.getElementsOfString(weekdays)) {
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
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Aufsplitten des Zeitplans: " + "Wochentage. Konfiguration: " + weekdays);
            }
        }
        Collections.sort(timePlanWeekdays);
        return timePlanWeekdays;
    }

    public static List<Integer> transformTimePlanMonthDays(String monthdays) {
        List<Integer> timePlanMonthdays = null;
        if ("".equals(monthdays) || "*".equals(monthdays)) {
            timePlanMonthdays = new ArrayList<Integer>();
            timePlanMonthdays.add(0);
        } else {
            try {
                timePlanMonthdays = Util.getElementsOfStringAsInt(monthdays);
                Collections.sort(timePlanMonthdays);
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Aufsplitten des Zeitplans: " + "Monate. Konfiguration: " + monthdays);
            }
        }
        return timePlanMonthdays;
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

    public String getTimePlanSeconds() {
        return timePlanSeconds;
    }

    public String getTimePlanMinutes() {
        return timePlanMinutes;
    }

    public String getTimePlanHours() {
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
    public String getTimePlanWeekdays() {
        return timePlanWeekdays;
    }

    /**
     * List of days in month.
     * 
     * @return
     */
    public String getTimePlanMonthdays() {
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

    public void setTimePlanSeconds(String timePlanSeconds) {
        this.timePlanSeconds = timePlanSeconds;
    }

    public void setTimePlanMinutes(String timePlanMinutes) {
        this.timePlanMinutes = timePlanMinutes;
    }

    public void setTimePlanHours(String timePlanHours) {
        this.timePlanHours = timePlanHours;
    }

    public void setTimePlanWeekdays(String timePlanWeekdays) {
        this.timePlanWeekdays = timePlanWeekdays;
    }

    public void setTimePlanMonthdays(String timePlanMonthdays) {
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

    /**
     * @param environment
     *            the environment to set
     */
    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * @return the environment
     */
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * @param arguments
     *            the arguments to set
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * @return the arguments
     */
    public List<String> getArguments() {
        return arguments;
    }
}
