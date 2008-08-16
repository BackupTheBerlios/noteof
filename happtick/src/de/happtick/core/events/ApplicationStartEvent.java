package de.happtick.core.events;

import java.util.Date;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.StartEvent;

public class ApplicationStartEvent implements StartEvent {

    private Long applicationId = new Long(-1);
    private Date startDate = new Date();

    public ApplicationStartEvent(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public EventType getEventType() {
        return EventType.EVENT_START;
    }

    @Override
    public Date getStartDate() {
        return this.startDate;
    }

}
