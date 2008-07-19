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
            throw new ActionFailedException(150L, "Zugriff auf Konstruktor der Klasse nicht mï¿½glich: " + className);
        }
    }

    // @SuppressWarnings("unchecked")
    // public synchronized static Service getService(String notEof_Home, String
    // className) throws ActionFailedException {
    // if (Util.isEmpty(className))
    // throw new ActionFailedException(150L,
    // "Fehlende Angabe des Klassennamen.");
    //
    // ClassLoader classLoader = new URLClassLoader(getLibs(notEof_Home),
    // ServiceLoader.class.getClassLoader());
    // try {
    // Class<Service> classBaseService = (Class<Service>)
    // Class.forName(className, true, classLoader);
    // Service newService;
    // newService = classBaseService.newInstance();
    // return newService;
    //
    // } catch (ClassNotFoundException e) {
    // throw new ActionFailedException(150L,
    // "Unbekannte Service-Klasse oder Klasse nicht gefunden. Bibliotheken, CLASS_PATH, NOTEOF_HOME-Umgebungsvariable prï¿½fen: "
    // + className);
    // } catch (InstantiationException e) {
    // throw new ActionFailedException(150L,
    // "Es konnte keine Instanz fï¿½r die Service-Klasse gebildet werden: " +
    // className);
    // } catch (IllegalAccessException e) {
    // throw new ActionFailedException(150L,
    // "Zugriff auf Konstruktor der Klasse nicht mï¿½glich: " + className);
    // } catch (Exception e) {
    // throw new ActionFailedException(150L, "Problem bei Laden der Klasse: " +
    // className);
    // }
    // }
    //
    // protected static URL[] getLibs(String notEof_Home) throws
    // ActionFailedException {
    // ArrayList<URL> urls = new ArrayList<URL>();
    //
    // File notEOFDir = new File(notEof_Home);
    // if (!notEOFDir.exists())
    // throw new ActionFailedException(150L,
    // String.format("Home Verzeichnis '%1s' existiert nicht",
    // notEOFDir.getAbsolutePath()));
    //
    // File libDir = new File(notEof_Home, "lib");
    // if (!libDir.isDirectory())
    // throw new ActionFailedException(150L,
    // String.format("Verzeichnis $NOTEOF_HOME/'%1s' does not exist",
    // libDir.getAbsolutePath()));
    //
    // File[] libs = libDir.listFiles();
    // for (File lib : libs) {
    // if (lib.getName().toLowerCase().endsWith(".jar")) {
    // try {
    // URI uri = libDir.toURI();
    // URL url = uri.toURL();
    // urls.add(url);
    // } catch (MalformedURLException ex) {
    // LocalLog.warn("Problem bei Zugriff auf das lib Verzeichnis: " +
    // libDir.getAbsolutePath(), ex);
    // }
    // }
    // }
    //
    // File binDir = new File(notEof_Home, "bin");
    // if (binDir.isDirectory()) {
    // try {
    // URI uri = binDir.toURI();
    // URL url = uri.toURL();
    // urls.add(url);
    // } catch (MalformedURLException ex) {
    // LocalLog.warn("Problem bei Zugriff auf das bin Verzeichnis: " +
    // binDir.getAbsolutePath(), ex);
    // }
    // }
    //
    // return urls.toArray(new URL[urls.size()]);
    // }
}
