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

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EmptyEvent;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

/**
 * Delivers events
 * 
 * @author dgo01
 * 
 */
public class EventQueueReader {
    private static Map<Integer, NotEOFEvent> events;
    private static Map<Integer, String> fileNames;
    private static int lastFileCounter = -1;

    static {
        new EventQueueReader();
    }

    private EventQueueReader() {
        initQueue();
    }

    private static void initQueue() {
        if (null == events) {
            events = new Hashtable<Integer, NotEOFEvent>();
            fileNames = new Hashtable<Integer, String>();

            List<String> fileNameList = BrokerUtil.getQueueFileNames();
            Collections.sort(fileNameList);

            for (String fileName : fileNameList) {
                NotEOFEvent event = null;
                try {
                    event = readEventFile(fileName);
                } catch (Exception e) {
                    LocalLog.warn("Fehler bei Verarbeiten der Queue. EventFile: " + fileName);
                }
                events.put(lastFileCounter, event);
                fileNames.put(lastFileCounter, fileName);
                lastFileCounter++;
            }
        }
    }

    /**
     * Delivers the last received !EOF Event.
     * 
     * @param event
     *            The !EOF event which was delivered by this method before.
     * @return Another, newer event or NULL if no new Event came in meanwhile.
     */
    public static NotEOFEvent getNextEvent(NotEOFEvent event) {
        // erster Zugriff
        if (null == event) {
            return events.get(lastFileCounter);
        }
        
        // das letzte event wurde bereits geliefert
        if (events.get(lastFileCounter).equals(event)) {
            return null;
        }
        
        // das naechste...
        Iterator<NotEOFEvent> it = events.values().iterator();
        while (it.hasNext()) {
            NotEOFEvent listEvent = it.next();
            if (listEvent.equals(event)) {
                if (it.hasNext()) {
                    return it.next();
                } else {
                    return null;
                }
            }
        }
        
        return null;
    }

    /**
     * Delivers the last received filename.
     * 
     * @param fileName
     *            The file name which was delivered by this method before.
     * @return Another, newer file name or NULL if no new Event came in
     *         meanwhile.
     */
    public static String getNextFileName(String fileName) {
        if (Util.isEmpty(fileName) || fileName != lastFileName) {
            return lastFileName;
        }
        return null;
    }

    protected synchronized static void update(String fileName, NotEOFEvent event) throws Exception {
        lastFileName = fileName;
        lastEvent = event;
    }

    private static void createLastNotEOFEvent(String fileName) throws Exception {
        lastEvent = readEventFile(fileName);
    }

    /*
     * Read directly - without parsing xml
     */
    private static NotEOFEvent readEventFile(String fileName) throws Exception {
        // String serviceClassName= "";
        // String serviceId = "";
        // String clientNetId = "";
        String eventTypeName = "";
        List<String> eventAttrNames = new ArrayList<String>();
        List<String> eventValues = new ArrayList<String>();
        List<String> eventDescs = new ArrayList<String>();

        File eventFile = new File(fileName);
        FileReader reader = new FileReader(eventFile);
        BufferedReader bReader = new BufferedReader(reader);

        String nextLine;
        while (null != (nextLine = bReader.readLine())) {
            if (nextLine.contains("</root>")) {
                break;
            }

            nextLine = nextLine.trim();
            nextLine.replaceAll("\"", "");
            // if (nextLine.startsWith("<Service ")) {
            // serviceClassName = parseServiceClassName(nextLine);
            // serviceId = parseServiceId(nextLine);
            // clientNetId = parseClientNetId(nextLine);
            // }

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

    // private static String parseServiceClassName(String nextLine) {
    // return null;
    // }
    // private static String parseServiceId(String nextLine) {
    // return null;
    // }
    // private static String parseClientNetId(String nextLine) {
    // return null;
    // }
    private static String parseEventType(String nextLine) {
        return null;
    }

    private static String parseEventAttrName(String nextLine) {
        // if null -> leeren String fuer die Liste liefern ""
        return null;
    }

    private static String parseEventValue(String nextLine) {
        // if null -> leeren String fuer die Liste liefern ""
        return null;
    }

    private static String parseEventDesc(String nextLine) {
        // if null -> leeren String fuer die Liste liefern ""
        return null;
    }
}
