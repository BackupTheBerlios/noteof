package de.happtick.configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import de.happtick.core.MasterTable;
import de.notEOF.core.util.Util;
import de.notIOC.exception.NotIOCException;
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
    private Map<String, String> executableArgs;
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
     * The class reads the configuration by itself.
     * 
     * @param nodeNameApplication
     *            Reference within the configuration file to the configuration
     *            of this client.
     */
    public ApplicationConfiguration(String nodeNameApplication) {

        this.nodeNameApplication = nodeNameApplication;
        String node = "";

        // applicationId
        node = "scheduler." + nodeNameApplication;
        applicationId = Util.parseLong(LocalConfigurationClient.getAttribute(node, "applicationId", "-1"), -1);

        // clientIp
        node = "scheduler." + nodeNameApplication + ".client";
        clientIp = LocalConfigurationClient.getAttribute(node, "ip", "localhost");

        // executable type
        node = "scheduler." + nodeNameApplication + ".executable";
        executableType = LocalConfigurationClient.getAttribute(node, "type", "UNKNOWN");
        // executable path
        executablePath = LocalConfigurationClient.getAttribute(node, "path", "");

        // option multiple start
        node = "scheduler." + nodeNameApplication + ".option";
        multipleStart = Util.parseBoolean(LocalConfigurationClient.getAttribute(node, "multiplestart", "false"), false);
        // option enforce
        enforce = Util.parseBoolean(LocalConfigurationClient.getAttribute(node, "enforce", "false"), false);

        // time plan
        String nodeTime = "scheduler." + nodeNameApplication + ".timeplan";
        // seconds
        // * or 0 means one time per minute
        node = nodeTime + ".seconds";
        String seconds = LocalConfigurationClient.getText(node);
        if ("".equals(seconds) || "*".equals(seconds) || "0".equals(seconds)) {
            timePlanSeconds.add(0);
        } else {
            timePlanSeconds = getElementsOfStringAsInt(node);
        }

        // minutes
        // * or 0 means one time per hour
        node = nodeTime + ".minutes";
        String minutes = LocalConfigurationClient.getText(node);
        if ("".equals(minutes) || "*".equals(minutes) || "0".equals(minutes)) {
            timePlanMinutes.add(0);
        } else {
            timePlanMinutes = getElementsOfStringAsInt(node);
        }

        // hours
        node = nodeTime + ".hours";
        String hours = LocalConfigurationClient.getText(node);
        if ("".equals(hours) || "*".equals(hours)) {
            for (int i = 0; i < 24; i++) {
                timePlanHours.add(i);
            }
        } else {
            timePlanHours = getElementsOfStringAsInt(node);
        }

        // days of week
        node = nodeTime + ".weekdays";
        String days = LocalConfigurationClient.getText(node);
        if ("".equals(days) || "*".equals(days)) {
            for (int i = 1; i < 8; i++) {
                timePlanWeekdays.add(Calendar.DAY_OF_WEEK, i);
            }
        }
        for (String element : (List<String>) getElementsOfString(node)) {
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
        String months = LocalConfigurationClient.getText(node);
        if ("".equals(months) || "*".equals(months)) {
            timePlanMonthdays.add(0);
        } else {
        timePlanMonthdays = getElementsOfStringAsInt(node);
        }

        // applications to wait for
        node = "scheduler." + nodeNameApplication + ".dependencies.waitfor";
        List<String> ids = null;
        try {
            ids = LocalConfigurationClient.getAttributeList(node, "applicationId");
        } catch (NotIOCException e) {
            LocalLog.warn("Fehler bei Lesen der Applikationen auf die gewartet werden soll.", e);
        }
        applicationsWaitFor = stringListToLongList(ids);

        // applications to start after
        node = "scheduler." + nodeNameApplication + ".dependencies.startafter";
        ids = null;
        try {
            ids = LocalConfigurationClient.getAttributeList(node, "applicationId");
        } catch (NotIOCException e) {
            LocalLog.warn("Fehler bei Lesen der Applikationen die anschließend gestartet werden sollen.", e);
        }
        applicationsStartAfter = stringListToLongList(ids);

        // applications to start synchronously
        node = "scheduler." + nodeNameApplication + ".dependencies.startsync";
        ids = null;
        try {
            ids = LocalConfigurationClient.getAttributeList(node, "applicationId");
        } catch (NotIOCException e) {
            LocalLog.warn("Fehler bei Lesen der Applikationen die gleichzeitig gestartet werden sollen.", e);
        }
        applicationsStartAfter = stringListToLongList(ids);

        // maxStartStop
        node = "scheduler." + nodeNameApplication + ".runtime";
        maxStartStop = LocalConfigurationClient.getAttributeInt(node, "maxStartStop", 0);
        // maxStepStep
        maxStepStep = LocalConfigurationClient.getAttributeInt(node, "maxStepStep", 0);
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
                    (calcDate.get(Calendar.DAY_OF_WEEK) == dayOfWeek ||
                     dayOfWeek == 0)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                System.out.println("Gefunden: " + calcDate.get(Calendar.DATE) + "." + (calcDate.get(Calendar.MONTH) +1) + "." + calcDate.get(Calendar.YEAR));
                break;
            }
            
            dayOfMonth++;
            if (dayOfMonth == timePlanMonthdays.size() ) {
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
    private List<Integer> getElementsOfStringAsInt(String node) {
        List<String> elementList = getElementsOfString(node);
        List<Integer> intList = new ArrayList<Integer>();
        for (String element : elementList) {
            intList.add(Util.parseInt(element, -1));
        }
        return intList;
    }

    /*
     * delivers elements of a text string comma separated or blank separated
     */
    private List<String> getElementsOfString(String node) {
        List<String> elementList;
        String elements = LocalConfigurationClient.getText(node);
        elements.replace(" ", ",");
        elements.replace(",,", ",");
            elementList = Util.stringToList(elements, ",");
        return elementList;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getNodeNameOfApplication() {
        return nodeNameApplication;
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

    public Map<String, String> getExecutableArgs() {
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
}
