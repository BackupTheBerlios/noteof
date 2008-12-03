package de.happtick.information.service;

import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ApplicationConfigurationWrapper;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.ChainConfigurationWrapper;
import de.happtick.configuration.EventConfiguration;
import de.happtick.configuration.EventConfigurationWrapper;
import de.happtick.core.MasterTable;
import de.happtick.core.enumeration.HapptickConfigTag;
import de.happtick.core.service.HapptickBaseService;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;

public class HapptickInformationService extends HapptickBaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        return HapptickConfigTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }

    @Override
    public void processClientMsg(Enum<?> configTag) throws ActionFailedException {
        // Client asks for all application configurations
        if (HapptickConfigTag.REQ_ALL_APPLICATION_CONFIGURATIONS.equals(configTag)) {
            deliverApplicationConfiguration();
        }

        // Client asks for all chain configurations
        if (HapptickConfigTag.REQ_ALL_CHAIN_CONFIGURATIONS.equals(configTag)) {
            deliverChainConfiguration();
        }

        // Client asks for all event configurations
        if (HapptickConfigTag.REQ_ALL_EVENT_CONFIGURATIONS.equals(configTag)) {
            deliverEventConfiguration();
        }

        // // Client asks for all event configurations
        // if (HapptickConfigTag.REQ_ALL_RAISE_CONFIGURATIONS.equals(configTag))
        // {
        // deliverRaiseConfiguration();
        // }
    }

    /*
     * Client asked for event configurations. Here the service sends the data.
     */
    private void deliverEventConfiguration() throws ActionFailedException {
        List<EventConfiguration> eventConfigurations = MasterTable.getEventConfigurationsAsList();
        if (null != eventConfigurations) {
            // tell client that there are configurations and he can begin to
            // request for them
            responseTo(HapptickConfigTag.RESP_ALL_EVENT_CONFIGURATIONS, HapptickConfigTag.INFO_OK.name());
            for (EventConfiguration eventConfiguration : eventConfigurations) {
                awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_EVENT_CONFIGURATION, HapptickConfigTag.RESP_NEXT_EVENT_CONFIGURATION,
                                            HapptickConfigTag.INFO_OK.name());

                EventConfigurationWrapper eventWrap = new EventConfigurationWrapper(eventConfiguration);
                Map<String, String> confVars = eventWrap.getMap();

                // send the map with list-data
                DataObject confObject = new DataObject();
                confObject.setMap(confVars);
                sendDataObject(confObject);
            }
            // the client requests as long for next configuration as we send
            // an OK . So the next statement answers with no more
            // configurations.
            awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_EVENT_CONFIGURATION, HapptickConfigTag.RESP_NEXT_EVENT_CONFIGURATION,
                                        HapptickConfigTag.INFO_NO_CONFIGURATION.name());
        }
    }

    // /*
    // * Client asked for event configurations. Here the service sends the data.
    // */
    // private void deliverRaiseConfiguration() throws ActionFailedException {
    // List<RaiseConfiguration> raiseConfigurations =
    // MasterTable.getRaiseConfigurationsAsList();
    // if (null != raiseConfigurations) {
    // // tell client that there are configurations and he can begin to
    // // request for them
    // responseTo(HapptickConfigTag.RESP_ALL_RAISE_CONFIGURATIONS,
    // HapptickConfigTag.INFO_OK.name());
    // for (RaiseConfiguration raiseConfiguration : raiseConfigurations) {
    //awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_RAISE_CONFIGURATION
    // , HapptickConfigTag.RESP_NEXT_RAISE_CONFIGURATION,
    // HapptickConfigTag.INFO_OK.name());
    //
    // RaiseConfigurationWrapper raiseWrap = new
    // RaiseConfigurationWrapper(raiseConfiguration);
    // Map<String, String> confVars = raiseWrap.getMap();
    //
    // // send the map with list-data
    // DataObject confObject = new DataObject();
    // confObject.setMap(confVars);
    // sendDataObject(confObject);
    // }
    // // the client requests as long for next configuration as we send
    // // an OK . So the next statement answers with no more
    // // configurations.
    //awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_RAISE_CONFIGURATION
    // , HapptickConfigTag.RESP_NEXT_RAISE_CONFIGURATION,
    // HapptickConfigTag.INFO_NO_CONFIGURATION.name());
    // }
    // }

    /*
     * Client asked for chain configurations. Here the service sends the data.
     */
    private void deliverChainConfiguration() throws ActionFailedException {
        List<ChainConfiguration> chainConfigurations = MasterTable.getChainConfigurationsAsList();
        if (null != chainConfigurations) {
            // tell client that there are configurations and he can begin to
            // request for them
            responseTo(HapptickConfigTag.RESP_ALL_CHAIN_CONFIGURATIONS, HapptickConfigTag.INFO_OK.name());
            for (ChainConfiguration chainConfiguration : chainConfigurations) {
                awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_CHAIN_CONFIGURATION, HapptickConfigTag.RESP_NEXT_CHAIN_CONFIGURATION,
                                            HapptickConfigTag.INFO_OK.name());

                ChainConfigurationWrapper chainWrap = new ChainConfigurationWrapper(chainConfiguration);
                Map<String, String> confVars = chainWrap.getMap();

                // send the map with list-data
                DataObject confObject = new DataObject();
                confObject.setMap(confVars);
                sendDataObject(confObject);
            }
            // the client requests as long for next configuration as we send
            // an OK . So the next statement answers with no more
            // configurations.
            awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_CHAIN_CONFIGURATION, HapptickConfigTag.RESP_NEXT_CHAIN_CONFIGURATION,
                                        HapptickConfigTag.INFO_NO_CONFIGURATION.name());
        }
    }

    /*
     * Client has asked for application configurations. Here the data is
     * exchanged between client and service.
     */
    private void deliverApplicationConfiguration() throws ActionFailedException {
        List<ApplicationConfiguration> applicationConfigurations = MasterTable.getApplicationConfigurationsAsList();
        if (null != applicationConfigurations) {
            // tell client that there are configurations and he can begin to
            // request for them
            responseTo(HapptickConfigTag.RESP_ALL_APPLICATION_CONFIGURATIONS, HapptickConfigTag.INFO_OK.name());
            for (ApplicationConfiguration applicationConfiguration : applicationConfigurations) {
                awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION,
                                            HapptickConfigTag.INFO_OK.name());

                ApplicationConfigurationWrapper applWrap = new ApplicationConfigurationWrapper(applicationConfiguration);
                Map<String, String> confVars = applWrap.getMap();

                // send the map with list-data
                DataObject confObject = new DataObject();
                confObject.setMap(confVars);
                sendDataObject(confObject);
            }
            // the client requests as long for next configuration as we send
            // an OK . So the next statement answers with no more
            // configurations.
            awaitRequestAnswerImmediate(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION,
                                        HapptickConfigTag.INFO_NO_CONFIGURATION.name());
        }
    }

    public List<EventType> getObservedEvents() {
        return null;
    }

}
