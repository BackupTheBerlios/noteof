package de.happtick.application.client;

import de.happtick.core.client.ApplicationClient;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.ClientObserver;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

/**
 * This class is the connector between the application and an application service which runs on server side. <p>
 * Main mission is to control the start allowance of the application and to inform the server about events on client side.
 * @author dirk
 *
 */
public class Client {
    
    private String serverAddress;
    private int serverPort;
    private Long applicationId;
    private boolean isWorkAllowed = false;
    private ApplicationClient applicationClient;
    private AllowanceWaiter allowanceWaiter;
    private ClientObserver clientObserver;
    
    /**
     * If this constructor is used at a later time point the serverAddress and the port to the happtick scheduler must be set.
     * Maybe it is a way to write ip and port into a configuration file and get them by using the class LocalConfigurationClient. 
     */
    public Client() {
        
    }
    
    /**
     * Constructor with connection informations.
     * @param applicationId Unique identifier for the configured applications within the happtick configuration. This id is used by the scheduler to distinguish between the applications.
     * @param serverAddress The ip to the happtick server where the scheduler is running.
     * @param serverPort The port of the happtick server where the scheduler is running.
     */
    public Client(long applicationId, String serverAddress, int serverPort) {
        this.applicationId = applicationId;
        this .serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
    
    /**
     * Connect with the happtick server. Exactly this means to connect with an application service on the happtick server. <br>
     * The service later decides if the application may run -> startAllowed(). <br>
     * If you use this method, the connection informations (ip, port of happtick server) must be set before.
     * @throws HapptickException 
     */
    public void connect() throws HapptickException {
        connect(this.serverAddress, this.serverPort);
    }
    
    /**
     * Connect with the happtick server. Exactly this means to connect with an application service on the happtick server. <br>
     * The service later decides if the application may run -> startAllowed().
     * @param serverAddress The ip to the happtick server where the scheduler is running.
     * @param serverPort The port of the happtick server where the scheduler is running.
     * @throws HapptickException 
     */
    public void connect(String serverAddress, int serverPort) throws HapptickException {
        if (Util.isEmpty(serverAddress)) throw new HapptickException(50L, "Server Addresse: " + serverAddress);
        if (0 == serverPort)throw new HapptickException(50L, "Server Port = " + serverPort);
        if (null == applicationId) throw new HapptickException(50L, "Application Id ist NULL");
        
        if (null == applicationClient) {
            applicationClient = new ApplicationClient();
        }

        try {
            applicationClient.connect(serverAddress, serverPort, null);
            applicationClient.setApplicationId(applicationId);
        } catch (ActionFailedException e) {
            throw new HapptickException(100L, e);
        }
    }

    /**
     * Sets the unique application id.
     * @param applicationId Unique identifier for the configured applications within the happtick configuration. This id is used by the scheduler to distinguish between the applications.
     */
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }
    
    /**
     * 
     * @return The hopefully unique application id like which is used in the happtick configuration.
     */
    public Long getApplicationId() {
        return this.applicationId;
    }
    
    /**
     * Set the connection data for communication with happtick server / happtick application service
     */
    public void setServerConnectionData(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Ask the application service if the application may do it's job. <p>
     * Perhaps there is actually another process of the application running and it is not allowed to have more than one active working processes of this application kind.
     * Then this instance can wait till the service allows to start. Or it stops itself and will be started at a later moment by the scheduler. <br>
     * The situation that the application isn't allowed to work maybe can arrive <br>
     * - if it is started manually <br>
     * - if the scheduler makes faults
     * - application or network errors derange the communication between the application clients and the application services so that the scheduler starte the application twice or multiple.  
     * @return True if the application can start the work.
     * @throws HapptickException 
     */
    public boolean isWorkAllowed() throws HapptickException {
        checkApplicationClientInitialized();
        // only when start allowance not given yet service must be asked for
        if (! isWorkAllowed) {
            isWorkAllowed= applicationClient.isWorkAllowed();
        }
        return isWorkAllowed;
    }
    
    /**
     * Alternately to wait for start allowance by calling the method isWorkAllowed() repeatedly within a loop it is possible to let the application informed by this method.
     * Condition is that the application implements the interface Observer and waits for start allowance in the method update(). When the allowance is given the application client calls the method observers startAllowanceEvent()<br>
     * @throws HapptickException 
     */
    public void observeForWorkAllowance(ClientObserver clientObserver) throws HapptickException {
        checkApplicationClientInitialized();
        this.clientObserver = clientObserver;
        allowanceWaiter = new AllowanceWaiter();
        Thread waiterThread = new Thread(allowanceWaiter);
        waiterThread.start();
    }
    
    /**
     * Errors can be shown within the happtick monitoring tool or written to logfiles. <p>
     * Errors don't release events. 
     * @param id The error identifier.
     * @param level Error level.
     * @param errorDescription Additional information for solving the problem.
     * @throws HapptickException 
     */
    public void setError(long id, int level, String errorDescription) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.setError(id, level, errorDescription);
    }
    
