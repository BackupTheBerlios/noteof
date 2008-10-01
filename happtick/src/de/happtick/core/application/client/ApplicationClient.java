package de.happtick.core.application.client;

import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.AlarmEvent;
import de.happtick.core.interfaces.ClientObserver;
import de.happtick.core.interfaces.ErrorEvent;
import de.happtick.core.interfaces.EventEvent;
import de.happtick.core.interfaces.LogEvent;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;

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
public class ApplicationClient extends BaseClient {

    private boolean isWorkAllowed = false;
    private AllowanceWaiter allowanceWaiter;
    private ClientObserver clientObserver;

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
            writeMsg(ApplicationTag.PROCESS_STOP_EVENT);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_EXIT_CODE, ApplicationTag.RESP_EXIT_CODE, String.valueOf(exitCode));
            // give service a little bit time...
            // TODO Was ist das für eine Zahl?
            readMsgTimedOut(7654);
            super.close();
        } catch (ActionFailedException e) {
            throw new HapptickException(207L, e);
        }
    }

    /**
     * Send event to service that the client has started his work.
     * 
     * @throws HapptickException
     */
    public void startWork() throws HapptickException {
        try {
            writeMsg(ApplicationTag.PROCESS_START_WORK_EVENT);
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
    public void setApplicationId(Long applicationId) throws HapptickException {
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
    public void setStartId(String... args) throws HapptickException {
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
        // only when start allowance not given yet service must be asked for
        if (!isWorkAllowed) {
            try {
                // inform service that an requests for start allowance will
                // follow
                writeMsg(ApplicationTag.PROCESS_START_ALLOWANCE);
                isWorkAllowed = ApplicationTag.INFO_TRUE.name().equals(requestTo(ApplicationTag.REQ_START_ALLOWED, ApplicationTag.RESP_START_ALLOWED));
            } catch (ActionFailedException e) {
                throw new HapptickException(201L, e);
            }
        }
        return isWorkAllowed;
    }

    /**
     * Errors can be shown within the happtick monitoring tool or written to
     * logfiles.
     * <p>
     * Errors don't release events.
     * 
     * @param id
     *            The error identifier
     * @param level
     *            Error level.
     * @param errorDescription
     *            Additional information for solving the problem.
     * @throws HapptickException
     */
    public void sendError(ErrorEvent event) throws HapptickException {
        // inform service that a new error will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_ERROR);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ERROR_ID, ApplicationTag.RESP_ERROR_ID, String.valueOf(event.getId()));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ERROR_LEVEL, ApplicationTag.RESP_ERROR_LEVEL, String.valueOf(event.getLevel()));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ERROR_TEXT, ApplicationTag.RESP_ERROR_TEXT, event.getDescription());
        } catch (ActionFailedException e) {
            throw new HapptickException(202L, e);
        }
    }

    /**
     * Happtick is able to react to events. There are standard events like start
     * and stop of application. The relations between them are configurable.
     * Supplemental events and actions can be configured for single
     * applications.
     * 
     * @param event
     *            Object of Type EventEvent
     * @throws HapptickException
     */
    public void sendEvent(EventEvent event) throws HapptickException {
        // inform service that a new event will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_EVENT);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_EVENT_ID, ApplicationTag.RESP_EVENT_ID, String.valueOf(event.getId()));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_EVENT_TEXT, ApplicationTag.RESP_EVENT_TEXT, event.getInformation());
        } catch (ActionFailedException e) {
            throw new HapptickException(203L, e);
        }
    }

    /**
     * Releases an alert. <br>
     * Like errors alarms can have a level. The controlling alarm system of
     * happtick decides what to do depending to the alarm level.
     * 
     * @param alarm
     *            The fired event of type AlarmEvent.
     * @throws HapptickException
     */
    public void sendAlarm(AlarmEvent alarm) throws HapptickException {
        // inform service that a new alarm will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_ALARM);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ALARM_TYPE, ApplicationTag.RESP_ALARM_TYPE, String.valueOf(alarm.getType()));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ALARM_LEVEL, ApplicationTag.RESP_ALARM_LEVEL, String.valueOf(alarm.getLevel()));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ALARM_TEXT, ApplicationTag.RESP_ALARM_TEXT, alarm.getDescription());
        } catch (ActionFailedException e) {
            throw new HapptickException(204L, e);
        }
    }

    /**
     * Log informations can be visualized within the happtick monitoring tool or
     * written to log files on the server.
     * 
     * @param log
     *            Implementation of LogEvent.
     * @throws HapptickException
     */
    public void sendLog(LogEvent log) throws HapptickException {
        // inform service that a new alarm will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_LOG);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_LOG_TEXT, ApplicationTag.RESP_LOG_TEXT, log.getText());
        } catch (ActionFailedException e) {
            throw new HapptickException(205L, e);
        }
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
}
