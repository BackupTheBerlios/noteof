package de.happtick.core.application.service;

import java.util.ArrayList;
import java.util.List;

import de.happtick.core.MasterTable;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.events.ActionEvent;
import de.happtick.core.events.AlarmEvent;
import de.happtick.core.events.ErrorEvent;
import de.happtick.core.events.LogEvent;
import de.happtick.core.events.StartedEvent;
import de.happtick.core.events.StoppedEvent;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.server.Server;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.util.Util;

public class ApplicationService extends BaseService {

    private Long applicationId = new Long(-1);
    private String startId;
    private AlarmEvent lastAlarmEvent;
    private ErrorEvent lastErrorEvent;
    private ActionEvent lastActionEvent;
    private LogEvent lastLogEvent;
    private StoppedEvent stoppedEvent;
    private StartedEvent startedEvent;

    private int exitCode = 0;

    /**
     * This method is called by BaseService directly when the connection with
     * client is established.
     */
    public void implementationFirstSteps() {
        MasterTable.addService(this);
    }

    /**
     * Delivers the communication tag class which client and service use.
     */
    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    /**
     * Delivers the unique application id.
     * 
     * @return Id which is hard coded within the application class.
     */
    public Long getApplicationId() {
        return this.applicationId;
    }

    /**
     * The exit code is the 'result' of the client.
     * 
     * @return The code which the application has sent before it stopped.
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * Indicates whether the LifeSignSystem is active for this service and its
     * clients.
     */
    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

    /**
     * Returns the last from client fired event
     * 
     * @param eventType
     * @return
     */
    public NotEOFEvent getLastEvent(EventType eventType) {
        if (eventType.equals(EventType.EVENT_ALARM))
            return lastAlarmEvent;
        if (eventType.equals(EventType.EVENT_ERROR))
            return lastErrorEvent;
        if (eventType.equals(EventType.EVENT_EVENT))
            return lastActionEvent;
        if (eventType.equals(EventType.EVENT_LOG))
            return lastLogEvent;
        if (eventType.equals(EventType.EVENT_CLIENT_STARTED))
            return startedEvent;
        if (eventType.equals(EventType.EVENT_CLIENT_STOPPED))
            return stoppedEvent;
        return null;
    }

    /*
     * Set last event of client
     */
    private void updateEvent(NotEOFEvent event) {
        if (event.getClass().equals(AlarmEvent.class)) {
            lastAlarmEvent = (AlarmEvent) event;
        }
        if (event.getClass().equals(ErrorEvent.class)) {
            lastErrorEvent = (ErrorEvent) event;
        }
        if (event.getClass().equals(ActionEvent.class)) {
            lastActionEvent = (ActionEvent) event;
        }
        if (event.getClass().equals(LogEvent.class)) {
            lastLogEvent = (LogEvent) event;
        }
        if (event.getClass().equals(StoppedEvent.class)) {
            stoppedEvent = (StoppedEvent) event;
        }
        if (event.getClass().equals(StartedEvent.class)) {
            startedEvent = (StartedEvent) event;
        }
        Server.getInstance().updateObservers(this, event);
    }

    /**
     * Here the service part of the communication acts between an application
     * client and an application service is implemented.
     */
    @Override
    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        // Application Id
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_APPLICATION_ID)) {
            Long applicationId = new Long(requestTo(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID));
            this.applicationId = applicationId;
        }

        // Start Id given by StartClient
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ID)) {
            String startId = requestTo(ApplicationTag.REQ_START_ID, ApplicationTag.RESP_START_ID);
            this.startId = startId;
        }

        // LOG event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_LOG)) {
            String text = requestTo(ApplicationTag.REQ_LOG_TEXT, ApplicationTag.RESP_LOG_TEXT);
            NotEOFEvent event = new LogEvent();
            event.addAttribute("information", text);
            updateEvent(event);
        }

        // ERROR event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ERROR)) {
            String id = requestTo(ApplicationTag.REQ_ERROR_ID, ApplicationTag.RESP_ERROR_ID);
            String level = requestTo(ApplicationTag.REQ_ERROR_LEVEL, ApplicationTag.RESP_ERROR_LEVEL);
            String description = requestTo(ApplicationTag.REQ_ERROR_TEXT, ApplicationTag.RESP_ERROR_TEXT);
            NotEOFEvent event = new ErrorEvent();
            event.addAttribute("description", description);
            event.addAttribute("errorId", id);
            event.addAttribute("level", level);
            updateEvent(event);
        }

        // ALARM event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ALARM)) {
            String type = requestTo(ApplicationTag.REQ_ALARM_TYPE, ApplicationTag.RESP_ALARM_TYPE);
            String level = requestTo(ApplicationTag.REQ_ALARM_LEVEL, ApplicationTag.RESP_ALARM_LEVEL);
            String text = requestTo(ApplicationTag.REQ_ALARM_TEXT, ApplicationTag.RESP_ALARM_TEXT);
            NotEOFEvent event = new AlarmEvent();
            event.addAttribute("description", text);
            event.addAttribute("level", level);
            event.addAttribute("type", type);
            updateEvent(event);
        }

        // ACTION event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_EVENT)) {
            String id = requestTo(ApplicationTag.REQ_EVENT_ID, ApplicationTag.RESP_EVENT_ID);
            String information = requestTo(ApplicationTag.REQ_EVENT_TEXT, ApplicationTag.RESP_EVENT_TEXT);
            NotEOFEvent event = new ActionEvent();
            event.addAttribute("information", information);
            event.addAttribute("eventId", id);
            updateEvent(event);
        }

        // STOP event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_STOP_EVENT)) {
            this.exitCode = Util.parseInt(requestTo(ApplicationTag.REQ_EXIT_CODE, ApplicationTag.RESP_EXIT_CODE), -1);
            NotEOFEvent event = new StoppedEvent();
            event.addAttribute("applicationId", String.valueOf(this.applicationId));
            event.addAttribute("clientNetId", String.valueOf(super.getClientNetId()));
            event.addAttribute("startId", this.startId);
            event.addAttribute("exitCode", String.valueOf(exitCode));
            updateEvent(event);
            writeMsg(ApplicationTag.INFO_TRUE);
        }

        // START event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_WORK_EVENT)) {
            NotEOFEvent event = new StartedEvent();
            event.addAttribute("applicationId", String.valueOf(this.applicationId));
            event.addAttribute("clientNetId", String.valueOf(super.getClientNetId()));
            event.addAttribute("startId", this.startId);
            updateEvent(event);
        }

        // Request for start allowance
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ALLOWANCE)) {
            // TODO Wird wohl mit MasterTable oder scheduler ausgehandelt...
        }
    }

    public List<EventType> getObservedEvents() {
        List<EventType> observedTypes = new ArrayList<EventType>();
        observedTypes.add(EventType.EVENT_ANY_TYPE);
        return observedTypes;
    }

    /**
     * Delivers the start id which is generated by StartClient on client host.
     * 
     * @return The generated start id or a String with length 0.
     */
    public String getStartId() {
        return startId;
    }
}
