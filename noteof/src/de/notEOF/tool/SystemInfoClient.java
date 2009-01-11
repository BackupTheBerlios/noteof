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
                System.out.println("=============================================");
                System.out.println("SERVICES");
                System.out.println("    Active:          " + event.getAttribute("Counter:ActiveServices"));
                System.out.println("    Finished:        " + event.getAttribute("Counter:FinishedServices"));
                System.out.println("    Sum:             " + event.getAttribute("Counter:SumServices"));
                System.out.println("EVENTS");
                System.out.println("    In Work:         " + event.getAttribute("Counter:ActiveEvents"));
                System.out.println("    Completed:       " + event.getAttribute("Counter:CompletedEvents"));
                System.out.println("    Sum:             " + event.getAttribute("Counter:SumEvents"));
                System.out.println("EVENT THREADS");
                System.out.println("    Pending:         " + event.getAttribute("Counter:ActiveEventThreads"));
                System.out.println("    Finished:        " + event.getAttribute("Counter:CompletedEventThreads"));
                System.out.println("    Sum:             " + event.getAttribute("Counter:SumEventThreads"));
                System.out.println("  Processing Times");
                System.out.println("    Last Event:      " + event.getAttribute("State:LastEventProcessingTime"));
                System.out.println("    Average:         " + event.getAttribute("State:AvgEventProcessingTime"));
                System.out.println("    Maximum:         " + event.getAttribute("State:MaxEventProcessingTime"));
                System.out.println("  Internal Tuning");
                System.out.println("    Buffer Time:     " + event.getAttribute("State:DispatcherWaitTime"));
                System.out.println("OBSERVERS");
                System.out.println("    Sum              " + event.getAttribute("Counter:Observers"));
                System.out.println("=============================================");
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
