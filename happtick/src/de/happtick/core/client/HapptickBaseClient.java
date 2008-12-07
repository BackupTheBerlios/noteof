package de.happtick.core.client;

import java.net.Socket;
import java.util.List;

import de.happtick.core.exception.HapptickException;
import de.happtick.mail.client.HapptickEventClient;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;
import de.notEOF.dispatch.client.DispatchClient;
import de.notEOF.mail.MailExpressions;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.client.EventReceiveClient;
import de.notEOF.mail.interfaces.EventRecipient;
import de.notEOF.mail.interfaces.MailMatchExpressions;

/**
 * Simplifies the acts between application and service.
 * <p>
 * This class assembles the possibilities of communicating to a service and to
 * receive mails and events. <br>
 * This client is not an application client. If the connect() method of this
 * class is used without a special NotEOFClient a basically client is used. <br>
 * But also this client offers the functionalities of an application client plus
 * the mail functions if a client of type NotEOFClient is used for the connect()
 * method.
 * 
 * @author Dirk
 */
public abstract class HapptickBaseClient {
    protected String serverAddress;
    protected int serverPort;
    protected String[] args;
    protected NotEOFClient notEofClient;
    private EventReceiveClient eventClient;
    private EventRecipient eventRecipient;

    List<NotEOFEvent> acceptedEvents;
    private MailExpressions acceptedExpressions;
    // private boolean usingEvents = false;
    private boolean acceptingOwnMails = false;

    // private boolean useInternalClient = false;

    /*
     * Internal class for sending mails and events. Used if the
     * MailEventRecipient has no own client.
     */
    private class InternalClient extends BaseClient implements NotEOFClient {
        @Override
        public Class<?> serviceForClientByClass() {
            return de.happtick.core.service.HapptickSimpleService.class;
        }

        @Override
        public String serviceForClientByName() {
            return null;
        }

        @Override
        public String getServerAddress() {
            return super.getPartnerHostAddress();
        }

        @Override
        public int getServerPort() {
            return super.getPartnerPort();
        }
    }

    public void close() throws HapptickException {
        try {
            eventClient.stop();
            notEofClient.close();
        } catch (ActionFailedException e) {
            throw new HapptickException(700L, "Event Client.", e);
        }
    }

    /**
     * Delivers the internal client which is connected with the server.
     * <p>
     * Useful if comunication is required in classes which are not extended from
     * any NotEOFClient-Class. <br>
     * The internal client establishes a communication connection to the NotEOF
     * core service SimpleService.
     * <p>
     * Attention! The internal client is not available before the connect() of
     * this class was executed.
     * 
     * @return A client which can be used for communication acts to the server.
     */
    public NotEOFClient getSimpleClient() {
        return this.notEofClient;
    }

    private void useInternalClientForSendMailsAndEvents() {
        // useInternalClient = true;
        this.notEofClient = new InternalClient();
    }

    /**
     * Delivers the port of the NotEOF server.
     * <p>
     * Was set by the connect method.
     * 
     * @return Port of central server.
     */
    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * Delivers the ip address of the NotEOF server.
     * <p>
     * Was set by the connect method.
     * 
     * @return IP address of central server.
     */
    public String getServerAddress() {
        return this.serverAddress;
    }

    // public void reconnect() throws HapptickException {
    // System.out.println("reconnect");
    // if (this.useInternalClient) {
    // this.notEofClient = null;
    // }
    // connect(this.serverAddress, this.serverPort, this.args, false);
    // System.out.println("Reconnect. Bin jetzt neu verbunden...");
    //
    // eventClient = null;
    // System.out.println("Reconnect. Using Events? " + usingEvents);
    // if (usingEvents) {
    // System.out.println("Reconnect. Use Events");
    // useEvents();
    // System.out.println("Reconnect. StartAcceptingEvents");
    // startAcceptingEvents();
    // }
    // System.out.println("reconnect Alle Aktionen abgeschlossen");
    // }

