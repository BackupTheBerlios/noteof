package de.notEOF.core.brokerage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Set;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventBroker;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;

public class EventQueueWriter implements EventBroker {
    private static long queueId = 0;

    @Override
    public void postEvent(Service service, NotEOFEvent event) {
        try {
            queueEvent(service, event);
        } catch (ActionFailedException e) {
            LocalLog.warn("Event konnte nicht in die Queue geschrieben werden.", e);
        }
    }

    private static void queueEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        // fileName
        String fileName = BrokerUtil.createFileName(event);

        // createFile
        createFile(fileName, service, event);
    }

    private static void createFile(String fileName, Service service, NotEOFEvent event) throws ActionFailedException {
        File tempFile = new File(BrokerUtil.getQueuePath() + "/" + "temp_" + fileName);

        if (queueId == 0)
            queueId = new Date().getTime() - 1;

        while (new Date().getTime() <= queueId) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        queueId = new Date().getTime();

        // Here the event gets his unique identifier
        event.setQueueId(queueId);

        try {
            FileWriter writer = new FileWriter(tempFile.getAbsoluteFile());
            BufferedWriter bWriter = new BufferedWriter(writer);

            bWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bWriter.newLine();
            bWriter.newLine();
            bWriter.write("<root>");

            // service
            String className = "";
            String serviceId = "";
            String clientNetId = "";
            if (null != service) {
                className = service.getClass().getSimpleName();
                serviceId = service.getServiceId();
                clientNetId = service.getClientNetId();
            }
            bWriter.newLine();
            bWriter.write("    <Service name=\"" + className + "\" serviceId=\"" + serviceId + "\" clientNetId=\"" + clientNetId + "\"></Service>");
            bWriter.newLine();

            // eventType
            bWriter.write("    <Type name=\"" + event.getEventType() + "\"></Type>");
            bWriter.newLine();

            // all attributes of the event
            Set<String> keys = event.getAttributes().keySet();
            for (String key : keys) {
                String value = event.getAttribute(key);
                String desc = event.getAttributeDescriptions().get(key);
                bWriter.write("    <Attribute name=\"" + key + "\" value=\"" + value + "\" description=\"" + desc + "\"></Attribute>");
                bWriter.newLine();
            }
            bWriter.write("</root>");
            bWriter.close();
            File origFile = new File(BrokerUtil.getQueuePath() + "/" + fileName);
            tempFile.renameTo(origFile);
            EventQueueReader.update(event);
        } catch (Exception e) {
            LocalLog.error("Event konnte nicht in die Queue geschrieben werden: " + event.getEventType(), e);
        }
    }
}
