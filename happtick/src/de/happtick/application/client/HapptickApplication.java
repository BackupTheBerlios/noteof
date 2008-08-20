package de.happtick.application.client;

import de.happtick.core.application.client.ApplicationClient;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.AlarmEvent;
import de.happtick.core.interfaces.ClientObserver;
import de.happtick.core.interfaces.ErrorEvent;
import de.happtick.core.interfaces.EventEvent;
import de.happtick.core.interfaces.LogEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

/**
 * This class is the connector between the application and an application
 * service which runs on server side.
 * <p>
 * Main mission is to control the start allowance of the application and to
 * inform the server about events on client side.
 * 
 * @author dirk
 * 
 */
public class HapptickApplication {

    private String serverAddress;
    private int serverPort;
    private Long applicationId;
    private boolean isWorkAllowed = false;
    private ApplicationClient applicationClient;
    private String[] args;

    /**
     * If this constructor is used at a later time point the serverAddress and
     * the port to the happtick scheduler must be set. Maybe it is a way to
     * write ip and port into a configuration file and get them by using the
     * class LocalConfigurationClient.
     */
    public HapptickApplication() {

    }

    /**
     * Constructor with connection informations.
     * 
     * @param applicationId
     *            Unique identifier for the configured applications within the
     *            happtick configuration. This id is used by the scheduler to
     *            distinguish between the applications.
     * @param serverAddress
     *            The ip to the happtick server where the scheduler is running.
     * @param serverPort
     *            The port of the happtick server where the scheduler is
     *            running.
     */
    public HapptickApplication(long applicationId, String serverAddress, int serverPort, String... args) {
        this.applicationId = applicationId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;
    }

    /**
     * Connect with the happtick server. Exactly this means to connect with an
     * application service on the happtick server. <br>
     * The service later decides if the application may run -> startAllowed(). <br>
     * If you use this method, the connection informations (ip, port of happtick
     * server) must be set before.
     * 
     * @throws HapptickException
     */
    public void connect() throws HapptickException {
        connect(this.serverAddress, this.serverPort);
    }

    /**
     * Connect with the happtick server. Exactly this means to connect with an
     * application service on the happtick server. <br>
     * The service later decides if the application may run -> startAllowed().
     * 
     * @param serverAddress
     *            The ip to the happtick server where the scheduler is running.
     * @param serverPort
     *            The port of the happtick server where the scheduler is
     *            running.
     * @throws HapptickException
     */
    public void connect(String serverAddress, int serverPort) throws HapptickException {
        if (Util.isEmpty(serverAddress))
            throw new HapptickException(50L, "Server Addresse: " + serverAddress);
        if (0 == serverPort)
            throw new HapptickException(50L, "Server Port = " + serverPort);
        if (null == applicationId)
            throw new HapptickException(50L, "Application Id ist NULL");

        if (null == applicationClient) {
            applicationClient = new ApplicationClient();
        }

        try {
            // connect with service
            applicationClient.connect(serverAddress, serverPort, null);
            // set unique application id
            applicationClient.setApplicationId(applicationId);
            // use args to set start id if start client has started this and the
            // id was set within the calling parameters
            applicationClient.setStartId(args);
        } catch (ActionFailedException e) {
            throw new HapptickException(100L, e);
        }
    }

    /**
     * Sets the unique application id.
     * 
     * @param applicationId
     *            Unique identifier for the configured applications within the
     *            happtick configuration. This id is used by the scheduler to
     *            distinguish between the applications.
     */
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * 
     * @return The hopefully unique application id like which is used in the
     *         happtick configuration.
     */
    public Long getApplicationId() {
        return this.applicationId;
    }

