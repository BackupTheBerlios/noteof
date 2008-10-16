package de.happtick.application.service;

import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.events.AlarmEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.server.Server;

/**
 * This class is the standard (base class) implementation for application
 * service.
 * <p>
 * If there are special custom requirements it can be overwritten and adapted.
 * 
 * @author Dirk
 * 
 */
public class ServiceBla extends ApplicationService {

    /**
     * Sample how the basic service can be overwritten to use own messages...
     */
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        super.processClientMsg(incomingMsgEnum);

        if (incomingMsgEnum.equals(ApplicationTag.REQ_SAMPLE_TAG)) {
            @SuppressWarnings("unused")
            String id = requestTo(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID);
            NotEOFEvent event = new AlarmEvent();
            Server.getInstance().updateObservers(this, event);
        }
    }

}
