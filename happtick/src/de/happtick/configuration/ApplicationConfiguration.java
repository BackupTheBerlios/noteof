package de.happtick.configuration;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * This class represents configuration entries of applications.
 * 
 * @author Dirk
 * 
 */
public class ApplicationConfiguration {

    private Long applicationId;
    private String nodeName;
    private String clientIp;
    private String executablePath;
    private boolean multipleStart;
    private boolean enforce;
    private Map<String, String> executableArgs;
    private List<Integer> timePlanSeconds;
    private List<Integer> timePlanMinutes;
    private List<Integer> timePlanHours;
    private List<Integer> timePlanWeekdays;
    private List<String> timePlanMonthdays;
    private List<Long> applicationsWaitFor;
    private List<Long> applicationsStartAfter;
    private List<Long> applicationsStartSync;

    /**
     * The class reads the configuration by itself.
     * 
     * @param nodeName
     *            Reference within the configuration file to the configuration
     *            of this client.
     */
    public ApplicationConfiguration(String nodeName) {

        GregorianCalendar greg = new GregorianCalendar();
        greg.get(Calendar.DAY_OF_WEEK);
        int val = Calendar.SUNDAY;

        // TODO
        // weekdays umwandeln in Calendar-Tage. z.B. Donnerstag -> int day =
        // Calendar.THURSDAY

    }
}
