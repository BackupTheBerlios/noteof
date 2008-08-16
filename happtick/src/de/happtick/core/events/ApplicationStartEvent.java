package de.happtick.core.events;

import de.notEOF.core.event.ServiceStartEvent;

public class ApplicationStartEvent extends ServiceStartEvent {

    private Long applicationId = new Long(-1);

    public ApplicationStartEvent(String serviceId, Long applicationId) {
        super(serviceId);
        this.applicationId = applicationId;
    }

    public Long getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }
}
