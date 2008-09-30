package de.happtick.configuration;

import java.net.Socket;
import java.util.List;

import de.happtick.configuration.client.HapptickConfigurationClient;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.core.util.Util;

/**
 * This class delivers the happtick configuration.
 * <p>
 * To use this class there must run a !EOF-Server on the server where the
 * configuration is stored.
 * 
 * @author Dirk
 * 
 */
public class HapptickConfiguration {

    private HapptickConfigurationClient happConfClient;
    private ConfigurationClient confClient;
    private Socket socketToServer;
    private String ip;
    private int port;

    public HapptickConfiguration(Socket socketToServer, TimeOut timeout, String[] args) throws ActionFailedException {
        happConfClient = new HapptickConfigurationClient(socketToServer, timeout, args);
        this.socketToServer = socketToServer;
    }

    public HapptickConfiguration(String ip, int port, TimeOut timeout, String... args) throws ActionFailedException {
        happConfClient = new HapptickConfigurationClient(ip, port, timeout, args);
        this.ip = ip;
        this.port = port;
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
        return happConfClient.getApplicationConfigurations();
    }

    /**
     * Requests the service for a list of all configured chains. <br>
     * Every call of this function executes a complex communication act between
     * the client and the service. So be sure that a repeated call is really
     * required before you do so.
     * 
     * @return A list with the chain configurations like stored in the happtick
     *         xml file.
     * @throws ActionFailedException
     *             If any exception occurs.
     */
    public List<ChainConfiguration> getChainConfigurations() throws ActionFailedException {
        return happConfClient.getChainConfigurations();
    }

    /**
     * The scheduler of happtick can work in different manners. <br>
     * This method looks if the timer-mode is active.
     * 
     * @return True if timer mode is active. False if not OR the request to the
     *         service couldn't be sent successfull.
     * @throws ActionFailedException
     *             If problems with the communication occured.
     */
    public boolean isTimerActive() throws ActionFailedException {
        initConfClient();
        return Util.parseBoolean(confClient.getAttribute("scheduler.use", "timer"), false);
    }

    /**
     * The scheduler of happtick can work in different manners. <br>
     * This method looks if the chain-mode is active.
     * 
     * @return True if chain mode is active. False if not OR the request to the
     *         service couldn't be sent successfull.
     * @throws ActionFailedException
     *             If problems with the communication occured.
     */
    public boolean isChainActive() throws ActionFailedException {
        initConfClient();
        return Util.parseBoolean(confClient.getAttribute("scheduler.use", "chain"), false);
    }

    /**
     * If the happtick configuration is not part of the servers basic
     * configuration this method enables adding one more configuration file to
     * the server. <br>
     * There is no limit for adding files.
     * 
     * @param filename
     *            Complete path of the happtick configuration file including
     *            path.
     * @throws ActionFailedException
     *             If there are problems with communication.
     */
    public void setHapptickConfigurationFile(String fileName) throws ActionFailedException {
        initConfClient();
        confClient.addConfigurationFile(fileName);
    }

    private void initConfClient() throws ActionFailedException {
        if (null == confClient) {
            if (null != socketToServer) {
                confClient = new ConfigurationClient(socketToServer, null, null);
            } else {
                confClient = new ConfigurationClient(ip, port, null);
            }
        }
    }
}
