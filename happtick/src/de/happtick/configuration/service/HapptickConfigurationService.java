package de.happtick.configuration.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.enumeration.HapptickConfigTag;
import de.happtick.core.MasterTable;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;

public class HapptickConfigurationService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        return HapptickConfigTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }

    @Override
    public void processMsg(Enum<?> configTag) throws ActionFailedException {
        // Client asks for all application configurations
        if (HapptickConfigTag.REQ_ALL_APPLICATION_CONFIGURATIONS.equals(configTag)) {
            deliverApplicationConfiguration();
        }
    }
    
    
    /*
     * client has asked for application configurations.
     * here the data is exchanged between client and service.
     */
    private void deliverApplicationConfiguration() throws ActionFailedException {
        List<ApplicationConfiguration> applicationConfigurations = MasterTable.getApplicationConfigurationsAsList();
        if (null != applicationConfigurations) {
            // tell client that there are configurations and he can begin to
            // request for them
            responseTo(HapptickConfigTag.RESP_ALL_APPLICATION_CONFIGURATIONS, HapptickConfigTag.INFO_OK.name());
            for (ApplicationConfiguration applicationConfiguration : applicationConfigurations) {
                // prepare and send part one:
                // the applicationId like configured
                awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION,
                                            applicationConfiguration.getApplicationId().toString());

                // prepare part two:
                // all data except the map of calling arguments (parameters)
                // as one big list
                Map<String, String> confVars = new HashMap<String, String>();
                // confVars.put("applicationId",
                // String.valueOf(applicationConfiguration
                // .getApplicationId()));
                confVars.put("nodeNameApplication", String.valueOf(applicationConfiguration.getNodeNameOfApplication()));
                confVars.put("clientIp", String.valueOf(applicationConfiguration.getClientIp()));
                confVars.put("executableType", String.valueOf(applicationConfiguration.getExecutableType()));
                confVars.put("executablePath", String.valueOf(applicationConfiguration.getExecutablePath()));
                confVars.put("multipleStart", String.valueOf(applicationConfiguration.isMultipleStart()));
                confVars.put("enforce", String.valueOf(applicationConfiguration.isEnforce()));
                confVars.put("maxStartStop", String.valueOf(applicationConfiguration.getMaxStartStop()));
                confVars.put("maxStepStep", String.valueOf(applicationConfiguration.getMaxStepStep()));

                // time values comma separated
                // seconds
                String timePlanString = "";
                if (null != applicationConfiguration.getTimePlanSeconds()) {
                    for (Integer timeValue : applicationConfiguration.getTimePlanSeconds()) {
                        timePlanString = timePlanString + timeValue + ",";
                    }
                    if (timePlanString.endsWith(","))
                        timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
                }
                confVars.put("timePlanSeconds", timePlanString);

                // minutes
                timePlanString = "";
                if (null != applicationConfiguration.getTimePlanMinutes()) {
                    for (Integer timeValue : applicationConfiguration.getTimePlanMinutes()) {
                        timePlanString = timePlanString + timeValue + ",";
                    }
                    if (timePlanString.endsWith(","))
                        timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
                }
                confVars.put("timePlanMinutes", timePlanString);

                // hours
                timePlanString = "";
                if (null != applicationConfiguration.getTimePlanHours()) {
                    for (Integer timeValue : applicationConfiguration.getTimePlanHours()) {
                        timePlanString = timePlanString + timeValue + ",";
                    }
                    if (timePlanString.endsWith(","))
                        timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
                }
                confVars.put("timePlanHours", timePlanString);

                // weekdays
                timePlanString = "";
                if (null != applicationConfiguration.getTimePlanWeekdays()) {
                    for (Integer timeValue : applicationConfiguration.getTimePlanWeekdays()) {
                        timePlanString = timePlanString + timeValue + ",";
                    }
                    if (timePlanString.endsWith(","))
                        timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
                }
                confVars.put("timePlanWeekdays", timePlanString);

                // days of month
                timePlanString = "";
                if (null != applicationConfiguration.getTimePlanMonthdays()) {
                    for (Integer timeValue : applicationConfiguration.getTimePlanMonthdays()) {
                        timePlanString = timePlanString + timeValue + ",";
                    }
                    if (timePlanString.endsWith(","))
                        timePlanString = timePlanString.substring(0, timePlanString.length() - 1);
                }
                confVars.put("timePlanMonthdays", timePlanString);

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
                confVars.put("applicationsWaitFor", appIdString);

                // applications to start after
                appIdString = "";
                if (null != applicationConfiguration.getApplicationsStartAfter()) {
                    for (Long appId : applicationConfiguration.getApplicationsStartAfter()) {
                        appIdString = appIdString + appId + ",";
                    }
                    if (appIdString.endsWith(","))
                        appIdString = appIdString.substring(0, appIdString.length() - 1);
                }
                confVars.put("applicationsStartAfter", appIdString);

                // applications start synchronously
                appIdString = "";
                if (null != applicationConfiguration.getApplicationsStartSync()) {
                    for (Long appId : applicationConfiguration.getApplicationsStartSync()) {
                        appIdString = appIdString + appId + ",";
                    }
                    if (appIdString.endsWith(","))
                        appIdString = appIdString.substring(0, appIdString.length() - 1);
                }
                confVars.put("applicationsStartSync", appIdString);

                // send part two: the map with list-data
                DataObject confObject = new DataObject();
                confObject.setMap(confVars);
                sendDataObject(confObject);

                // prepare and send part three:
                // arguments
                DataObject argsData = new DataObject();
                Map<String, String> args;
                args = applicationConfiguration.getExecutableArgs();
                if (null == args) {
                    args = new HashMap<String, String>();
                }
                argsData.setMap(args);
                sendDataObject(argsData);

            }
            // the client requests as long for next configuration as we send
            // a valid applicationId. So the next statement answers with an
            // empty id.
            awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION, "");
        }
    }

    public List<EventType> getObservedEvents() {
        return null;
    }

}
