package de.notEOF.core.event;

import java.util.HashMap;
import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;

public abstract class NotEOFEventBase implements NotEOFEvent {

    protected Map<String, String> attributes;
    protected Map<String, String> descriptions = new HashMap<String, String>();
    protected EventType eventType;

    // forces the derived class to initialize the internal list of descriptions.
    protected abstract void initDescriptions();

    public void addAttribute(String key, String value) throws ActionFailedException {
        initDescriptions();
        if (null == attributes)
            attributes = new HashMap<String, String>();

        // check if the key is known within the derived class
        if (null == descriptions.get(key))
            throw new ActionFailedException(1150L, key);
        attributes.put(key, value);
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public abstract EventType getEventType();

    public String getAttribute(String key) {
        if (null != attributes)
            return attributes.get(key);
        return null;
    }

    public Map<String, String> getAttributeDescriptions() {
        return descriptions;
    }

    public final void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public final void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    public final void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