    /**
     * Connect with the happtick server.
     * <p>
     * Exactly this means to connect with an application service on the happtick
     * server. <br>
     * This method allows to explicitly define a client which is used for
     * interactions to the server. Using special clients depends to the
     * application requirements. <br>
     * If not special client is required use the other connect() method without
     * this parameter.
     * <p>
     * Later the service will decide if the application may run ->
     * startAllowed().
     * 
     * @param serverAddress
     * @param serverPort
     * @param args
     * @param notEofClient
     *            Client with special functionality. NULL is not allowed here.
     * @throws HapptickException
     *             Thrown if client is empty (NULL) or connection couldn't be
     *             established.
     */
    public void connect(String serverAddress, int serverPort, String[] args, NotEOFClient notEofClient, boolean dispatched) throws HapptickException {
        if (Util.isEmpty(notEofClient))
            throw new HapptickException(50L, "NotEOFClient is leer.");

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;

        this.notEofClient = notEofClient;

        if (dispatched) {
            try {
                Socket socketToService = dispatchSocket(serverAddress, serverPort, (String[]) null);
                connect(socketToService, args, false);
            } catch (ActionFailedException e) {
                LocalLog.error("HapptickBaseClient.connect: Achtung! dispatched ist noch nicht getestet!!!", e);
            }
        } else {
            connect(serverAddress, serverPort, args, false);
        }
    }

    private Socket dispatchSocket(String serverAddress, int serverPort, String[] args) {
        Socket socketToService = null;
        try {
            BaseTimeOut baseTimeOut = new BaseTimeOut(0, 60000);
            DispatchClient dispatchClient;
            dispatchClient = new DispatchClient(serverAddress, serverPort, baseTimeOut, (String[]) null);
            String serviceClassName = notEofClient.serviceForClientByName();
            socketToService = dispatchClient.getServiceConnection(serviceClassName, 0);
        } catch (ActionFailedException e) {
            LocalLog.error("HapptickBaseClient.connect: Achtung! dispatched ist noch nicht getestet!!!", e);
        }
        return socketToService;
    }

