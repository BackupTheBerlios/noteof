package de.notEOF.core.brokerage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EmptyEvent;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;

/**
 * Delivers events
 * 
 * @author dgo01
 * 
 */
public class EventQueueReader {
    private static Map<Long, NotEOFEvent> events;
    private static List<Long> queueIds;

    static {
        new EventQueueReader();
    }

    private EventQueueReader() {
        initQueue();
    }

    private synchronized static void initQueue() {
        if (null == events) {
            events = new Hashtable<Long, NotEOFEvent>();
            queueIds = new ArrayList<Long>();

            List<File> eventFiles = BrokerUtil.getQueueFiles();
            if (eventFiles.size() > 0) {
                Collections.sort(eventFiles);

                for (File file : eventFiles) {
                    NotEOFEvent event = null;
                    try {
                        event = readEventFile(file);
                        addEvent(event);
                    } catch (Exception e) {
                        LocalLog.warn("Fehler bei Verarbeiten der Queue. EventFile: " + file.getName(), e);
                    }
                }
                Collections.sort(queueIds);
                reduceStorage();
            }
        }
    }

    private synchronized static void addEvent(NotEOFEvent event) {
        events.put(event.getQueueId(), event);
        queueIds.add(event.getQueueId());
    }

    /*
     * This method is not made to delete events from the queue. To do this the
     * files must be deleted AND the event here must be deleted later.
     */
    protected synchronized static void deleteEvent(NotEOFEvent event) {
        // TODO Pruefen, ob das Loeschen aus der Liste mit Long-Werten so ok
        // ist...
        Long id = event.getQueueId();
        deleteEvent(id);
    }

    private synchronized static void deleteEvent(Long queueId) {
        queueIds.remove(queueId);
        events.remove(queueId);
    }

    private static void reduceStorage() {
        // not more than 1000 events in queue
        while (queueIds.size() > 1000) {
            Long id = queueIds.get(0);
            deleteEvent(id);
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
        return events.get(queueId);
    }

    /**
     * Delivers the last received !EOF Event.
     * 
     * @param event
     *            The !EOF event which was delivered by this method before.
     * @return Another, newer event or NULL if no new Event came in meanwhile.
     */
    public synchronized static NotEOFEvent getNextEvent(NotEOFEvent event) {
        // erster Zugriff
        // oder das letzte Event gibt's nicht mehr
        if (null == event || !queueIds.contains(event.getQueueId())) {
            if (queueIds.size() > 0) {
                return events.get(queueIds.get(queueIds.size() - 1));
            }
            return null;
        }

        // wenn vorheriges event null oder unbekannt war, kommen wir hier nicht
        // hin...
        for (Integer i = queueIds.size() - 1; i >= 0; i--) {
            NotEOFEvent listEvent = events.get(queueIds.get(i));

            if (listEvent.getQueueId().equals(event.getQueueId())) {
                // das zuletzt gelieferte gefunden
                // jetzt das naechste...
                if (i + 1 < queueIds.size() - 1) {
                    return events.get(queueIds.get(i + 1));
                }
            }
        }
        return null;
    }

    protected synchronized static void update(NotEOFEvent event) throws Exception {
        addEvent(event);
        reduceStorage();
    }

    /*
     * Read directly - without parsing xml
     */
    private static NotEOFEvent readEventFile(File eventFile) throws Exception {
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
