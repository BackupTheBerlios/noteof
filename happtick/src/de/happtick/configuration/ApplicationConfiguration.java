package de.happtick.configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
        node = nodeTime + ".seconds";
        timePlanSeconds = getElementsOfStringAsInt(node);
        
        // minutes
        node = nodeTime + ".minutes";
        timePlanMinutes= getElementsOfStringAsInt(node);

        // hours
        node = nodeTime + ".hours";
        timePlanHours= getElementsOfStringAsInt(node);
        
        // days of week
        node = nodeTime + ".weekdays";
        for (String element : (List<String>)getElementsOfString(node)) {
            if (element.equalsIgnoreCase("MO")) timePlanWeekdays.add(Calendar.MONDAY);
            if (element.equalsIgnoreCase("TU")) timePlanWeekdays.add(Calendar.TUESDAY);
            if (element.equalsIgnoreCase("WE") || element.equalsIgnoreCase("MI")) timePlanWeekdays.add(Calendar.WEDNESDAY);
            if (element.equalsIgnoreCase("TH")|| element.equalsIgnoreCase("DO")) timePlanWeekdays.add(Calendar.THURSDAY);
            if (element.equalsIgnoreCase("FR")) timePlanWeekdays.add(Calendar.FRIDAY);
            if (element.equalsIgnoreCase("SA")) timePlanWeekdays.add(Calendar.SATURDAY);
            if (element.equalsIgnoreCase("SU")|| element.equalsIgnoreCase("SO")) timePlanWeekdays.add(Calendar.SUNDAY);
        }

        // days of month
        node = nodeTime + ".monthdays";
        timePlanMonthdays= getElementsOfStringAsInt(node);
        
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
        if (elements.indexOf(",") > 0) {
            elementList = Util.stringToList(elements, ",");
        } else {
            elementList = Util.stringToList(elements, " ");
        }
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
     * List of days in weeks. <p>
     * Use Object Calendar to map the values to a weekday (e.g. Calendar.MONDAY). 
     * @return
     */
    public List<Integer> getTimePlanWeekdays() {
        return timePlanWeekdays;
    }

    /**
     * List of days in month.
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
}
