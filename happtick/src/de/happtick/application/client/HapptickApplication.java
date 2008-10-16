package de.happtick.application.client;

import java.util.List;

import de.happtick.core.application.client.ApplicationClient;
import de.happtick.core.events.ActionEvent;
import de.happtick.core.events.AlarmEvent;
import de.happtick.core.events.ErrorEvent;
import de.happtick.core.events.LogEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.ClientObserver;
import de.happtick.mail.client.HapptickMailEventClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.Util;
import de.notEOF.mail.MailExpressions;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.client.MailAndEventClient;
import de.notEOF.mail.interfaces.MailAndEventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;

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
    private MailAndEventClient mailEventClient;
    private MailAndEventRecipient mailEventRecipient;

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
    public HapptickApplication(long applicationId, String serverAddress, int serverPort, String... args) throws HapptickException {
        this.applicationId = applicationId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;
        connect();
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
     * @see ActionEvent
     * @param event
     *            Implementation of EventEvent
     * @throws HapptickException
     */
    public void sendEvent(ActionEvent event) throws HapptickException {
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

    private void initMailEventClient(MailAndEventRecipient mailEventRecipient) throws HapptickException {
        if (null == this.mailEventClient) {
            this.mailEventRecipient = mailEventRecipient;
            try {
                this.mailEventClient = new HapptickMailEventClient(serverAddress, serverPort, null, null);
            } catch (ActionFailedException e) {
                throw new HapptickException(601L, e);
            }
        }
    }

    /**
     * 
     * This steps are recommended to receive Mails and Events: <br>
     * 1. Call useMailsAndEvents() for initializing the mail system. <br>
     * 2. Call addInterestingMailExpressions() and / or addInterestingEvents()
     * to tell the server which mails and events the client is interested in. <br>
     * 3. Call startAcceptingMailsEvents() for receiving mails and events.
     * <p>
     * For sending mails or events this steps are NOT required.
     */
    public void startAcceptingMailsEvents() throws HapptickException {
        try {
            mailEventClient.awaitMailOrEvent(this.mailEventRecipient);
        } catch (ActionFailedException e) {
            throw new HapptickException(601L, e);
        }
    }

    /**
     * Enables the application to receive mails and events from the server or
     * other services.
     * <p>
     * If a mail reaches the central server the services are informed about
     * this. <br>
     * To get a mail it is important to set destinations or headers which the
     * client waits for.
     * <p>
     * 
     * @param mailEventRecipient
     *            The application which uses this method and wants to be
     *            informed about mails or events must implement this interface.
     *            To use the function call it by putting in the class itself as
     *            parameter (e.g. this).
     * @param expressions0
     *            To get a mail it is important to set destinations and/or
     *            headers which the client waits for. <br>
     *            Object which implements the interface MailExpressions. There
     *            are two implementations of MailExpressions: MailDestinations
     *            and MailHeaders. One of them is allowed here. NULL is allowed
     *            also. If this value isn't NULL the other param MailExpression
     *            expression1 must be of the other type. E.g. if this is of type
     *            MailDestinations the param expression1 must be NULL or of type
     *            MailHeaders. And vice versa.
     * @param expressions1
     *            To get a mail it is important to set destinations and/or
     *            headers which the client waits for. <br>
     *            Object which implements the interface MailExpressions. There
     *            are two implementations of MailExpressions: MailDestinations
     *            and MailHeaders. One of them is allowed here. NULL is allowed
     *            also. If this value isn't NULL the other param MailExpression
     *            expression0 must be of the other type. E.g. if this is of type
     *            MailDestinations the param expression0 must be NULL or of type
     *            MailHeaders. And vice versa.
     * @param events
     *            List with Events which the client is interested in.
     * @throws HapptickException
     *             Is raised when the connection with service could not be
     *             established or other problems occured.
     */
    public void useMailsAndEvents(MailAndEventRecipient mailEventRecipient, MailExpressions expressions0, MailExpressions expressions1, List<NotEOFEvent> events)
            throws HapptickException {
        initMailEventClient(mailEventRecipient);
        addInterestingMailExpressions(expressions0);
        addInterestingMailExpressions(expressions1);
        addInterestingEvents(events);
    }

    /**
     * Enables the application to receive mails and events from the server or
     * other services.
     * <p>
     * If a mail reaches the central server the services are informed about
     * this. <br>
     * To get a mail it is important to set destinations or headers which the
     * client waits for.
     * <p>
     * This steps are recommended to receive Mails and Events: <br>
     * 1. Call useMailsAndEvents() for initializing the mail system. <br>
     * 2. Call addInterestingMailExpressions() and / or addInterestingEvents()
     * to tell the server which mails and events the client is interested in. <br>
     * 3. Call startAcceptingMailsEvents() for receiving mails and events.
     * <p>
     * For sending mails or events this steps are NOT required.
     * 
     * @param mailEventRecipient
     *            The application which uses this method and wants to be
     *            informed about mails or events must implement this interface.
     *            To use the function call it by putting in the class itself as
     *            parameter (e.g. this).
     * @throws HapptickException
     *             Is raised when the connection with service could not be
     *             established or other problems occured.
     */
    public void useMailsAndEvents(MailAndEventRecipient mailEventRecipient) throws HapptickException {
        initMailEventClient(mailEventRecipient);
    }

    /**
     * Add expressions which this client is interested in.
     * <p>
     * This steps are recommended to receive Mails and Events: <br>
     * 1. Call useMailsAndEvents() for initializing the mail system. <br>
     * 2. Call addInterestingMailExpressions() and / or addInterestingEvents()
     * to tell the server which mails and events the client is interested in. <br>
     * 3. Call startAcceptingMailsEvents() for receiving mails and events.
     * <p>
     * For sending mails or events this steps are NOT required.
     * 
     * @param expressions
     *            To get a mail it is important to set destinations and/or
     *            headers which the client waits for. Object which implements
     *            the interface MailExpressions. There are two implementations
     *            of MailExpressions: MailDestinations and MailHeaders. One of
     *            them is allowed here. NULL is allowed also.
     * 
     * @throws ActionFailedException
     *             If the list couldn't be transmitted to the service.
     */
    public void addInterestingMailExpressions(MailMatchExpressions expressions) throws HapptickException {
        if (null == mailEventClient)
            throw new HapptickException(604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");
        if (null != expressions) {
            try {
                mailEventClient.addInterestingMailExpressions(expressions);
            } catch (ActionFailedException e) {
                throw new HapptickException(602L, expressions.getClass().getName() + ".", e);
            }
        }
    }

    /**
     * Add events which the application that uses this class is interested in.
     * <p>
     * This steps are recommended to receive Mails and Events: <br>
     * 1. Call useMailsAndEvents() for initializing the mail system. <br>
     * 2. Call addInterestingMailExpressions() and / or addInterestingEvents()
     * to tell the server which mails and events the client is interested in. <br>
     * 3. Call startAcceptingMailsEvents() for receiving mails and events.
     * <p>
     * For sending mails or events this steps are NOT required.
     * 
     * @param events
     *            A list with objects which implement the interface
     *            {@link NotEOFEvent}.
     * @throws HapptickException
     */
    public void addInterestingEvents(List<NotEOFEvent> events) throws HapptickException {
        if (null == mailEventClient)
            throw new HapptickException(604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");
        if (null != events) {
            try {
                mailEventClient.addInterestingEvents(events);
            } catch (ActionFailedException e) {
                throw new HapptickException(603L, e);
            }
        }
    }

    /**
     * Sends a {@link NotEOFMail} to the server.
     * <p>
     * The idea is to send mails with a special header or destination (which can
     * be e.g. a applicationId). So one or more clients which are interested in
     * such a mail receive the mail. <br>
     * Furthermore at the mail the attribute toClientNetId can be set if known.
     * Then the mail reaches only one client.
     * 
     * @param mail
     *            The mail.
     * @throws ActionFailedException
     */
    public void sendMail(NotEOFMail mail) throws HapptickException {
        checkApplicationClientInitialized();
        try {
            applicationClient.sendMail(mail);
        } catch (ActionFailedException e) {
            throw new HapptickException(600L, e);
        }
    }
}
