package de.happtick.core.application.service;

import java.util.ArrayList;
import java.util.List;

import de.happtick.core.MasterTable;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.events.ApplicationAlarmEvent;
import de.happtick.core.events.ApplicationErrorEvent;
import de.happtick.core.events.ApplicationEventEvent;
import de.happtick.core.events.ApplicationLogEvent;
import de.happtick.core.events.ApplicationStartEvent;
import de.happtick.core.events.ApplicationStopEvent;
import de.happtick.core.interfaces.AlarmEvent;
import de.happtick.core.interfaces.ErrorEvent;
import de.happtick.core.interfaces.EventEvent;
import de.happtick.core.interfaces.LogEvent;
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
    private EventEvent lastEventEvent;
    private LogEvent lastLogEvent;
    private ApplicationStopEvent stopEvent;
    private ApplicationStartEvent startEvent;

    private int exitCode = 0;

    /**
     * This method is called by BaseService directly when the connection with client is established.
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
     * Indicates whether the LifeSignSystem is active for this service and its clients.
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
            return lastEventEvent;
        if (eventType.equals(EventType.EVENT_LOG))
            return lastLogEvent;
        if (eventType.equals(EventType.EVENT_START))
            return startEvent;
        if (eventType.equals(EventType.EVENT_STOP))
            return stopEvent;
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
        if (event.getClass().equals(EventEvent.class)) {
            lastEventEvent = (EventEvent) event;
        }
        if (event.getClass().equals(LogEvent.class)) {
            lastLogEvent = (LogEvent) event;
        }
        if (event.getClass().equals(ApplicationStopEvent.class)) {
            stopEvent = (ApplicationStopEvent) event;
        }
        if (event.getClass().equals(ApplicationStartEvent.class)) {
            startEvent = (ApplicationStartEvent) event;
        }
        Server.getInstance().updateObservers(this, event);
    }

    /**
     * Here service part of the communication acts between an application client and an application service is implemented.
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
            updateEvent(new ApplicationLogEvent(text));
        }

        // ERROR event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ERROR)) {
            Long id = Util.parseLong(requestTo(ApplicationTag.REQ_ERROR_ID, ApplicationTag.RESP_ERROR_ID), -1);
            int level = Util.parseInt(requestTo(ApplicationTag.REQ_ERROR_LEVEL, ApplicationTag.RESP_ERROR_LEVEL), -1);
            String description = requestTo(ApplicationTag.REQ_ERROR_TEXT, ApplicationTag.RESP_ERROR_TEXT);
            updateEvent(new ApplicationErrorEvent(id, level, description));
        }

        // ALARM event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ALARM)) {
            int type = Util.parseInt(requestTo(ApplicationTag.REQ_ALARM_TYPE, ApplicationTag.RESP_ALARM_TYPE), -1);
            int level = Util.parseInt(requestTo(ApplicationTag.REQ_ALARM_LEVEL, ApplicationTag.RESP_ALARM_LEVEL), -1);
            String text = requestTo(ApplicationTag.REQ_ALARM_TEXT, ApplicationTag.RESP_ALARM_TEXT);
            updateEvent(new ApplicationAlarmEvent(type, level, text));
        }

        // EVENT event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_EVENT)) {
            Long id = Util.parseLong(requestTo(ApplicationTag.REQ_EVENT_ID, ApplicationTag.RESP_EVENT_ID), -1);
            String information = requestTo(ApplicationTag.REQ_EVENT_TEXT, ApplicationTag.RESP_EVENT_TEXT);
            updateEvent(new ApplicationEventEvent(id, information));
        }

        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_STOP_EVENT)) {
            this.exitCode = Util.parseInt(requestTo(ApplicationTag.REQ_EXIT_CODE, ApplicationTag.RESP_EXIT_CODE), -1);
            updateEvent(new ApplicationStopEvent(this.getServiceId(), this.getApplicationId(), exitCode));
            writeMsg(ApplicationTag.INFO_TRUE);
        }

        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_WORK_EVENT)) {
            updateEvent(new ApplicationStartEvent(this.getServiceId(), this.getApplicationId()));
        }

        // Request for start allowance
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ALLOWANCE)) {
            // TODO Wird wohl mit MasterTable oder scheduler ausgehandelt...
        }
    }

    public List<EventType> getObservedEvents() {
        List<EventType> observedTypes = new ArrayList<EventType>();
        observedTypes.add(EventType.EVENT_ALL_TYPES);
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
