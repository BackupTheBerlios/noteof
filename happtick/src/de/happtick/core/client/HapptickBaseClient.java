package de.happtick.core.client;

import java.net.Socket;
import java.util.List;

import de.happtick.core.exception.HapptickException;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;
import de.notEOF.dispatch.client.DispatchClient;
import de.notEOF.mail.NotEOFMail;

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
public abstract class HapptickBaseClient extends BaseClient {
    protected String serverAddress;
    protected int serverPort;
    protected String[] args;
    protected NotEOFClient notEofClient;

    List<NotEOFEvent> acceptedEvents;
    private boolean usingEvents = false;
    private boolean useInternalClient = false;

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
            System.out.println("HapptickBaseClient.close()  Vor eventClient.stop()");
            System.out.println("HapptickBaseClient.close()  Nach eventClient.stop()");
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
        useInternalClient = true;
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

    public void reconnect() throws HapptickException {
        System.out.println("reconnect");
        System.out.println("Reconnect. Using Events? " + usingEvents);
        System.out.println("Reconnect. Eventliste NULL? " + (null == acceptedEvents));

        if (this.useInternalClient) {
            this.notEofClient = null;
        }
        connect(this.serverAddress, this.serverPort, this.args, false);
        System.out.println("Reconnect. Bin jetzt neu verbunden...");

        System.out.println("reconnect Alle Aktionen abgeschlossen");
    }

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
                System.out.println("notEofClient.serverAddress: " + serverAddress);
                System.out.println("notEofClient.serverPort: " + serverPort);
                notEofClient.connect(serverAddress, serverPort, null);
            } catch (ActionFailedException e) {
                LocalLog.warn(" HapptickBaseClient. Verbindung mit Service konnte bisher nicht hergestellt werden: "
                        + notEofClient.getClass().getCanonicalName());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
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