    /**
     * Set the connection data for communication with happtick server / happtick
     * application service
     */
    public void setServerConnectionData(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Ask the application service if the application may do it's job.
     * <p>
     * Perhaps there is actually another process of the application running and
     * it is not allowed to have more than one active working processes of this
     * application kind. Then this instance can wait till the service allows to
     * start. Or it stops itself and will be started at a later moment by the
     * scheduler. <br>
     * The situation that the application isn't allowed to work maybe can arrive
     * <br>
     * - if it is started manually <br>
     * - if the scheduler makes faults - application or network errors derange
     * the communication between the application clients and the application
     * services so that the scheduler starte the application twice or multiple.
     * 
     * @return True if the application can start the work.
     * @throws HapptickException
     */
    public boolean isWorkAllowed() throws HapptickException {
        checkApplicationClientInitialized();
        // only when start allowance not given yet service must be asked for
        if (!isWorkAllowed) {
            isWorkAllowed = applicationClient.isWorkAllowed();
        }
        return isWorkAllowed;
    }

    /**
     * Alternately to wait for start allowance by calling the method
     * isWorkAllowed() repeatedly within a loop it is possible to let the
     * application informed by this method. Condition is that the application
     * implements the interface Observer and waits for start allowance in the
     * method update(). When the allowance is given the application client calls
     * the method observers startAllowanceEvent()<br>
     * 
     * @throws HapptickException
     */
    public void observeForWorkAllowance(ClientObserver clientObserver) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.observeForWorkAllowance(clientObserver);
    }

    /**
     * Errors can be shown within the happtick monitoring tool or written to
     * logfiles.
     * <p>
     * Errors don't release events.
     * 
     * @param id
     *            The error identifier.
     * @param level
     *            Error level.
     * @param errorDescription
     *            Additional information for solving the problem.
     * @throws HapptickException
     */
    public void sendError(ErrorEvent event) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.sendError(event);
    }

    /**
     * Happtick is able to react to events. There are standard events like start
     * and stop of application. The relations between them are configurable.
     * Supplemental events and actions can be configured for single
     * applications.
     * 
     * @see EventEvent
     * @param event
     *            Implementation of EventEvent
     * @throws HapptickException
     */
    public void sendEvent(EventEvent event) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.sendEvent(event);
    }

    /**
     * Releases an alert. <br>
     * Like errors alarms can have a level. The controlling alarm system of
     * happtick decides what to do depending to the alarm level.
     * 
     * @see AlarmEvent
     * @param alarm
     *            Object which implements type AlarmEvent.
     * @throws HapptickException
     */
    public void sendAlarm(AlarmEvent alarm) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.sendAlarm(alarm);
    }

    /**
     * Log informations can be visualized within the happtick monitoring tool or
     * written to log files on the server.
     * 
     * @see LogEvent
     * @param log
     *            Object which implements type LogEvent.
     * @throws HapptickException
     */
    public void sendLog(LogEvent log) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.sendLog(log);
    }

    /**
     * Informs the happtick server that the application has stopped.
     * <p>
     * Very important to call this at end of work! <br>
     * The connections between happtick clients and happtick services are
     * controlled by a so called 'LifeSignSystem'. The connection will not be
     * closed as long as the underlying communication layer hasn't stopped. And
     * so the java vm stays active.
     * 
     * @throws HapptickException
     */
    public void stop() throws HapptickException {
        stop(0);
    }

    /**
     * Informs the happtick server that the application has stopped.
     * <p>
     * Very important to call this at end of work! <br>
     * The connections between happtick clients and happtick services are
     * controlled by a so called 'LifeSignSystem'. The connection will not be
     * closed as long as the underlying communication layer hasn't stopped. And
     * so the java vm stays active.
     * 
     * @param exitCode
     *            Result value of the application.
     * @throws HapptickException
     */
    public void stop(int exitCode) throws HapptickException {

        if (null != applicationClient) {
            applicationClient.stop(exitCode);
        }
    }

    public void startWork() throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.startWork();
    }

    /**
     * If the using class has started the observing for awaiting the start
     * allowance this can be stopped here.
     */
    public void stopObservingForStartAllowance() {
        applicationClient.stopObservingForStartAllowance();
    }

    /*
     * Check if the applicationClient exists...
     */
    private void checkApplicationClientInitialized() throws HapptickException {
        if (null == applicationClient)
            throw new HapptickException(50L, "Client ist nicht initialisiert. Vermutlich wurde kein connect durchgeführt.");
    }

}
