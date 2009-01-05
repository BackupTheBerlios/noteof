package de.notEOF.core.brokerage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.EventBroker;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Statistics;

public class EventBroadcaster implements EventBroker{
    private static boolean updatingObservers = false;
    private static boolean registeringObserver = false;
    private static long queueId = 0;
    protected static Map<String, EventObserver> eventObservers;
    private static final long MAX_OBSERVER_WAIT_TIME = 60000;
    private static UpdateObserver updateObserver;

    private static EventBuffer eventBuffer;

    public  void postEvent(Service service, NotEOFEvent event) {
        if (null == eventBuffer) {
            eventBuffer = new EventBuffer();
            new Thread(eventBuffer).start();
        }
        Statistics.addNewEvent();
        eventBuffer.process(service, event);
    }

    /**
     * Fires an event to all registered Observer.
     * <p>
     * Precondition for getting information on observer side is to initialize
     * the observed event list.
     * 
     * @param eventObservers
     *            List which contains all observers.
     * @param service
     *            The Observable which fires the event.
     * @param event
     *            Implementation of Type ClientEvent.
     */
    public static void updateAllObserver(Service service, NotEOFEvent event) {
        if (null == event) {
            return;
        }
        if (null == eventObservers) {
            return;
        }

        if (null == updateObserver) {
            updateObserver = new UpdateObserver();
            new Thread(updateObserver).start();
        }

        // set Timestamp if empty
        event.setTimeStampSend();

        while (registeringObserver) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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

        // updatingObservers = true;

        // all observer
        if (eventObservers.size() > 0) {
            Set<String> set = eventObservers.keySet();
            for (String observerName : set) {
                // but only inform observer, when event in his list
                EventObserver eventObserver = eventObservers.get(observerName);
                if (null != eventObserver && null != eventObserver.getObservedEvents()) {
                    for (EventType type : eventObserver.getObservedEvents()) {
                        if (type.equals(EventType.EVENT_ANY_TYPE) || type.equals(event.getEventType())
                                && !EventType.EVENT_SYSTEM_INFO.equals(event.getEventType())) {
                            try {
                                updateObserver.addEvent(eventObserver, service, event);
                            } catch (Exception e) {
                                LocalLog.error("Fehler bei Weiterleitung eines Events an einen Observer. Observer: " + eventObserver.getName(), e);
                            }
                            break;
                        }
                    }
                }
            }
        }
        Statistics.addFinishedEvent();
        // updatingObservers = false;
    }

    private static class UpdateObserver implements Runnable {
        private Map<String, ObserverUpdater> observerThreads = new HashMap<String, ObserverUpdater>();

        protected void addEvent(EventObserver eventObserver, Service service, NotEOFEvent event) {

            ObserverUpdater updater = observerThreads.get(eventObserver.getName());
            if (null == updater || updater.hasStopped()) {
                updater = new ObserverUpdater(eventObserver, this);
                observerThreads.put(eventObserver.getName(), updater);
                new Thread(updater).start();
                Statistics.addNewEventThread();
            }
            updater.addEvent(service, event);
        }

        protected void removeUpdater(String key, boolean removeObserver) {
            if (removeObserver) {
                unregisterFromEvents(eventObservers.get(key));
            }
            observerThreads.remove(key);
            Statistics.addFinishedEventThread();
        }

        @Override
        public void run() {
            boolean removed = false;
            while (true) {
                removed = false;
                Set<String> keys = observerThreads.keySet();
                for (String key : keys) {
                    ObserverUpdater updater = observerThreads.get(key);
                    if (null == updater || updater.hasStopped()) {
                        removeUpdater(key, false);
                        removed = true;
                        break;
                    }
                    updater.checkAndClear();
                }
                if (!removed) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class ObserverUpdater implements Runnable {
        private EventObserver eventObserver;
        private UpdateObserver controlObserver;
        private List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
        private List<Service> services = new ArrayList<Service>();
        private boolean stopped = false;
        private long lastAddTime = new Date().getTime();
        private static Object o = new Object();

        protected ObserverUpdater(EventObserver eventObserver, UpdateObserver controlObserver) {
            this.eventObserver = eventObserver;
            this.controlObserver = controlObserver;
        }

        protected boolean hasStopped() {
            return stopped;
        }

        private void updateEventList(Service service, NotEOFEvent event) {
            if (null == event) {
                services.remove(0);
                events.remove(0);
            } else {
                services.add(service);
                events.add(event);

                if (services.size() != events.size()) {
                    System.out.println("EventDistributor$ObserverUpdater.updateEventList. !!! Listendifferenz (vielleicht wegen services, die NULL sind?).");
                }
            }
        }

        protected void addEvent(Service service, NotEOFEvent event) {
            if (stopped)
                return;
            updateEventList(service, event);
            synchronized (o) {
                o.notify();
            }
        }

        protected void checkAndClear() {
            // update am observer laeuft jetzt seit mind. n millis
            if (new Date().getTime() - lastAddTime > MAX_OBSERVER_WAIT_TIME) {
                if (!hasStopped()) {
                    stopped = true;
                    clear();
                    synchronized (o) {
                        o.notify();
                    }
                }
            }
        }

        private void clear() {
            services.clear();
            events.clear();
        }

        @Override
        public void run() {
            try {
                while (!stopped) {
                    while (!events.isEmpty() && !stopped) {
                        lastAddTime = new Date().getTime();
                        eventObserver.update(services.get(0), events.get(0));
                        long processTime = new Date().getTime() - events.get(0).getTimeStampSend();
                        Statistics.setEventDuration(processTime);
                        updateEventList(null, null);
                        Thread.sleep(25);
                    }
                    // wait a while for new events
                    synchronized (o) {
                        o.wait(15000);
                    }
                }
            } catch (Exception ex) {
                stopped = true;
                clear();
                controlObserver.removeUpdater(eventObserver.getName(), true);
            }
            stopped = true;
        }
    }

    public static synchronized void registerForEvents(EventObserver eventObserver) {
        // wait for updating the observers
        while (updatingObservers) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (null == eventObservers) {
            eventObservers = new HashMap<String, EventObserver>();
        }
        eventObservers.put(eventObserver.getName(), eventObserver);
        Statistics.addNewObserver();
    }

    public static synchronized void unregisterFromEvents(EventObserver eventObserver) {
        if (null != eventObservers && null != eventObserver) {
            registeringObserver = true;

            // wait for updating the observers
            while (updatingObservers) {
                try {
                    Thread.sleep(55);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                eventObservers.remove(eventObserver.getName());
                Statistics.addFinishedObserver();
            } catch (Exception e) {
                LocalLog.warn("EventObserver konnte nicht entfernt werden: " + eventObserver.getName(), e);
            }
            registeringObserver = false;
        }
    }

    public static List<EventObserver> getSystemInfoObservers() {
        if (null == eventObservers)
            return null;
        List<EventObserver> systemObservers = new ArrayList<EventObserver>();
        for (EventObserver observer : eventObservers.values()) {
            for (EventType type : observer.getObservedEvents()) {
                if (type.equals(EventType.EVENT_SYSTEM_INFO)) {
                    systemObservers.add(observer);
                    break;
                }
            }
        }
        return systemObservers;
    }
}
