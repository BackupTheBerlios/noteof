package de.notEOF.core.brokerage;

import java.util.List;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;

public class QueueObserver implements Runnable {
    private EventObserver eventObserver;
    private List<EventType> observedEventTypes;
    private NotEOFEvent lastEvent;
    private boolean stopped = false;
    private boolean updating = false;

    // TODO holt sich als erstes die erwarteten events
    // TODO wenn ein observer.update fehlschlaegt, selbst ein
    // unregister(eventObserver) durchfuehren

    protected QueueObserver(EventObserver eventObserver) {
        this.eventObserver = eventObserver;
        observedEventTypes = eventObserver.getObservedEvents();
        System.out.println("Anzahl überwachter Ereigniss für " + eventObserver.getName() + " = " + observedEventTypes.size());
    }

    public void stop() {
        stopped = true;
    }

    protected void wakeUp() {
        // TODO das ist die Stelle zum Ausbremsen, wenn zuviele Events zu
        // schnell aufeinander folgen
        new Thread(new Updater()).start();
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
                if (!updating) {
                    new Thread(new Updater()).start();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Ich lebe noch " + eventObserver.getName());
        }
    }

    private class Updater implements Runnable {

        @Override
        public void run() {
            updating = true;
            updateEventObserver();
            updating = false;
        }

        private void updateEventObserver() {
            if (null == eventObserver) {
                StaticBroker.unregisterFromEvents(eventObserver);
            }

            NotEOFEvent event = EventQueueReader.getNextEvent(lastEvent);
            if (null != event) {
                try {
                    System.out.println("Empfangenes Event: " + event.getEventType().toString() + "  QueueObserver sendet jetzt an EventObserver: "
                            + eventObserver.getName());
                    for (EventType type : observedEventTypes) {
                        System.out.println("Observed Event: " + type.toString());
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

    protected void finalize() {
        EventQueue.msg(eventObserver.getName());
    }
}
