package de.happtick.core.application.service;

import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.events.ApplicationAlarmEvent;
import de.happtick.core.interfaces.AlarmEvent;
import de.happtick.core.interfaces.EventObserver;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;
import de.notEOF.core.util.Util;

public class ApplicationService extends BaseService {

    protected EventObserver eventObserver;

    /**
     * Delivers the communication tag class which client and service use.
     */
    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    /**
     * Indicates whether the LifeSignSystem is active for this service and its
     * clients.
     */
    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }

    /**
     * Register an observer which will be notified if an alarm has released.
     * 
     * @param observer
     *            An Object which implements the ServiceObserver.
     */
    public void registerForEvents(EventObserver eventObserver) {
        this.eventObserver = eventObserver;
    }

    /**
     * Here service part of the communication acts between an application client
     * and an application service is implemented.
     */
    @Override
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        if (null == eventObserver)
            throw new ActionFailedException(60L, "Observer für Events ist NULL");

        // Application Id
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_APPLICATION_ID)) {
            Long applicationId = new Long(requestTo(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID));
        }

        // LOG event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_LOG)) {

        }

        // ERROR event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ERROR)) {

        }

        // ALARM event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ALARM)) {
            int type = Util.parseInt(requestTo(ApplicationTag.REQ_ALARM_TYPE, ApplicationTag.RESP_ALARM_TYPE), -1);
            int level = Util.parseInt(requestTo(ApplicationTag.REQ_ALARM_LEVEL, ApplicationTag.RESP_ALARM_LEVEL), -1);
            String text = requestTo(ApplicationTag.REQ_ALARM_TEXT, ApplicationTag.RESP_ALARM_TEXT);
            AlarmEvent event = new ApplicationAlarmEvent(type, level, text);
            eventObserver.update(event);
        }

        // EVENT event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_EVENT)) {

        }

        // Request for start allowance
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ALLOWANCE)) {

        }
    }
}
