package de.happtick.core.service;

import de.happtick.core.enumeration.ApplicationTag;
import de.notEOF.configuration.enumeration.ConfigurationTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.BaseService;

public class ApplicationService extends BaseService {

    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    @Override
    public boolean isLifeSignSystemActive() {
        return true;
    }
    
    @Override
    public void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        // Application Id
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_APPLICATION_ID)) {
            
        }
        
        // LOG event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_LOG)) {
            
        }
        
        // ERROR event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ERROR)) {
            
        }
        
        // ALARM event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_ALARM)) {
            
        }
        
        // EVENT event
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_NEW_EVENT)) {
            
        }
        
        // Request for start allowance
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ALLOWANCE)) {
            
        }
    }
}
