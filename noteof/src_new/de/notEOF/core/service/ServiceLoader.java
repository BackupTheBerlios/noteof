package de.notEOF.core.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import de.notEOF.core.configuration.ConfigurationManager;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

/**
 * Dynamically loads Objects of type BaseService.<br>
 * The Classes must be stored within jar files in the lib directory
 * ($NOTEOF_HOME/lib) or saved in the path bin directory ($NOTEOF/bin).
 * 
 * @author Dirk
 * 
 */
public class ServiceLoader {

    @SuppressWarnings("unchecked")
    public synchronized static BaseService getServiceObject(String className) throws ActionFailedException {
        if (Util.isEmpty(className))
            throw new ActionFailedException(150L, "Fehlende Angabe des Klassennamen.");

        ClassLoader classLoader = new URLClassLoader(getLibs(), ServiceLoader.class.getClassLoader());
        try {
            Class<BaseService> classBaseService = (Class<BaseService>) Class.forName(className, true, classLoader);
            BaseService newService;
            newService = classBaseService.newInstance();
            return newService;

        } catch (ClassNotFoundException e) {
            throw new ActionFailedException(150L,
                    "Unbekannte Service-Klasse oder Klasse nicht gefunden. Bibliotheken, CLASS_PATH, NOTEOF_HOME-Umgebungsvariable prüfen: " + className);
        } catch (InstantiationException e) {
            throw new ActionFailedException(150L, "Es konnte keine Instanz für die Service-Klasse gebildet werden: " + className);
        } catch (IllegalAccessException e) {
            throw new ActionFailedException(150L, "Zugriff auf Konstruktor der Klasse nicht möglich: " + className);
        }
    }

    protected static URL[] getLibs() throws ActionFailedException {
        ArrayList<URL> urls = new ArrayList<URL>();

        String home = "";
        try {
            home = ConfigurationManager.getInstance().getNotEOFHome();
        } catch (Exception ex) {
            throw new ActionFailedException(150L, "Home Verzeichnis konnte nicht ermittelt werden", ex);
        }

        File notEOFDir = new File(home);
        if (!notEOFDir.exists())
            throw new ActionFailedException(150L, String.format("Home Verzeichnis '%1s' existiert nicht", notEOFDir.getAbsolutePath()));

        File binDir = new File(home, "bin");
        if (binDir.isDirectory()) {
            try {
                URI uri = binDir.toURI();
                URL url = uri.toURL();
                urls.add(url);
            } catch (MalformedURLException ex) {
                LocalLog.warn("Problem bei Zugriff auf das bin Verzeichnis: " + binDir.getAbsolutePath(), ex);
            }
        }

        File libDir = new File(home, "lib");
        if (!libDir.isDirectory())
            throw new ActionFailedException(150L, String.format("Verzeichnis $NOTEOFHOME/'%1s' does not exist", libDir.getAbsolutePath()));

        File[] libs = libDir.listFiles();
        for (File lib : libs) {
            if (lib.getName().toLowerCase().endsWith(".jar")) {
                try {
                    URI uri = libDir.toURI();
                    URL url = uri.toURL();
                    urls.add(url);
                } catch (MalformedURLException ex) {
                    LocalLog.warn("Problem bei Zugriff auf das lib Verzeichnis: " + libDir.getAbsolutePath(), ex);
                }
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }
}
