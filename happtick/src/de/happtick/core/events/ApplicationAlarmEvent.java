package de.happtick.core.events;

import de.happtick.core.interfaces.AlarmEvent;
import de.notEOF.core.enumeration.EventType;

public class ApplicationAlarmEvent implements AlarmEvent {

    private String description;
    private int type;
    private int level;

    public ApplicationAlarmEvent() {
    }

    public ApplicationAlarmEvent(int type, int level, String description) {
        setType(type);
        setLevel(level);
        setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public int getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setType(int type) {
        this.type = type;
    }

    public EventType getEventType() {
        // TODO Auto-generated method stub
        return EventType.EVENT_ALARM;
    }
}
