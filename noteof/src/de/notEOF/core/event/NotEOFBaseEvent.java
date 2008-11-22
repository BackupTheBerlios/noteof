package de.notEOF.core.event;

import java.util.HashMap;
import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.Util;

/**
 * Basic Event class for send fast messages through the net.
 * <p>
 * Any Event class should be extended from this AND should implement the
 * Interface NotEOFEvent. <br>
 * For more details of the methods have a look to the interface.
 * 
 * @see NotEOFEvent
 * 
 * @author Dirk
 * 
 */
public abstract class NotEOFBaseEvent implements NotEOFEvent {

    public static EventType EVENT_TYPE;
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

    public EventType getEventType() {
        return eventType;
    }

    public String getAttribute(String key) {
        if (null != attributes) {
            // String value = attributes.get(key);
            // if (null == value)
            // throw new ActionFailedException(1155L, key);
            return attributes.get(key);
        }
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

    public void setQueueId(Long queueId) {
        try {
            this.descriptions.put("internal->queueId", "basic attribute 'queueId' of !EOF");
            addAttribute("internal->queueId", String.valueOf(queueId));
        } catch (ActionFailedException e) {
            e.printStackTrace();
        }
    }

    public Long getQueueId() {
        return Util.parseLong(getAttribute("internal->queueId"), 0);
    }

    public void setRequestQueueId(Long queueId) {
        try {
            this.descriptions.put("internal->queueResponseId", "basic attribute 'queueResponseId' of !EOF");
            addAttribute("internal->queueResponseId", String.valueOf(queueId));
        } catch (ActionFailedException e) {
            e.printStackTrace();
        }
    }

    public Long getRequestQueueId() {
        return Util.parseLong(getAttribute("internal->queueResponseId"), 0);
    }

    /**
     * Most Events are related to an application.
     * <p>
     * This method can be used to transport the applicationId. <br>
     * 
     * @param applicationId
     *            Id of application
     */
    public void setApplicationId(Long applicationId) {
        try {
            this.descriptions.put("internal->applicationId", "basic attribute 'applicationId' of !EOF");
            addAttribute("internal->applicationId", String.valueOf(applicationId));
        } catch (ActionFailedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Most Events are related to an application.
     * <p>
     * This method delivers the applicationId if was set before the event was
     * raised. <br>
     * 
     * @return application id or NULL
     */
    public Long getApplicationId() {
        return Util.parseLong(getAttribute("internal->applicationId"), -1);
    }

}
