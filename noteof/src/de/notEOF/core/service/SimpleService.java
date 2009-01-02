package de.notEOF.core.service;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.enumeration.MailTag;

/**
 * This service is used when a client doesn't need a server accept for simple
 * actions like sending and receiving mails or events.
 * <p>
 * The SimpleService doesn't process client messages (messages are NOT mails or
 * events).
 * 
 * @author Dirk
 */
public class SimpleService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        return MailTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }

    /**
     * This service doesn't process any client message.
     */
    @Override
    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
    }
}
