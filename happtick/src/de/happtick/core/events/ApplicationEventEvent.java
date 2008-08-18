package de.happtick.core.events;

import de.happtick.core.interfaces.EventEvent;
import de.notEOF.core.enumeration.EventType;

public class ApplicationEventEvent implements EventEvent {

    private String information;
    private Long eventId = new Long(-1);

    public ApplicationEventEvent(Long id, String information) {
        this.eventId = id;
        this.information = information;
    }

    public Long getId() {
        return this.eventId;
    }

    public String getInformation() {
        return this.information;
    }

    public void setId(Long eventId) {
        this.eventId = eventId;
    }

    public void setInfomation(String information) {
        this.information = information;
    }

    public EventType getEventType() {
        return EventType.EVENT_EVENT;
    }

}