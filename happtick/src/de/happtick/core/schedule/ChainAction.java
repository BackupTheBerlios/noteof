package de.happtick.core.schedule;

public class ChainAction {

    private String action;
    private boolean skip;
    private String addresseeType;
    private Long addresseeId;

    public ChainAction(String action, String addresseeType, Long addresseeId, boolean skip) {
        this.setAction(action);
        this.setSkip(skip);
        this.setAddresseeType(addresseeType);
        this.setAddresseeId(addresseeId);
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

    /**
     * @param skip
     *            the skip to set
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
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
     * @param addresseeId
     *            the addresseeId to set
     */
    public void setAddresseeId(Long addresseeId) {
        this.addresseeId = addresseeId;
    }

    /**
     * @return the addresseeId
     */
    public Long getAddresseeId() {
        return addresseeId;
    }
}
