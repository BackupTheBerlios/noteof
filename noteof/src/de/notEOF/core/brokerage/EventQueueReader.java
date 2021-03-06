package de.notEOF.core.brokerage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EmptyEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;

/**
 * Delivers events
 * 
 * @author dgo01
 * 
 */
public class EventQueueReader {
    private static List<QueuedEvent> queuedEvents;
    private static boolean initialization = true;
    private static int maxMemoryEvents = 100;

    static {
        try {
            NotEOFConfiguration conf = new LocalConfiguration();
            maxMemoryEvents = conf.getAttributeInt("brokerage.Queue", "maxMemoryEvents", 100);
            new EventQueueReader();
        } catch (ActionFailedException e) {
            LocalLog.warn("Fehler bei Ermittlung der max. Anzahl Speicherelemente im EventQueReader.", e);
        }
    }

    private EventQueueReader() {
        initQueue();
    }

    private static void sortQueuedEvents() {
        for (int i = queuedEvents.size(); i >= 0; i--) {
            boolean toSort = false;
            boolean moved = false;
            for (int index = 0; index < i - 1; index++) {
                if (queuedEvents.get(index).getQueueId().longValue() > queuedEvents.get(index + 1).getQueueId().longValue()) {
                    QueuedEvent qe = queuedEvents.get(index + 1);
                    queuedEvents.set(index + 1, queuedEvents.get(index));
                    queuedEvents.set(index, qe);
                    moved = true;
                }
                if (moved && index < i - 1) {
                    toSort = true;
                    break;
                }
            }
            if (!toSort)
                break;
        }

        Long lastVal = new Long(0);
        for (QueuedEvent val : queuedEvents) {
            if (val.getQueueId().longValue() <= lastVal.longValue()) {
                System.out.println("!!!!!!!!!!!!!! lastVal: " + lastVal + "; nextVal: " + val.getQueueId());
            }
            lastVal = val.getQueueId();
        }
    }

    private synchronized static void initQueue() {
        queuedEvents = new ArrayList<QueuedEvent>();

        List<File> eventFiles = BrokerUtil.getQueueFiles();
        if (eventFiles.size() > 0) {
            for (File file : eventFiles) {
                NotEOFEvent event = null;
                try {
                    event = readEventFile(file);
                    if (null != event)
                        addEvent(event);
                } catch (Exception e) {
                    LocalLog.warn("Fehler bei Verarbeiten der Queue. EventFile: " + file.getName(), e);
                }
            }
            sortQueuedEvents();
            reduceStorage();
        }
        initialization = false;
    }

    private synchronized static void addEvent(NotEOFEvent event) {
        if (!containsQueuedEvent(event.getQueueId())) {
            queuedEvents.add(new QueuedEvent(event.getQueueId(), event));
        }
    }

    /*
     * This method is not made to delete events from the queue. To do this the
     * files must be deleted AND the event here must be deleted later.
     */
    protected synchronized static void deleteEvent(NotEOFEvent event) {
        deleteEvent(event.getQueueId());
    }

    private synchronized static void deleteEvent(Long queueId) {
        deleteQueuedEvent(queueId);
    }

    private static QueuedEvent deleteQueuedEvent(Long queueId) {
        for (QueuedEvent qE : queuedEvents) {
            if (qE.getQueueId().longValue() == queueId.longValue()) {
                queuedEvents.remove(qE);
                return qE;
            }
        }
        return null;
    }

    private static void reduceStorage() {
        // not more than 1000 events in queue
        while (queuedEvents.size() > maxMemoryEvents) {
            queuedEvents.remove(0);
        }
    }

    /**
     * Delivers an event with defined queueId.
     * 
     * @param queueId
     *            Queue Id's are part of the events. They should be unique.
     * @return The event with queueId or NULL if not found.
     */
    public static NotEOFEvent getEvent(Long queueId) {
        QueuedEvent qE = findQueuedEvent(queueId);
        if (null != qE) {
            return qE.getEvent();
        }
        return null;
    }

    public static QueuedEvent getQueuedEvent(Long queueId) {
        return findQueuedEvent(queueId);
    }

    private static boolean containsQueuedEvent(Long queuedId) {
        return findQueuedEvent(queuedId) != null;
    }

    private static QueuedEvent findQueuedEvent(Long queuedId) {
        for (QueuedEvent qE : queuedEvents) {
            if (qE.getQueueId().longValue() == queuedId.longValue()) {
                return qE;
            }
        }
        return null;
    }

    private static class QueuedEvent {
        private Long queueId;
        private NotEOFEvent event;

        protected QueuedEvent(Long queueId, NotEOFEvent event) {
            this.setQueueId(queueId);
            this.setEvent(event);
        }

        public void setQueueId(Long queueId) {
            this.queueId = queueId;
        }

        public Long getQueueId() {
            return queueId;
        }

        public void setEvent(NotEOFEvent event) {
            this.event = event;
        }

        public NotEOFEvent getEvent() {
            return event;
        }
    }

