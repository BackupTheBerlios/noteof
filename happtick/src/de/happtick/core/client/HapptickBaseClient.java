package de.happtick.core.client;

import java.util.List;

import de.happtick.core.exception.HapptickException;
import de.happtick.mail.client.HapptickMailEventClient;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.Util;
import de.notEOF.mail.MailExpressions;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.client.MailAndEventReceiveClient;
import de.notEOF.mail.interfaces.MailAndEventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;

public abstract class HapptickBaseClient {
    protected String serverAddress;
    protected int serverPort;
    protected String[] args;
    protected NotEOFClient notEofClient;
    private MailAndEventReceiveClient mailEventClient;
    private MailAndEventRecipient mailEventRecipient;

    /*
     * Internal class for sending mails and events. Used if the
     * MailEventRecipient has no own client.
     */
    private class internalClient extends BaseClient implements NotEOFClient {
        @Override
        public Class<?> serviceForClientByClass() {
            return de.notEOF.core.service.SimpleService.class;
        }

        @Override
        public String serviceForClientByName() {
            return null;
        }
    }

    public void useInternalClientForSendMailsAndEvents() {
        this.notEofClient = new internalClient();
    }

    public int getNotEOFServerPort() {
        return serverPort;
    }

    public String getNotEOFServerAddress() {
        return serverAddress;
    }

    /**
     * This method should be called by the extended classes as soon as
     * possible...
     * <p>
     * It forces the extended classes to do something. Within the implementation
     * of this abstract function they should call the method init();
     * 
     * @param serverAddress
     * @param serverPort
     * @param args
     * @param notEofClient
     */
    // protected abstract void initHapptickBaseClient(String serverAddress, int
    // serverPort, String[] args, NotEOFClient notEofClient) throws
    // HapptickException;
    // protected void init(String serverAddress, int serverPort, String[] args,
    // NotEOFClient notEofClient) throws HapptickException {
    // this.serverAddress = serverAddress;
    // this.serverPort = serverPort;
    // this.args = args;
    //
    // if (null != notEofClient) {
    // this.notEofClient = notEofClient;
    // } else {
    // useInternalClientForSendMailsAndEvents();
    // }
    // connect();
    // }
    /**
     * Connect with the happtick server. Exactly this means to connect with an
     * application service on the happtick server. <br>
     * The service later decides if the application may run -> startAllowed(). <br>
     * If you use this method, the connection informations (ip, port of happtick
     * server) must be set before.
     * 
     * @throws HapptickException
     */
    public void connect(String serverAddress, int serverPort, String[] args, NotEOFClient notEofClient) throws HapptickException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;

        if (null != notEofClient) {
            this.notEofClient = notEofClient;
        } else {
            useInternalClientForSendMailsAndEvents();
        }
        connect(serverAddress, serverPort);
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
        if (Util.isEmpty(notEofClient))
            throw new HapptickException(50L, "NotEOFClient is leer.");
        if (Util.isEmpty(serverAddress))
            throw new HapptickException(50L, "Server Addresse ist leer.");
        if (0 == serverPort)
            throw new HapptickException(50L, "Server Port = 0");

        try {
            // connect with service
            if (!notEofClient.isLinkedToService())
                notEofClient.connect(serverAddress, serverPort, null);
        } catch (ActionFailedException e) {
            throw new HapptickException(100L, e);
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
     * Send any event to the service.
     * 
     * @param event
     *            The implementation of NotEOFEvent should not use additional
     *            data because only standard values are supported here. If there
     *            are more members in the event class they will not be
     *            transported to the service.
     * @see NotEOFEvent
     * @throws HapptickException
     */
    public void sendEvent(NotEOFEvent event) throws HapptickException {
        checkClientInitialized();
        try {
            notEofClient.sendEvent(event);
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, "Event: " + event.getClass().getSimpleName(), e);
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
     * <p>
     * By default own mails are ignored. If you want to receive own mails too,
     * use the same named function with the additional boolean argument
     * 'acceptOwnMails'.
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
        useMailsAndEvents(mailEventRecipient, false);
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
     * <p>
     * 
     * @param mailEventRecipient
     *            The application which uses this method and wants to be
     *            informed about mails or events must implement this interface.
     *            To use the function call it by putting in the class itself as
     *            parameter (e.g. this).
     * @param acceptOwnMails
     *            If the value of this argument is set to TRUE the client
     *            receives it's own sent mails. Normally you should set this
     *            parameter to FALSE or - easier - use the same named function
     *            without this argument.
     * @throws HapptickException
     *             Is raised when the connection with service could not be
     *             established or other problems occured.
     */
    public void useMailsAndEvents(MailAndEventRecipient mailEventRecipient, boolean acceptOwnMails) throws HapptickException {
        if (Util.isEmpty(this.notEofClient))
            throw new HapptickException(605, "notEofClient ist leer.");

        initMailEventClient(mailEventRecipient);
        if (!acceptOwnMails) {
            try {
                mailEventClient.addIgnoredClientNetId(this.notEofClient.getClientNetId());
            } catch (ActionFailedException e) {
                throw new HapptickException(605L, "Der Empfang eigener Mails konnte nicht unterdrueckt werden", e);
            }
        }
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
        checkClientInitialized();
        try {
            this.notEofClient.sendMail(mail);
        } catch (ActionFailedException e) {
            throw new HapptickException(600L, e);
        }
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

    /*
     * Check if the applicationClient exists...
     */
    protected void checkClientInitialized() throws HapptickException {
        if (Util.isEmpty(notEofClient))
            throw new HapptickException(50L, "Client ist nicht initialisiert. Vermutlich wurde kein connect durchgeführt.");
    }

    /**
     * Set the connection data for communication with happtick server / happtick
     * application service
     */
    public void setServerConnectionData(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

}
