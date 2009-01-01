package de.happtick.core.start.client;

import de.happtick.core.event.ApplicationStartErrorEvent;
import de.happtick.core.util.ExternalCalls;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

public class HapptickApplicationStarter {

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
    public HapptickApplicationStarter(StartClient startClient, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent)
            throws ActionFailedException {

        Starter starter = new Starter(startClient, serverAddress, serverPort, startId, startEvent);
        new Thread(starter).start();
    }

    private class Starter implements Runnable {
        private StartClient startClient;
        private String serverAddress;
        private int serverPort;
        private String startId;
        private NotEOFEvent startEvent;

        protected Starter(StartClient startClient, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent) {
            this.startClient = startClient;
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.startId = startId;
            this.startEvent = startEvent;
        }

        public void run() {
            String applicationId = null;
            String applicationPath = null;
            applicationId = String.valueOf(this.startEvent.getAttribute("workApplicationId"));
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
                System.out.println("HapptickApplicationStarter!!!!!!!!!!!   STARTET  111111111111111");
                calls.startApplication(serverAddress, serverPort, startId, startEvent);

                System.out.println("HapptickApplicationStarter!!!!!!!!!!!   HAT GESTARTET  2222222222");

                started = true;
            } catch (ActionFailedException he) {
                errNo = he.getErrNo();
                errMsg = he.getMessage();
                th = he;
            } catch (Throwable e) {
                errMsg = e.getMessage();
                th = e;
            }

            if (!started) {
                LocalLog.error("Applikation konnte nicht gestartet werden: " + applicationPath, th);
                try {
                    ApplicationStartErrorEvent errorEvent = new ApplicationStartErrorEvent();
                    errorEvent.setApplicationId(Util.parseLong(startEvent.getAttribute("workApplicationId"), -1));
                    errorEvent.addAttribute("clientNetId", this.startClient.getClientNetId());
                    errorEvent.addAttribute("startId", startId);
                    errorEvent.addAttribute("errorDescription", errMsg);
                    errorEvent.addAttribute("errorId", String.valueOf(errNo));
                    errorEvent.addAttribute("errorLevel", "0");
                    errorEvent.addAttribute("startIgnitionTime", String.valueOf(startEvent.getTimeStampSend()));
                    // main class now will send the event to the server
                    startClient.sendEvent(errorEvent);
                } catch (Exception e) {
                    LocalLog.warn("StartErrorEvent konnte nicht versendet werden.", e);
                }
            }
        }
    }
}
