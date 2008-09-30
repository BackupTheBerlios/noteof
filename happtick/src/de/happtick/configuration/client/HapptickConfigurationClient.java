package de.happtick.configuration.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ApplicationConfigurationWrapper;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.ChainConfigurationWrapper;
import de.happtick.configuration.enumeration.HapptickConfigTag;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.util.Util;

/**
 * Because of it is possible that the configuration of happtick is stored at
 * another server, the configuration should be read in a network manner. <br>
 * This class offers access to the happtick configuration by using the !EOF
 * client/server functionality. <br>
 * 
 * It delivers <br>
 * - Configurations of applications <br>
 * - Configurations of events <br>
 * - Configurations of chains <br>
 * - The kind how the scheduling of happtick is used (chains and/or timer).
 * 
 * @author Dirk
 * 
 */
public class HapptickConfigurationClient extends BaseClient {

    public HapptickConfigurationClient(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, args);
    }

    public HapptickConfigurationClient(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        super(ip, port, timeout, args);
    }

    /**
     * Requests the service for a list of all configured chains.
     * <p>
     * Informations about configuration are transported over network by every
     * single use of this method.
     * 
     * @return A list with the chain configurations stoed in the happtick xml
     *         file, null if there is no configuration.
     * @throws ActionFailedException
     *             Can be fired by network/communication problems or other
     *             faults.
     */
    public List<ChainConfiguration> getChainConfigurations() throws ActionFailedException {
        // Initialize return list
        List<ChainConfiguration> chainConfs = new ArrayList<ChainConfiguration>();

        // send first request to service that the chains are required
        if (HapptickConfigTag.INFO_OK.name().equals(requestTo(HapptickConfigTag.REQ_ALL_CHAIN_CONFIGURATIONS, HapptickConfigTag.RESP_ALL_CHAIN_CONFIGURATIONS))) {
            // service perhaps will send some chains
            String next = requestTo(HapptickConfigTag.REQ_NEXT_CHAIN_CONFIGURATION, HapptickConfigTag.RESP_NEXT_CHAIN_CONFIGURATION);
            while (!Util.isEmpty(next) && next.equals(HapptickConfigTag.INFO_OK.name())) {
                ChainConfiguration chainConf = null;
                DataObject vars = receiveDataObject();
                if (null != vars) {
                    Map<String, String> confVars = vars.getMap();
                    if (null != confVars) {
                        ChainConfigurationWrapper chainWrap = new ChainConfigurationWrapper(confVars);
                        chainConf = chainWrap.getChainConfiguration();
                    }
                }

                chainConfs.add(chainConf);
                next = requestTo(HapptickConfigTag.REQ_NEXT_CHAIN_CONFIGURATION, HapptickConfigTag.RESP_NEXT_CHAIN_CONFIGURATION);
            }
        }

        return null;
    }

    /**
     * Requests the service for a list of all configured applications.
     * <p>
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
