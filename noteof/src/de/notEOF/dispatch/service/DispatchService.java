package de.notEOF.dispatch.service;

import java.util.GregorianCalendar;
import java.util.List;

import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.communication.SimpleSocketData;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.util.Util;
import de.notEOF.dispatch.client.DispatchClient;
import de.notEOF.dispatch.enumeration.DispatchTag;
import de.notIOC.exception.NotIOCException;

public class DispatchService extends BaseService implements Service {

    /**
     * This service (and it's client) uses the DispatchTag class.
     */
    @Override
    public Class<?> getCommunicationTagClass() {
        return DispatchTag.class;
    }

    public void init() {
        // activateLifeSignSystem();
    }

    public void checkServerConfiguration() throws ActionFailedException {
        // @TODO methode verbindet sich mit jedem Server einmal...
    }

    /**
     * Awaits requests of clients for service types. <br>
     * The clients ask here, if the service is available by this server or
     * another one. <br>
     * Please respect that the service closes the connection after he answered
     * to the client by itself. <br>
     * The service prooves its own list and answers with one of these responses:
     * <p>
     * - DispatchService is not really supported here (no configuration or
     * something else). <br>
     * - Type is not configured <br>
     * - Max. number of clients is exceeded <br>
     * - Service is available by ip and port (can be another !EOF server)
     */
    @Override
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        if (incomingMsgEnum.equals(DispatchTag.REQ_SERVICE)) {
            String serviceIp = "";
            String servicePort = "";

            // It is important to distinct between different clients:
            // If another dispatcher is the client no more server will be asked
            // for service type.
            // It the client is no dispatcher later we try to find the service
            // type at another server if this server doesn't supports the
            // service type.
            boolean clientIsDispatcher = false;
            String clientType = requestTo(DispatchTag.REQ_CLIENT_IS_DISPATCHER, DispatchTag.RESP_CLIENT_IS_DISPATCHER).trim();
            if (DispatchTag.INFO_CLIENT_IS_DISPATCHER.name().equals(clientType)) {
                clientIsDispatcher = true;
            }

            // now ask client for the requested service type
            String requestedServiceType = requestTo(DispatchTag.REQ_SERVICE_TYPE, DispatchTag.RESP_SERVICE_TYPE).trim();

            // how much time the service has to search?
            int timeOutToSearch = Util.parseInt(requestTo(DispatchTag.REQ_MAX_TIME_SEARCH, DispatchTag.RESP_MAX_TIME_SEARCH), 60000);
            long endTime = 0;
            if (0 != timeOutToSearch) {
                endTime = System.currentTimeMillis() + timeOutToSearch;
            } else {
                endTime = new GregorianCalendar(2063, 6, 28, 12, 0, 0).getTimeInMillis();
            }

            // Perhaps a service is already running or was created in the past.
            // BaseService can help.
            int activeServicesForType = 0;
            int maxServicesForType = 0;
            List<Service> serviceList = getServiceListByTypeName(Util.simpleClassName(requestedServiceType));
            if (null != serviceList) {
                activeServicesForType = serviceList.size();
            }

            // is the type known by configuration?
            // attention: all attributes must be configured in the list in
            // configuration file because of if in the list an attribute is
            // missed, the two list elements below are not 'synchron' - the
            // number of their elements would differ and not match.

            // double code lines seem a little bit buggy at the first look but
            // they are not really the same. It is possible that the user
            // configures simple class names or canonical class names. And the
            // lists are not sure synchroniously.
            boolean dispatchSupported = false;
            boolean confEntryFound = false;
            List<String> simpleNames = null;
            List<String> maxClients = null;
            try {
                simpleNames = LocalConfigurationClient.getAttributeList("serviceTypes", "simpleName");
                maxClients = LocalConfigurationClient.getAttributeList("serviceTypes", "maxClients");
            } catch (NotIOCException nex) {
                LocalLog.warn("Configuration of services maybe is corrupt or missed (serviceTypes by simpleName).", nex);
            }

            // search matching type in configuration via simple class name
            if (null != simpleNames && null != maxClients && simpleNames.size() == maxClients.size()) {
                dispatchSupported = true;
                for (int i = 0; i < simpleNames.size(); i++) {
                    if (endTime <= System.currentTimeMillis())
                        break;
                    String typeName = simpleNames.get(i).trim();
                    if (typeName.equals(requestedServiceType)) {
                        // type exists in configuration
                        maxServicesForType = Util.parseInt(maxClients.get(i), 0);
                        confEntryFound = true;
                        serviceIp = getServerHostAddress();
                        servicePort = String.valueOf(getServerPort());
                        break;
                    }
                }
            }

            if (!confEntryFound && endTime > System.currentTimeMillis()) {
                // search matching type in configuration via canonical class
                // name
                List<String> canonicalNames = null;
                try {
                    canonicalNames = LocalConfigurationClient.getAttributeList("serviceTypes", "canonicalName");
                } catch (NotIOCException nex) {
                    LocalLog.warn("Configuration of services maybe is corrupt or missed(serviceTypes by canonicalName).", nex);
                }
                if (null != canonicalNames && null != maxClients && canonicalNames.size() == maxClients.size()) {
                    dispatchSupported = true;
                    for (int i = 0; i < canonicalNames.size(); i++) {
                        if (endTime <= System.currentTimeMillis())
                            break;
                        String typeName = canonicalNames.get(i).trim();
                        if (typeName.equals(requestedServiceType)) {
                            // type exists in configuration
                            maxServicesForType = Util.parseInt(maxClients.get(i), 0);
                            confEntryFound = true;
                            serviceIp = getServerHostAddress();
                            servicePort = String.valueOf(getServerPort());
                            break;
                        }
                    }
                }
            }

            boolean maxClientsExceeded = (maxServicesForType - activeServicesForType) < 1;
            System.out.println("maxClientsExceeded " + maxClientsExceeded);
            System.out.println("dispatchSupported " + dispatchSupported);
            System.out.println("confEntryFound " + confEntryFound);

            // if not found the service local or max. number of clients exceeded
            // or dispatcher isn't supported really here and the client is not a
            // dispatcher itself, try to find the service type at another
            // server.
            if ((!(dispatchSupported && confEntryFound) //
                    || (maxClientsExceeded && maxServicesForType > 0)) //
                    && !clientIsDispatcher) {
                // get new dispatch client and send same request to other server
                // which are configured
                List<String> eofServerIp = null;
                List<String> eofServerPort = null;
                try {
                    eofServerIp = LocalConfigurationClient.getAttributeList("notEOFServer", "ip");
                    eofServerPort = LocalConfigurationClient.getAttributeList("notEOFServer", "port");

                    // search by ip's
                    // if configuration here isn't correct, make an entry into
                    // log
                    if (null != eofServerIp && null != eofServerPort && eofServerIp.size() == eofServerPort.size()) {
                        DispatchClient dispatchClient = null;
                        for (int i = 0; i < eofServerIp.size(); i++) {
                            if (endTime <= System.currentTimeMillis())
                                break;

                            if (eofServerIp.get(i).equals(getServerHostAddress())) {
                            } else {
                                SimpleSocketData socketData = null;
                                try {
                                    BaseTimeOut timeout = new BaseTimeOut(1000, 1500);
                                    dispatchClient = new DispatchClient(eofServerIp.get(i), Util.parseInt(eofServerPort.get(i), 0), timeout, false,
                                            (String[]) null);
                                    dispatchClient.IS_CLIENT_FOR_SERVICE = true;
                                    socketData = dispatchClient.getSocketData(requestedServiceType, timeOutToSearch);
                                    if (null != socketData) {
                                        // client has received valid address
                                        dispatchSupported = true;
                                        confEntryFound = true;
                                        serviceIp = socketData.getIp();
                                        servicePort = socketData.getPortString();
                                        break;
                                    }
                                } catch (ActionFailedException afx) {
                                    // catch the failed attempt of dispatch
                                    // client
                                    // to not leave this method by
                                    // ActionFailedException because the
                                    // originating
                                    // client must be informed
                                    if (socketData != null) {
                                        LocalLog.warn("Couldn't find service type at server " + socketData.getIp() + ":" + socketData.getPortString(), afx);
                                    } else {
                                        LocalLog.warn("Connection problem while connecting with server " + eofServerIp.get(i) + ":" + eofServerPort.get(i));
                                    }
                                }
                            }
                        }
                    } else {
                        LocalLog.warn("Configuration of serverlist is empty.");
                    }
                } catch (NotIOCException nex) {
                    LocalLog.warn("Configuration of serverlist maybe is corrupt or missed.");
                }
            }

            // Client asks if the service is available
            awaitRequest(DispatchTag.REQ_SERVICE_AVAILABLE);
            // Then the service gives the calculated answer
            if (!dispatchSupported) {
                // Dispatching not configured (well)
                responseTo(DispatchTag.RESP_SERVICE_AVAILABLE, DispatchTag.RESULT_DISPATCHING_NOT_SUPPORTED.name());
            } else if (!confEntryFound) {
                // service type not in configuration
                responseTo(DispatchTag.RESP_SERVICE_AVAILABLE, DispatchTag.RESULT_SERVICE_TYPE_UNKNOWN.name());
            } else if (maxServicesForType - activeServicesForType < 1) {
                // max. number of allowed clients exceeded
                responseTo(DispatchTag.RESP_SERVICE_AVAILABLE, DispatchTag.RESULT_MAX_NUMBER_CLIENTS_EXCEEDED.name());
            } else {
                // OK - send ip and port
                responseTo(DispatchTag.RESP_SERVICE_AVAILABLE, DispatchTag.RESULT_SERVICE_AVAILABLE.name());

                // client must ask for ip
                awaitRequestAnswerImmediate(DispatchTag.REQ_IP, DispatchTag.RESP_IP, serviceIp);
                // client must ask for port
                awaitRequestAnswerImmediate(DispatchTag.REQ_PORT, DispatchTag.RESP_PORT, servicePort);
            }

        } else {
            // Client sent wrong request...
            responseTo(DispatchTag.INFO_UNKNOWN_MSG, "");
        }

        // That's it. Client should disconnect at this point also...
        System.out.println("Service wird gestoppt");
        stopService();
        close();
    }
}
