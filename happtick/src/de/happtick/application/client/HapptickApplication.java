package de.happtick.application.client;

import java.net.Socket;
import java.util.List;

import de.happtick.core.application.client.ApplicationClient;
import de.happtick.core.interfaces.ClientObserver;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.dispatch.client.DispatchClient;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.EventRecipient;

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
public abstract class HapptickApplication implements EventRecipient {

    private Long applicationId;
    private boolean isWorkAllowed = false;
    private ApplicationClient applicationClient;
    String serverAddress;
    int serverPort;
    String[] args;
    private EventRecipient eventRecipient;
    private List<NotEOFEvent> interestingEvents;
    private boolean acceptingEvents = false;

    public HapptickApplication(Long applicationId, String serverAddress, int serverPort, String... args) throws ActionFailedException {
        this(applicationId, serverAddress, serverPort, null, args);
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
     * @param args
     *            A HapptickApplication must be called with the parameter
     *            --startId=<value>.
     */
    public HapptickApplication(Long applicationId, String serverAddress, int serverPort, EventRecipient eventRecipient, String... args)
            throws ActionFailedException {

        // Verify the hard coded applicationId and the applicationId which is
        // used by the StartClient which got it from the central scheduler.
        ArgsParser parser = new ArgsParser(args);
        Long receivedId;
        if (parser.containsStartsWith("--applicationId")) {
            receivedId = Util.parseLong(parser.getValue("applicationId"), -1);
            if (Util.isEmpty(applicationId) && Util.isEmpty(receivedId)) {
                throw new ActionFailedException(10404L, "Empfangene applicationId und hart codierte applicationId sind leer.");
            }
            if (Util.isEmpty(applicationId) && !Util.isEmpty(receivedId)) {
                applicationId = receivedId;
            } else if (!Util.isEmpty(applicationId) && applicationId != receivedId)
                throw new ActionFailedException(10404L, "Empfangene applicationId und hart codierte applicationId sind nicht identisch. Hart codiert: "
                        + applicationId + "; Empfangen: " + receivedId);
        }

        this.applicationId = applicationId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;
        this.eventRecipient = eventRecipient;

        connect();
    }

    private void connect() throws ActionFailedException {
        applicationClient = new ApplicationClient();

        // TODO Wenn dipatched getestet, kann der letzte Parameter auch nach
        // oben frei gegeben werden...
        connect(serverAddress, serverPort, args, false);
        applicationClient.startIdToService(args);
        applicationClient.applicationIdToService(applicationId);
        if (!Util.isEmpty(eventRecipient)) {
            applicationClient.setEventRecipient(eventRecipient);
        }

    }

    public void reconnect() throws ActionFailedException {
        connect();
        if (acceptingEvents) {
            addInterestingEvents(interestingEvents);
            startAcceptingEvents();
        }
    }

    public void setEventRecipient(EventRecipient eventRecipient) {
        this.eventRecipient = eventRecipient;
        applicationClient.setEventRecipient(eventRecipient);
    }

    public void addInterestingEvents(List<NotEOFEvent> events) throws ActionFailedException {
        applicationClient.addInterestingEvents(events);
        interestingEvents = events;
    }

    public void startAcceptingEvents() {
        try {
            applicationClient.startAcceptingEvents();
            acceptingEvents = true;
        } catch (ActionFailedException e) {
            e.printStackTrace();
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
     * @throws ActionFailedException
     */
    public boolean isWorkAllowed() throws ActionFailedException {
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
     * @throws ActionFailedException
     */
    public void observeForWorkAllowance(ClientObserver clientObserver) throws ActionFailedException {
        checkClientInitialized();
        applicationClient.observeForWorkAllowance(clientObserver);
    }

    public void sendEvent(NotEOFEvent event) throws ActionFailedException {
        checkClientInitialized();
        boolean success = false;
        while (!success) {
            success = true;
            try {
                applicationClient.sendEvent(event);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                reconnect();
            }
        }
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
     * @throws ActionFailedException
     */
    public void sendError(String errorId, String description, String level) throws ActionFailedException {
        checkClientInitialized();
        boolean success = false;
        while (!success) {
            try {
                applicationClient.sendError(errorId, description, level);
                success = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                reconnect();
            }
        }
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
     * @throws ActionFailedException
     */
    public void sendActionEvent(String eventId, String information) throws ActionFailedException {
        checkClientInitialized();
        boolean success = false;
        while (!success) {
            try {
                applicationClient.sendActionEvent(eventId, information);
                success = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                reconnect();
            }
        }
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
     * @throws ActionFailedException
     */
    public void sendAlarm(String type, String description, String level) throws ActionFailedException {
        checkClientInitialized();
        boolean success = false;
        while (!success) {
            try {
                applicationClient.sendAlarm(type, description, level);
                success = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                reconnect();
            }
        }
    }

    /**
     * Log informations can be visualized within the happtick monitoring tool or
     * written to log files on the server.
     * 
     * @param information
     *            Detailed Text. Object which implements type LogEvent.
     * @throws ActionFailedException
     */
    public void sendLog(String information) throws ActionFailedException {
        checkClientInitialized();
        boolean success = false;
        while (!success) {
            try {
                applicationClient.sendLog(information);
                success = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                reconnect();
            }
        }
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
     * @throws ActionFailedException
     */
    public void stop() throws ActionFailedException {
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
     * @throws ActionFailedException
     */
    public void stop(int exitCode) throws ActionFailedException {

        if (null != applicationClient) {
            applicationClient.stop(exitCode);
        }
    }

    public void startWork() throws ActionFailedException {
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

    private Socket dispatchSocket(String serverAddress, int serverPort, String[] args) {
        Socket socketToService = null;
        try {
            BaseTimeOut baseTimeOut = new BaseTimeOut(0, 60000);
            DispatchClient dispatchClient;
            dispatchClient = new DispatchClient(serverAddress, serverPort, baseTimeOut, (String[]) null);
            String serviceClassName = applicationClient.serviceForClientByName();
            socketToService = dispatchClient.getServiceConnection(serviceClassName, 0);
        } catch (ActionFailedException e) {
            LocalLog.error("HapptickBaseClient.connect: Achtung! dispatched ist noch nicht getestet!!!", e);
        }
        return socketToService;
    }

    public void connect(Socket socket, String[] args, boolean dispatched) throws ActionFailedException {
        String serverAddress = socket.getInetAddress().getHostAddress();
        int serverPort = socket.getLocalPort();
        connect(serverAddress, serverPort, args, dispatched);
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
     * @throws ActionFailedException
     */
    public void connect(String serverAddress, int serverPort, String[] args, boolean dispatched) throws ActionFailedException {
        if (Util.isEmpty(serverAddress))
            throw new ActionFailedException(10050L, "Server Addresse ist leer.");
        if (0 == serverPort)
            throw new ActionFailedException(10050L, "Server Port = 0");

        if (dispatched) {
            Socket socketToService = dispatchSocket(serverAddress, serverPort, (String[]) null);
            serverAddress = socketToService.getInetAddress().getHostAddress();
            serverPort = socketToService.getLocalPort();
        }

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;

        // connect with service
        checkClientInitialized();
    }

    /*
     * Check if the applicationClient exists...
     */
    protected void checkClientInitialized() throws ActionFailedException {
        if (Util.isEmpty(applicationClient))
            throw new ActionFailedException(10050L, "Client ist nicht initialisiert. Vermutlich wurde kein connect durchgeführt.");

        // connect with service
        while (!applicationClient.isLinkedToService()) {
            try {
                applicationClient.connect(serverAddress, serverPort, null);
            } catch (ActionFailedException e) {
                LocalLog.warn("Verbindung mit Service konnte bisher nicht hergestellt werden: " + applicationClient.getClass().getCanonicalName());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    public String getLocalAddress() {
        return applicationClient.getLocalAddress();
    }

    public String getServerAddress() {
        return applicationClient.getServerAddress();
    }

    public int getServerPort() {
        return applicationClient.getServerPort();
    }

    /**
     * Method to accept incoming events.
     * <p>
     * If you want to receive events, override this method.
     * 
     * @param event
     *            The incoming event.
     */
    @Override
    public void processEvent(NotEOFEvent event) {
        LocalLog.info("HapptickApplication.processEvent. Methode wurde nicht ueberschrieben. Event Type: " + event.getEventType());
    }

    /**
     * If the client has send an event and the interface for sending events
     * throws an exception, this method will be called.
     * 
     * @param exception
     *            The raised exception.
     */
    @Override
    public void processEventException(Exception exception) {
        LocalLog.info("HapptickApplication.processEventException. Methode wurde nicht ueberschrieben. Exception Message: " + exception.getMessage());
    }

    /**
     * Method to accept incoming mails.
     * <p>
     * If you want to receive mails, override this method.
     * 
     * @param mail
     *            The incoming mail.
     */
    @Override
    public void processMail(NotEOFMail mail) {
        LocalLog.info("HapptickApplication.processMail. Methode wurde nicht ueberschrieben. Mail Header: " + mail.getHeader());
    }

    /**
     * If the client has send a mail and the interface for sending mails throws
     * an exception, this method will be called.
     * 
     * @param exception
     *            The raised exception.
     */
    @Override
    public void processMailException(Exception exception) {
        LocalLog.info("HapptickApplication.processMailException. Methode wurde nicht ueberschrieben. Exception Message: " + exception.getMessage());
    }

    /**
     * This method is called when any service or client releases the stop event.
     * <p>
     * In opposition to other events, this method only will be called when the
     * stop event is ment to the special client with matching clientNetId to the
     * clientNetId which is stored within the event.
     * 
     * @param event
     *            The incoming stop event.
     */
    @Override
    public void processStopEvent(NotEOFEvent event) {
        LocalLog.info("HapptickApplication.processStopEvent. Methode wurde nicht ueberschrieben. Event Attribute workApplicationId: "
                + event.getAttribute("workApplicationId"));
    }
}
