package de.notEOF.core.brokerage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
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
import de.notEOF.core.util.Statistics;

public class EventQueue implements EventBroker {
    // private static List<Object> fileObserverWaitObjects;
    private static Map<EventObserver, QueueObserver> queueObservers;
    private static long queueId = 0;
    private static List<Job> jobs;
    private static Object waitObject = new Object();

    static {
        // Generate a new Instance of the queue observer which sends new Events
        // to the event observer
        queueObservers = new HashMap<EventObserver, QueueObserver>();
        jobs = new ArrayList<Job>();
        new Thread(new QueuePoster()).start();
        Statistics.setEventWaitTime(0);
    }

    @Override
    public void postEvent(Service service, NotEOFEvent event) {
        // try {
        Job job = new Job(service, event);
        updateJobs(job);
        synchronized (waitObject) {
            waitObject.notify();
        }
        // queueEvent(service, event);
        // } catch (ActionFailedException e) {
        // LocalLog.warn("Event konnte nicht in die Queue geschrieben werden.",
        // e);
        // }
    }

    private synchronized static void queueEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        // new worker thread
        createFileInformObservers(service, event);
    }

    private static synchronized void updateQueueObservers(EventObserver eventObserver, QueueObserver queueObserver) {
        if (null == queueObserver) {
            queueObservers.remove(eventObserver);
        } else {
            queueObservers.put(eventObserver, queueObserver);
        }
    }

    private static synchronized void updateJobs(Job job) {
        if (null == job) {
            jobs.remove(0);
            Statistics.addFinishedEvent();
        } else {
            jobs.add(job);
            Statistics.addNewEvent();
        }
    }

    private static class Job {
        private Service service;
        private NotEOFEvent event;

        protected Job(Service service, NotEOFEvent event) {
            this.service = service;
            this.event = event;
        }

        protected Service getService() {
            return this.service;
        }

        protected NotEOFEvent getEvent() {
            return this.event;
        }
    }

    private static class QueuePoster implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (waitObject) {
                        waitObject.wait();
                    }
                    while (jobs.size() > 0) {
                        queueEvent(jobs.get(0).getService(), jobs.get(0).getEvent());
                        updateJobs(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /*
     * Generates the queue file, stores the event in the private lists and sends
     * a signal to the observers.
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
        // File tempFile = new File(BrokerUtil.getQueuePath() + "/" + "temp_" +
        // fileName);

        try {
            File tempFile = File.createTempFile(fileName, ".hap");

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
            bWriter.flush();
            bWriter.close();

            File origFile = new File(BrokerUtil.getQueuePath() + "/" + fileName);
            tempFile.renameTo(origFile);
            while (!origFile.exists())
                ;

            EventQueueReader.update(event);
            wakeUpQueueObservers();
        } catch (Exception e) {
            LocalLog.error("Event konnte nicht in die Queue geschrieben werden: " + event.getEventType(), e);
        }
    }

    /*
     * Wakes up the sleeping queue observers. They will look by themselve what
     * to do now...
     */
    private static void wakeUpQueueObservers() {
        Collection<QueueObserver> collection = queueObservers.values();
        for (QueueObserver observer : collection) {
            try {
                observer.wakeUp();
            } catch (Exception e) {
                Set<Entry<EventObserver, QueueObserver>> set = queueObservers.entrySet();
                Iterator<Entry<EventObserver, QueueObserver>> it = set.iterator();
                while (it.hasNext()) {
                    Entry<EventObserver, QueueObserver> entry = it.next();
                    if (entry.getValue().equals(observer)) {
                        updateQueueObservers(entry.getKey(), null);
                    }
                }
            }
        }

    }

    @Override
    public void registerForEvents(EventObserver eventObserver, Long lastReceivedQueueId) {
        QueueObserver queueObserver = new QueueObserver(eventObserver);
        updateQueueObservers(eventObserver, queueObserver);
        if (null == lastReceivedQueueId || lastReceivedQueueId >= 0) {
            queueObserver.acknowledge();
        } else {
            queueObserver.offset(lastReceivedQueueId);
        }
        new Thread(queueObserver).start();
    }

    @Override
    public void unregisterFromEvents(EventObserver eventObserver) {
        // Stop the fileObserver which served the event observer
        try {
            QueueObserver queueObserver = queueObservers.get(eventObserver);
            if (null != queueObserver) {
                System.out.println("Stoppe observer");
                updateQueueObservers(eventObserver, null);
                queueObserver.stop();
            }
        } catch (Exception e) {
            System.out.println("Das ist ok so!!!");
        }
    }

    private static List<EventObserver> theObservers() {
        if (null == queueObservers)
            return null;

        List<EventObserver> observers = new ArrayList<EventObserver>();
        Set<EventObserver> keySet = queueObservers.keySet();
        for (EventObserver observer : keySet) {
            observers.add(observer);
        }
        return observers;
    }

    @Override
    public List<EventObserver> getEventObservers() {
        return theObservers();
    }
}
