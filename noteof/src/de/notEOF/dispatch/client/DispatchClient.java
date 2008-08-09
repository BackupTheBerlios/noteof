package de.notEOF.dispatch.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.SimpleSocketData;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.TimeOut;
import de.notEOF.dispatch.enumeration.DispatchTag;
import de.notEOF.dispatch.service.DispatchService;

/**
 * Client for searching a service type in the net. If he has success ip and port
 * are delivered.
 * <p>
 * This client can absolutely used only one time for one connection to a
 * service. That means, that if the connected service could not find the
 * searched service type, the instance of this client cannot used for more
 * requests. The connected service will close the connection even this client
 * closes the connection after they have interchanged their data.
 * <p>
 * For searching a service typ use the method getServiceConnection().
 * 
 * @author Dirk
 * 
 */

public class DispatchClient extends BaseClient {
    /**
     * Change this to true if the client is used by a DispatchService <br>
     * Should only be done by work at the framework
     **/
    public boolean IS_CLIENT_FOR_SERVICE = false;

    public DispatchClient(Socket socketToServer, TimeOut timeout, boolean activateLifeSignSystem, String[] args) throws ActionFailedException {
        super(socketToServer, timeout, activateLifeSignSystem, args);
    }

    public DispatchClient(String ip, int port, TimeOut timeout, boolean activateLifeSignSystem, String... args) throws ActionFailedException {
        super(ip, port, timeout, activateLifeSignSystem, args);
        // TODO activateLifeSignSystem mit Zeit aus timeout
    }

    @Override
    public Class<?> serviceForClientByClass() {
        return DispatchService.class;
    }

    /**
     * The job of this client is to ask DispatchService(s) if a service type
     * exists. <br>
     * The DispatchService looks local and - if necessary - asks other server
     * for the service type. If the client has luck the service delivers ip and
     * port to any server which offers the service.
     * 
     * @param serviceTypeName
     *            The name of the service type which is searched for. The name
     *            of service type must exactly match the name of the class which
     *            implements the service (no blanks, case sensitive). It is
     *            allowed to use only the simple (short) class name or the
     *            canonical name (e.g. ApplicationService or
     *            de.notEOF.application.service.ApplicationService). <br>
     *            Whether the service will be found by dispatch service depends
     *            to the service-configuration you made (did you use simpleName
     *            or canonicalName?). Of cause you can configure both
     *            (simpleName and canonicalName) and then it never minds which
     *            type of name you fill in here.
     * @param timeOutForSearch
     *            Is the time in milliseconds the dispatch service has to search
     *            for a server which can deliver the requested service.<br>
     *            Attention! The service has not the competence to observe this
     *            time exactly. E.g. during the establishing of connections the
     *            waiting time depends to some things which are not controlled
     *            by the service.
     *            <p>
     *            Value 0 for timeOutForSearch means that there is no timeout
     *            used.
     * 
     * @return This method creates and returns the socket to the server or null
     *         if the service wasn't found in the net.
     */
    public Socket getServiceConnection(String serviceTypeName, int timeOutForSearch) throws ActionFailedException {

        // the result of this function
        Socket socketToService = null;

        SimpleSocketData socketData = getSocketData(serviceTypeName, timeOutForSearch);
        if (null != socketData) {
            try {
                socketToService = new Socket(socketData.getIp(), socketData.getPort());
            } catch (UnknownHostException e) {
                throw new ActionFailedException(1003L, "Gesuchter Typ: " + serviceTypeName + "; Erhaltene IP und Port: " + socketData.getIp() + ":"
                        + socketData.getPort());
            } catch (IOException e) {
                throw new ActionFailedException(1004L, "Gesuchter Typ: " + serviceTypeName + "; Erhaltene IP und Port: " + socketData.getIp() + ":"
                        + socketData.getPort());
            }
        }
        return socketToService;
    }

