package de.notEOF.core.mail;

import java.util.Date;

import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notIOC.util.Util;

public class NotEOFMail {
    private String bodyText;
    private DataObject bodyData;
    private String header;
    private String requestMailId;
    private String fromServiceId;
    private String destination;
    private Date generated;
    private String responseMailId;

    /**
     * @param bodyText
     *            The content of the message as simple String.
     * @param fromServiceId
     *            The service id is unique during the server runtime.
     * @param destination
     *            The 'address' of the recipient. May not be NULL. The services
     *            by themselve check the destination information if they are
     *            interested in this mail.
     */
    public NotEOFMail(String header, String bodyText, Service fromService, String destination) throws ActionFailedException {
        if (Util.isEmpty(bodyText))
            throw new ActionFailedException(1100L, "Body Text ist leer.");
        this.bodyText = bodyText;
        initMail(header, fromService, destination);
    }

    /**
     * @param bodyData
     *            The content of the message as complex DataObject.
     * @param fromServiceId
     *            The service id is unique during the server runtime.
     * @param toServiceId
     *            The 'address' of the recipient. May not be NULL.
     */
    public NotEOFMail(String header, DataObject bodyData, Service fromService, String toServiceId) throws ActionFailedException {
        if (null == bodyData)
            throw new ActionFailedException(1100L, "Body DataObject ist NULL.");
        this.setBodyData(bodyData);
        initMail(header, fromService, toServiceId);
    }

    public NotEOFMail() {

    }

    private void initMail(String header, Service fromService, String destination) throws ActionFailedException {
        if (null == fromService)
            throw new ActionFailedException(1100L, "fromService ist NULL.");
        if (Util.isEmpty(destination))
            throw new ActionFailedException(1100L, "destination ist NULL.");

        this.setHeader(header);
        this.fromServiceId = fromService.getServiceId();
        this.destination = destination;
        this.generated = new Date();
        this.requestMailId = String.valueOf(new Date().getTime()) + fromServiceId;
        this.generated = new Date();
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param bodyData
     *            the bodyData to set
     */
    public void setBodyData(DataObject bodyData) {
        this.bodyData = bodyData;
    }

    /**
     * @return the bodyData
     */
    public DataObject getBodyData() {
        return bodyData;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getFromServiceId() {
        return fromServiceId;
    }

    public void setFromServiceId(String fromServiceId) {
        this.fromServiceId = fromServiceId;
    }

    public String getDestination() {
        return destination;
    }

    /**
     * Destination can be any string. It's meaning depends to the way of use.
     * 
     * @param destination
     *            Any String to identifiy the recipient.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getGenerated() {
        return generated;
    }

    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    public String getRequestMailId() {
        return this.requestMailId;
    }

    /**
     * @param responseMailId
     *            the responseMailId to set
     */
    public void setResponseMailId(String responseMailId) {
        this.responseMailId = responseMailId;
    }

    /**
     * @return the responseMailId
     */
    public String getResponseMailId() {
        return responseMailId;
    }
}
