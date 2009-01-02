package de.notEOF.tool;

import de.notEOF.core.client.BaseClient;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.SystemInfoEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFClient;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.service.SimpleService;
import de.notEOF.core.util.ArgsParser;
import de.notEOF.core.util.Util;
import de.notEOF.mail.interfaces.EventRecipient;

public class SystemInfoClient extends BaseClient implements NotEOFClient, EventRecipient {

    public SystemInfoClient(String ip, int port, String... args) throws ActionFailedException {
        super(ip, port, null, args);
        init();
    }

    private void init() throws ActionFailedException {
        try {
            addInterestingEvent(new SystemInfoEvent());
            startAcceptingEvents();
        } catch (ActionFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Jetzt gilts!");
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public synchronized void processEvent(NotEOFEvent event) {
        System.out.println("SystemInfoClient.processEvent. Es ist was angekommen.");
        try {
            if (null != event && EventType.EVENT_SYSTEM_INFO.equals(event.getEventType())) {
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("=======================================================================");
                System.out.println("Sum Services:                  " + event.getAttribute("Server:Counter:SumServices"));
                System.out.println("Active Services:               " + event.getAttribute("Server:Counter:ActiveServices"));
                System.out.println("Finished Services:             " + event.getAttribute("Server:Counter:FinishedServices"));
                System.out.println("Sum Events:                    " + event.getAttribute("Util:Counter:SumEvents"));
                System.out.println("Completed Events:              " + event.getAttribute("Util:Counter:CompletedEvents"));
                System.out.println("Active Event Threads:          " + event.getAttribute("Util:Counter:ActiveEventThreads"));
                System.out.println("Sum Event Threads:             " + event.getAttribute("Util:Counter:SumEventThreads"));
                System.out.println("Completed Event Threads:       " + event.getAttribute("Util:Counter:CompletedEventThreads"));
                System.out.println("Last Event Processing Time:    " + event.getAttribute("Util:State:LastEventProcessingTime"));
                System.out.println("Average Event Processing Time: " + event.getAttribute("Util:State:AvgEventProcessingTime"));
                System.out.println("Maximum Event Processing Time: " + event.getAttribute("Util:State:MaxEventProcessingTime"));
                System.out.println("=======================================================================");
            }
        } catch (Exception e) {
            LocalLog.error("Fehler bei Verarbeitung des Events.", e);
        }
    }

    public void processEventException(Exception exception) {
        super.reconnect();
    }

    @Override
    public Class<?> serviceForClientByClass() {
        return SimpleService.class;
    }

    @Override
    public String serviceForClientByName() {
        return null;
    }

    @Override
    public String getServerAddress() {
        return super.getPartnerHostAddress();
    }

    @Override
    public int getServerPort() {
        return super.getPartnerPort();
    }

    public static void main(String... args) throws ActionFailedException {
        String ip = "localhost";
        int port = 5000;

        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.containsStartsWith("--serverIp")) {
            ip = argsParser.getValue("serverIp");
        }
        if (argsParser.containsStartsWith("--serverPort")) {
            port = Util.parseInt(argsParser.getValue("serverPort"), 5000);
        }
        System.out.println("ip = " + ip + "; port = " + port);
        new SystemInfoClient(ip, port, args);

    }
}
