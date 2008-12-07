package de.happtick.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        map.put("internal->applicationId", String.valueOf(applicationConfiguration.getApplicationId()));
        map.put("nodeNameApplication", String.valueOf(applicationConfiguration.getNodeNameApplication()));
        map.put("clientIp", String.valueOf(applicationConfiguration.getClientIp()));
        map.put("executableType", String.valueOf(applicationConfiguration.getExecutableType()));
        map.put("executablePath", String.valueOf(applicationConfiguration.getExecutablePath()));
        map.put("multipleStart", String.valueOf(applicationConfiguration.isMultipleStart()));
        map.put("partOfChain", String.valueOf(applicationConfiguration.isPartOfChain()));
        map.put("windowsSupport", String.valueOf(applicationConfiguration.isWindowsSupport()));
        map.put("enforce", String.valueOf(applicationConfiguration.isEnforce()));
        map.put("maxStartStop", String.valueOf(applicationConfiguration.getMaxStartStop()));
        map.put("maxStepStep", String.valueOf(applicationConfiguration.getMaxStepStep()));
        map.put("executableArgs", String.valueOf(applicationConfiguration.getExecutableArgs()));

        map.put("timePlanSeconds", String.valueOf(applicationConfiguration.getTimePlanSeconds()));
        map.put("timePlanMinutes", String.valueOf(applicationConfiguration.getTimePlanMinutes()));
        map.put("timePlanHours", String.valueOf(applicationConfiguration.getTimePlanHours()));
        map.put("timePlanWeekdays", String.valueOf(applicationConfiguration.getTimePlanWeekdays()));
        map.put("timePlanMonthdays", String.valueOf(applicationConfiguration.getTimePlanMonthdays()));
        // time values comma separated

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

        // environment configuration for application
        if (!Util.isEmpty(applicationConfiguration.getEnvironment())) {
            Set<String> envSet = applicationConfiguration.getEnvironment().keySet();
            int i = 0;
            for (String key : envSet) {
                String val = applicationConfiguration.getEnvironment().get(key);
                key = "$ENV$" + i++ + "ENV->" + key;
                map.put(key, val);
            }
        }
    }

    /*
     * wrap map to object
     */
    private void createApplicationConfiguration() {
        applicationConfiguration = new ApplicationConfiguration(Long.valueOf(map.get("internal->applicationId")));

        // atomize the vars to class attributes
        applicationConfiguration.setNodeNameApplication(map.get("nodeNameApplication"));
        applicationConfiguration.setClientIp(map.get("clientIp"));
        applicationConfiguration.setExecutableType(map.get("executableType"));
        applicationConfiguration.setExecutablePath(map.get("executablePath"));
        applicationConfiguration.setMultipleStart(Util.parseBoolean(map.get("multipleStart"), false));
        applicationConfiguration.setEnforce(Util.parseBoolean(map.get("enforce"), false));
        applicationConfiguration.setWindowsSupport(Util.parseBoolean(map.get("windows"), false));
        applicationConfiguration.setPartOfChain(Util.parseBoolean(map.get("partOfChain"), false));
        applicationConfiguration.setMaxStartStop(Util.parseInt(map.get("maxStartStop"), 0));
        applicationConfiguration.setMaxStepStep(Util.parseInt(map.get("maxStepStep"), 0));
        applicationConfiguration.setExecutableArgs(map.get("executableArgs"));

        applicationConfiguration.setTimePlanSeconds(map.get("timePlanSeconds"));
        applicationConfiguration.setTimePlanMinutes(map.get("timePlanMinutes"));
        applicationConfiguration.setTimePlanHours(map.get("timePlanHours"));
        applicationConfiguration.setTimePlanWeekdays(map.get("timePlanWeekdays"));
        applicationConfiguration.setTimePlanMonthdays(map.get("timePlanMonthdays"));

        // dependency lists also are transported as csv strings
        applicationConfiguration.setApplicationsWaitFor(csv2ListLong(map, "applicationsWaitFor"));
        applicationConfiguration.setApplicationsStartAfter(csv2ListLong(map, "applicationsStartAfter"));
        applicationConfiguration.setApplicationsStartSync(csv2ListLong(map, "applicationsStartSync"));

        Map<String, String> environment = new HashMap<String, String>();

        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (key.startsWith("$ENV$")) {
                String val = map.get(key);
                int pos = key.indexOf("ENV->") + "ENV->".length();
                System.out.println("ApplicationConfigurationWrapper... key vorher: " + key);
                key = key.substring(pos);
                System.out.println("ApplicationConfigurationWrapper... key nachher: " + key);
                environment.put(key, val);
            }
        }
        applicationConfiguration.setEnvironment(environment);
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
