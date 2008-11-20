package de.happtick.configuration;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

/**
 * Represents the configuration of an event which is stored in the configuration
 * file.
 * 
 * @author Dirk
 * 
 */
public class EventConfiguration {

    private Long eventId;
    private String nodeNameEvent;
    private String eventClassName;
    private String keyName;
    private String keyValue;
    private Long applicationId;
    private Long addresseeId;
    private String addresseeType;
    private String action;

    /**
     * Simple constructor
     */
    public EventConfiguration(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * Using this constructor the class fills up itself with data by reading the
     * configuration.
     * 
     * @param nodeNameEvent
     *            The xml path of the events configuration (e.g.
     *            scheduler.events.event1).
     * @param conf
     *            Object for reading access to the configuration
     */
    public EventConfiguration(String nodeNameEvent, NotEOFConfiguration conf) throws ActionFailedException {
        try {
            this.nodeNameEvent = nodeNameEvent;

            String node = "";

            // eventId
            // scheduler.events.chain1
            node = "scheduler.events." + nodeNameEvent;
            eventId = Util.parseLong(conf.getAttribute(node, "eventId"), -1);
            // eventClassName
            eventClassName = conf.getAttribute(node, "eventClassName");
            // keyName
            keyName = conf.getAttribute(node, "keyName");
            // keyValue
            keyValue = conf.getAttribute(node, "keyValue");
            // applicationId (maybe null)
            applicationId = Util.parseLong(conf.getAttribute(node, "applicationId"), -1);
            // addresseeId (maybe null)
            addresseeId = Util.parseLong(conf.getAttribute(node, "addresseeId"), -1);
            // addresseeType (maybe null)
            addresseeType = conf.getAttribute(node, "addresseeType");
            // action (maybe null)
            action = conf.getAttribute(node, "action");

        } catch (Exception ex) {
            LocalLog.error("Konfiguration der Events konnte nicht fehlerfrei gelesen werden. Event: " + nodeNameEvent, ex);
            throw new ActionFailedException(401, "Initialisierung EventConfiguration", ex);
        }
    }

    public Long getEventId() {
        return eventId;
    }

    public String getNodeNameEvent() {
        return nodeNameEvent;
    }

    public void setNodeNameEvent(String nodeNameEvent) {
        this.nodeNameEvent = nodeNameEvent;
    }

    public String getEventClassName() {
        return eventClassName;
    }

    public void setEventClassName(String eventClassName) {
        this.eventClassName = eventClassName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getApplicationId() {
        return this.applicationId;
    }

    public void setAddresseeId(Long addresseeId) {
        this.addresseeId = addresseeId;
    }

    public Long getAddresseeId() {
        return this.addresseeId;
    }

    /**
     * @param addresseeType
     *            the addresseeType to set
     */
    public void setAddresseeType(String addresseeType) {
        this.addresseeType = addresseeType;
    }

    /**
     * @return the addresseeType
     */
    public String getAddresseeType() {
        return addresseeType;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }
}
