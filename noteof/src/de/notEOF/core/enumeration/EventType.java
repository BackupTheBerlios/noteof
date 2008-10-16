package de.notEOF.core.enumeration;

/**
 * Defines constant event types to distinguish between fired events. Events are
 * very similar. To distinguish them this types are defined.
 * 
 * @author Dirk
 * 
 */
public enum EventType {

    /** Default if the type is not specified */
    EVENT_DEFAULT, //
    /** Alarm was raised */
    EVENT_ALARM,
    /** Error has been occured */
    EVENT_ERROR,
    /** New LOG entry is delivered with this event */
    EVENT_LOG,
    /**
     * This event tells about an action which happened before - events of this
     * type can raise another action and should be the standard.
     */
    EVENT_ACTION,
    /** START event indicates that a service (client) was started */
    EVENT_START,
    /** STOP event indicates that a service (client) was stopped */
    EVENT_STOP,
    /** If a service has changed it's state this event happens */
    EVENT_SERVICE_CHANGE,
    /**
     * This event contains a complete mail. It is used to transport mails from
     * service to client. Handling of this events is a very special feature of
     * the framework !EOF.
     */
    EVENT_MAIL,
    /**
     * This event contains another event. It is used to transport events from
     * service to client. Handling of this events is a very special feature of
     * the framework !EOF.
     */
    EVENT_EVENT,
    /**
     * Is not really an event type. This value can be set by observer services
     * if they want to be informed by any raised event.
     * 
     */
    EVENT_ANY_TYPE;
}
