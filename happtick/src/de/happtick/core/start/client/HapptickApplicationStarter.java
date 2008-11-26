package de.happtick.core.start.client;

import de.happtick.core.event.ApplicationStartErrorEvent;
import de.happtick.core.exception.HapptickException;
import de.happtick.core.util.ExternalCalls;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

public class HapptickApplicationStarter {
    NotEOFClient notEOFClient;

    /**
     * 
     * @param startId
     *            Unique ID for the application (used by Master Table and
     *            Scheduler)
     * @param startEvent
     *            Contains the start parameters of the application
     * @param notEOFClient
     *            Used for communication acts between client and service (send
     *            events, mails, ...)
     */
    public HapptickApplicationStarter(NotEOFClient notEOFClient, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent)
            throws HapptickException {

        Starter starter = new Starter(notEOFClient, serverAddress, serverPort, startId, startEvent);
        Thread workerThread = new Thread(starter);
        workerThread.start();
    }

    private class Starter implements Runnable {
        private NotEOFClient notEOFClient;
        private String serverAddress;
        private int serverPort;
        private String startId;
        private NotEOFEvent startEvent;

        protected Starter(NotEOFClient notEOFClient, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent) {
            this.notEOFClient = notEOFClient;
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.startId = startId;
            this.startEvent = startEvent;
        }

        public void run() {
            String applicationId = null;
            String applicationPath = null;
            applicationId = this.startEvent.getAttribute("applicationId");
            applicationPath = this.startEvent.getAttribute("applicationPath");

            if (Util.isEmpty(applicationId))
                LocalLog.error("Fehler bei Starten einer Anwendung. Fehlender Wert im Start-Event: applicationId");
            if (Util.isEmpty(applicationPath))
                LocalLog.error("Fehler bei Starten einer Anwendung. Fehlender Wert im Start-Event: applicationPath");

            boolean started = false;
            String errMsg = "";
            long errNo = 0;
            Throwable th = null;

            try {
                ExternalCalls calls = new ExternalCalls();
                calls.startHapptickApplication(serverAddress, serverPort, startId, startEvent);
                started = true;
            } catch (HapptickException he) {
                errNo = he.getErrNo();
                errMsg = he.getMessage();
                th = he;
            } catch (Throwable e) {
                errMsg = e.getMessage();
                th = e;
            }

            if (!started) {
                LocalLog.error("Applikation konnte nicht gestartet werden.", th);
                try {
                    ApplicationStartErrorEvent errorEvent = new ApplicationStartErrorEvent();
                    errorEvent.setApplicationId(startEvent.getApplicationId());
                    errorEvent.addAttribute("applicationId", String.valueOf(startEvent.getAttribute("applicationId")));
                    errorEvent.addAttribute("clientNetId", this.notEOFClient.getClientNetId());
                    errorEvent.addAttribute("startId", startId);
                    errorEvent.addAttribute("errorDescription", errMsg);
                    errorEvent.addAttribute("errorId", String.valueOf(errNo));
                    errorEvent.addAttribute("errorLevel", "0");
                    // main class now will send the event to the server
                    notEOFClient.sendEvent(errorEvent);
                } catch (Exception e) {
                    LocalLog.warn("StartErrorEvent konnte nicht versendet werden.", e);
                }
            }
        }
    }
}
