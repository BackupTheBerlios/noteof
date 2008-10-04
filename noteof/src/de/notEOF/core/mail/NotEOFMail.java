package de.notEOF.core.mail;

import java.util.Date;

import de.notEOF.core.interfaces.Service;

public class NotEOFMail {
    private String body;
    private String mailId;
    private String fromServiceId;
    private String toServiceId;
    private String destination;
    private Date generated;

    /**
     * @param body
     *            The content of the message.
     * @param fromServiceId
     *            The service id is unique during the server runtime.
     * @param toServiceId
     *            The 'address' of the recipient. May be NULL if it is not
     *            known. If the value of toServiceId is NULL the server and the
     *            services try to find out one or more recipients by the
     *            destination string value.
     * @param destination
     */
    public NotEOFMail(String body, Service fromService, String toServiceId, String destination) {
        this.body = body;
        this.fromServiceId = fromService.getServiceId();
        this.toServiceId = toServiceId;
        this.destination = destination;
        this.generated = new Date();
        this.mailId = String.valueOf(new Date().getTime()) + fromServiceId;
        this.generated = new Date();
    }

    public NotEOFMail() {

    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFromServiceId() {
        return fromServiceId;
    }

    public void setFromServiceId(String fromServiceId) {
        this.fromServiceId = fromServiceId;
    }

    public String getToServiceId() {
        return toServiceId;
    }

    public void setToServiceId(String toServiceId) {
        this.toServiceId = toServiceId;
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

    public String getMailId() {
        return this.mailId;
    }
}
