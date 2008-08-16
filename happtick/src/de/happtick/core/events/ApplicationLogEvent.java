package de.happtick.core.events;

import de.happtick.core.interfaces.LogEvent;
import de.notEOF.core.enumeration.EventType;

public class ApplicationLogEvent implements LogEvent {

    private String logText;

    public ApplicationLogEvent(String logText) {
        this.logText = logText;
    }

    @Override
    public String getText() {
        return this.logText;
    }

    @Override
    public void setText(String logText) {
        this.logText = logText;
    }

    @Override
    public EventType getEventType() {
        return EventType.EVENT_LOG;
    }

}
