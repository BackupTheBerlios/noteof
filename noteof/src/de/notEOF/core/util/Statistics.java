package de.notEOF.core.util;

import de.notEOF.core.event.SystemInfoEvent;
import de.notEOF.core.exception.ActionFailedException;
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
    private static long newThreadCounter = 0;
    private static long finishedThreadCounter = 0;
    private static long newServiceCounter = 0;
    private static long finishedServiceCounter = 0;
    private static long minThreadDuration = 10000;
    private static long maxThreadDuration = 0;
    private static long threadDuration = 0;
    private static long sumThreadDuration = 0;
    private static long avgThreadDuration = 0;
    private static boolean stopped = true;

    public static void activateEvents(long updateMillis) {
        if (stopped) {
            new Thread(new EventSender(updateMillis)).start();
        }
    }

    public static void stopEvents() {
        stopped = true;
    }

    public static void addNewEvent() {
        ++newEventCounter;
    }

    public static void addFinishedEvent() {
        ++finishedEventCounter;
    }

    public static void addNewEventThread() {
        ++newThreadCounter;
    }

    public static void addFinishedEventThread() {
        ++finishedThreadCounter;
    }

    public static void addNewService() {
        ++newServiceCounter;
    }

    public static void addFinishedService() {
        ++finishedServiceCounter;
    }

    public static void setEventThreadDuration(long millis) {
        threadDuration = millis;
        sumThreadDuration += millis;

        if (millis < minThreadDuration) {
            minThreadDuration = millis;
        }
        if (millis > maxThreadDuration) {
            maxThreadDuration = millis;
        }

        if (0 < finishedThreadCounter) {
            avgThreadDuration = sumThreadDuration / finishedThreadCounter;
        }
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
                    Util.updateAllObserver(null, buildEvent());

                    Thread.sleep(updateMillis);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private NotEOFEvent buildEvent() throws ActionFailedException {
            NotEOFEvent event = new SystemInfoEvent();

            event.addAttribute("Util:Counter:SumEvents", String.valueOf(newEventCounter));
            event.addAttribute("Util:Counter:CompletedEvents", String.valueOf(finishedEventCounter));
            event.addAttribute("Util:Counter:SumEventThreads", String.valueOf(newThreadCounter));
            event.addAttribute("Util:Counter:ActiveEventThreads", String.valueOf(newThreadCounter - finishedThreadCounter));
            event.addAttribute("Util:Counter:CompletedEventThreads", String.valueOf(finishedThreadCounter));
            event.addAttribute("Util:State:LastEventProcessingTime", String.valueOf(threadDuration));
            event.addAttribute("Util:State:MinEventProcessingTime", String.valueOf(avgThreadDuration));
            event.addAttribute("Util:State:MaxEventProcessingTime", String.valueOf(maxThreadDuration));
            event.addAttribute("Server:Counter:SumServices", String.valueOf(newServiceCounter));
            event.addAttribute("Server:Counter:ActiveServices", String.valueOf(newServiceCounter - finishedServiceCounter));
            event.addAttribute("Server:Counter:FinishedServices", String.valueOf(finishedServiceCounter));

            return event;
        }
    }
}
