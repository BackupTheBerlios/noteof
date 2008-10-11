package de.happtick.mail.service;

import de.notEOF.mail.service.MailEventService;

public class HapptickMailEventService extends MailEventService {

    @Override
    protected boolean interestedInMail(String destination, String header) {
        System.out.println("Interested in Mail? " + destination + "; " + header);
        return super.interestedInMail(destination, header);
    }
}
