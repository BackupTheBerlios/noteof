package de.noteof.core.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import de.noteof.core.configuration.ConfigurationManager;
import de.noteof.core.exception.ActionFailedException;
import de.noteof.core.logging.LocalLog;

/**
 * Dynamically loads Objects of type BaseService.<br>
 * The Classes must be stored within jar files in the lib directory
 * ($NOTEOF_HOME/lib) or saved in the path bin directory ($NOTEOF/bin).
 * 
 * @author Dirk
 * 
 */
public class ServiceLoader {
    // ClassLoader classLoader = new URLClassLoader(IccsLoaderEnv.getLibs(),
    // Marvin.class.getClassLoader());
    // Thread.currentThread().setContextClassLoader(classLoader);
    // Properties version = new Properties();
    // version.load(classLoader.getResourceAsStream("etc/VERSION"));

    protected static URL[] getLibs() throws ActionFailedException {
        ArrayList<URL> urls = new ArrayList<URL>();

        String home = "";
        try {
            home = ConfigurationManager.getInstance().getNotEofHome();
        } catch (Exception ex) {
            throw new ActionFailedException(150L, "Home Verzeichnis konnte nicht ermittelt werden", ex);
        }

        File noteofDir = new File(home);
        if (!noteofDir.exists())
            throw new ActionFailedException(150L, String.format("Home Verzeichnis '%1s' existiert nicht", noteofDir.getAbsolutePath()));

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
