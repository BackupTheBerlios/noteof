package de.notEOF.core.event;

import java.util.Date;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.StopEvent;

public class ServiceStopEvent implements StopEvent {

    private Date stopDate = new Date();
    private String serviceId;

    public ServiceStopEvent(String serviceId) {
        this.serviceId = serviceId;
    }

    public EventType getEventType() {
        return EventType.EVENT_STOP;
    }

    public Date getStopDate() {
        return this.stopDate;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }
}
