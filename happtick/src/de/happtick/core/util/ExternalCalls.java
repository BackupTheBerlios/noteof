package de.happtick.core.util;

import java.io.IOException;
import java.lang.reflect.Method;

import de.happtick.core.exception.HapptickException;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

/**
 * Diese Klasse soll den Aufruf von externen Anwendungen und Prozessen
 * vereinheitlichen und vereinfachen. <br>
 * Sollte fuer weitere Aufgaben erweitert werden.
 */
public class ExternalCalls {

    /**
     * Ruft eine andere main()-Methode auf.
     * <p>
     * Dabei koennen zusaetzlich Aufrufargumente (args[]) mit angegeben werden.
     * 
     * @param className
     *            Name der Klasse (z.B. de.happtick.core.LocalApplStarter)
     * @param args
     *            Aufrufargumente (z.B. --applicationPath=/home/appl.sh)
     */
    public static void call(String className, String applicationPath, String applicationId, String startId, String serverAddress, String serverPort,
            String arguments) {

        String[] args = new String[99];
        args[0] = "--applicationPath=" + applicationPath;
        args[1] = "--applicationId=" + applicationId;
        args[2] = "--startId=" + startId;
        args[3] = "--serverAddress=" + serverAddress;
        args[4] = "--serverPort=" + serverPort;
        args[5] = arguments;

        try {
            Class<?> clazz = Class.forName(className);
            Method methode = clazz.getMethod("main", new Class[] { args.getClass() });
            methode.invoke(null, new Object[] { args });
        } catch (ClassNotFoundException clEx) {
            LocalLog.warn("Klasse nicht gefunden: " + className);
        } catch (Exception ex) {
            LocalLog.error("Error in call", ex);
        }
    }

    /**
     * Starts an external application
     * <p>
     * 
     * @param applicationPath
     *            Complete path of application.
     * @param applicationId
     *            Like stored in central configuration.
     * @param startId
     *            Calculated by the this calling process.
     * @param serverAddress
     *            !EOF-Server address to connect to.
     * @param serverPort
     *            !EOF-Server port for connecting.
     * @param arguments
     *            Additional calling arguments. Depend to the application.
     * @return The started Process
     */
    public static Process startHapptickApplication(String applicationPath, String startId, String serverAddress, String serverPort, String[] arguments)
            throws HapptickException {
        Process proc = null;
        // nur weitermachen, wenn auch eine Anwendung eingetragen wurde...
        if (Util.isEmpty(applicationPath))
            throw new HapptickException(650L, "applicationPath");

        Runtime runtime = Runtime.getRuntime();
        try {
            String[] cmdLine = new String[2];
            cmdLine[0] = applicationPath;

            // build arguments
            // String args = "--applicationId=" + applicationId.trim();
            String args = "--startId=" + startId.trim();
            args += " --serverAddress=" + serverAddress.trim();
            args += " --serverPort=" + serverPort.trim();
            args += " " + arguments;
            cmdLine[1] = args.trim();
            proc = runtime.exec(cmdLine);
        } catch (IOException ioEx) {
            throw new HapptickException(651L, "Application: " + applicationPath);
        }
        return proc;
    }
}