package de.happtick.configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.happtick.core.MasterTable;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.util.Util;
import de.notIOC.logging.LocalLog;

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
                timePlanSeconds.add(0);
            } else {
                timePlanSeconds = getElementsOfStringAsInt(node, conf);
            }

            // minutes
            // * or 0 means one time per hour
            node = nodeTime + ".minutes";
            String minutes = conf.getText(node);
            if ("".equals(minutes) || "*".equals(minutes) || "0".equals(minutes)) {
                timePlanMinutes.add(0);
            } else {
                timePlanMinutes = getElementsOfStringAsInt(node, conf);
            }

            // hours
            node = nodeTime + ".hours";
            String hours = conf.getText(node);
            if ("".equals(hours) || "*".equals(hours)) {
                for (int i = 0; i < 24; i++) {
                    timePlanHours.add(i);
                }
            } else {
                timePlanHours = getElementsOfStringAsInt(node, conf);
            }

            // days of week
            timePlanWeekdays = new ArrayList<Integer>();
            node = nodeTime + ".weekdays";
            String days = conf.getText(node);
            if ("".equals(days) || "*".equals(days)) {
                for (int i = 1; i < 8; i++) {
                    timePlanWeekdays.add(Calendar.DAY_OF_WEEK, i);
                }
            }
            for (String element : (List<String>) getElementsOfString(node, conf)) {
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

            // days of month
            node = nodeTime + ".monthdays";
            String months = conf.getText(node);
            if ("".equals(months) || "*".equals(months)) {
                timePlanMonthdays.add(0);
            } else {
                timePlanMonthdays = getElementsOfStringAsInt(node, conf);
            }

            // applications to wait for
            node = "scheduler." + nodeNameApplication + ".dependencies.waitfor";
            List<String> ids = null;
            try {
                ids = conf.getAttributeList(node, "applicationId");
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Lesen der Applikationen auf die gewartet werden soll.", e);
            }
            applicationsWaitFor = stringListToLongList(ids);

            // applications to start after
            node = "scheduler." + nodeNameApplication + ".dependencies.startafter";
            ids = null;
            try {
                ids = conf.getAttributeList(node, "applicationId");
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Lesen der Applikationen die anschließend gestartet werden sollen.", e);
            }
            applicationsStartAfter = stringListToLongList(ids);

            // applications to start synchronously
            node = "scheduler." + nodeNameApplication + ".dependencies.startsync";
            ids = null;
            try {
                ids = conf.getAttributeList(node, "applicationId");
            } catch (ActionFailedException e) {
                LocalLog.warn("Fehler bei Lesen der Applikationen die gleichzeitig gestartet werden sollen.", e);
            }
            applicationsStartAfter = stringListToLongList(ids);

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
     * Calculates the next start point up from now
     * 
     * @return Date when the application should run only depending to the
     *         configuration, ignoring other active processes etc.
     */
    public Date calculateNextStart() {
        // when multipleStart or enforce the application could start immediately
        if (multipleStart || enforce) {
            // give the system a little bit time to work...
            long now = System.currentTimeMillis() + MasterTable.getTimeDelayForStart();
            return new Date(now);
        }

        Calendar actDate = new GregorianCalendar();
        Calendar calcDate = new GregorianCalendar();
        // seconds
        for (int confSecond : timePlanSeconds) {
            calcDate.set(Calendar.SECOND, confSecond);
            if (calcDate.getTimeInMillis() >= actDate.getTimeInMillis())
                break;
        }
        if (calcDate.getTimeInMillis() < actDate.getTimeInMillis()) {
            // next minute, smallest second
            calcDate.set(Calendar.SECOND, timePlanSeconds.get(0));
            calcDate.add(Calendar.MINUTE, 1);
        }

        // minutes
        for (int confMinute : timePlanMinutes) {
            calcDate.set(Calendar.MINUTE, confMinute);
            if (calcDate.getTimeInMillis() >= actDate.getTimeInMillis())
                break;
        }
        if (calcDate.getTimeInMillis() < actDate.getTimeInMillis()) {
            // next hour, smallest minute
            calcDate.set(Calendar.MINUTE, timePlanMinutes.get(0));
            calcDate.add(Calendar.HOUR_OF_DAY, 1);
        }

        // hours
        for (int confHour : timePlanHours) {
            calcDate.set(Calendar.HOUR_OF_DAY, confHour);
            if (calcDate.getTimeInMillis() >= actDate.getTimeInMillis())
                break;
        }
        if (calcDate.getTimeInMillis() < actDate.getTimeInMillis()) {
            // next day, smallest hour
            calcDate.set(Calendar.HOUR, timePlanHours.get(0));
            calcDate.add(Calendar.DATE, 1);
        }

        // days of month and days of week
        boolean found = false;
        int dayOfMonth = 0;
        if (timePlanMonthdays.get(dayOfMonth) > calcDate.get(Calendar.DATE))
            calcDate.set(Calendar.DATE, timePlanMonthdays.get(dayOfMonth));
        while (!found) {
            for (int dayOfWeek : timePlanWeekdays) {
                if (calcDate.getTimeInMillis() >= actDate.getTimeInMillis() && //
                        (calcDate.get(Calendar.DATE) == timePlanMonthdays.get(dayOfMonth) || //
                        timePlanMonthdays.get(dayOfMonth) == 0) && //
                        (calcDate.get(Calendar.DAY_OF_WEEK) == dayOfWeek || dayOfWeek == 0)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }

            dayOfMonth++;
            if (dayOfMonth == timePlanMonthdays.size()) {
                dayOfMonth = 0;
                calcDate.add(Calendar.MONTH, 1);
            }
            calcDate.set(Calendar.DAY_OF_MONTH, timePlanMonthdays.get(dayOfMonth));
        }

        // buffer for performance problems
        calcDate.add(Calendar.MILLISECOND, MasterTable.getTimeDelayForStart());
        return calcDate.getTime();
    }

    /*
     * Converts a List<String> to List<Long>
     */
    private List<Long> stringListToLongList(List<String> stringList) {
        List<Long> newList = new ArrayList<Long>();
        if (null != stringList) {
            for (String element : stringList) {
                newList.add(Util.parseLong(element, -1));
            }
        }
        return newList;
    }

    /*
     * delivers elements of a text string comma separated or blank separated
     */
    private List<Integer> getElementsOfStringAsInt(String node, NotEOFConfiguration conf) throws ActionFailedException {
        List<String> elementList = getElementsOfString(node, conf);
        List<Integer> intList = new ArrayList<Integer>();
        for (String element : elementList) {
            intList.add(Util.parseInt(element, -1));
        }
        return intList;
    }

    /*
     * delivers elements of a text string comma separated or blank separated
     */
    private List<String> getElementsOfString(String node, NotEOFConfiguration conf) throws ActionFailedException {
        List<String> elementList;
        String elements = conf.getText(node);
        elements = elements.replace(" ", ",");
        elements = elements.replace(",,", ",");
        elementList = Util.stringToList(elements, ",");
        return elementList;
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
}
