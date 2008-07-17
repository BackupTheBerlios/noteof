package de.notEOF.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Diese Klasse dient dem Parsen von Start-Parametern die mit "-" anfangen.
 */

public class ArgsParser {

    /**
     * Liste der Argumente, welche keine Parameter sind.<br>
     * Ein Argument definiere ich so:<br>
     * Alle Werte, die beim Start übergeben wurden.
     */
    private final List<String> argumentList = new ArrayList<String>();

    /**
     * Liste der Parameter.<br>
     * Parameter definiere ich so: Ein Parameter ist ein Argument, welches mit
     * "-" oder "--" beginnt.<br>
     * Ein Parameter mit "-" besteht im allgemeinen aus genau einem weiteren
     * Zeichen. z.B. "-s".<br>
     * Ein Parameter mit "--" besteht grundsätzlich aus "--", einem Wort, einem
     * "="-Zeichen und einem Wert. z.B. "--verbose=true".
     */
    private final List<String> parameterList = new ArrayList<String>();

    private String args[];

    /**
     * @param args
     */
    public ArgsParser(String[] args) {
        this.args = args;
        if (args != null) {
            for (String arg : args) {
                if (arg != null && arg.startsWith("-")) {
                    parameterList.add(arg);
                } else {
                    argumentList.add(arg);
                }
            }
        }
    }

    /**
     * Prüft ob ein Parameter in den Argumenten enthalten ist, der mit dem
     * übergebenen param anfängt.
     * 
     * @param param
     * @return
     */
    public boolean containsStartsWith(String param) {
        if (null != param) {
            for (String arg : parameterList) {
                if (arg.startsWith(param)) {
                    return true;
                }
            }
        }
        // return argList.contains(param);
        return false;
    }

    /**
     * Prüft ob der übergebene Parameter in den Argumenten enthalten ist.
     * 
     * @param param
     * @return
     */
    public boolean contains(String param) {
        return parameterList.contains(param);
    }

    /**
     * Zählt wie oft der übergebene Parameter in den Argumenten enthalten ist.
     * 
     * @param param
     * @return
     */
    public int count(String param) {
        int result = 0;
        if (param != null) {
            for (String arg : parameterList) {
                if (param.equals(arg)) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Zählt wie oft der übergebene Parameter in den Argumenten enthalten ist
     * und liefert true wenn es eine GERADE Anzahl ist.
     * 
     * @param param
     * @return
     */
    public boolean isEven(String param) {
        return count(param) % 2 == 0;
    }

    /**
     * Zählt wie oft der übergebene Parameter in den Argumenten enthalten ist
     * und liefert true wenn es eine UNGERADE Anzahl ist.
     * 
     * @param param
     * @return
     */
    public boolean isOdd(String param) {
        return count(param) % 2 == 1;
    }

    /**
     * Holt den (ersten) Wert anhand des param: --param=wert
     * 
     * @param param
     * @return - wert oder null
     */
    public String getValue(String param) {
        if (param != null) {
            String key = "--" + param + "=";
            for (String arg : parameterList) {
                if (arg != null && arg.startsWith(key)) {
                    return arg.substring(key.length());
                }
            }
        }
        return null;
    }

    /**
     * Holt den (ersten) Wert anhand des param: param=wert <br>
     * In dieser Variante wird das -- nicht zwingend vorgeschrieben
     * 
     * @param param
     * @return - wert oder null
     */
    public String getValue2(String param) {
        if (param != null) {
            String key = param + "=";
            for (String arg : parameterList) {
                if (arg != null && arg.startsWith(key)) {
                    return arg.substring(key.length());
                }
            }
        }
        return null;
    }

    /**
     * Entfernt alle Parameter die mit <code>param</code> beginnen aus der
     * Parameter-Liste.
     * 
     * @param param
     */
    public void removeParameterAll(String param) {
        if (param != null) {
            Iterator<String> iter = parameterList.iterator();
            while (iter.hasNext()) {
                if (iter.next().startsWith(param))
                    iter.remove();
            }
        }
    }

    /**
     * Entfernt den ersten Parameter, der mit <code>param</code> beginnt aus der
     * Parameter-Liste.
     * 
     * @param param
     */
    public void removeParameterFirst(String param) {
        if (param != null) {
            Iterator<String> iter = parameterList.iterator();
            while (iter.hasNext()) {
                if (iter.next().startsWith(param)) {
                    iter.remove();
                    return;
                }
            }
        }
    }

    /**
     * Entfernt das erste exakte Vorkommen von <code>arg</code> aus der
     * Argument-Liste.
     * 
     * @param param
     */
    public void removeArgumentFirst(String arg) {
        argumentList.remove(arg);
    }

    /**
     * Entfernt alle (exakten) Vorkommen von <code>arg</code> aus der
     * Argument-Liste.
     * 
     * @param param
     */
    public void removeArgumentAll(String arg) {
        while (argumentList.contains(arg)) {
            argumentList.remove(arg);
        }
    }

    /**
     * Holt alle Werte anhand des param: --param=wert1 --param=wert2 ...
     * 
     * @param param
     * @return - wert oder null
     */
    public List<String> getValues(String param) {
        List<String> result = new ArrayList<String>();
        if (param != null) {
            String key = "--" + param + "=";
            for (String arg : parameterList) {
                if (arg != null && arg.startsWith(key)) {
                    result.add(arg.substring(key.length()));
                }
            }
        }
        return result;
    }

    /**
     * Listet alle Argumente auf, welche mit "-" beginnen.
     * 
     * @return
     */
    public List<String> getParameters() {
        return new ArrayList<String>(parameterList);
    }

    /**
     * Listet alle Argumente auf, welche NICHT mit "-" beginnen. (Die Odd-List)
     * 
     * @return
     */
    public List<String> getArguments() {
        return new ArrayList<String>(argumentList);
    }

    /**
     * Liefert die im Konstruktor uebergebenen args[] unveraendert zurueck.
     * 
     * @return Der originale args-Wert, wie im Konstruktor reingereicht.
     */
    public String[] getOriginalArgs() {
        return args;
    }

    /**
     * Liefert alle Argumente gefolgt von allen Parametern als Array.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public String[] getArgs() {
        ArrayList args = new ArrayList<String>();
        args.addAll(argumentList);
        args.addAll(parameterList);
        return (String[]) args.toArray(new String[0]);
    }

}