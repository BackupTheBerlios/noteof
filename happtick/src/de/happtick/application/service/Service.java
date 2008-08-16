package de.happtick.application.service;

import de.happtick.core.application.service.ApplicationService;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.events.ApplicationAlarmEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.ClientEvent;

/**
 * This class is the standard (base class) implementation for application
 * service.
 * <p>
 * If there are special custom requirements it can be overwritten and adapted.
 * 
 * @author Dirk
 * 
 */
public class Service extends ApplicationService {

    /**
     * Sample how the basic service can be overwritten to use own messages...
     */
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        super.processMsg(incomingMsgEnum);

        if (incomingMsgEnum.equals(ApplicationTag.REQ_SAMPLE_TAG)) {
            @SuppressWarnings("unused")
            String id = requestTo(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID);
            ClientEvent event = new ApplicationAlarmEvent();
            eventObserver.update(event);
        }
    }

}
