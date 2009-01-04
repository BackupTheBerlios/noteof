package de.notEOF.core.util;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;

public class EventBuffer implements Runnable {
    private List<Service> services = new ArrayList<Service>();
    private List<NotEOFEvent> events = new ArrayList<NotEOFEvent>();
    private int waitTime = 0;
    private int waitDivisor = 1;
    private boolean working = false;
    private static Object o = new Object();

    private void updateEventList(Service service, NotEOFEvent event) {
        if (null == event) {
            services.remove(0);
            events.remove(0);
        } else {
            services.add(service);
            events.add(event);

            if (services.size() != events.size()) {
                System.out.println("EventDistributor.updateEventList. !!! Listendifferenz (vielleicht wegen services, die NULL sind?).");
            }
        }
    }

    protected synchronized void process(Service service, NotEOFEvent event) {
        if (Statistics.getNewEventCounter() - Statistics.getFinishedEventCounter() < 10) {
            waitDivisor = 1;
        }
        if (Statistics.getNewEventCounter() - Statistics.getFinishedEventCounter() > 100) {
            waitDivisor = 10;
        }
        if (Statistics.getNewEventCounter() - Statistics.getFinishedEventCounter() > 200) {
            waitDivisor = 100;
        }
        updateEventList(service, event);
        if (!working) {
            synchronized (o) {
                o.notify();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            while (!events.isEmpty() && null != EventDistributor.eventObservers) {
                working = true;
                EventDistributor.updateAllObserver(services.get(0), events.get(0));
                updateEventList(null, null);
                // calculate waiting time
                waitTime = (EventDistributor.eventObservers.size() * 2) / waitDivisor;
                Statistics.setEventWaitTime(waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            working = false;

            try {
                synchronized (o) {
                    o.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
