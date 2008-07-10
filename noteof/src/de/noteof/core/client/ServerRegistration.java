package de.noteof.core.client;

import de.noteof.core.communication.TalkLine;
import de.noteof.core.enumeration.ServerTag;
import de.noteof.core.exception.ActionFailedException;
import de.noteof.core.service.BaseService;
import de.noteof.core.util.ArgsParser;
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
     * @param args
     *            Additional informations get by calling parameters (e.g.
     *            service id)
     * @param timeOutMillis
     *            Maximum of milliseconds till the registration must be
     *            completed.
     * @throws ActionFailedException
     */
    public ServerRegistration(Class service, TalkLine messageLayer, int timeOutMillis, String... args) throws ActionFailedException {
        Registration registration = new Registration();
        Thread registrationThread = new Thread(registration);
        registrationThread.run();
        serviceId = registration.register(service, messageLayer, args);

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
        protected String register(Class<BaseService> service, TalkLine talkLine, String... args) throws ActionFailedException {
            // First step: Say hello to the server
            if (!Util.equalsToString(talkLine.requestTo(ServerTag.REQ_REGISTRATION, ServerTag.RESP_REGISTRATION), ServerTag.VAL_OK.name())) {
                throw new ActionFailedException(22L, "Anmeldung vom Server abgelehnt.");
            }

            // see if a service id already is given with the args (calling
            // parameter)
            String deliveredServiceId = "";
            ArgsParser argsParser = new ArgsParser(args);
            if (argsParser.containsStartsWith("--serviceId_")) {
                deliveredServiceId = argsParser.getValue("serviceId_");
            }

            // Second step: Server asks client for an existing service id
            talkLine.awaitRequestAnswerImmediate(ServerTag.REQ_SERVICE_ID, ServerTag.RESP_SERVICE_ID, deliveredServiceId);

            // Third step: Ask for a service
            // It is not guaranteed that the service number is the same as
            // delivered by args
            String serviceClassName = service.getCanonicalName();
            if (Util.isEmpty(serviceClassName))
                serviceClassName = service.getName();
            talkLine.awaitRequestAnswerImmediate(ServerTag.REQ_TYPE_NAME, ServerTag.RESP_TYPE_NAME, serviceClassName);
            String serviceId = talkLine.requestTo(ServerTag.REQ_SERVICE, ServerTag.RESP_SERVICE);
            if (Util.isEmpty(serviceId)) {
                throw new ActionFailedException(22L, "Server hat dem Client keinen Service zugeordnet.");
            }
            linkedToService = true;
            return serviceId;
        }
    }
}
