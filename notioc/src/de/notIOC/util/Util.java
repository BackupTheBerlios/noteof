package de.notIOC.util;

import java.util.Collection;

/**
 * When used several times in one file use this: import static
 * de.iccs.util.Util.*; And access the Util.*-methods directly.
 */
public class Util {

    private static Thread consoleWaitThread;

    private Util() {
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

    /**
     * Startet ein animiertes Warten auf der Konsole. <br>
     * Dort wird ein sich drehendes Symbol gezeigt. <br>
     * Das Rotationsevent endet mit Aufruf der Methode stopWait().
     */
    public static void startWait() {
        if (consoleWaitThread == null) {
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

}