package de.happtick.core.application.service;

import java.util.ArrayList;
import java.util.List;

import de.happtick.core.MasterTable;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.service.HapptickBaseService;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

public class ApplicationService extends HapptickBaseService {

    private Long applicationId = new Long(-1);
    private String startId;
    // private AlarmEvent lastAlarmEvent;
    // private ErrorEvent lastErrorEvent;
    // private ActionEvent lastActionEvent;
    // private LogEvent lastLogEvent;
    // private StoppedEvent stoppedEvent;
    // private StartedEvent startedEvent;

    private int exitCode = 0;
    private boolean clientIsActive = false;

    /**
     * Overwrite HapptickBaseService because the service mustn't be added to the
     * master table before the client has send his applicationId.
     */
    public void implementationFirstSteps() {
        // don't delete this method...
    }

    /**
     * Delivers the communication tag class which client and service use.
     */
    @Override
    public Class<?> getCommunicationTagClass() {
        return ApplicationTag.class;
    }

    /**
     * Delivers the unique application id.
     * 
     * @return Id which is hard coded within the application class.
     */
    public Long getApplicationId() {
        return this.applicationId;
    }

    /**
     * The exit code is the 'result' of the client.
     * 
     * @return The code which the application has sent before it stopped.
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * Indicates whether the LifeSignSystem is active for this service and its
     * clients.
     */
    @Override
    public boolean isLifeSignSystemActive() {
        return false;
    }

    /**
     * Here the service part of the communication acts between an application
     * client and an application service is implemented.
     */
    @Override
    public void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException {
        // Application Id
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_APPLICATION_ID)) {
            Long applicationId = new Long(requestTo(ApplicationTag.REQ_APPLICATION_ID, ApplicationTag.RESP_APPLICATION_ID));
            this.applicationId = applicationId;
            // now it is a good time point to register at master tables
            MasterTable.addService(this);
        }

        // Start Id given by StartClient
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ID)) {
            String startId = requestTo(ApplicationTag.REQ_START_ID, ApplicationTag.RESP_START_ID);
            this.startId = startId;
        }

        // STOP
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_STOP_WORK)) {
            this.exitCode = Util.parseInt(requestTo(ApplicationTag.REQ_EXIT_CODE, ApplicationTag.RESP_EXIT_CODE), -1);
            this.clientIsActive = false;
            writeMsg(ApplicationTag.INFO_TRUE);
        }

        // START
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_WORK)) {
            System.out.println("ApplicationService: incomingMsg = START_...");
            this.clientIsActive = true;
            writeMsg(ApplicationTag.INFO_TRUE);
        }

        // Request for start allowance
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ALLOWANCE)) {
            // TODO Wird wohl mit MasterTable oder scheduler ausgehandelt...
        }
    }

    public List<EventType> getObservedEvents() {
        List<EventType> observedTypes = new ArrayList<EventType>();
        observedTypes.add(EventType.EVENT_ANY_TYPE);
        return observedTypes;
    }

    /**
     * Delivers the start id which is generated by StartClient on client host.
     * 
     * @return The generated start id or a String with length 0.
     */
    public String getStartId() {
        return startId;
    }

    /**
     * Shows if the client has started or finished his activities.
     * 
     * @return TRUE after client start and before client stopped.
     */
    public boolean isClientActive() {
        return this.clientIsActive;
    }
}
