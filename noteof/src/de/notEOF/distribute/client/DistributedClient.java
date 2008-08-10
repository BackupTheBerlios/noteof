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
    private BaseClient client;

    /**
     * The constructor tries to establish a connection to any server which
     * offers the needed service.
     * 
     * @param client
     *            An instance of Client which was derived from class BaseClient.
     * @param socketData
     *            SocketData holds the data which are needed for establishing a
     *            connection.
     * @param timeOut
     *            Individual or basically Object of type BaseTimeOut.
     * @throws ActionFailedException
     */
    public DistributedClient(BaseClient client, SimpleSocketData socketData, BaseTimeOut timeOut) throws ActionFailedException {
        this.client = client;
        BaseTimeOut baseTimeOut = new BaseTimeOut(0, 60000);
        DispatchClient dispatchClient = new DispatchClient(socketData.getIp(), socketData.getPort(), baseTimeOut, false, (String[]) null);
        String serviceClassName = client.getServiceClassName();
        Socket socketToService = dispatchClient.getServiceConnection(serviceClassName, 0);

        if (null != socketToService) {
            client.connect(socketToService, timeOut);
            System.out.println(client.getServiceId());
        }
    }

    public BaseClient getClient() {
        return client;
    }
}
