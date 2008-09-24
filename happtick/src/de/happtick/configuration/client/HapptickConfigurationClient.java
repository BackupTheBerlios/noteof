package de.happtick.configuration.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ApplicationConfigurationWrapper;
import de.happtick.configuration.enumeration.HapptickConfigTag;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.util.Util;

public class HapptickConfigurationClient extends BaseClient {

    public HapptickConfigurationClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public HapptickConfigurationClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
    }

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
            String next = requestTo(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION);
            while (!Util.isEmpty(next) && next.equals(HapptickConfigTag.INFO_OK.name())) {
                ApplicationConfiguration applConf = null;
                // request for all configuration data except parameters (calling
                // args)
                DataObject vars = receiveDataObject();
                if (null != vars) {
                    Map<String, String> confVars = vars.getMap();
                    if (null != confVars) {
                        ApplicationConfigurationWrapper applWrap = new ApplicationConfigurationWrapper(confVars);
                        applConf = applWrap.getApplicationConfiguration();
                    }
                }

                // request for parameters
                DataObject params = receiveDataObject();
                applConf.setExecutableArgs(params.getMap());

                applConfs.add(applConf);
                next = requestTo(HapptickConfigTag.REQ_NEXT_APPLICATION_CONFIGURATION, HapptickConfigTag.RESP_NEXT_APPLICATION_CONFIGURATION);
            }
        }
        return applConfs;
    }

    public Class<?> serviceForClientByClass() {
        return null;
    }

    public String serviceForClientByName() {
        return "de.happtick.configuration.service.HapptickConfigurationService";
    }
}
