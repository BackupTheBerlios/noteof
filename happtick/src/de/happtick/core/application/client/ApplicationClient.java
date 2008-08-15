package de.happtick.core.application.client;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.interfaces.AlarmEvent;
import de.notEOF.core.client.BaseClient;
import de.notEOF.core.exception.ActionFailedException;

/**
 * This client has less business logic and more connection / communication
 * logic. <br>
 * The business logic is implemented in the Client class.
 * 
 * @see HapptickApplication
 * @author dirk
 * 
 */
public class ApplicationClient extends BaseClient {

    private boolean isWorkAllowed = false;

    @Override
    public Class<?> serviceForClientByClass() {
        return ApplicationService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }

    /**
     * Send id of application to the service.
     * 
     * @param applicationId
     *            Unique id of application.
     * @throws HapptickException
     */
    public void setApplicationId(Long applicationId) throws HapptickException {
        try {
            // inform service that an requests for start allowance will follow
            writeMsg(ApplicationTag.PROCESS_APPLICATION_ID);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID, String.valueOf(applicationId));
        } catch (ActionFailedException e) {
            throw new HapptickException(206L, e);
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
    public void setError(long errorId, int level, String errorDescription) throws HapptickException {
        // inform service that a new error will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_ERROR);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ERROR_ID, ApplicationTag.RESP_ERROR_ID, String.valueOf(errorId));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ERROR_LEVEL, ApplicationTag.RESP_ERROR_LEVEL, String.valueOf(level));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_ERROR_TEXT, ApplicationTag.RESP_ERROR_TEXT, errorDescription);
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
     * @param eventId
     *            An event id which is the link to the configuration.
     * @param additionalInformation
     *            Informations which can be used at another place (e.g. the
     *            happtick monitoring).
     * @throws HapptickException
     */
    public void setEvent(int eventId, String additionalInformation) throws HapptickException {
        // inform service that a new event will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_EVENT);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_EVENT_ID, ApplicationTag.RESP_EVENT_ID, String.valueOf(eventId));
            awaitRequestAnswerImmediate(ApplicationTag.REQ_EVENT_TEXT, ApplicationTag.RESP_EVENT_TEXT, additionalInformation);
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
     *            The fired event initialized with alarm data.
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
     * @param logInformation
     *            Variable informations, depending to the applications job.
     * @throws HapptickException
     */
    public void setLog(String logInformation) throws HapptickException {
        // inform service that a new alarm will follow
        try {
            writeMsg(ApplicationTag.PROCESS_NEW_LOG);
            awaitRequestAnswerImmediate(ApplicationTag.REQ_LOG_TEXT, ApplicationTag.RESP_LOG_TEXT, logInformation);
        } catch (ActionFailedException e) {
            throw new HapptickException(205L, e);
        }
    }
}
