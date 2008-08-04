package de.notEOF.dispatch.service;

import java.util.List;

import de.notEOF.configuration.client.LocalConfigurationClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.util.Util;
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
        activateLifeSignSystem();
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

            // Perhaps a service is already running or was created in the past.
            // BaseService can help.
            int activeServicesForType = 0;
            int maxServicesForType = 0;
            List<Service> serviceList = getServiceListByTypeName(requestedServiceType);
            if (null != serviceList) {
                System.out.println("Anzahl aktiver Clients: " + serviceList.size());
                activeServicesForType = serviceList.size();
            }

            // is the type known by configuration?
            // attention: all attributes must be configured in the list in
            // configuration file because of if in the list an attribute is
            // missed, the two list elements below are not 'synchron' - the
            // number of their elements would differ and not match.
            List<String> types = LocalConfigurationClient.getList("serviceTypes.[@type]");
            List<String> maxClients = LocalConfigurationClient.getList("serviceTypes.[@maxClients]");

            // search matching type in configuration
            boolean dispatchSupported = false;
            boolean confEntryFound = false;
            if (null != types && null != maxClients && types.size() == maxClients.size()) {
                dispatchSupported = true;
                for (int i = 0; i < types.size(); i++) {
                    String typeName = types.get(i).trim();
                    if (typeName.equals(requestedServiceType)) {
                        // type exists in configuration
                        System.out.println("max allowed: " + maxClients.get(i));
                        maxServicesForType = Util.parseInt(maxClients.get(i), 0);
                        confEntryFound = true;
                        break;
                    }
                }
            }
            boolean maxClientsExceeded = (maxServicesForType - activeServicesForType) > 0;

            // if not found the service local or max. number of clients exceeded
            // and the client is not a dispatcher
            // too, try to find the service type at another server.
            if (!(dispatchSupported && confEntryFound) && maxClientsExceeded && !clientIsDispatcher) {
                // neuen dispatch client holen und konfigurierte server
                // sequentiell abfragen
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
                awaitRequestAnswerImmediate(DispatchTag.REQ_IP, DispatchTag.RESP_IP, getServerHostAddress());
                // client must ask for port
                awaitRequestAnswerImmediate(DispatchTag.REQ_PORT, DispatchTag.RESP_PORT, String.valueOf(getServerPort()));
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
