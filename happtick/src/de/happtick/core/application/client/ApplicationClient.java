package de.happtick.core.application.client;

import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.event.ActionEvent;
import de.happtick.core.event.AlarmEvent;
import de.happtick.core.event.ErrorEvent;
import de.happtick.core.interfaces.ClientObserver;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.interfaces.EventRecipient;

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
public class ApplicationClient extends BaseClient implements NotEOFClient, EventRecipient {

    private boolean isWorkAllowed = false;
    private AllowanceWaiter allowanceWaiter;
    private ClientObserver clientObserver;
    private Long applicationId;
    private boolean stopped = false;
    private EventRecipient eventRecipient = null;

    @Override
    public Class<?> serviceForClientByClass() {
        return ApplicationService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }

    public void stop() throws ActionFailedException {
        stop(0);
    }

    public final void implementationLastSteps() {
        if (!stopped) {
            stop(0);
        }
    }

    public void processEvent(NotEOFEvent event) {
        if (!Util.isEmpty(this.eventRecipient)) {
            eventRecipient.processEvent(event);
        }
    }

    public void processEventException(Exception ex) {
        eventRecipient.processEventException(ex);
    }

    public void processMail(NotEOFMail mail) {
        eventRecipient.processMail(mail);
    }

    public void processMailException(Exception ex) {
        eventRecipient.processMailException(ex);
    }

    public void processStopEvent(NotEOFEvent event) {
        eventRecipient.processStopEvent(event);
    }

    public void setEventRecipient(EventRecipient eventRecipient) {
        this.eventRecipient = eventRecipient;
    }

    /**
     * Send stop event to service.
     * 
     * @param exitCode
     *            The return code or another result of the application.
     * @throws ActionFailedException
     */
    public void stop(int exitCode) {
        stopped = true;
        if (null != allowanceWaiter)
            allowanceWaiter.stop();

        try {
            writeMsg(ApplicationTag.PROCESS_STOP_WORK);
            awaitRequestAnswerImmediateTimedOut(ApplicationTag.REQ_EXIT_CODE, ApplicationTag.RESP_EXIT_CODE, String.valueOf(exitCode), 10000);
        } catch (Exception ex) {
            // LocalLog.warn("Problem bei Stoppen des ApplicationClient. 1XXX!",
            // ex);
            // LocalLog.warn("Problem bei Stoppen des ApplicationClient. 1a!");
        }
        try {
            // give service a little bit time...
            // TODO Was ist das fuer eine Zahl?
            readMsgTimedOut(7654);
        } catch (Exception ex) {
            // LocalLog.warn("Problem bei Stoppen des ApplicationClient. 2!",
            // ex);
        }
        try {
            super.close();
        } catch (Exception ex) {
            // LocalLog.warn("Problem bei Stoppen des ApplicationClient. 3!",
            // ex);
        }
    }

