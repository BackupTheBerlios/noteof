package de.happtick.core.events;

import de.happtick.core.interfaces.ErrorEvent;
import de.notEOF.core.enumeration.EventType;

public class ApplicationErrorEvent implements ErrorEvent {

    private Long errorId = new Long(-1);
    private int level;
    private String description;

    public ApplicationErrorEvent(Long errorId, int level, String description) {
        this.errorId = errorId;
        this.level = level;
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public Long getId() {
        return this.errorId;
    }

    public int getLevel() {
        return this.level;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Long errorId) {
        this.errorId = errorId;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public EventType getEventType() {
        return EventType.EVENT_ERROR;
    }

}
