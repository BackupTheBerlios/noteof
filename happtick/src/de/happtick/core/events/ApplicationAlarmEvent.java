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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public EventType getEventType() {
        // TODO Auto-generated method stub
        return EventType.EVENT_ALARM;
    }
}
