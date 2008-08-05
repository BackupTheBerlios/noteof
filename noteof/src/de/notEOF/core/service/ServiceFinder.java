package de.notEOF.core.service;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.util.NotEOFClassFinder;
import de.notEOF.core.util.Util;

/**
 * Dynamically loads Objects of type BaseService.<br>
 * The Classes must be stored within jar files in the lib directory
 * ($NOTEOF_HOME/lib) or saved in the path bin directory ($NOTEOF/bin).
 * 
 * @author Dirk
 * 
 */
public class ServiceFinder {

    @SuppressWarnings("unchecked")
    public synchronized static Service getService(String notEof_Home, String className) throws ActionFailedException {
        if (Util.isEmpty(className))
            throw new ActionFailedException(150L, "Fehlende Angabe des Klassennamen.");

        Class<Service> classService = (Class<Service>) NotEOFClassFinder.getClass(notEof_Home, className);
        Service newService;
        try {
            newService = classService.newInstance();
            return newService;
        } catch (InstantiationException e) {
            throw new ActionFailedException(150L, "Es konnte keine Instanz für die Service-Klasse gebildet werden: " + className);
        } catch (IllegalAccessException e) {
            throw new ActionFailedException(150L, "Zugriff auf Konstruktor der Klasse nicht möglich: " + className);
        }
    }
}