    /**
     * Happtick is able to react to events. There are standard events like start and stop of application. The relations between them are configurable. Supplemental events and actions can be configured for single applications.
     * @param eventId An event id which is the link to the configuration.
     * @param additionalInformation Informations which can be used at another place (e.g. the happtick monitoring).
     * @throws HapptickException 
     */
    public void setEvent(int eventId, String additionalInformation) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.setEvent(eventId, additionalInformation);
    }
   
    /**
     * Releases an alert. <br>
     * Like errors alarms can have a level. The controlling alarm system of happtick decides what to do depending to the alarm level.  
     * @param alarmType The type of alarm. Later this can decide how the alarm will be processed (e.g. a target system, a hardware, what ever).  
     * @param level The importance of the alarm.
     * @param alarmDescription An additional information which can be helpful to solve the problem.
     * @throws HapptickException 
     */
    public void setAlarm(long alarmType, int level, String alarmDescription) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.setAlarm(alarmType, level, alarmDescription);
    }
   
    /**
     * Log informations can be visualized within the happtick monitoring tool or written to log files on the server.
     * @param logInformation Variable informations, depending to the applications job.
     * @throws HapptickException 
     */
    public void setLog(String logInformation) throws HapptickException {
        checkApplicationClientInitialized();
        applicationClient.setLog(logInformation);
    }
    
    /**
     * Informs the happtick server that the application has stopped. <p>
     * Very important to call this at end of work! <br>
     * The connections between happtick clients and happtick services are controlled by a so called 'LifeSignSystem'. The connection will not be closed as long as the underlying communication layer hasn't stopped. And so the java vm stays active.  
     * @throws HapptickException 
     */
    public void stop() throws HapptickException {
        if (null != allowanceWaiter) allowanceWaiter.stop();

        if (null != applicationClient) {
        try {
            applicationClient.close();
        } catch (ActionFailedException e) {
            throw new HapptickException(200L, e);
        }
        }
    }
    
    /**
     * If the using class has started the observing for awaiting the start allowance this can be stopped here.
     */
    public void stopObservingForStartAllowance() {
        if (null != allowanceWaiter) allowanceWaiter.stop();
    }

    /*
     * Check if the applicationClient exists...
     */
    private void checkApplicationClientInitialized() throws HapptickException {
        if (null == applicationClient) throw new HapptickException(50L, "Client ist nicht initialisiert. Vermutlich wurde kein connect durchgeführt.");
    }
    
    
    /*
     * Class runs in a thread and waits for allowance by service to start work
     */
    private class AllowanceWaiter implements Runnable {
        private boolean stopped = false;
        
        public boolean stopped() {
            return stopped;
        }
        public void stop() {
            stopped = true;
        }
        
        public void run() {
            try {
                while (!stopped || !isWorkAllowed()) {
                Thread.sleep(1000);
                }
                if (isWorkAllowed()) {
                    clientObserver.startAllowanceEvent(true);
                }
            } catch (Exception e) {
                stopped = true;
                clientObserver.startAllowanceEvent(false);
            }
        }
    }
}
