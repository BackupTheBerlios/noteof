package de.notEOF.core.brokerage;

import java.util.List;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.GenericEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Statistics;

public class QueueObserver implements Runnable {
    private EventObserver eventObserver;
    private List<EventType> observedEventTypes;
    private NotEOFEvent lastEvent;
    private boolean stopped = false;
    private boolean updating = false;
    private boolean ack = false;
    private Long lastReceivedQueueId;

    private NotEOFEvent initialEvent;

    // TODO holt sich als erstes die erwarteten events
    // TODO wenn ein observer.update fehlschlaegt, selbst ein
    // unregister(eventObserver) durchfuehren

    protected QueueObserver(EventObserver eventObserver) {
        this.eventObserver = eventObserver;
        observedEventTypes = eventObserver.getObservedEvents();
        // initialEvent = EventQueueReader.getNextEvent(null, null);
        initialEvent = EventQueueReader.getNextEvent(null);
    }

    public void stop() {
        stopped = true;
    }

    protected void wakeUp() {
        // TODO das ist die Stelle zum Ausbremsen, wenn zuviele Events zu
        // schnell aufeinander folgen
        updating = true;
        ack = false;
        new Thread(new Updater()).start();
    }

    protected void acknowledge() {
        // initialEvent = null;
        ack = true;
    }

    protected void offset(Long lastReceivedQueueId) {
        this.lastReceivedQueueId = lastReceivedQueueId;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(1000);
                if (stopped) {
                    // Stopp-Signal erhalten
                    break;
                }
                // if (!updating && !ack) {
                if (!updating) {
                    new Thread(new Updater()).start();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Updater implements Runnable {

        @Override
        public void run() {
            updating = true;
            Statistics.addNewEventThread();
            updateEventObserver();
            Statistics.addFinishedEventThread();
            updating = false;
        }

        private void updateEventObserver() {
            if (null == eventObserver) {
                StaticBroker.unregisterFromEvents(eventObserver);
            }

            // Long eventTimeCreated = null;
            if (ack) {
                // eventTimeCreated = new Date().getTime();
            }

            if (null != lastReceivedQueueId) {
                lastEvent = new GenericEvent();
                lastEvent.setQueueId(lastReceivedQueueId);
                lastReceivedQueueId = null;
            }

            // NotEOFEvent event = EventQueueReader.getNextEvent(lastEvent,
            // eventTimeCreated);
            NotEOFEvent event = EventQueueReader.getNextEvent(lastEvent);
            // doppelte events ausblenden
            if (null != lastEvent && null != event && lastEvent.getQueueId().longValue() == event.getQueueId().longValue()) {
                return;
            }

            if (null != event && //
                    (null == initialEvent || //
                    (null != initialEvent && initialEvent.getQueueId().longValue() < event.getQueueId().longValue()))) {

                try {
                    // initialEvent = null;
                    for (EventType type : observedEventTypes) {
                        if (event.equals(type)) {
                            eventObserver.update(null, event);
                        }
                    }
                    lastEvent = event;
                } catch (ActionFailedException e) {
                    LocalLog.warn("QueueObserver konnte neues Event nicht weiterleiten. EventObserver: " + eventObserver.getName(), e);
                }
            }
        }
    }
}
