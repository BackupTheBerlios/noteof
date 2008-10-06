package de.happtick.mail.service;

import java.util.List;

import de.notEOF.mail.service.MailEventService;

public class HapptickMailEventService extends MailEventService {
    private List<String> interestingDestinations;

    public void addInterestingDestinations(String destinations) {
        if (null == interestingDestinations) interestingDestinations = new List<String >;
    }

    @Override
    protected boolean interestedInMail(String destination, String header) {
        // TODO Auto-generated method stub
        return false;
    }

}
