package de.happtick.configuration;

/**
 * Stores configuration of events
 * 
 * @author Dirk eventClassName="ApplicationStopEvent" startApplicationId="1"
 *         startChainId="1"></event>
 */
public class EventConfiguration {

    private Long eventId;
    private Long firedByApplicationId;
    private String eventClassName;
    private Long startApplicationId;
    private Long startChainId;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getFiredByApplicationId() {
        return firedByApplicationId;
    }

    public void setFiredByApplicationId(Long firedByApplicationId) {
        this.firedByApplicationId = firedByApplicationId;
    }

    public String getEventClassName() {
        return eventClassName;
    }

    public void setEventClassName(String eventClassName) {
        this.eventClassName = eventClassName;
    }

    public Long getStartApplicationId() {
        return startApplicationId;
    }

    public void setStartApplicationId(Long startApplicationId) {
        this.startApplicationId = startApplicationId;
    }

    public Long getStartChainId() {
        return startChainId;
    }

    public void setStartChainId(Long startChainId) {
        this.startChainId = startChainId;
    }

}
