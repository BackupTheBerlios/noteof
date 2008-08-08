package de.notEOF.distribute.client;

import java.net.Socket;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.BaseTimeOut;
import de.notEOF.core.communication.SimpleSocketData;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.dispatch.client.DispatchClient;

/**
 * This client simplifies the facility of scale client/service networking by
 * !EOF.
 * 
 * @author Dirk
 * 
 */
public class DistributedClient {

    public DistributedClient(BaseClient client, SimpleSocketData socketDataDispatchServer, BaseTimeOut timeOut) throws ActionFailedException {
        BaseTimeOut baseTimeOut = new BaseTimeOut(0, 60000);
        DispatchClient dispatchClient = new DispatchClient(socketDataDispatchServer.getIp(), socketDataDispatchServer.getPort(), baseTimeOut, (String[]) null);
        String serviceClassName = client.getServiceClassName();
        Socket socketToService = dispatchClient.getServiceConnection(serviceClassName, 0);

        if (null != socketToService) {
            client.connect(socketToService, timeOut);
            System.out.println(client.getServiceId());
        }

    }
}
