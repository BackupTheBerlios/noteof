package de.happtick.application.client;

import de.happtick.core.application.client.ApplicationClient;
import de.happtick.core.client.HapptickBaseClient;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.ClientObserver;

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
public class HapptickApplication extends HapptickBaseClient {

    private Long applicationId;
    private boolean isWorkAllowed = false;
    private ApplicationClient applicationClient = new ApplicationClient();

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
     * @param args
     *            A HapptickApplication must be called with the parameter
     *            --startId=<value>.
     */
    public HapptickApplication(long applicationId, String serverAddress, int serverPort, String... args) throws HapptickException {
        connect(serverAddress, serverPort, args, applicationClient);
        // initHapptickBaseClient(serverAddress, serverPort, args,
        // applicationClient);
        this.applicationId = applicationId;
        applicationClient.applicationIdToService(applicationId);
        applicationClient.startIdToService(args);
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

    public String getClientNetId() {
        return this.applicationClient.getClientNetId();
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
        checkClientInitialized();
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
        checkClientInitialized();
        applicationClient.observeForWorkAllowance(clientObserver);
    }

    /**
     * Errors can be shown within the happtick monitoring tool or written to
     * logfiles.
     * <p>
     * Errors don't release events.
     * 
     * @param errorId
     *            The error identifier.
     * @param level
     *            Error level.
     * @param Description
     *            Additional information for solving the problem.
     * @throws HapptickException
     */
    public void sendError(String errorId, String description, String level) throws HapptickException {
        checkClientInitialized();
        applicationClient.sendError(errorId, description, level);
    }

    /**
     * Happtick is able to react to events. There are standard events like start
     * and stop of application. The relations between them are configurable.
     * Supplemental events and actions can be configured for single
     * applications.
     * 
     * @param eventId
     *            Id which is used in the configuration.
     * @param information
     *            Additional information related to the action.
     * @throws HapptickException
     */
    public void sendActionEvent(String eventId, String information) throws HapptickException {
        checkClientInitialized();
        applicationClient.sendActionEvent(eventId, information);
    }

    /**
     * Releases an alert. <br>
     * Like errors alarms can have a level. The controlling alarm system of
     * happtick decides what to do depending to the alarm level.
     * 
     * @param description
     *            Alarm text. What happened exactly.
     * @param level
     *            Meaning of alarm (info, warning or anything else). Depends to
     *            the application.
     * @throws HapptickException
     */
    public void sendAlarm(String type, String description, String level) throws HapptickException {
        checkClientInitialized();
        applicationClient.sendAlarm(type, description, level);
    }

    /**
     * Log informations can be visualized within the happtick monitoring tool or
     * written to log files on the server.
     * 
     * @param information
     *            Detailed Text. Object which implements type LogEvent.
     * @throws HapptickException
     */
    public void sendLog(String information) throws HapptickException {
        checkClientInitialized();
        applicationClient.sendLog(information);
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
        checkClientInitialized();
        applicationClient.startWork();
    }

    /**
     * If the using class has started the observing for awaiting the start
     * allowance this can be stopped here.
     */
    public void stopObservingForStartAllowance() {
        applicationClient.stopObservingForStartAllowance();
    }

    // @Override
    // protected void initHapptickBaseClient(String serverAddress, int
    // serverPort, String[] args, NotEOFClient notEofClient) throws
    // HapptickException {
    // super.init(serverAddress, serverPort, args, notEofClient);
    // }
}
