package de.happtick.configuration;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.util.Util;

public class ChainLink {
    private Long linkId;
    private Long addresseeId;
    private String addresseeType;
    private Long conditionEventId;
    private String conditionKey;
    private String conditionValue;
    private Long preventEventId;
    private String preventKey;
    private String preventValue;
    private boolean skip;

    public ChainLink(String node, NotEOFConfiguration conf) throws ActionFailedException {
        // sample node: scheduler.chains.chain1.link0
        linkId = Util.parseLong(conf.getAttribute(node, "linkId"), -1);
        addresseeId = (Util.parseLong(conf.getAttribute(node, "addresseId"), -1));
        addresseeType = conf.getAttribute(node, "addresseeType");
        conditionEventId = Util.parseLong(conf.getAttribute(node, "conditionEventId"), -1);
        conditionKey = conf.getAttribute(node, "conditionKey");
        conditionValue = conf.getAttribute(node, "conditionValue");
        preventEventId = Util.parseLong(conf.getAttribute(node, "preventEventId"), -1);
        preventKey = conf.getAttribute(node, "preventKey");
        preventValue = conf.getAttribute(node, "preventValue");
        skip = Util.parseBoolean(conf.getAttribute(node, "skip"), false);
    }

    public ChainLink(Long linkId, Long addresseId, String addresseeType, Long conditionEventId, String conditionKey, String conditionValue,
            Long preventEventId, String preventKey, String preventValue, boolean skip) {
        this.linkId = linkId;
        this.addresseeId = (addresseId);
        this.addresseeType = addresseeType;
        this.conditionEventId = conditionEventId;
        this.conditionKey = conditionKey;
        this.conditionValue = conditionValue;
        this.preventEventId = preventEventId;
        this.preventKey = preventKey;
        this.preventValue = preventValue;
        this.skip = skip;
    }

    /**
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * @return the preventEventId. Is an event id.
     */
    public Long getPreventEventId() {
        if ((null != preventEventId) && (-1 != preventEventId))
            return preventEventId;
        return null;
    }

    /**
     * @return the conditionEventId. Is an event id.
     */
    public Long getConditionEventId() {
        if ((null != conditionEventId) && (-1 != conditionEventId))
            return conditionEventId;
        return null;
    }

    /**
     * @return the id of this link
     */
    public Long getLinkId() {
        return linkId;
    }

    /**
     * @return the type
     */
    public String getAddresseeType() {
        return addresseeType;
    }

    /**
     * @param conditionKey
     *            the conditionKey to set
     */
    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    /**
     * @return the conditionKey
     */
    public String getConditionKey() {
        return conditionKey;
    }

    /**
     * @param conditionValue
     *            the conditionValue to set
     */
    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    /**
     * @return the conditionValue
     */
    public String getConditionValue() {
        return conditionValue;
    }

    /**
     * @param preventKey
     *            the preventKey to set
     */
    public void setPreventKey(String preventKey) {
        this.preventKey = preventKey;
    }

    /**
     * @return the preventKey
     */
    public String getPreventKey() {
        return preventKey;
    }

    /**
     * @param preventValue
     *            the preventValue to set
     */
    public void setPreventValue(String preventValue) {
        this.preventValue = preventValue;
    }

    /**
     * @return the preventValue
     */
    public String getPreventValue() {
        return preventValue;
    }

    /**
     * @param addresseId
     *            the addresseId to set
     */
    public void setAddresseeId(Long addresseId) {
        this.addresseeId = addresseId;
    }

    /**
     * @return the addresseId
     */
    public Long getAddresseeId() {
        return addresseeId;
    }
}
