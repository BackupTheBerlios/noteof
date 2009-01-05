package de.notEOF.core.brokerage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
    private static int lastFileCounter = -1;

    static {
        new EventQueueReader();
    }

    private EventQueueReader() {
        initQueue();
    }

    private static void initQueue() {
        if (null == events) {
            events = new Hashtable<Long, NotEOFEvent>();

            List<File> eventFiles = BrokerUtil.getQueueFiles();
            if (eventFiles.size() > 0) {
                Collections.sort(eventFiles);

                for (File file : eventFiles) {
                    NotEOFEvent event = null;
                    try {
                        event = readEventFile(file);
                        events.put(event.getQueueId(), event);
                        lastFileCounter++;
                    } catch (Exception e) {
                        LocalLog.warn("Fehler bei Verarbeiten der Queue. EventFile: " + file.getName(), e);
                    }
                }
                while (events.size() > 1000) {
                    events.remove(0);
                }
            }
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
    public synchronized static NotEOFEvent getNextEvent(NotEOFEvent event, List<EventType> eventTypes) {
        List<NotEOFEvent> eventList = new ArrayList<NotEOFEvent>();
        eventList.addAll(events.values());

        // erster Zugriff
        // oder das letzte Event gibt's nicht mehr
        if (null == event || !events.containsKey(event.getQueueId())) {
            for (Integer i = eventList.size() - 1; i >= 0; i--) {
                NotEOFEvent listEvent = eventList.get(i);

                for (EventType type : eventTypes) {
                    if (listEvent.equals(type)) {
                        return listEvent;
                    }
                }
            }
            // nix passendes gefunden
            return null;
        }

        // wenn event null oder das vorherige event unbekannt war, kommen wir
        // hier nicht hin...
        for (Integer i = eventList.size() - 1; i >= 0; i--) {
            NotEOFEvent listEvent = eventList.get(i);

            if (listEvent.getQueueId().equals(event.getQueueId())) {
                // das zuletzt gelieferte gefunden
                // jetzt wieder in entgegengesetzter Richtung suchen...
                int y = i + 1;
                while (y < eventList.size() - 1) {
                    NotEOFEvent nextEvent = eventList.get(y);
                    for (EventType type : eventTypes) {
                        if (nextEvent.equals(type)) {
                            return listEvent;
                        }
                    }
                }
            }
        }

        return null;
    }

    protected synchronized static void update(NotEOFEvent event) throws Exception {
        events.put(event.getQueueId(), event);
        lastFileCounter++;

        // not more than 1000 events in queue
        if (events.size() > 1000) {
            while (events.size() > 1000) {
                events.remove(new Integer(0));
            }
        }
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
            System.out.println("ZEILE: " + nextLine);

            if (nextLine.startsWith("<Type ")) {
                eventTypeName = (parseEventType(nextLine));
                System.out.println("TYPENAME: " + eventTypeName);
            }

            if (nextLine.startsWith("<Attribute ")) {
                System.out.println("EVENTATTRIBUTENAME: " + parseEventAttrName(nextLine));
                eventAttrNames.add(parseEventAttrName(nextLine));
                System.out.println("EVENTVALUE: " + parseEventValue(nextLine));
                eventValues.add(parseEventValue(nextLine));
                System.out.println("EVENTDESC: " + parseEventDesc(nextLine));
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

        System.out.println("Kontrolle: event.getType: " + event.getEventType());
        Set<Entry<String, String>> set = event.getAttributes().entrySet();
        Iterator<Entry<String, String>> it = set.iterator();

        while (it.hasNext()) {
            Entry<String, String> e = it.next();
            System.out.println("Kontrolle: event.getAttribute: " + e.getKey());
            System.out.println("Kontrolle: event.getValue: " + e.getValue());
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
