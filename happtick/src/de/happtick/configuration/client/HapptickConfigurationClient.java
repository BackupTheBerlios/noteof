package de.happtick.configuration.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.enumeration.HapptickConfigTag;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

public class HapptickConfigurationClient extends BaseClient {

    /**
     * Requests the service for a list of all configured applications. <br>
     * Every call of this function executes a complex communication act between
     * the client and the service. So be sure that a repeated call is really
     * required before you do so.
     * 
     * @return A list with the application configurations like stored in the
     *         happtick xml file.
     * @throws ActionFailedException
     */
    public List<ApplicationConfiguration> getApplicationConfigurations() throws ActionFailedException {
        // the list to return as result
        List<ApplicationConfiguration> applConfs = new ArrayList<ApplicationConfiguration>();

        // Client sends initial request for all application configurations
        if (HapptickConfigTag.INFO_OK.name().equals(
                                                    requestTo(HapptickConfigTag.REQ_ALL_APPLICATION_CONFIGURATIONS,
                                                              HapptickConfigTag.RESP_ALL_APPLICATION_CONFIGURATIONS))) {
            // OK, service tells that there are application configurations
            String applicationId = requestTo(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION);
            while (!Util.isEmpty(applicationId)) {
                Long appId = Util.parseLong(applicationId, 0);
                // new configuration with id
                ApplicationConfiguration applConf = new ApplicationConfiguration(appId);

                // request for all configuration data except parameters (calling
                // args)
                DataObject vars = receiveDataObject();
                if (null != vars) {
                    Map<String, String> confVars = vars.getMap();

                    if (null != confVars) {
                        // atomize the vars to class attributes
                        applConf.setNodeNameApplication(confVars.get("nodeNameApplication"));
                        applConf.setClientIp(confVars.get("clientIp"));
                        applConf.setExecutableType(confVars.get("executableType"));
                        applConf.setExecutablePath(confVars.get("executablePath"));
                        applConf.setMultipleStart(Util.parseBoolean(confVars.get("multipleStart"), false));
                        applConf.setEnforce(Util.parseBoolean(confVars.get("enforce"), false));
                        applConf.setMaxStartStop(Util.parseInt(confVars.get("maxStartStop"), 0));
                        applConf.setMaxStepStep(Util.parseInt(confVars.get("maxStepStep"), 0));

                        // timeplan values as list are formatted like csv (comma
                        // separated)
                        applConf.setTimePlanSeconds(csv2ListInteger(confVars, "timePlanSeconds"));
                        applConf.setTimePlanMinutes(csv2ListInteger(confVars, "timePlanMinutes"));
                        applConf.setTimePlanHours(csv2ListInteger(confVars, "timePlanHours"));
                        applConf.setTimePlanWeekdays(csv2ListInteger(confVars, "timePlanWeekdays"));
                        applConf.setTimePlanMonthdays(csv2ListInteger(confVars, "timePlanMonthdays"));

                        // dependency lists also are transported as csv strings
                        applConf.setApplicationsWaitFor(csv2ListLong(confVars, "applicationsWaitFor"));
                        applConf.setApplicationsStartAfter(csv2ListLong(confVars, "applicationsStartAfter"));
                        applConf.setApplicationsStartSync(csv2ListLong(confVars, "applicationsStartSync"));
                    }
                }

                // request for parameters
                DataObject params = receiveDataObject();
                applConf.setExecutableArgs(params.getMap());

                applConfs.add(applConf);

                applicationId = requestTo(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION);
            }
        }
        return applConfs;
    }

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

    public Class<?> serviceForClientByClass() {
        // TODO Auto-generated method stub
        return null;
    }

    public String serviceForClientByName() {
        return "de.happtick.configuration.service.HapptickConfigurationService";
    }
}
