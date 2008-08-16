package de.happtick.core.events;

import java.util.Date;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.StopEvent;

public class ApplicationStopEvent implements StopEvent {

    private Date stopDate = new Date();
    private Long applicationId = new Long(-1);
    private int exitCode;

    public ApplicationStopEvent(Long applicationId, int exitCode) {
        this.applicationId = applicationId;
        this.exitCode = exitCode;
    }

    public Long getApplicationId() {
        return this.applicationId;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public EventType getEventType() {
        return EventType.EVENT_STOP;
    }

    @Override
    public Date getStopDate() {
        return this.stopDate;
    }

}
