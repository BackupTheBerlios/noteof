package de.happtick.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.notEOF.core.util.Util;

/**
 * Used to create attributes of an ApplicationConfiguration object by a map or
 * vice versa
 * 
 * @author Dirk
 * 
 */
public class ApplicationConfigurationWrapper {

    private ApplicationConfiguration applicationConfiguration;
    private Map<String, String> map;

    /**
     * This constructor creates a map with attribute-values of an
     * ApplicationConfiguration.
     * <p>
     * The map with attributes is delivered by the method getMap() <br>
     * Attention! The ApplicationConfiguration has so named 'arguments'. This is
     * a list of Strings which are available by the ApplicationConfiguration
     * object itself. To get arguments use the method getExecutableArgs().
     * 
     * @param applicationConfiguration
     *            The Object to be wrapped into map and list format.
     */
    public ApplicationConfigurationWrapper(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
        createMap();
    }

    /**
     * This constructor creates an object of type
     * {@link ApplicationConfiguration}.
     * <p>
     * The map must contain all required / supported attributes of class
     * ApplicationConfiguration. <br>
     * Ensure that one value of the map must contain the applicationId!
     * 
     * @param map
     *            Map which contains the attributes, identified by keys which
     *            names are identical with the attribute names of the class.
     */
    public ApplicationConfigurationWrapper(Map<String, String> map) {
        this.map = map;
        createApplicationConfiguration();
    }

    /**
     * Delivers the Map which was created by attributes of the
     * ApplicationConfiguration (see construction of this wrapper).
     * 
     * @return The map or NULL if an error occured by the conversion.
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Returns an ApplicationConfiguration object which was created by the map
     * values (see construction of this wrapper).
     * 
     * @return Object of type {@link ApplicationConfiguration}
     */
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    /*
     * wrap object to map
     */
    private void createMap() {
        // all data except the map of calling arguments (parameters)
        // as one big list
        map = new HashMap<String, String>();
        map.put("applicationId", String.valueOf(applicationConfiguration.getApplicationId()));
        map.put("nodeNameApplication", String.valueOf(applicationConfiguration.getNodeNameApplication()));
        map.put("clientIp", String.valueOf(applicationConfiguration.getClientIp()));
        map.put("executableType", String.valueOf(applicationConfiguration.getExecutableType()));
        map.put("executablePath", String.valueOf(applicationConfiguration.getExecutablePath()));
        map.put("multipleStart", String.valueOf(applicationConfiguration.isMultipleStart()));
        map.put("enforce", String.valueOf(applicationConfiguration.isEnforce()));
        map.put("maxStartStop", String.valueOf(applicationConfiguration.getMaxStartStop()));
        map.put("maxStepStep", String.valueOf(applicationConfiguration.getMaxStepStep()));

        // time values comma separated
        // seconds
        String timePlanString = "";
        if (null != applicationConfiguration.getTimePlanSeconds()) {
            for (Integer timeValue : applicationConfiguration.getTimePlanSeconds()) {
                timePlanString += timeValue + ",";
            }
            if (timePlanString.endsWith(","))
                timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
        }
        map.put("timePlanSeconds", timePlanString);

        // minutes
        timePlanString = "";
        if (null != applicationConfiguration.getTimePlanMinutes()) {
            for (Integer timeValue : applicationConfiguration.getTimePlanMinutes()) {
                timePlanString = timePlanString + timeValue + ",";
            }
            if (timePlanString.endsWith(","))
                timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
        }
        map.put("timePlanMinutes", timePlanString);

        // hours
        timePlanString = "";
        if (null != applicationConfiguration.getTimePlanHours()) {
            for (Integer timeValue : applicationConfiguration.getTimePlanHours()) {
                timePlanString = timePlanString + timeValue + ",";
            }
            if (timePlanString.endsWith(","))
                timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
        }
        map.put("timePlanHours", timePlanString);

        // weekdays
        timePlanString = "";
        if (null != applicationConfiguration.getTimePlanWeekdays()) {
            for (Integer timeValue : applicationConfiguration.getTimePlanWeekdays()) {
                timePlanString = timePlanString + timeValue + ",";
            }
            if (timePlanString.endsWith(","))
                timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
        }
        map.put("timePlanWeekdays", timePlanString);

        // days of month
        timePlanString = "";
        if (null != applicationConfiguration.getTimePlanMonthdays()) {
            for (Integer timeValue : applicationConfiguration.getTimePlanMonthdays()) {
                timePlanString = timePlanString + timeValue + ",";
            }
            if (timePlanString.endsWith(","))
                timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
        }
        map.put("timePlanMonthdays", timePlanString);

        // dependencies also as comma separated lists
        // applications to wait for
        String appIdString = "";
        if (null != applicationConfiguration.getApplicationsWaitFor()) {
            for (Long appId : applicationConfiguration.getApplicationsWaitFor()) {
                appIdString = appIdString + appId + ",";
            }
            if (appIdString.endsWith(","))
                appIdString = appIdString.substring(0, appIdString.length() - 1);
        }
        map.put("applicationsWaitFor", appIdString);

        // applications to start after
        appIdString = "";
        if (null != applicationConfiguration.getApplicationsStartAfter()) {
            for (Long appId : applicationConfiguration.getApplicationsStartAfter()) {
                appIdString = appIdString + appId + ",";
            }
            if (appIdString.endsWith(","))
                appIdString = appIdString.substring(0, appIdString.length() - 1);
        }
        map.put("applicationsStartAfter", appIdString);

        // applications start synchronously
        appIdString = "";
        if (null != applicationConfiguration.getApplicationsStartSync()) {
            for (Long appId : applicationConfiguration.getApplicationsStartSync()) {
                appIdString = appIdString + appId + ",";
            }
            if (appIdString.endsWith(","))
                appIdString = appIdString.substring(0, appIdString.length() - 1);
        }
        map.put("applicationsStartSync", appIdString);
    }

