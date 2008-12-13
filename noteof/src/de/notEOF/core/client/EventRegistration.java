package de.notEOF.core.client;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;

public class EventRegistration {

    public EventRegistration(TalkLine talkLine, String clientNetId) throws ActionFailedException {
        Registration registration = new Registration();
        try {
            registration.register(talkLine, clientNetId);
        } catch (Exception ex) {
            throw new ActionFailedException(22L, ex);
        }
    }

    /*
     * Inner class (perhaps later runs in an own thread).
     */
    private class Registration {

        // Register at the server and ask for a service
        protected void register(TalkLine talkLine, String clientNetId) throws ActionFailedException {
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_CLIENT_TYPE, BaseCommTag.RESP_CLIENT_TYPE, "RECEIVE_CLIENT");
            talkLine.awaitRequestAnswerImmediate(BaseCommTag.REQ_CLIENT_ID, BaseCommTag.RESP_CLIENT_ID, clientNetId);
        }
    }
}
