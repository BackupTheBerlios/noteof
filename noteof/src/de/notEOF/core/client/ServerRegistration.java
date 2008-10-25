package de.notEOF.core.client;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;

public class ServerRegistration {

    /**
     * Class which establishes the connection to the server.
     */

    private boolean linkedToService = false;
    private String clientNetId;
    private String serviceId;

    /**
     * Construction of this class...
     * 
     * @param service
     *            The class which represents the service especially for the
     *            registering client.
     * @param talkLine
     *            An existing physical connection to the server.
     * @param args
     *            Additional informations get by calling parameters (e.g.
     *            service id)
     * @param timeOutMillis
     *            Maximum of milliseconds till the registration must be
     *            completed.
     * @throws ActionFailedException
     */
    public ServerRegistration(String serviceClassName, TalkLine talkLine, int timeOutMillis, String... args) throws ActionFailedException {
        Registration registration = new Registration();
        // Thread registrationThread = new Thread(registration);
        // registrationThread.start();

        if (timeOutMillis > 0) {
            Waiter waiter = new Waiter(talkLine, timeOutMillis);
            Thread waiterThread = new Thread(waiter);
            waiterThread.start();
        }
        try {
            serviceId = registration.register(serviceClassName, talkLine, args);
        } catch (Exception ex) {
            throw new ActionFailedException(22L, ex);
        }

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

    public String getClientNetId() {
        return this.clientNetId;
    }

    /**
     * Tells if the registration at the server was successfull.
     * 
     * @return True after the server linked a service to the client.
     */
    public boolean isLinkedToService() {
        return linkedToService;
    }

    private class Waiter implements Runnable {
        private long endTime;

        // private TalkLine talkLine;

        public Waiter(TalkLine talkLine, int timeOut) {
            this.endTime = System.currentTimeMillis() + timeOut;
            // this.talkLine = talkLine;
        }

        public void run() {
            try {
                while (!linkedToService && endTime > System.currentTimeMillis()) {
                    Thread.sleep(200);
                }

                if (!linkedToService) {
                    // talkLine.close();
                } else {
                }
            } catch (InterruptedException ex) {
                // talkLine.close();
            }
        }
    }

    /*
     * Inner class (perhaps later runs in an own thread).
     */
    private class Registration {

        // Register at the server and ask for a service
        protected String register(String serviceClassName, TalkLine talkLine, String... args) throws ActionFailedException {
            // First step: Say hello to the server
            System.out.println("ServerRegistration register... sendet 0");
            if (!Util.equalsToString(talkLine.requestTo(BaseCommTag.REQ_REGISTRATION, BaseCommTag.RESP_REGISTRATION), BaseCommTag.VAL_OK.name())) {
                throw new ActionFailedException(22L, "Anmeldung vom Server abgelehnt.");
            }

            // second step: Get netwide id
            clientNetId = talkLine.requestTo(BaseCommTag.REQ_CLIENT_ID, BaseCommTag.RESP_CLIENT_ID);

            // see if a service id already is given with the args (calling
            // parameter)
            String deliveredServiceId = "";
            ArgsParser argsParser = new ArgsParser(args);
            if (argsParser.containsStartsWith("--serviceId_")) {
                deliveredServiceId = argsParser.getValue("serviceId_");
            }

            // Third step: Server asks client for an existing service id
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_SERVICE_ID, BaseCommTag.RESP_SERVICE_ID, deliveredServiceId);

            // Fourth step: Ask for a service
            // It is not guaranteed that the service number is the same as
            // delivered by args
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_TYPE_NAME, BaseCommTag.RESP_TYPE_NAME, serviceClassName);
            String serviceId = talkLine.requestTo(BaseCommTag.REQ_SERVICE, BaseCommTag.RESP_SERVICE);
            String activateLifeSignSystem = talkLine.requestTo(BaseCommTag.REQ_LIFE_SIGN_ACTIVATE, BaseCommTag.RESP_LIFE_SIGN_ACTIVATE);
            if (BaseCommTag.VAL_TRUE.name().equals(activateLifeSignSystem)) {
                talkLine.activateLifeSignSystem(true);
            }
            if (Util.isEmpty(serviceId)) {
                throw new ActionFailedException(22L, "Server hat dem Client keinen Service zugeordnet.");
            }
            linkedToService = true;
            return serviceId;
        }
    }
}
