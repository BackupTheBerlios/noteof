package de.notEOF.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.event.EventFinder;
import de.notEOF.core.event.GenericEvent;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;

/**
 * When used several times in one file use this: import static
 * de.iccs.util.Util.*; And access the Util.*-methods directly.
 */
public class Util {
    private static Thread consoleWaitThread;
    private static boolean updatingObservers = false;
    private static boolean registeringObserver = false;
    private static long queueId = 0;
    private static Map<String, EventObserver> eventObservers;

    private static boolean observerUpdaterActive = false;
    private static Updater updater = null;
    private static List<Service> serviceList = new ArrayList<Service>();
    private static List<NotEOFEvent> eventList = new ArrayList<NotEOFEvent>();

    private Util() {
    }

    /**
     * Parses elements of the string which are delimited by the delimiter.
     * 
     * @param simpleString
     *            The textual list.
     * @param delimiter
     *            The sign between the values.
     * @return An String array with the elements.
     */
    public static String[] stringToArray(String simpleString, String delimiter) {
        List<String> list = stringToList(simpleString, delimiter);
        if (!isEmpty(list)) {
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        }
        return null;
    }

    /*
     * Converts a List<String> to List<Long>
     */
    public static List<Long> stringListToLongList(List<String> stringList) {
        List<Long> newList = new ArrayList<Long>();
        if (null != stringList) {
            for (String element : stringList) {
                newList.add(Util.parseLong(element, -1));
            }
        }
        return newList;
    }

    /*
     * delivers elements of a text string comma separated or blank separated
     */
    public static List<Integer> getElementsOfStringAsInt(String node, NotEOFConfiguration conf) throws ActionFailedException {
        List<String> elementList = getElementsOfString(node, conf);
        List<Integer> intList = new ArrayList<Integer>();
        for (String element : elementList) {
            intList.add(Util.parseInt(element, -1));
        }
        return intList;
    }

    public static List<Integer> getElementsOfStringAsInt(String elements) throws ActionFailedException {
        List<String> elementList = getElementsOfString(elements);
        List<Integer> intList = new ArrayList<Integer>();
        for (String element : elementList) {
            intList.add(Util.parseInt(element, -1));
        }
        return intList;
    }

    /*
     * delivers elements of a text string comma separated or blank separated
     */
    public static List<String> getElementsOfString(String node, NotEOFConfiguration conf) throws ActionFailedException {
        String elements = conf.getText(node);
        return getElementsOfString(elements);
    }

    public static List<String> getElementsOfString(String elements) throws ActionFailedException {
        List<String> elementList;
        elements = elements.replace(" ", ",");
        elements = elements.replace(",,", ",");
        elementList = Util.stringToList(elements, ",");
        return elementList;
    }

    /**
     * Parses elements of the string which are delimited by the delimiter.
     * 
     * @param simpleString
     *            The textual list.
     * @param delimiter
     *            The sign between the values.
     * @return A List with the String elements.
     */
    public static List<String> stringToList(String simpleString, String delimiter) {
        List<String> elements = new ArrayList<String>();
        simpleString.trim();
        // delimiter at begin or end is not valid
        while (!isEmpty(simpleString) && simpleString.startsWith(delimiter)) {
            simpleString = simpleString.substring(1);
        }
        while (!isEmpty(simpleString) && simpleString.endsWith(delimiter)) {
            simpleString = simpleString.substring(0, simpleString.length() - 1);
        }

        // look for elements
        while (!isEmpty(simpleString) && simpleString.indexOf(delimiter) > -1) {
            String element = simpleString.substring(0, simpleString.indexOf(delimiter));
            elements.add(element);
            simpleString = simpleString.substring(simpleString.indexOf(delimiter));
            // destroy multiple delimiters
            while (!isEmpty(simpleString) && simpleString.startsWith(delimiter)) {
                simpleString = simpleString.substring(1);
            }
        }

        // last entry, no delimiter
        if (!isEmpty(simpleString)) {
            elements.add(simpleString);
        }

        return elements;
    }

    /**
     * Liefert bei einem Null-String einen leeren String. Vereinfacht den
     * Vergleich von Strings.
     * 
     * @param theObject
     *            Ein Objekt
     * @return Der String oder ein leerer String (nicht null)
     */
    public static String toString(Object theObject) {
        if (null == theObject)
            return "";
        if (null == String.valueOf(theObject))
            return "";
        return String.valueOf(theObject);
    }

