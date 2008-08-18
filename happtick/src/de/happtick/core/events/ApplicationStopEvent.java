package de.happtick.core.events;

import de.notEOF.core.event.ServiceStopEvent;


public class ApplicationStopEvent extends ServiceStopEvent {

    private Long applicationId = new Long(-1);
    private int exitCode;

    public ApplicationStopEvent(String serviceId, Long applicationId, int exitCode) {
        super(serviceId);
        this.applicationId = applicationId;
        this.exitCode = exitCode;
    }

    public Long getApplicationId() {
        return this.applicationId;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
}