    /**
     * Delivers the last received !EOF Event.
     * 
     * @param event
     *            The !EOF event which was delivered by this method before.
     * @param eventCreated
     *            If not NULL this method delivers the newest event when this
     *            event is younger than the value of eventCreated (event.queueId
     *            > eventCreated).
     * @return Another, newer event or NULL if no new Event came in meanwhile.
     */
    // public synchronized static NotEOFEvent getNextEvent(NotEOFEvent event,
    // Long eventCreated) {
    public synchronized static NotEOFEvent getNextEvent(NotEOFEvent event) {
        // erster Zugriff
        // oder das letzte Event gibt's nicht mehr
        // if (null == event || !containsQueuedEvent(event.getQueueId()) || null
        // != eventCreated) {
        if (null == event || !containsQueuedEvent(event.getQueueId())) {
            if (queuedEvents.size() > 0) {
                // if (null == eventCreated || (null != eventCreated &&
                // queuedEvents.get(queuedEvents.size() - 1).getQueueId() >
                // eventCreated)) {

                // Liste reduzieren
                for (int i = 0; i < queuedEvents.size() - 2; i++) {
                    queuedEvents.remove(i);
                }
                return queuedEvents.get(queuedEvents.size() - 1).getEvent();
                // }
            }
            return null;
        }

        // wenn vorheriges event null oder unbekannt war, kommen wir hier nicht
        // hin...
        for (Integer i = queuedEvents.size() - 1; i >= 0; i--) {
            NotEOFEvent listEvent = queuedEvents.get(i).getEvent();
            if (0 == listEvent.getQueueId().compareTo(event.getQueueId())) {
                // das zuletzt gelieferte gefunden
                // jetzt das naechste...
                if (i < queuedEvents.size() - 1) {
                    return queuedEvents.get(i + 1).getEvent();
                }
            }
        }
        return null;
        // return queuedEvents.get(queuedEvents.size() - 1).getEvent();
    }

    protected synchronized static void update(NotEOFEvent event) throws Exception {
        while (initialization)
            ;
        addEvent(event);
        reduceStorage();
    }

    /*
     * Read directly - without parsing xml
     */
    private synchronized static NotEOFEvent readEventFile(File eventFile) throws Exception {
        if (!eventFile.isFile())
            return null;

        String eventTypeName = "";
        List<String> eventAttrNames = new ArrayList<String>();
        List<String> eventValues = new ArrayList<String>();
        List<String> eventDescs = new ArrayList<String>();

        FileReader reader = new FileReader(eventFile.getAbsoluteFile());
        BufferedReader bReader = new BufferedReader(reader);

        String nextLine;
        while (null != (nextLine = bReader.readLine())) {
            if (nextLine.contains("</root>")) {
                break;
            }

            nextLine = nextLine.trim();
            nextLine.replaceAll("\"", "");

            if (nextLine.startsWith("<Type ")) {
                eventTypeName = (parseEventType(nextLine));
            }

            if (nextLine.startsWith("<Attribute ")) {
                eventAttrNames.add(parseEventAttrName(nextLine));
                eventValues.add(parseEventValue(nextLine));
                eventDescs.add(parseEventDesc(nextLine));
            }
        }

        bReader.close();

        // create event
        NotEOFEvent event = new EmptyEvent();
        EventType type = EventType.valueOf(eventTypeName);
        event.setEventType(type);

        for (int i = 0; i < eventAttrNames.size(); i++) {
            String attrName = eventAttrNames.get(i);
            String value = eventValues.get(i);
            String desc = eventDescs.get(i);

            event.addAttributeDescription(attrName, desc);
            event.addAttribute(attrName, value);
        }

        return event;
    }

    private static String parseEventType(String nextLine) {
        int pos = nextLine.indexOf("></Type>") - 1;
        nextLine = nextLine.substring(0, pos).trim();
        pos = nextLine.indexOf("name=") + "name=".length() + 1;
        nextLine = nextLine.substring(pos).trim();
        return nextLine;
    }

    private static String parseEventAttrName(String nextLine) {
        int pos = nextLine.indexOf(" value=") - 1;
        nextLine = nextLine.substring(0, pos).trim();
        pos = nextLine.indexOf("name=") + "name=".length() + 1;
        nextLine = nextLine.substring(pos);
        nextLine.replaceAll("\"", "");
        return nextLine;
    }

    private static String parseEventValue(String nextLine) {
        int pos = nextLine.indexOf(" description=") - 1;
        nextLine = nextLine.substring(0, pos);
        pos = nextLine.indexOf("value=") + "value=".length() + 1;
        nextLine = nextLine.substring(pos);
        nextLine.replaceAll("\"", "");
        return nextLine;
    }

    private static String parseEventDesc(String nextLine) {
        int pos = nextLine.indexOf("></Attribute") - 1;
        nextLine = nextLine.substring(0, pos);
        pos = nextLine.indexOf("description=") + "description=".length() + 1;
        nextLine = nextLine.substring(pos);
        nextLine.replaceAll("\"", "");
        return nextLine;
    }
}