    /**
     * Prueft, ob ein String NULL oder LEER ist (getrimmt).
     */
    public static boolean isEmpty(String string) {
        return string == null || string.trim().equals("");
    }

    /**
     * Pr�ft ob object NULL, ein Leerstring (getrimmt), ein leeres Array
     * (length == 0) oder eine leere Collection ist.
     * 
     * @param object
     * @return
     */
    public static boolean isEmpty(Object object) {
        if (object == null)
            return true;
        if (object instanceof String)
            return isEmpty((String) object);
        if (object.getClass().isArray())
            return isEmpty((Object[]) object);
        if (object instanceof Collection)
            return ((Collection<?>) object).isEmpty();
        return false;
    }

    /**
     * @param array
     * @return true wenn das array null ist oder keine Elemente hat (length ==
     *         0)
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Liefert den Wert des an eine Methode uebergebenen Arguments. <br>
     * Wird z.B. fuer die Auswertung der Argumente an die main-Methode
     * benoetigt.
     * 
     * @param arg
     *            Ist das String-Array
     * @param index
     *            Ist der Index des Arguments, beginnend mit 0
     * @return "" oder den Wert. Ist also 'Nullsafe'
     */
    public static String getArgPerIndex(String[] arg, int index) {
        if (!isEmpty(arg) && arg.length > index) {
            return arg[index];
        }
        return "";
    }

    /**
     * Liefert aus den an eine Methode uebergebenen Argumenten zu einem
     * Schluessel/Parameter den Wert. <br>
     * Wird z.B. fuer die Auswertung der Argumente an die main-Methode
     * benoetigt. <br>
     * Beispiel: Ein Programm prog wird mit den Argumenten -home hotel
     * aufgerufen: <br>
     * prog -home hotel <br>
     * Dann ruft man diese Methode auf mit (arg, "-home")
     * 
     * @param args
     *            Ist das String array, wie an die Methode (z.B. main)
     *            uebergeben wurde
     * @param keyName
     *            Ist der identifizierende Schluessel (z.B. -home)
     * @return Liefert den Wert aus dem Array, der dem keyNamen folgt oder NULL
     *         wenn keyName nicht gefunden werden konnte oder kein Parameter
     *         folgte.
     */
    public static String getArgPerKey(String[] args, String keyName) {
        for (int i = 0; i < args.length; i++) {
            if (!isEmpty(args[i]) && args[i].startsWith(keyName) && i + 1 < args.length) {
                return args[i + 1];
            }
        }
        return null;
    }

    /**
     * Checks if an calling argument is set.
     * <p>
     * This Check is restrictly, the argument must exactly match the prooved
     * hard coded value.<br>
     * Samples:
     * <ul>
     * <li>Match => Code: args[0] = argument; Call: argument</>
     * <li>Match => Code: args[0] = argument; Call: argument = value</>
     * <li>No Match => Code: args[0] = argument=x; Call: argument = value</>
     * <li>No Match => Code: args[0] = --argument; Call: argName = argument</>
     * </ul>
     * 
     * @param args
     *            Array which must be checked for matching Strings.
     * @param argName
     *            The argument which is searched.
     * @return TRUE if the Argument exactly matches.
     */
    public static boolean isArgSet(String[] args, String argName) {
        for (int i = 0; i < args.length; i++) {
            if (!isEmpty(args[i]) && args[i].equalsIgnoreCase(argName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vergleicht zwei Strings miteinander. <br>
     * Vorteil dieser Methode: NULL-Strings werden zu leeren Strings. <br>
     * Daher werden auch NULL-Objekte wie Strings miteinander verglichen.
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equalsToString(String s1, String s2) {
        if (isEmpty(s1))
            s1 = "";
        if (isEmpty(s2))
            s2 = "";
        return s1.equals(s2);
    }

    /**
     * Vergleicht zwei Strings miteinander. <br>
     * Vorteil dieser Methode: NULL-Strings werden zu leeren Strings. <br>
     * Anschlie�end wird ein equalsIgnoreCase durchgef�hrt. <br>
     * NULL-Objekte werden wie Strings miteinander verglichen.
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equalsToStringNoCase(String s1, String s2) {
        if (isEmpty(s1))
            s1 = "";
        if (isEmpty(s2))
            s2 = "";
        return s1.equalsIgnoreCase(s2);
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    public static boolean equalsNotNull(Object o1, Object o2) {
        return o1 != null && o2 != null && equals(o1, o2);
    }

    /**
     * Parses the input as int. Returns def if the parse process fails (due to
     * null or non-number)
     * 
     * @param input
     * @param def
     *            - default value if parse fails
     * @return
     */
    public static int parseInt(String input, int def) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return def;
        }
    }

    public static short parseShort(String input, short def) {
        try {
            return Short.parseShort(input);
        } catch (Exception e) {
            return def;
        }
    }

    public static double parseDouble(String input, double def) {
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            return def;
        }
    }

    public static float parseFloat(String input, float def) {
        try {
            return Float.parseFloat(input);
        } catch (Exception e) {
            return def;
        }
    }

    public static long parseLong(String input, long def) {
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            return def;
        }
    }

