package de.notEOF.core.event;

import java.util.Date;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.StartEvent;

public class ServiceStartEvent implements StartEvent {

    private Date startDate = new Date();
    private String serviceId;

    public ServiceStartEvent(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public EventType getEventType() {
        return EventType.EVENT_START;
    }

    @Override
    public Date getStartDate() {
        return this.startDate;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

}