    /*
     * wrap map to object
     */
    private void createApplicationConfiguration() {
        applicationConfiguration = new ApplicationConfiguration(Long.valueOf(map.get("applicationId")));

        // atomize the vars to class attributes
        applicationConfiguration.setNodeNameApplication(map.get("nodeNameApplication"));
        applicationConfiguration.setClientIp(map.get("clientIp"));
        applicationConfiguration.setExecutableType(map.get("executableType"));
        applicationConfiguration.setExecutablePath(map.get("executablePath"));
        applicationConfiguration.setMultipleStart(Util.parseBoolean(map.get("multipleStart"), false));
        applicationConfiguration.setEnforce(Util.parseBoolean(map.get("enforce"), false));
        applicationConfiguration.setMaxStartStop(Util.parseInt(map.get("maxStartStop"), 0));
        applicationConfiguration.setMaxStepStep(Util.parseInt(map.get("maxStepStep"), 0));

        // timeplan values as list are formatted like csv (comma
        // separated)
        applicationConfiguration.setTimePlanSeconds(csv2ListInteger(map, "timePlanSeconds"));
        applicationConfiguration.setTimePlanMinutes(csv2ListInteger(map, "timePlanMinutes"));
        applicationConfiguration.setTimePlanHours(csv2ListInteger(map, "timePlanHours"));
        applicationConfiguration.setTimePlanWeekdays(csv2ListInteger(map, "timePlanWeekdays"));
        applicationConfiguration.setTimePlanMonthdays(csv2ListInteger(map, "timePlanMonthdays"));

        // dependency lists also are transported as csv strings
        applicationConfiguration.setApplicationsWaitFor(csv2ListLong(map, "applicationsWaitFor"));
        applicationConfiguration.setApplicationsStartAfter(csv2ListLong(map, "applicationsStartAfter"));
        applicationConfiguration.setApplicationsStartSync(csv2ListLong(map, "applicationsStartSync"));
    }

    /*
     * Transformation of comma separated Strings to lists
     */
    private List<Integer> csv2ListInteger(Map<String, String> confVars, String key) {
        String csvString = confVars.get(key);
        if (null == csvString)
            return null;

        List<String> stringList = Util.stringToList(csvString, ",");
        List<Integer> intList = new ArrayList<Integer>();
        for (String element : stringList) {
            intList.add(Util.parseInt(element, -1));
        }
        return intList;
    }

    /*
     * Transformation of comma separated Strings to lists
     */
    private List<Long> csv2ListLong(Map<String, String> confVars, String key) {
        String csvString = confVars.get(key);
        if (null == csvString)
            return null;

        List<String> stringList = Util.stringToList(csvString, ",");
        List<Long> intList = new ArrayList<Long>();
        for (String element : stringList) {
            intList.add(Util.parseLong(element, -1));
        }
        return intList;
    }

}
