package de.notEOF.mail.client;

import java.util.Date;
import java.util.Map;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.communication.DataObject;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;
import de.notEOF.mail.NotEOFMail;
import de.notEOF.mail.enumeration.MailTag;
import de.notEOF.mail.interfaces.MailEventRecipient;

public class MailEventClient extends BaseClient {

    @Override
    public Class<?> serviceForClientByClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serviceForClientByName() {
        return "de.notEOF.mail.service.MailEventService";
    }

    public void awaitMailEvent(MailEventRecipient recipient) throws ActionFailedException {
        // wake up!
        awaitRequestAnswerImmediate(MailTag.REQ_READY_FOR_MAIL, MailTag.RESP_READY_FOR_MAIL, BaseCommTag.VAL_OK.name());

        NotEOFMail mail = new NotEOFMail();
        if (BaseCommTag.VAL_TRUE.name().equals(requestTo(MailTag.REQ_MAIL_ENVELOPE, MailTag.RESP_MAIL_ENVELOPE))) {
            DataObject envelopeObject = receiveDataObject();
            Map<String, String> envelope = envelopeObject.getMap();

            mail.setToClientNetId(envelope.get("toClientNetId"));
            mail.setHeader(envelope.get("header"));
            mail.setMailId(envelope.get("mailId"));
            mail.setDestination(envelope.get("destination"));

            Date generated = new Date();
            Long dateAsLong = Util.parseLong(envelope.get("generated"), 0);
            generated.setTime(dateAsLong);
            mail.setGenerated(generated);
        }

        // body text
        String bodyText = requestTo(MailTag.REQ_BODY_TEXT, MailTag.RESP_BODY_TEXT);
        mail.setBodyText(bodyText);

        // body data
        if (BaseCommTag.VAL_TRUE.name().equals(requestTo(MailTag.REQ_BODY_DATA_EXISTS, MailTag.RESP_BODY_DATA_EXISTS))) {
            DataObject bodyData = receiveDataObject();
            mail.setBodyData(bodyData);
        }

        recipient.processMail(mail);
    }
}