    public static Date parseDate(String inputMillis, Date def) {
        try {
            long millis = parseLong(inputMillis, 0);
            if (0 == millis)
                return def;
            return new Date(millis);
        } catch (Exception e) {
            return def;
        }
    }

    public static Boolean parseBoolean(String inputBoolean, Boolean def) {
        try {
            return Boolean.parseBoolean(inputBoolean);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Startet ein animiertes Warten auf der Konsole. <br>
     * Dort wird ein sich drehendes Symbol gezeigt. <br>
     * Das Rotationsevent endet mit Aufruf der Methode stopWait().
     */
    public static void startWait() {
        if (consoleWaitThread == null) {
            // ConsoleWait cWait = new ConsoleWait();
            // consoleWaitThread = new Thread(cWait);
            consoleWaitThread.start();
        }
    }

    /**
     * Beendet das wilde Rotieren auf der Konsole, das mit startWait eingeleitet
     * wurde.
     */
    public static void stopWait() {
        if (consoleWaitThread != null && consoleWaitThread.isAlive()) {
            consoleWaitThread.interrupt();
            try {
                consoleWaitThread.join(); // Thread muss noch schnell sauber
                // machen...
            } catch (Exception e) {
            }
            consoleWaitThread = null;
        }
    }

    /**
     * Shortens a canonical class name to a simple class name. <br>
     * Does not more than cutting all parts of the name until the last
     * occurrence of a dot. <br>
     * Sample: de.notEOF.application.service.ApplicationService will become to
     * ApplicationService.
     * 
     * @param className
     * @return simple name
     */
    public static String simpleClassName(String className) {
        String simpleName = className;
        while (simpleName.contains(".")) {
            simpleName = simpleName.substring(simpleName.indexOf(".") + 1);
        }
        return simpleName;
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
        if (null == eventObservers)
            eventObservers = new HashMap<String, EventObserver>();
        eventObservers.put(eventObserver.getName(), eventObserver);
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
            } catch (Exception e) {
                LocalLog.warn("EventObserver konnte nicht entfernt werden: " + eventObserver.getName(), e);
            }
            registeringObserver = false;
        }
    }

    // the object which sends the event would stand still till the observers all
    // have processed the event.
    // so the updating is processed within a thread.
    private static class ObserverUpdater implements Runnable {
        private EventObserver observer;
        private Service service;
        private NotEOFEvent event;

        protected ObserverUpdater(EventObserver observer, Service service, NotEOFEvent event) {
            this.observer = observer;
            this.service = service;
            this.event = event;
        }

        public void run() {
            observer.update(service, event);
        }
    }

    public static void postEvent(Service service, NotEOFEvent event) {
        if (!observerUpdaterActive) {
            updater = new Updater();
            Thread thread = new Thread(updater);
            thread.start();
            observerUpdaterActive = true;
        }
        updater.addEvent(service, event);
    }

    private static class Updater implements Runnable {

        public void addEvent(Service service, NotEOFEvent event) {
            serviceList.add(service);
            eventList.add(event);
        }

