package de.notEOF.core.event;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.util.NotEOFClassFinder;
import de.notEOF.core.util.Util;

/**
 * Dynamically loads Objects of type NotEOFEvent.<br>
 * The Classes must be stored within jar files in the lib directory (e.g.
 * $NOTEOF_HOME/lib) or saved in the path bin directory (e.g. $NOTEOF/bin).
 * 
 * @author Dirk
 * 
 */
public class EventFinder {

    @SuppressWarnings("unchecked")
    public synchronized static NotEOFEvent getNotEOFEvent(String notEof_Home, String className) throws ActionFailedException {
        if (Util.isEmpty(className))
            throw new ActionFailedException(1152L, "Fehlende Angabe des Klassennamen.");

        Class<NotEOFEvent> classEvent = (Class<NotEOFEvent>) NotEOFClassFinder.getClass(notEof_Home, className);
        NotEOFEvent newEvent;
        try {
            newEvent = classEvent.newInstance();
            return newEvent;
        } catch (InstantiationException e) {
            throw new ActionFailedException(1152L, "Es konnte keine Instanz für die Event-Klasse gebildet werden: " + className);
        } catch (IllegalAccessException e) {
            throw new ActionFailedException(1152L, "Zugriff auf Konstruktor der Klasse nicht möglich: " + className);
        }
    }
}
