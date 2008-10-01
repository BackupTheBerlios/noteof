package de.happtick.configuration;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.util.Util;

public class EventAction {
    private Long id;
    private String actionType;
    private String applicationType;

    public EventAction(String node, NotEOFConfiguration conf) throws ActionFailedException {
        // sample node: scheduler.events.event1.
        id = Util.parseLong(conf.getAttribute(node, "id"), -1);
        applicationType = conf.getAttribute(node, "applicationType");
        actionType = conf.getAttribute(node, "actionType"); // start or stop
    }

    public EventAction(Long id, String applicationType, String actionType) {
        this.id = id;
        this.applicationType = applicationType;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the application type
     */
    public String getApplicationType() {
        return applicationType;
    }

    /**
     * @return the action type
     */
    public String getActionType() {
        return actionType;
    }
}
