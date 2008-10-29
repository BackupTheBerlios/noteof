package de.notEOF.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notIOC.logging.LocalLog;

/**
 * When used several times in one file use this: import static
 * de.iccs.util.Util.*; And access the Util.*-methods directly.
 */
public class Util {
    // private static long allEventsCounter = 0;
    // private static Date startDate = new Date();
    private static Thread consoleWaitThread;

    private Util() {
    }

    /**
     * Parses elements of the string which are delimited by the delimiter.
     * 
     * @param simpleString
     *            The textual list.
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
     * Prï¿½ft ob object NULL, ein Leerstring (getrimmt), ein leeres Array
     * (length == 0) oder eine leere Collection ist.
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean isEmpty(Object object) {
        if (object == null)
            return true;
        if (object instanceof String)
            return isEmpty((String) object);
        if (object.getClass().isArray())
            return isEmpty((Object[]) object);
        if (object instanceof Collection)
            return ((Collection) object).isEmpty();
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
     * Prueft, ob ein Argument gesetzt wurde. <br>
     * Die Pruefung ist hier genau, d.h., das Argument muss exakt dem
     * Suchbegriff entsprechen. <br>
     * Bsp.: args[0] = argument, argName = argument -> Treffer <br>
     * args[0] = argument=x, argName = argument -> Kein Treffer <br>
     * args[0] = --argument, argName = argument -> Kein Treffer
     * 
     * @param args
     *            Array der zu untersuchenden Strings
     * @param argName
     *            Der exakt zu matchende Suchbegriff
     * @return
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
     * Anschlieï¿½end wird ein equalsIgnoreCase durchgefï¿½hrt. <br>
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

    public static void registerForEvents(Map<String, EventObserver> eventObservers, EventObserver eventObserver) {
        // if (null == eventObservers)
        // eventObservers = new HashMap<String, EventObserver>();
        eventObservers.put(eventObserver.getName(), eventObserver);
    }

    public static void unregisterFromEvents(Map<String, EventObserver> eventObservers, EventObserver eventObserver) {
        if (null != eventObservers && null != eventObserver) {
            try {
                eventObservers.remove(eventObserver.getName());
            } catch (Exception e) {
                LocalLog.warn("EventObserver konnte nicht entfernt werden: " + eventObserver.getName(), e);
            }
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
    public synchronized static void updateAllObserver(Map<String, EventObserver> eventObservers, Service service, NotEOFEvent event) {
        if (null == event)
            return;
        if (null == eventObservers)
            return;

        // if (null != service)
        // System.out.println("Auslösender Service: " +
        // service.getClass().getSimpleName());
        // System.out.println("Ausgelöstes Event:   " + event.getEventType());

        // if (allEventsCounter++ > 1000) {
        // Date newDate = new Date();
        // long millis = newDate.getTime() - startDate.getTime();
        // long millisPerRec = millis / allEventsCounter;
        // System.out.println("Alle Events bisher: " + allEventsCounter +
        // "; Millis pro Event: " + millisPerRec);
        // }

        boolean retry = true;

        // all observer
        if (eventObservers.size() > 0) {
            while (retry) {
                retry = false;
                Set<String> set = eventObservers.keySet();
                for (String observerName : set) {
                    // but only inform observer, when event in his list
                    EventObserver eventObserver = eventObservers.get(observerName);
                    if (null != eventObserver && null != eventObserver.getObservedEvents()) {
                        for (EventType type : eventObserver.getObservedEvents()) {
                            if (type.equals(EventType.EVENT_ANY_TYPE) || type.equals(event.getEventType())) {
                                try {
                                    eventObserver.update(service, event);
                                } catch (Exception e) {
                                    // eventObservers.remove(observerName);
                                    LocalLog.error("Fehler bei Weiterleitung eines Events an einen Observer. Observer: " + eventObserver.getName(), e);
                                }
                                // break for inner loop
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}