package de.happtick.core.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

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
     * <br>
     * Diese Methode ist speziell fuer Anwendungen gedacht, die mit dem
     * Happtick-Framework erstellt wurden und durch den Scheduler gestartet
     * werden sollen. <br>
     * Zum Start sonstiger Klassen (z.B. Server) muss die Method call()
     * verwendet werden.
     * 
     * @param className
     *            Name der Klasse (z.B. de.happtick.core.LocalApplStarter)
     * @param args
     *            Aufrufargumente (z.B. --applicationPath=/home/appl.sh)
     */
    public void callHapptickMain(String className, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent) throws HapptickException {
        String applicationId = null;
        String applicationPath = null;
        String startIgnitionTime = null;
        String arguments = null;
        String windowsSupport = null;

        applicationPath = startEvent.getAttribute("applicationPath");
        applicationId = String.valueOf(startEvent.getApplicationId());
        windowsSupport = startEvent.getAttribute("windowsSupport");
        arguments = startEvent.getAttribute("arguments");
        startIgnitionTime = startEvent.getAttribute("startIgnitionTime");

        if (Util.isEmpty(applicationId))
            throw new HapptickException(650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new HapptickException(650L, "applicationPath");

        int arrSize = 7;
        if (null != arguments)
            arrSize = 8;
        String[] args = new String[arrSize];
        args[0] = "--applicationPath=" + applicationPath;
        args[1] = "--applicationId=" + applicationId;
        args[2] = "--startId=" + startId;
        args[3] = "--serverAddress=" + serverAddress;
        args[4] = "--serverPort=" + String.valueOf(serverPort);
        args[5] = "--startIgnitionTime=" + startIgnitionTime;
        args[6] = "--windowsSupport=" + String.valueOf(windowsSupport);
        if (null != arguments)
            args[7] = arguments;

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

    public void call(String className, String[] args) {
        LocalLog.info("Externe Anwendung wird gestartet: " + className);
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
     * Starts an Application.
     * <p>
     * This can be a Happtick Java Application or an Application of type UNKNOWN
     * (not developed by using Happtick framework).
     * 
     * @param applicationPath
     * @param arguments
     * @param windowsSupport
     *            if true the application will be started with 'cmd /c
     *            start/wait'. If this is used the windows script should end
     *            with 'exit' as last command
     * @return
     * @throws HapptickException
     */
    public Process startApplication(String applicationPath, String arguments, boolean windowsSupport) throws HapptickException {
        System.gc();
        Process proc = null;

        // ProcessBuilder pb = new ProcessBuilder("java", "de.happdemo.Actor",
        // "--serverIp=localhost", "--serverPort=3000", "--applicationId=1",
        // "--soundFile=c:\\Projekte\\workspace\\happdemo\\soundfiles\\a.wav");
        // Map<String, String> env = pb.environment();
        // env.put("NOTEOF_HOME", "C:\\Projekte\\workspace\\noteof");
        // env.put("LIB_PATH", "C:\\Projekte\\workspace\\notioc\\lib");
        // env.put("CLASSPATH", env.get("NOTEOF_HOME") + "\\lib\\noteof.jar");
        // env.put("CLASSPATH", env.get("CLASSPATH") + ";" + env.get("LIB_PATH")
        // + "\\notioc.jar");
        // env.put("CLASSPATH", env.get("CLASSPATH") + ";" + env.get("LIB_PATH")
        // + "\\jdom.jar");
        // env.put("CLASSPATH", env.get("CLASSPATH") +
        // ";c:\\Projekte\\workspace\\happtick\\lib\\happtick.jar");
        // env.put("CLASSPATH", env.get("CLASSPATH") +
        // ";c:\\Projekte\\workspace\\happdemo\\bin");
        //
        // Set<String> bla = env.keySet();
        // for (String key : bla) {
        // System.out.println("KEY= " + key + "     VAL= " + env.get(key));
        // }
        //
        // try {
        // proc = pb.start();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        System.out.println("ExternalCalls.startApplication...");
        try {
            Runtime runtime = Runtime.getRuntime();

            String cmdLine = applicationPath + " " + arguments;

            if (windowsSupport)
                cmdLine = "cmd /c start /wait \"\" " + cmdLine;

            cmdLine.trim();
            proc = runtime.exec(cmdLine);
        } catch (IOException ioEx) {
            throw new HapptickException(651L, "Application: " + applicationPath, ioEx);
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
            applicationId = String.valueOf(startEvent.getApplicationId());
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

        boolean windowsSupport = Util.parseBoolean(startEvent.getAttribute("windowsSupport"), false);

        LocalLog.info("Happtick Application Starting. ApplicationId: " + applicationId + "; ApplicationPath: " + applicationPath + "; Arguments: " + arguments);
        return startApplication(applicationPath, args, windowsSupport);
    }
}