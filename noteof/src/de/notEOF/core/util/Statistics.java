package de.notEOF.core.util;

import de.notEOF.core.event.SystemInfoEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Internal Class to collect system informations.
 * <p>
 * The informations can be sent cyclic by a SystemInfoEvent.
 * 
 * @author Dirk
 * 
 */
public class Statistics {
    private static long newEventCounter = 0;
    private static long finishedEventCounter = 0;
    private static long newObserverCounter = 0;
    private static long finishedObserverCounter = 0;
    private static long newThreadCounter = 0;
    private static long finishedThreadCounter = 0;
    private static long newServiceCounter = 0;
    private static long finishedServiceCounter = 0;
    private static long maxEventDuration = 0;
    private static long eventDuration = 0;
    private static long sumEventDuration = 0;
    private static long avgEventDuration = 0;
    private static boolean stopped = true;
    private static long eventWaitTime = 0;
    private static long lastEventProcessTime = 0;

    public static void activateEvents(long updateMillis) {
        if (stopped) {
            new Thread(new EventSender(updateMillis)).start();
        }
    }

    public static void stopEvents() {
        stopped = true;
    }

    public static void addNewEvent() {
        setNewEventCounter(getNewEventCounter() + 1);
    }

    public static void addFinishedEvent() {
        setFinishedEventCounter(getFinishedEventCounter() + 1);
    }

    public static void addNewObserver() {
        ++newObserverCounter;
    }

    public static void addFinishedObserver() {
        ++finishedObserverCounter;
    }

    public static void addNewEventThread() {
        ++newThreadCounter;
    }

    public static void addFinishedEventThread() {
        if (finishedThreadCounter > newThreadCounter) {
            finishedThreadCounter = newThreadCounter - 1;
        }
        ++finishedThreadCounter;
    }

    public static void addNewService() {
        ++newServiceCounter;
    }

    public static void addFinishedService() {
        ++finishedServiceCounter;
    }

    public static long countObservers() {
        if (finishedObserverCounter > newObserverCounter) {
            finishedObserverCounter = newObserverCounter;
        }
        return newObserverCounter - finishedObserverCounter;
    }

    public static void setEventDuration(long millis) {
        eventDuration = millis;

        if (Long.MAX_VALUE - (3600000) < sumEventDuration) {
            sumEventDuration = millis;
        } else {
            sumEventDuration += millis;
        }

        if (millis > maxEventDuration) {
            maxEventDuration = millis;
        }

        if (0 < finishedEventCounter) {
            avgEventDuration = sumEventDuration / finishedEventCounter;
        }
    }

    /**
     * @param newEventCounter
     *            the newEventCounter to set
     */
    public static void setNewEventCounter(long newEventCounter) {
        Statistics.newEventCounter = newEventCounter;
    }

    /**
     * @return the newEventCounter
     */
    public static long getNewEventCounter() {
        return newEventCounter;
    }

    /**
     * @param finishedEventCounter
     *            the finishedEventCounter to set
     */
    public static void setFinishedEventCounter(long finishedEventCounter) {
        Statistics.finishedEventCounter = finishedEventCounter;
    }

    /**
     * @return the finishedEventCounter
     */
    public static long getFinishedEventCounter() {
        return finishedEventCounter;
    }

    /**
     * @param eventWaitTime
     *            the eventWaitTime to set
     */
    public static void setEventWaitTime(long eventWaitTime) {
        Statistics.eventWaitTime = eventWaitTime;
    }

    /**
     * @return the eventWaitTime
     */
    public static long getEventWaitTime() {
        return eventWaitTime;
    }

    /**
     * @param lastEventProcessTime
     *            the lastEventProcessTime to set
     */
    public static void setLastEventProcessTime(long lastEventProcessTime) {
        Statistics.lastEventProcessTime = lastEventProcessTime;
    }

    /**
     * @return the lastEventProcessTime
     */
    public static long getLastEventProcessTime() {
        return lastEventProcessTime;
    }

    private static class EventSender implements Runnable {
        private long updateMillis = 10000;

        protected EventSender(long updateMillis) {
            this.updateMillis = updateMillis;
        }

        @Override
        public void run() {
            stopped = false;
            while (!stopped) {
                try {
                    NotEOFEvent event = buildEvent();
                    if (null != EventDistributor.getSystemInfoObservers()) {
                        for (EventObserver observer : EventDistributor.getSystemInfoObservers()) {
                            observer.update(null, event);
                        }
                    }
                    // EventDistributor.postEvent(null, buildEvent());
                    Thread.sleep(updateMillis);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private NotEOFEvent buildEvent() throws ActionFailedException {
            NotEOFEvent event = new SystemInfoEvent();

            event.addAttribute("Counter:SumEvents", String.valueOf(getNewEventCounter()));
            event.addAttribute("Counter:CompletedEvents", String.valueOf(getFinishedEventCounter()));
            event.addAttribute("Counter:SumEventThreads", String.valueOf(newThreadCounter));
            event.addAttribute("Counter:ActiveEventThreads", String.valueOf(newThreadCounter - finishedThreadCounter));
            event.addAttribute("Counter:CompletedEventThreads", String.valueOf(finishedThreadCounter));
            event.addAttribute("State:LastEventProcessingTime", String.valueOf(eventDuration));
            event.addAttribute("State:AvgEventProcessingTime", String.valueOf(avgEventDuration));
            event.addAttribute("State:MaxEventProcessingTime", String.valueOf(maxEventDuration));
            event.addAttribute("Counter:SumServices", String.valueOf(newServiceCounter));
            event.addAttribute("Counter:ActiveServices", String.valueOf(newServiceCounter - finishedServiceCounter));
            event.addAttribute("Counter:FinishedServices", String.valueOf(finishedServiceCounter));
            event.addAttribute("Counter:Observers", String.valueOf(countObservers()));
            event.addAttribute("State:DispatcherWaitTime", String.valueOf(getEventWaitTime()));

            return event;
        }
    }
}