    public void connect(Socket socket, String[] args, boolean dispatched) throws HapptickException {
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
     * @throws HapptickException
     */
    public void connect(String serverAddress, int serverPort, String[] args, boolean dispatched) throws HapptickException {
        if (Util.isEmpty(serverAddress))
            throw new HapptickException(50L, "Server Addresse ist leer.");
        if (0 == serverPort)
            throw new HapptickException(50L, "Server Port = 0");

        if (dispatched) {
            Socket socketToService = dispatchSocket(serverAddress, serverPort, (String[]) null);
            serverAddress = socketToService.getInetAddress().getHostAddress();
            serverPort = socketToService.getLocalPort();
        }

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.args = args;

        System.out.println("HapptickBaseClient.connect. ist notEofClient null? " + (null == this.notEofClient));
        if (null == this.notEofClient)
            useInternalClientForSendMailsAndEvents();

        // connect with service
        while (!notEofClient.isLinkedToService()) {
            try {
                notEofClient.connect(serverAddress, serverPort, null);
            } catch (ActionFailedException e) {
                LocalLog.warn("Verbindung mit Service konnte bisher nicht hergestellt werden: " + notEofClient.getClass().getCanonicalName());
                // throw new HapptickException(100L, e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * This steps are recommended to receive Mails and Events: <br>
     * 1. Call useMailsAndEvents() for initializing the mail system. <br>
     * 2. Call addInterestingMailExpressions() and / or addInterestingEvents()
     * to tell the server which mails and events the client is interested in. <br>
     * 3. Call startAcceptingMailsEvents() for receiving mails and events.
     * <p>
     * For sending mails or events this steps are NOT required.
     */
    public void startAcceptingEvents() throws HapptickException {
        try {
            eventClient.awaitMailOrEvent(this.eventRecipient);
            System.out.println("eventClient wartet jetzt auf mails...");
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
    public void useEvents(EventRecipient mailEventRecipient, MailExpressions expressions, List<NotEOFEvent> events, boolean acceptOwnMails)
            throws HapptickException {
        // usingEvents = true;
        acceptingOwnMails = acceptOwnMails;
        acceptedExpressions = expressions;
        acceptedEvents = events;
        // useEvents();
        this.eventRecipient = mailEventRecipient;
        initMailEventClient(mailEventRecipient);
        addInterestingMailExpressions(acceptedExpressions);
        addInterestingEvents(acceptedEvents);
        if (!acceptingOwnMails) {
            try {
                eventClient.addIgnoredClientNetId(this.notEofClient.getClientNetId());
            } catch (ActionFailedException e) {
                throw new HapptickException(605L, "Der Empfang eigener Mails konnte nicht unterdrueckt werden", e);
            }
        }
    }

    /*
     * For Reconnect only!
     */
    // private void useEvents() throws HapptickException {
    // this.usingEvents = true;
    // initMailEventClient(eventRecipient);
    // addInterestingMailExpressions(acceptedExpressions);
    // addInterestingEvents(acceptedEvents);
    // if (!acceptingOwnMails) {
    // try {
    // eventClient.addIgnoredClientNetId(this.notEofClient.getClientNetId());
    // } catch (ActionFailedException e) {
    // throw new HapptickException(605L,
    // "Der Empfang eigener Mails konnte nicht unterdrueckt werden", e);
    // }
    // }
    // }
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
    public void useEvents(EventRecipient mailEventRecipient) throws HapptickException {
        this.eventRecipient = mailEventRecipient;
        useEvents(eventRecipient, false);
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
    public void useEvents(EventRecipient mailEventRecipient, boolean acceptOwnMails) throws HapptickException {
        if (Util.isEmpty(this.notEofClient))
            throw new HapptickException(605, "Vor Aufruf dieser Methode muss die Method connect() aufgerufen werden.");

        // this.usingEvents = true;
        this.eventRecipient = mailEventRecipient;
        acceptingOwnMails = acceptOwnMails;
        eventClient = null;
        initMailEventClient(eventRecipient);
        if (!acceptingOwnMails) {
            try {
                eventClient.addIgnoredClientNetId(this.notEofClient.getClientNetId());
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
     * @throws ActionFailedException
     *             If the list couldn't be transmitted to the service.
     */
    public void addInterestingMailExpressions(MailMatchExpressions expressions) throws HapptickException {
        if (null == eventClient)
            throw new HapptickException(604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");
        if (null != expressions) {
            try {
                eventClient.addInterestingMailExpressions(expressions);
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
        if (null == eventClient)
            throw new HapptickException(604L, "Empfang von Mails oder Events ist noch nicht aktiviert.");

        System.out.println("Events null? " + events);
        if (null != events) {
            try {
                eventClient.addInterestingEvents(events);
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

    private void initMailEventClient(EventRecipient mailEventRecipient) throws HapptickException {
        if (null == this.eventRecipient) {
            this.eventRecipient = mailEventRecipient;
        }

        while (null == this.eventClient || !this.eventClient.isLinkedToService()) {
            try {
                this.eventClient = new HapptickEventClient(serverAddress, serverPort, null, null);
            } catch (ActionFailedException e) {
                LocalLog.warn("Verbindung zum Empfang von Events konnte bisher nicht aufgebaut werden.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /*
     * Check if the applicationClient exists...
     */
    protected void checkClientInitialized() throws HapptickException {
        if (Util.isEmpty(notEofClient))
            throw new HapptickException(50L, "Client ist nicht initialisiert. Vermutlich wurde kein connect durchgeführt.");

        // connect with service
        while (!notEofClient.isLinkedToService()) {
            try {
                notEofClient.connect(serverAddress, serverPort, null);
            } catch (ActionFailedException e) {
                LocalLog.warn("Verbindung mit Service konnte bisher nicht hergestellt werden: " + notEofClient.getClass().getCanonicalName());
                // throw new HapptickException(100L, e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }
}
