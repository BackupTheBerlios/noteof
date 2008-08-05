package de.notEOF.dispatch.service;

import java.util.List;

import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.util.Util;
import de.notEOF.dispatch.SimpleSocketData;
import de.notEOF.dispatch.client.DispatchClient;
import de.notEOF.dispatch.enumeration.DispatchTag;

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

            System.out.println("Wurde von Dispatcher aufgerufen? " + clientIsDispatcher);
            System.out.println("Bin Service auf: " + getServerHostAddress() + ":" + getServerPort());

            // now ask client for the requested service type
            String requestedServiceType = requestTo(DispatchTag.REQ_SERVICE_TYPE, DispatchTag.RESP_SERVICE_TYPE).trim();

            // the dispatcher isn't interested in the canonicalName...
            // requestedServiceType =
            // Util.simpleClassName(requestedServiceType);

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
            List<String> simpleNames = LocalConfigurationClient.getList("serviceTypes.[@simpleName]");
            List<String> maxClients = LocalConfigurationClient.getList("serviceTypes.[@maxClients]");

            // search matching type in configuration via simple class name
            if (null != simpleNames && null != maxClients && simpleNames.size() == maxClients.size()) {
                dispatchSupported = true;
                for (int i = 0; i < simpleNames.size(); i++) {
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

            if (!confEntryFound) {
                // search matching type in configuration via canonical class
                // name
                List<String> canonicalNames = LocalConfigurationClient.getList("serviceTypes.[@canonicalName]");
                if (null != canonicalNames && null != maxClients && canonicalNames.size() == maxClients.size()) {
                    dispatchSupported = true;
                    for (int i = 0; i < canonicalNames.size(); i++) {
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

            // if not found the service local or max. number of clients exceeded
            // or dispatcher isn't supported really here and the client is not a
            // dispatcher itself, try to find the service type at another
            // server.
            if ((!(dispatchSupported && confEntryFound) //
                    || (maxClientsExceeded && maxServicesForType > 0)) //
                    && !clientIsDispatcher) {
                // get new dispatch client and send same request to other server
                // which are configured
                List<String> eofServerIp = LocalConfigurationClient.getList("notEOFServer.[@ip]");
                List<String> eofServerPort = LocalConfigurationClient.getList("notEOFServer.[@port]");

                // search by ip's
                // if configuration here isn't correct, make an entry into log
                if (null != eofServerIp && null != eofServerPort && eofServerIp.size() == eofServerPort.size()) {
                    DispatchClient dispatchClient = null;
                    for (int i = 0; i < eofServerIp.size(); i++) {
                        System.out.println("eofServerIp = " + eofServerIp.get(i));
                        System.out.println("eofServerPort = " + Util.parseInt(eofServerPort.get(i), 0));

                        if (eofServerIp.get(i).equals(getServerHostAddress())) {
                            System.out.println("EIGENE ADRESSE...");
                        } else {
                            SimpleSocketData socketData = null;
                            try {
                                BaseTimeOut timeout = new BaseTimeOut(500, 500);
                                dispatchClient = new DispatchClient(eofServerIp.get(i), Util.parseInt(eofServerPort.get(i), 0), timeout, (String[]) null);
                                dispatchClient.IS_CLIENT_FOR_SERVICE = true;
                                socketData = dispatchClient.getSocketData(requestedServiceType);
                                if (null != socketData) {
                                    // client has received valid address
                                    dispatchSupported = true;
                                    confEntryFound = true;
                                    serviceIp = socketData.getIp();
                                    servicePort = socketData.getPortString();
                                    break;
                                }
                            } catch (ActionFailedException afx) {
                                // catch the failed attempt of dispatch client
                                // to
                                // not leave this method by
                                // ActionFailedException
                                // because the originating client must be
                                // informed
                                // nothing to do...
                                if (socketData != null)
                                    LocalLog.warn("Couldn't find service type at server " + socketData.getIp() + ":" + socketData.getPortString(), afx);
                            }
                        }
                    }
                } else {
                    LocalLog.warn("Configuration of serverlist maybe is corrupt or missed.");
                }
            }

            System.out.println("Bin der vom Dispatcher aufgerufene? " + clientIsDispatcher);
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
        stopService();
        close();
    }
}
