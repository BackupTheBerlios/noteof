package de.notEOF.mail;

import java.util.Date;
import java.util.Random;

import de.notEOF.core.communication.DataObject;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notIOC.util.Util;

public class NotEOFMail {
    private String bodyText;
    private DataObject bodyData;
    private Service fromService;
    private String toClientNetId;
    private String fromClientNetId;
    private String header;
    private String mailId;
    private String destination;
    private Date generated;

    /**
     * @param header
     *            The header of the mail.
     * @param bodyText
     *            The content of the message as simple String.
     * @param fromServiceId
     *            The service id is unique during the server runtime.
     * @param destination
     *            The 'address' of the recipient. May not be NULL. The services
     *            by themselve check the destination information if they are
     *            interested in this mail.
     */
    public NotEOFMail(String header, String destination, String bodyText, Service fromService) throws ActionFailedException {
        if (Util.isEmpty(bodyText))
            throw new ActionFailedException(1100L, "Body Text ist leer.");
        this.bodyText = bodyText;
        initMail(header, fromService, destination);
    }

    /**
     * @param header
     *            The header of the mail.
     * @param bodyText
     *            The content of the message as simple String.
     * @param destination
     *            The 'address' of the recipient. May not be NULL. The services
     *            by themselve check the destination information if they are
     *            interested in this mail.
     */
    public NotEOFMail(String header, String destination, String bodyText) throws ActionFailedException {
        if (Util.isEmpty(bodyText))
            throw new ActionFailedException(1100L, "Body Text ist leer.");
        this.bodyText = bodyText;
        initMail(header, null, destination);
    }

    /**
     * @param header
     *            The header of the mail.
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
        // if (null == fromService)
        // throw new ActionFailedException(1100L, "fromService ist NULL.");
        // if (Util.isEmpty(destination))
        // throw new ActionFailedException(1100L, "destination ist NULL.");

        Random rd = new Random();
        rd.nextInt();

        this.setHeader(header);
        this.fromService = fromService;
        this.destination = destination;
        this.generated = new Date();
        this.mailId = String.valueOf(new Date().getTime()) + Math.abs(new Random().nextInt());
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
        if (null == header)
            return "";
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
        if (null == bodyText)
            return "";
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public Service getFromService() {
        return fromService;
    }

    public void setFromService(Service fromService) {
        this.fromService = fromService;
    }

    public String getToClientNetId() {
        if (null == toClientNetId)
            return "";
        return toClientNetId;
    }

    /**
     * If clientNetId is known it can be set here to make it safer that the
     * client gets the post.
     * 
     * @param toClientNetId
     */
    public void setToClientNetId(String toClientNetId) {
        this.toClientNetId = toClientNetId;
    }

    public String getFromClientNetId() {
        if (null == fromClientNetId)
            return "";
        return fromClientNetId;
    }

    public void setFromClientNetId(String fromClientNetId) {
        this.fromClientNetId = fromClientNetId;
    }

    public String getDestination() {
        if (null == destination)
            return "";
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

    /**
     * Shouldn't be used outside of the framework!
     * 
     * @param generated
     */
    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    public String getMailId() {
        return this.mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }
}
