package de.happtick.core.application.client;

import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.event.ActionEvent;
import de.happtick.core.event.AlarmEvent;
import de.happtick.core.event.ErrorEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.ClientObserver;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;

/**
 * This client has less business logic and more connection / communication
 * logic. <br>
 * The business logic must be implemented in the class which uses this client. <br>
 * The main goal to use the ApplicationClient is e.g. to ensure that no
 * instances of the application already is running and to inform the service
 * about events. <br>
 * 
 * @author dirk
 */
public class ApplicationClient extends BaseClient implements NotEOFClient {

    private boolean isWorkAllowed = false;
    private AllowanceWaiter allowanceWaiter;
    private ClientObserver clientObserver;
    private Long applicationId;

    @Override
    public Class<?> serviceForClientByClass() {
        return ApplicationService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }

    public void stop() throws HapptickException {
        stop(0);
    }

    /**
     * Send stop event to service.
     * 
     * @param exitCode
     *            The return code or another result of the application.
     * @throws HapptickException
     */
    public void stop(int exitCode) throws HapptickException {
        if (null != allowanceWaiter)
            allowanceWaiter.stop();

        try {
            writeMsg(ApplicationTag.PROCESS_STOP_WORK);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_EXIT_CODE, ApplicationTag.RESP_EXIT_CODE, String.valueOf(exitCode));
            // give service a little bit time...
            // TODO Was ist das für eine Zahl?
            readMsgTimedOut(7654);
            super.close();
        } catch (ActionFailedException e) {
            throw new HapptickException(207L, e);
        }
    }

    public String requestTo(Enum<?> requestHeader, Enum<?> expectedResponseHeader) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: requestTo");
        }
        return super.requestTo(requestHeader, expectedResponseHeader);
    }

    public void awaitRequest(Enum<?> expectedRequestHeader) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: awaitRequest");
        }
        super.awaitRequest(expectedRequestHeader);
    }

    public void awaitRequestAnswerImmediate(Enum<?> expectedRequestHeader, Enum<?> responseHeader, String value) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: awaitRequestAnswerImmediate");
        }
        super.awaitRequestAnswerImmediate(expectedRequestHeader, responseHeader, value);
    }

    public synchronized void sendMail(NotEOFMail mail) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: sendMail");
        }
        super.sendMail(mail);
    }

    public void responseTo(Enum<?> responseHeader, String value) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: responseTo");
        }
        super.responseTo(responseHeader, value);
    }

    public String readMsg() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: readMsg");
        }
        return super.readMsg();
    }

    public String readMsgNoTimeOut() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: readMsgNoTimeOut");
        }
        return super.readMsgNoTimeOut();
    }

    public String readMsgTimedOut(int timeOutMillis) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: readMsgTimedOut");
        }
        return super.readMsgTimedOut(timeOutMillis);
    }

    public void writeMsg(String msg) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: writeMsg");
        }
        super.writeMsg(msg);
    }

    public void writeMsg(Enum<?> requestHeader) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: writeMsg");
        }
        super.writeMsg(requestHeader);
    }

    public DataObject receiveDataObject() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: receiveDataObject");
        }
        return super.receiveDataObject();
    }

    public void sendDataObject(DataObject dataObject) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: sendDataObject");
        }
        super.sendDataObject(dataObject);
    }

    /**
     * Send event to service that the client has started his work.
     * 
     * @throws HapptickException
     */
    public void startWork() throws HapptickException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Methode: startWork");
        }

        try {
            writeMsg(ApplicationTag.PROCESS_START_WORK);
            readMsgTimedOut(7654);
        } catch (ActionFailedException e) {
            throw new HapptickException(207L, e);
        }
    }

    /**
     * Send id of application to the service.
     * 
     * @param applicationId
     *            Unique id of application. This id is used in the happtick
     *            configuration for scheduling.
     * @throws HapptickException
     */
    public void applicationIdToService(Long applicationId) throws HapptickException {
        if (null == this.applicationId)
            this.applicationId = applicationId;
        try {
            writeMsg(ApplicationTag.PROCESS_APPLICATION_ID);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID, String.valueOf(applicationId));
        } catch (ActionFailedException e) {
            throw new HapptickException(206L, e);
        }
    }

    /**
     * Send start id which the start client has generated and send per args.
     * 
     * @param args
     *            Parameterlist. Maybe it contains the param --startId.
     * @throws HapptickException
     */
    public void startIdToService(String... args) throws HapptickException {
        if (null != args) {
            String startId = "";
            ArgsParser argsParser = new ArgsParser(args);
            if (argsParser.containsStartsWith("--startId")) {
                startId = argsParser.getValue("startId");
            }

            if (!Util.isEmpty(startId)) {
                try {
                    writeMsg(ApplicationTag.PROCESS_START_ID);
                    awaitRequestAnswerImmediate(ApplicationTag.REQ_START_ID, ApplicationTag.RESP_START_ID, startId);
                } catch (ActionFailedException e) {
                    throw new HapptickException(206L, e);
                }
            }
        }
    }

    /**
     * Ask the application service if the application may do it's job.
     * <p>
     * Perhaps there is actually another process of the application running and
     * it is not allowed to have more than one active working processes of this
     * application kind. Then this instance can wait till the service allows to
     * start. Or it stops itself and will be started at a later moment by the
     * scheduler. <br>
     * The situation that the application isn't allowed to work maybe can arrive
     * <br>
     * - if it is started manually <br>
     * - if the scheduler makes faults - application or network errors derange
     * the communication between the application clients and the application
     * services so that the scheduler starte the application twice or multiple.
     * 
     * @return True if the application can start the work.
     * @throws HapptickException
     */
    public boolean isWorkAllowed() throws HapptickException {
        // simpliest case
        if (isWorkAllowed)
            return true;

        // if this client has a clientNetId it was started by scheduler.
        if (!Util.isEmpty(getClientNetId()))
            return true;

        // only when start allowance not given yet service must be asked for
        try {
            // inform service that an requests for start allowance will
            // follow
            writeMsg(ApplicationTag.PROCESS_START_ALLOWANCE);
            isWorkAllowed = ApplicationTag.INFO_TRUE.name().equals(requestTo(ApplicationTag.REQ_START_ALLOWED, ApplicationTag.RESP_START_ALLOWED));
        } catch (ActionFailedException e) {
            throw new HapptickException(201L, e);
        }

        return isWorkAllowed;
    }

    /**
     * Errors can be shown within the happtick monitoring tool or written to
     * logfiles.
     * <p>
     * 
     * @param errorId
     *            Unique identifier of the error.
     * @param description
     *            Error text.
     * @param level
     *            Kind of error (info, warning, error or anything else). Depends
     *            to the application.
     * @throws HapptickException
     */
    public void sendError(String errorId, String description, String level) throws HapptickException {
        ErrorEvent event = new ErrorEvent();
        try {
            event.addAttribute("errorId", errorId);
            event.addAttribute("description", description);
            event.addAttribute("level", level);
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, "Event: " + event.getClass().getSimpleName(), e);
        }
        sendEvent(event);
    }

    /**
     * Happtick is able to react to events. There are standard events like start
     * and stop of application. The relations between them are configurable.
     * Supplemental events and actions can be configured for single
     * applications.
     * 
     * @param eventId
     *            Id which is used in the configuration.
     * @param information
     *            Additional information related to the action.
     * @throws HapptickException
     */
    public void sendActionEvent(String eventId, String information) throws HapptickException {
        ActionEvent event = new ActionEvent();
        try {
            event.addAttribute("eventId", eventId);
            event.addAttribute("information", information);
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, "Event: " + event.getClass().getSimpleName(), e);
        }
        sendEvent(event);
    }

    /**
     * Send any event to the service.
     * 
     * @param event
     *            The implementation of NotEOFEvent should not use additional
     *            data because only standard values are supported here. If there
     *            are more members in the event class they will not be
     *            transported to the service.
     * @throws HapptickException
     */
    public void sendEvent(NotEOFEvent event) throws HapptickException {
        if (!isWorkAllowed()) {
            throw new HapptickException(208L, "Versenden eines Events.");
        }

        try {
            System.out.println("ApplicationClient - sendEvent: super.sendEvent()");
            event.setApplicationId(this.applicationId);
            super.sendEvent(event);
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, "Event: " + event.getClass().getSimpleName(), e);
        }
    }

    /**
     * Releases an alert. <br>
     * Like errors alarms can have a level. The controlling alarm system of
     * happtick decides what to do depending to the alarm level.
     * 
     * @param description
     *            Alarm text. What happened exactly.
     * @param level
     *            Meaning of alarm (info, warning or anything else). Depends to
     *            the application.
     * 
     * @throws HapptickException
     */
    public void sendAlarm(String type, String description, String level) throws HapptickException {
        AlarmEvent event = new AlarmEvent();
        try {
            event.addAttribute("type", type);
            event.addAttribute("description", description);
            event.addAttribute("level", level);
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, "Event: " + event.getClass().getSimpleName(), e);
        }
        sendEvent(event);
    }

    /**
     * Log informations can be visualized within the happtick monitoring tool or
     * written to log files on the server.
     * 
     * @param information
     *            Detailed Text.
     * @throws HapptickException
     */
    public void sendLog(String information) throws HapptickException {
        AlarmEvent event = new AlarmEvent();
        try {
            event.addAttribute("information", information);
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, "Event: " + event.getClass().getSimpleName(), e);
        }
        sendEvent(event);
    }

    /**
     * Alternately to wait for start allowance by calling the method
     * isWorkAllowed() repeatedly within a loop it is possible to let the
     * application informed by this method. Condition is that the application
     * implements the interface ClientObserver and waits for start allowance in
     * the method update(). When the allowance is given the application client
     * calls the method observers startAllowanceEvent()<br>
     * 
     * @throws HapptickException
     * @see {@link ClientObserver}
     */
    public void observeForWorkAllowance(ClientObserver clientObserver) throws HapptickException {
        this.clientObserver = clientObserver;
        allowanceWaiter = new AllowanceWaiter();
        Thread waiterThread = new Thread(allowanceWaiter);
        waiterThread.start();
    }

    /**
     * If the using class has started the observing for awaiting the start
     * allowance this can be stopped here.
     */
    public void stopObservingForStartAllowance() {
        if (null != allowanceWaiter)
            allowanceWaiter.stop();
    }

    /*
     * Class runs in a thread and waits for allowance by service to start work
     */
    private class AllowanceWaiter implements Runnable {
        private boolean stopped = false;

        public boolean stopped() {
            return stopped;
        }

        public void stop() {
            stopped = true;
        }

        public void run() {
            try {
                while (!stopped || !isWorkAllowed()) {
                    Thread.sleep(1000);
                }
                if (isWorkAllowed()) {
                    clientObserver.startAllowanceEvent(true);
                }
            } catch (Exception e) {
                stopped = true;
                clientObserver.startAllowanceEvent(false);
            }
        }
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
