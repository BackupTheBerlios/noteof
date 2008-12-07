package de.happdemo;

import de.happtick.core.event.HapptickEvent;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.NotEOFEvent;

public class SoundEvent extends HapptickEvent implements NotEOFEvent {

    @Override
    protected void initDescriptions() {
        descriptions.put("soundFile", "Path of SoundFile.");
        descriptions.put("state", "State: start, playing; played.");
        descriptions.put("delay", "Time to wait in millis.");
    }

    @Override
    protected void initEventType() {
        this.eventType = EventType.EVENT_SOUND;
    }
}
