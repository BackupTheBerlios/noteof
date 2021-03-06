package de.happtick.core.application.service;

import de.happtick.core.MasterTable;
import de.happtick.core.enumeration.ApplicationTag;
import de.happtick.core.event.ApplicationStartedEvent;
import de.happtick.core.event.ApplicationStoppedEvent;
import de.happtick.core.event.InternalClientStarterEvent;
import de.happtick.core.service.HapptickBaseService;
import de.happtick.core.util.Scheduling;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

public class ApplicationService extends HapptickBaseService implements Service {

    private Long applicationId = new Long(-1);
    private String startId;

    private int exitCode = 0;
    private boolean clientIsActive = false;
    private boolean startedEventSent = false;
    private boolean stoppedEventSent = false;
    private boolean isInternalStartClientService = false;
    private boolean internalStopSignalSent = false;

    /**
     * Overwrite HapptickBaseService because the service mustn't be added to the
     * master table before the client has send his applicationId.
     */
    public void implementationFirstSteps() {
        addObservedEvent(EventType.INTERNAL_CLIENT_STARTER_EVENT);
        getServer().registerForEvents(this);
    }

    public void implementationLastSteps() {
        if (isInternalStartClientService) {
            if (!internalStopSignalSent) {
                try {
                    String clientHostName = super.getTalkLine().getSocketToPartner().getInetAddress().getHostName();
                    NotEOFEvent event = new InternalClientStarterEvent();
                    event.addAttribute("clientIp", clientHostName);
                    event.addAttribute("state", "STOP");
                    MasterTable.updateStartClientEvent(event);
                } catch (ActionFailedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        raiseStoppedEvent(this.applicationId, this.startId, 0);
        super.implementationLastSteps();
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

    public synchronized void processEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        if (event.equals(EventType.INTERNAL_CLIENT_STARTER_EVENT)) {
            isInternalStartClientService = true;

            if ("STOP".equalsIgnoreCase(event.getAttribute("state"))) {
                internalStopSignalSent = true;
            }
            MasterTable.updateStartClientEvent(event);
        }
    }

    /**
     * Here the service part for the communication acts between an application
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

            if (!Util.isEmpty(this.startId)) {
                raiseStartedEvent(this.applicationId, this.startId);
            }
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
            // send Stop Event
            raiseStoppedEvent(this.applicationId, this.startId, exitCode);
        }

        // START
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_WORK)) {
            this.clientIsActive = true;
            writeMsg(ApplicationTag.INFO_TRUE);
        }

        // Request for start allowance
        if (incomingMsgEnum.equals(ApplicationTag.PROCESS_START_ALLOWANCE)) {
            ApplicationTag tag = ApplicationTag.INFO_TRUE;
            if (Scheduling.mustWaitForSameApplication(getApplicationId()))
                tag = ApplicationTag.INFO_FALSE;
            responseTo(ApplicationTag.RESP_START_ALLOWED, tag.name());
        }
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

    protected void raiseStartedEvent(Long workApplicationId, String startId) {
        if (!startedEventSent) {
            NotEOFEvent event = new ApplicationStartedEvent();
            try {
                event.addAttribute("workApplicationId", String.valueOf(workApplicationId));
                event.addAttribute("startId", String.valueOf(startId));
                postEvent(event, this);
                startedEventSent = true;
            } catch (Exception e) {
                LocalLog.warn("StartEvent konnte nicht versendet werden. ApplicationId: " + applicationId);
            }
        }
    }

    protected void raiseStoppedEvent(Long workApplicationId, String startId, int exitCode) {
        if (!stoppedEventSent) {
            NotEOFEvent event = new ApplicationStoppedEvent();
            try {
                event.addAttribute("workApplicationId", String.valueOf(workApplicationId));
                event.addAttribute("serviceId", String.valueOf(this.serviceId));
                event.addAttribute("startId", String.valueOf(startId));
                event.addAttribute("exitCode", String.valueOf(exitCode));
                postEvent(event, this);
                stoppedEventSent = true;
            } catch (Exception e) {
                LocalLog.warn("StopEvent konnte nicht versendet werden. ApplicationId: " + applicationId);
            }
        }
    }
}