    public String requestTo(Enum<?> requestHeader, Enum<?> expectedResponseHeader) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: requestTo");
        }
        return super.requestTo(requestHeader, expectedResponseHeader);
    }

    public void awaitRequest(Enum<?> expectedRequestHeader) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: awaitRequest");
        }
        super.awaitRequest(expectedRequestHeader);
    }

    public void awaitRequestAnswerImmediate(Enum<?> expectedRequestHeader, Enum<?> responseHeader, String value) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: awaitRequestAnswerImmediate");
        }
        super.awaitRequestAnswerImmediate(expectedRequestHeader, responseHeader, value);
    }

    public void awaitRequestTimedOut(Enum<?> expectedRequestHeader, int timeOutMillis) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: awaitRequest");
        }
        super.awaitRequestTimedOut(expectedRequestHeader, timeOutMillis);
    }

    public void awaitRequestAnswerImmediateTimedOut(Enum<?> expectedRequestHeader, Enum<?> responseHeader, String value, int timeOutMillis)
            throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: awaitRequestAnswerImmediate");
        }
        super.awaitRequestAnswerImmediateTimedOut(expectedRequestHeader, responseHeader, value, timeOutMillis);
    }

    public synchronized void sendMail(NotEOFMail mail) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: sendMail");
        }
        super.sendMail(mail);
    }

    public void responseTo(Enum<?> responseHeader, String value) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: responseTo");
        }
        super.responseTo(responseHeader, value);
    }

    public String readMsg() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: readMsg");
        }
        return super.readMsg();
    }

    public String readMsgNoTimeOut() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: readMsgNoTimeOut");
        }
        return super.readMsgNoTimeOut();
    }

    public String readMsgTimedOut(int timeOutMillis) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: readMsgTimedOut");
        }
        return super.readMsgTimedOut(timeOutMillis);
    }

    public void writeMsg(String msg) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: writeMsg");
        }
        super.writeMsg(msg);
    }

    public void writeMsg(Enum<?> requestHeader) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: writeMsg");
        }
        super.writeMsg(requestHeader);
    }

    public DataObject receiveDataObject() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: receiveDataObject");
        }
        return super.receiveDataObject();
    }

    public void sendDataObject(DataObject dataObject) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: sendDataObject");
        }
        super.sendDataObject(dataObject);
    }

    /**
     * Send event to service that the client has started his work.
     * 
     * @throws ActionFailedException
     */
    public void startWork() throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Methode: startWork");
        }

        try {
            writeMsg(ApplicationTag.PROCESS_START_WORK);
            readMsgTimedOut(7654);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10207L, e);
        }
    }

    /**
     * Sends application id to the service.
     * <p>
     * Every application running in the happtick environment must have an id.
     * This id must be unique for applications. Nevertheless it is possible that
     * more than one processes have the same application id during run time
     * because this id distincts applications - not processes. That means that
     * all processes of an application have the same application id. <br>
     * To distinct processes they have a 'start id' which they get when started
     * by the start client (happtick process which starts applications).
     * 
     * @param applicationId
     *            Unique id of application. This id is used in the happtick
     *            configuration for scheduling.
     * @throws ActionFailedException
     */
    public void applicationIdToService(Long applicationId) throws ActionFailedException {
        if (null == this.applicationId)
            this.applicationId = applicationId;
        try {
            writeMsg(ApplicationTag.PROCESS_APPLICATION_ID);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID, String.valueOf(applicationId));
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10206L, e);
        }
    }

    /**
     * Sends start id which the start client has generated.
     * <p>
     * Processes are started by a special happtick application - the start
     * client. The start client generates a unique id for every single process.
     * When the start client starts an application he uses the calling arguments
     * to pass the start id to the main method. So the start id is part of the
     * args list and follows the argument --startId. <br>
     * For example args[0] could be --startId and args[1] 1000. <br>
     * To get the start id you can use the util argsParser of this framework.
     * E.g. String startId = argsParser.getValue("startId").
     * <p>
     * If your application is derived from this class and should be part of the
     * scheduling it is strongly recommended to use this method to send the
     * start id to the service.
     * <p>
     * If the application is not started by the start client it doesn't matter.
     * Then the happtick system knows how to handle this call.
     * 
     * @param args
     *            Parameterlist. Maybe it contains the param --startId.
     * @throws ActionFailedException
     */
    public void startIdToService(String... args) throws ActionFailedException {
        if (null != args) {
            String startId = "";
            ArgsParser argsParser = new ArgsParser(args);
            if (argsParser.containsStartsWith("--startId")) {
                startId = argsParser.getValue("startId");
            }
            if (Util.isEmpty(startId)) {
                startId = "XXX";
            }

            try {
                writeMsg(ApplicationTag.PROCESS_START_ID);
                awaitRequestAnswerImmediate(ApplicationTag.REQ_START_ID, ApplicationTag.RESP_START_ID, startId);
            } catch (ActionFailedException e) {
                throw new ActionFailedException(10206L, e);
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
     * @throws ActionFailedException
     */
    public boolean isWorkAllowed() throws ActionFailedException {
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
            throw new ActionFailedException(10201L, e);
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
     * @throws ActionFailedException
     */
    public void sendError(String errorId, String description, String level) throws ActionFailedException {
        ErrorEvent event = new ErrorEvent();
        try {
            event.addAttribute("errorId", errorId);
            event.addAttribute("description", description);
            event.addAttribute("level", level);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10202L, "Event: " + event.getClass().getSimpleName(), e);
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
     * @throws ActionFailedException
     */
    public void sendActionEvent(String eventId, String information) throws ActionFailedException {
        ActionEvent event = new ActionEvent();
        try {
            event.addAttribute("eventId", eventId);
            event.addAttribute("information", information);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10202L, "Event: " + event.getClass().getSimpleName(), e);
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
     * @throws ActionFailedException
     */
    public void sendEvent(NotEOFEvent event) throws ActionFailedException {
        if (!isWorkAllowed()) {
            throw new ActionFailedException(10208L, "Versenden eines Events.");
        }

        try {
            event.setApplicationId(this.applicationId);
            super.sendEvent(event);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(202L, "Event: " + event.getClass().getSimpleName(), e);
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
     * @throws ActionFailedException
     */
    public void sendAlarm(String type, String description, String level) throws ActionFailedException {
        AlarmEvent event = new AlarmEvent();
        try {
            event.addAttribute("type", type);
            event.addAttribute("description", description);
            event.addAttribute("level", level);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10202L, "Event: " + event.getClass().getSimpleName(), e);
        }
        sendEvent(event);
    }

    /**
     * Log informations can be visualized within the happtick monitoring tool or
     * written to log files on the server.
     * 
     * @param information
     *            Detailed Text.
     * @throws ActionFailedException
     */
    public void sendLog(String information) throws ActionFailedException {
        AlarmEvent event = new AlarmEvent();
        try {
            event.addAttribute("information", information);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10202L, "Event: " + event.getClass().getSimpleName(), e);
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
     * @throws ActionFailedException
     * @see {@link ClientObserver}
     */
    public void observeForWorkAllowance(ClientObserver clientObserver) throws ActionFailedException {
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
                while (!stopped && !isWorkAllowed()) {
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

    public String getLocalAddress() {
        return super.getTalkLine().getSocketToPartner().getInetAddress().getHostName();
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