    /**
     * The job of this client is to ask DispatchService(s) if a service type
     * exists. <br>
     * The DispatchService looks local and - if necessary - asks other server
     * for the service type. If the client has luck the service delivers ip and
     * port to any server which offers the service.
     * 
     * @param serviceTypeName
     *            The name of the service type which is searched for. The name
     *            of service type must exactly match the name of the class which
     *            implements the service (no blanks, case sensitive). It is
     *            allowed to use only the simple (short) class name or the
     *            canonical name (e.g. ApplicationService or
     *            de.notEOF.application.service.ApplicationService). <br>
     *            Whether the service will be found by dispatch service depends
     *            to the service-configuration you made (did you use simpleName
     *            or canonicalName?). Of cause you can configure both
     *            (simpleName and canonicalName) and then it never minds which
     *            type of name you fill in here.
     * @param timeOutForSearch
     *            Is the time in milliseconds the dispatch service has to search
     *            for a server which can deliver the requested service.<br>
     *            Attention! The service has not the competence to observe this
     *            time exactly. E.g. during the establishing of connections the
     *            waiting time depends to some things which are not controlled
     *            by the service.
     *            <p>
     *            Value 0 for timeOutForSearch means that there is no timeout
     *            used.
     * 
     * @return An object of type {@link SimpleSocketData} which contains the ip
     *         and port to the server with the requested service.
     */
    public SimpleSocketData getSocketData(String serviceTypeName, int timeOutForSearch) throws ActionFailedException {
        System.out.println("Gesucht wird auf: " + getPartnerHostAddress() + ":" + getPartnerPort());

        // the result of this function
        SimpleSocketData socketData = null;

        // opening
        writeMsg(DispatchTag.REQ_SERVICE);

        // service asks if this client is a DispatcherService too
        DispatchTag tag = DispatchTag.INFO_CLIENT_IS_NO_DISPATCHER;
        if (IS_CLIENT_FOR_SERVICE)
            tag = DispatchTag.INFO_CLIENT_IS_DISPATCHER;
        awaitRequestAnswerImmediate(DispatchTag.REQ_CLIENT_IS_DISPATCHER, DispatchTag.RESP_CLIENT_IS_DISPATCHER, tag.name());

        // service wants to know which service type he has to look for
        awaitRequestAnswerImmediate(DispatchTag.REQ_SERVICE_TYPE, DispatchTag.RESP_SERVICE_TYPE, serviceTypeName);

        // service asks how much time he has to search
        awaitRequestAnswerImmediate(DispatchTag.REQ_MAX_TIME_SEARCH, DispatchTag.RESP_MAX_TIME_SEARCH, String.valueOf(timeOutForSearch));

        // now wait for the answer of the service...
        String response = requestTo(DispatchTag.REQ_SERVICE_AVAILABLE, DispatchTag.RESP_SERVICE_AVAILABLE);

        // some cases
        if (response.equals(DispatchTag.RESULT_DISPATCHING_NOT_SUPPORTED.name())) {
            // Dispatch service not really supported on server
            throw new ActionFailedException(1000L, "Gesuchter Typ: " + serviceTypeName + "; Gesucht auf: " + getPartnerHostAddress() + ":" + getPartnerPort());
        } else if (response.equals(DispatchTag.RESULT_SERVICE_TYPE_UNKNOWN.name())) {
            // unknown ServiceType
            throw new ActionFailedException(1001L, "Gesuchter Typ: " + serviceTypeName);
        } else if (response.equals(DispatchTag.RESULT_MAX_NUMBER_CLIENTS_EXCEEDED.name())) {
            // max. number of clients exceeded
            throw new ActionFailedException(1002L, "Gesuchter Typ: " + serviceTypeName);
        } else if (response.equals(DispatchTag.RESULT_SERVICE_AVAILABLE.name())) {
            // wow ...
            // ask for ip and port
            String ip = requestTo(DispatchTag.REQ_IP, DispatchTag.RESP_IP);
            String port = requestTo(DispatchTag.REQ_PORT, DispatchTag.RESP_PORT);
            socketData = new SimpleSocketData(ip, port);
        }

        close();
        return socketData;
    }

    @Override
    public String serviceForClientByName() {
        // TODO Auto-generated method stub
        return null;
    }
}
