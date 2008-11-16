package de.happtick.core.util;

import java.io.IOException;
import java.lang.reflect.Method;

import de.happtick.core.exception.HapptickException;
import de.notEOF.core.interfaces.NotEOFEvent;
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
    public void call(String className, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent) throws HapptickException {
        String applicationId = null;
        String applicationPath = null;
        String arguments = null;
        try {
            applicationPath = startEvent.getAttribute("applicationPath");
            applicationId = startEvent.getAttribute("applicationId");
            arguments = startEvent.getAttribute("arguments");
        } catch (Exception ex) {
            LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
        }

        if (Util.isEmpty(applicationId))
            throw new HapptickException(650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new HapptickException(650L, "applicationPath");

        int arrSize = 5;
        if (null != arguments)
            arrSize = 6;
        String[] args = new String[arrSize];
        args[0] = "--applicationPath=" + applicationPath;
        args[1] = "--applicationId=" + applicationId;
        args[2] = "--startId=" + startId;
        args[3] = "--serverAddress=" + serverAddress;
        args[4] = "--serverPort=" + String.valueOf(serverPort);
        if (null != arguments)
            args[5] = arguments;

        for (String arg : args) {
            System.out.println("ARGS... arg: " + arg);
        }

        // call the main method of the class with evaluated arguments
        LocalLog.info("External Application Starting. ApplicationId: " + applicationId + "; ApplicationPath: " + applicationPath + "; Arguments: " + arguments);
        try {
            Class<?> clazz = Class.forName(className);
            Method methode = clazz.getMethod("main", new Class[] { args.getClass() });
            methode.invoke(null, new Object[] { args });
            LocalLog.info("External Application Finished.  ApplicationId: " + applicationId + "; ApplicationPath: " + applicationPath + "; Arguments: "
                    + arguments);
        } catch (ClassNotFoundException clEx) {
            LocalLog.warn("Klasse nicht gefunden: " + className);
        } catch (Exception ex) {
            LocalLog.error("Error in call", ex);
        }
    }

    /**
     * Starts an Application.
     * <p>
     * This can be a Happtick Java Application or an Application of type UNKNOWN
     * (not developed by using Happtick framework).
     * 
     * @param applicationPath
     * @param arguments
     * @return
     * @throws HapptickException
     */
    public Process startApplication(String applicationPath, String arguments) throws HapptickException {
        Process proc = null;
        // Runtime runtime = Runtime.getRuntime();
        try {
            String[] cmdLine = new String[2];
            cmdLine[0] = applicationPath.trim();
            cmdLine[1] = arguments.trim();
            System.out.println("ExternalCalls.start... vor proc = runtime...");

            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            proc = builder.start();

            // proc = runtime.exec(cmdLine);
        } catch (IOException ioEx) {
            throw new HapptickException(651L, "Application: " + applicationPath);
        }
        return proc;
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
    public Process startHapptickApplication(String serverAddress, int serverPort, String startId, NotEOFEvent startEvent) throws HapptickException {
        String applicationId = null;
        String applicationPath = null;
        String arguments = null;
        try {
            applicationPath = startEvent.getAttribute("applicationPath");
            applicationId = startEvent.getAttribute("applicationId");
            arguments = startEvent.getAttribute("arguments");
        } catch (Exception ex) {
            LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
        }

        if (Util.isEmpty(applicationId))
            throw new HapptickException(650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new HapptickException(650L, "applicationPath");

        // special Happtick parameter for own Java applications
        String args = "--startId=" + startId.trim();
        args += " --applicationId=" + applicationId.trim();
        args += " --serverAddress=" + serverAddress.trim();
        args += " --serverPort=" + String.valueOf(serverPort);
        args += " " + arguments;

        System.out.println("ExternalCalls.startHapptickApplication: applicationPath = " + applicationPath);
        System.out.println("ExternalCalls.startHapptickApplication: args = " + args);

        LocalLog.info("Happtick Application Starting. ApplicationId: " + applicationId + "; ApplicationPath: " + applicationPath + "; Arguments: " + arguments);
        return startApplication(applicationPath, arguments);
    }
}