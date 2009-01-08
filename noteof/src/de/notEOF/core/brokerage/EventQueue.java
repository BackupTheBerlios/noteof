package de.notEOF.core.brokerage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventBroker;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;

public class EventQueue implements EventBroker {
    // private static List<Object> fileObserverWaitObjects;
    private static Map<EventObserver, QueueObserver> queueObservers;
    private static long queueId = 0;
    private static List<QueueWorker> workers;
    private static boolean workerIsActive=false;

    @Override
    public void postEvent(Service service, NotEOFEvent event) {
        try {
            queueEvent(service, event);
        } catch (ActionFailedException e) {
            LocalLog.warn("Event konnte nicht in die Queue geschrieben werden.", e);
        }
    }

    private static void queueEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        // init List if null
        if (null == workers) {
            workers = new ArrayList<QueueWorker>();
        }

        // new worker thread
        QueueWorker worker = new QueueWorker(service,event);
        workers.add(worker);
        activateNextWorker();
    }
    
    private synchronized static void activateNextWorker() {
        if (!workerIsActive && null!= workers && !workers.isEmpty()) {
            new Thread(workers.remove(0)).start();
        }
    }
    
    private static class QueueWorker implements Runnable{
        private Service service;
        private NotEOFEvent event;
        
        protected QueueWorker(Service service, NotEOFEvent event) {
            
        }
        

        /*
         * Generates the queue file, stores the event in the private lists and
         * sends a signal to the observers.
         * 
         * @param service
         * 
         * @param event
         * 
         * @throws ActionFailedException
         */
        private static void createFileInformObservers(Service service, NotEOFEvent event) throws ActionFailedException {
            // The queueId must be unique. But there are threads which could
            // create
            // it simultaneously.
            // if (queueId == 0)
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

            // fileName
            String fileName = BrokerUtil.createFileName(event);
            File tempFile = new File(BrokerUtil.getQueuePath() + "/" + "temp_" + fileName);

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
                wakeUpQueueObservers();
            } catch (Exception e) {
                LocalLog.error("Event konnte nicht in die Queue geschrieben werden: " + event.getEventType(), e);
            }
        }

        /*
         * Wakes up the sleeping queue observers. They will look by themselve
         * what to do now...
         */
        private static void wakeUpQueueObservers() {
            for (QueueObserver observer : queueObservers.values()) {
                try {
                    observer.wakeUp();
                } catch (Exception e) {
                    Set<Entry<EventObserver, QueueObserver>> bla = queueObservers.entrySet();
                    Iterator<Entry<EventObserver, QueueObserver>> it = bla.iterator();
                    while (it.hasNext()) {
                        Entry<EventObserver, QueueObserver> entry = it.next();
                        if (entry.getValue().equals(observer)) {
                            queueObservers.remove(entry.getKey());
                        }
                    }
                }
            }

        }


        @Override
        public void run() {
            // semaphore
            workerIsActive=true;
            
            // createFile
            try {
                createFileInformObservers(service, event);
            } catch (ActionFailedException e) {
                LocalLog.warn("Event konnte nicht in die Queue abgestellt werden. Die Observer wurden nicht benachrichtigt.",e);
            }
            
            // activate next worker
            workerIsActive= false;
            activateNextWorker();
        }

    }

    @Override
    public void registerForEvents(EventObserver eventObserver) {
        // Generate a new Instance of the queue observer which sends new Events
        // to the event observer
        if (null == queueObservers) {
            queueObservers = new HashMap<EventObserver, QueueObserver>();
        }
        QueueObserver observer = new QueueObserver(eventObserver);
        queueObservers.put(eventObserver, observer);
        observer.acknowledge();
        new Thread(observer).start();
    }

    @Override
    public void unregisterFromEvents(EventObserver eventObserver) {
        // Stop the fileObserver which served the event observer
        try {
            QueueObserver observer = queueObservers.remove(eventObserver);
            if (null != observer) {
                System.out.println("Stoppe observer");
                observer.stop();
            }
        } catch (Exception e) {
            System.out.println("Das ist ok so!!!");
        }
    }
}
