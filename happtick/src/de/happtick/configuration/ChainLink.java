package de.happtick.configuration;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.util.Util;

public class ChainLink {
    private Long id;
    private String applicationType;
    private Long conditionEventId;
    private Long preventEventId;
    private boolean skip;

    public ChainLink(String node, NotEOFConfiguration conf) throws ActionFailedException {
        // sample node: scheduler.chains.chain1.link0
        id = Util.parseLong(conf.getAttribute(node, "id"), -1);
        applicationType = conf.getAttribute(node, "applicationType");
        conditionEventId = Util.parseLong(conf.getAttribute(node, "conditionEventId"), -1);
        preventEventId = Util.parseLong(conf.getAttribute(node, "preventEventId"), -1);
        skip = Util.parseBoolean(conf.getAttribute(node, "skip"), false);
    }

    public ChainLink(Long id, String applicationType, Long conditionEventId, Long preventEventId, boolean skip) {
        this.id = id;
        this.applicationType = applicationType;
        this.conditionEventId = conditionEventId;
        this.preventEventId = preventEventId;
        this.skip = skip;
    }

    /**
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * @return the preventEventId
     */
    public Long getPreventEventId() {
        if ((null != preventEventId) && (-1 != preventEventId))
            return preventEventId;
        return null;
    }

    /**
     * @return the conditionEventId
     */
    public Long getConditionEventId() {
        if ((null != conditionEventId) && (-1 != conditionEventId))
            return conditionEventId;
        return null;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the type
     */
    public String getApplicationType() {
        return applicationType;
    }
}
