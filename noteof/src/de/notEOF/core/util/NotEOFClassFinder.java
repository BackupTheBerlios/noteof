package de.notEOF.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;

/**
 * Dynamically loads Classes of 'any' type.<br>
 * The Classes must be stored within jar files in the lib directory
 * ($NOTEOF_HOME/lib) or saved in the path bin directory ($NOTEOF/bin).
 * 
 * @author Dirk
 * 
 */
public class NotEOFClassFinder {

    public synchronized static Class<?> getClass(String notEof_Home, String className) throws ActionFailedException {
        List<String> paths = new ArrayList<String>();
        paths.add("");
        paths.add("de.notEOF.core.event");
        paths.add("de.happtick.core.event");

        NotEOFConfiguration conf = new LocalConfiguration();
        paths.addAll(conf.getTextList("additional.classPath"));

        for (String path : paths) {
            try {
                if (!Util.isEmpty(path))
                    path += ".";
                Class<?> theClass = getClassInternal(notEof_Home, path + className);
                return theClass;
            } catch (ActionFailedException e) {
            }
        }
        throw new ActionFailedException(150L,
                "Unbekannte Klasse oder Klasse nicht gefunden. Bibliotheken, CLASS_PATH, NOTEOF_HOME-Umgebungsvariable pruefen. Klasse: " + className);
    }

    private synchronized static Class<?> getClassInternal(String notEof_Home, String className) throws ActionFailedException {
        if (Util.isEmpty(className))
            throw new ActionFailedException(150L, "Fehlende Angabe des Klassennamen.");

        URL[] libs = getLibs(notEof_Home);

        ClassLoader classLoader = new URLClassLoader(libs, NotEOFClassFinder.class.getClassLoader());
        try {
            Class<?> clazz = (Class<?>) Class.forName(className, true, classLoader);
            return clazz;

        } catch (ClassNotFoundException e) {
            throw new ActionFailedException(150L,
                    "Unbekannte Klasse oder Klasse nicht gefunden. Bibliotheken, CLASS_PATH, NOTEOF_HOME-Umgebungsvariable pr�fen. Klasse: " + className, e);
        } catch (Exception e) {
            throw new ActionFailedException(150L, "Klasse: " + className);
        }
    }

    protected static URL[] getLibs(String notEof_Home) throws ActionFailedException {
        ArrayList<URL> urls = new ArrayList<URL>();

        try {
            File notEOFDir = new File(notEof_Home);
            if (!notEOFDir.exists())
                throw new ActionFailedException(150L, String.format("Home Verzeichnis '%1s' existiert nicht", notEOFDir.getAbsolutePath()));

            File libDir = new File(notEof_Home, "lib");
            if (!libDir.isDirectory())
                throw new ActionFailedException(150L, String.format("Verzeichnis $NOTEOF_HOME/'%1s' does not exist", libDir.getAbsolutePath()));

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

            File binDir = new File(notEof_Home, "bin");
            if (binDir.isDirectory()) {
                try {
                    URI uri = binDir.toURI();
                    URL url = uri.toURL();
                    urls.add(url);
                } catch (MalformedURLException ex) {
                    LocalLog.warn("Problem bei Zugriff auf das bin Verzeichnis: " + binDir.getAbsolutePath(), ex);
                }
            }

            return urls.toArray(new URL[urls.size()]);
        } catch (Exception e) {
            throw new ActionFailedException(153L, "notEof_Home ist hier: " + notEof_Home, e);
        }
    }
}
