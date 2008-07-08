package de.noteof.core.client;

import de.noteof.core.communication.MessageLayer;
import de.noteof.core.enumeration.ServerTag;
import de.noteof.core.exception.ActionFailedException;
import de.noteof.core.util.Util;

public class ServerRegistration {

    /**
     * Class which establishes the connection to the server.
     */

    private boolean linkedToService = false;
    private String serviceId;

    /**
     * Construction of this class...
     * 
     * @param clientTypeName
     *            The name of the clientType which the server needs for linking
     *            to a service.
     * @param messageLayer
     *            An existing physical connection to the server.
     * @param timeOutMillis
     *            Maximum of milliseconds till the registration must be
     *            completed.
     * @throws ActionFailedException
     */
    public ServerRegistration(String clientTypeName, MessageLayer messageLayer, int timeOutMillis) throws ActionFailedException {
        Registration registration = new Registration();
        Thread registrationThread = new Thread(registration);
        registrationThread.run();
        serviceId = registration.register(clientTypeName, messageLayer);

        // The registration hasn't every time of the world...
        long endTime = System.currentTimeMillis() + timeOutMillis;
        while (!linkedToService && endTime > System.currentTimeMillis())
            ;
        // if the timeout has exceeded the thread must be terminated not to
        // become a daemon.
        registration.stop();
        if (!linkedToService) {
            throw new ActionFailedException(22L, "Timout ist abgelaufen.");
        }
    }

    /**
     * When the registration was successfull the server advises the client of
     * the service.
     * 
     * @return The id of the service which is concerned with the client.
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Tells if the registration at the server was successfull.
     * 
     * @return True after the server linked a service to the client.
     */
    public boolean isLinkedToService() {
        return linkedToService;
    }

    /*
     * Inner class for running within a thread.
     */
    private class Registration implements Runnable {

        private boolean stopped = false;

        protected void stop() {
            stopped = true;
        }

        /**
         * runs while not linked and not stopped
         */
        public void run() {
            while (!(linkedToService || stopped)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ix) {
                }
            }
        }

        // Register at the server and ask for a service
        protected String register(String clientTypeName, MessageLayer messageLayer) throws ActionFailedException {
            // First step: Say hello to the server
            if (!Util.equalsToString(messageLayer.requestTo(ServerTag.REQ_REGISTRATION, ServerTag.RESP_REGISTRATION), ServerTag.VAL_OK.name())) {
                throw new ActionFailedException(22L, "Anmeldung vom Server abgelehnt.");
            }

            // Second step: Ask for a service
            messageLayer.awaitRequestAnswerImmediate(ServerTag.REQ_TYPE_NAME, ServerTag.RESP_TYPE_NAME, clientTypeName);
            String serviceId = messageLayer.requestTo(ServerTag.REQ_SERVICE, ServerTag.RESP_SERVICE);
            if (Util.isEmpty(serviceId)) {
                throw new ActionFailedException(22L, "Server hat dem Client keinen Service zugeordnet.");
            }
            linkedToService = true;
            return serviceId;
        }
    }
}