        @Override
        public void run() {
            while (!serviceList.isEmpty()) {
                Service service = serviceList.get(0);
                NotEOFEvent event = eventList.get(0);
                serviceList.remove(0);
                eventList.remove(0);
                updateAllObserver(service, event);
            }
            observerUpdaterActive = false;
            serviceList.clear();
            eventList.clear();
        }
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
    public synchronized static void updateAllObserver(Service service, NotEOFEvent event) {
        if (null == event) {
            return;
        }
        if (null == eventObservers) {
            return;
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

        updatingObservers = true;
        // all observer
        if (eventObservers.size() > 0) {
            Set<String> set = eventObservers.keySet();
            for (String observerName : set) {
                // but only inform observer, when event in his list
                EventObserver eventObserver = eventObservers.get(observerName);
                if (null != eventObserver && null != eventObserver.getObservedEvents()) {
                    for (EventType type : eventObserver.getObservedEvents()) {
                        if (type.equals(EventType.EVENT_ANY_TYPE) || type.equals(event.getEventType())) {
                            try {
                                new Thread(new ObserverUpdater(eventObserver, service, event)).start();
                            } catch (Exception e) {
                                LocalLog.error("Fehler bei Weiterleitung eines Events an einen Observer. Observer: " + eventObserver.getName(), e);
                            }
                            break;
                        }
                    }
                }
            }
        }
        updatingObservers = false;
    }

    /**
     * Lookup for the EventType by className of NotEOFEvent
     * 
     * @param className
     *            the class name (canonical if possible)
     * @return The EventType that is stored at a member of the class NotEOFEvent
     * @throws ActionFailedException
     *             Thrown if EventType could not be evaluated (e.g. because of
     *             the NotEOFEvent class was not found.
     */
    public static EventType lookForEventType(String className) throws ActionFailedException {
        if (className.startsWith("Alias:")) {
            return EventType.EVENT_GENERIC;
        }

        NotEOFEvent event = EventFinder.getNotEOFEvent(Server.getApplicationHome(), className);
        return event.getEventType();
    }

    /**
     * Delivers a new Generic Event with fix configured attributes
     * 
     * @param aliasClassName
     *            The className of the Event like configured as 'own' Event. The
     *            className maybe contains 'Alias:'. Then this Part must be
     *            erased...
     * @param aliasCleaned
     *            If the aliasClassName was cleaned of the prefix 'Alias:' this
     *            must be true, else false.
     * @return
     */
    public static NotEOFEvent getGenericEvent(String aliasClassName, boolean aliasCleaned) {
        if (!aliasCleaned) {
            // Alias: = 6 signs
            aliasClassName = aliasClassName.substring(6);
        }

        // read configuration of this own event
        String xmlPath = "ownEvents";

        NotEOFConfiguration localConf = new LocalConfiguration();
        try {
            Map<String, String> descriptions = new HashMap<String, String>();
            Map<String, String> attributes = new HashMap<String, String>();
            xmlPath = "ownEvents." + aliasClassName;
            List<String> confAttributes = localConf.getTextList(xmlPath + ".attribute");
            for (String attribute : confAttributes) {
                // keyName="firstName" keyValue="Dirk"
                // descriptionKey="firstName" descriptionValue="Vorname"
                String keyName = localConf.getAttribute(xmlPath + attribute, "keyName");
                String keyValue = localConf.getAttribute(xmlPath + attribute, "keyValue");
                String descriptionKey = localConf.getAttribute(xmlPath + attribute, "descriptionKey");
                String descriptionValue = localConf.getAttribute(xmlPath + attribute, "descriptionValue");

                descriptions.put(descriptionKey, descriptionValue);
                attributes.put(keyName, keyValue);

            }
            NotEOFEvent genericEvent = new GenericEvent();
            genericEvent.setDescriptions(descriptions);
            genericEvent.setAttributes(attributes);
            return genericEvent;
        } catch (ActionFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Only for tests...
     * 
     * @param bla
     * @param cal
     */
    public static void formatCal(String bla, Calendar cal) {

        String format = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.GERMAN);
        format += " der " + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        format += "." + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.GERMAN);
        format += "." + String.valueOf(cal.get(Calendar.YEAR));
        format += " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        format += ":" + String.valueOf(cal.get(Calendar.MINUTE));
        format += ":" + String.valueOf(cal.get(Calendar.SECOND));

        System.out.println(bla + "... " + format);
    }

}