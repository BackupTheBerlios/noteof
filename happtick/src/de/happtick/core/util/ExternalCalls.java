package de.happtick.core.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.notEOF.core.exception.ActionFailedException;
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
    public void callHapptickApplMain(String className, String serverAddress, int serverPort, String startId, NotEOFEvent startEvent) throws ActionFailedException {
        String applicationId = null;
        String applicationPath = null;
        String startIgnitionTime = null;
        String arguments = null;
        String windowsSupport = null;

        applicationPath = startEvent.getAttribute("applicationPath");
        applicationId = String.valueOf(startEvent.getAttribute("workApplicationId"));
        windowsSupport = startEvent.getAttribute("windowsSupport");
        arguments = startEvent.getAttribute("arguments");
        startIgnitionTime = startEvent.getAttribute("startIgnitionTime");

        if (Util.isEmpty(applicationId))
            throw new ActionFailedException(10650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new ActionFailedException(10650L, "applicationPath");

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

    public Process startApplication(String serverAddress, int serverPort, String startId, NotEOFEvent event) throws ActionFailedException {
        System.out.println("ExternalCalls windowsSupport? " + event.getAttribute("windowsSupport"));
        if (Util.parseBoolean(event.getAttribute("windowsSupport"), false)) {
            return startWindowsApplication(serverAddress, serverPort, startId, event);
        } else {
            return startHapptickApplication(serverAddress, serverPort, startId, event);
        }
    }

    public Process startHapptickApplication(String serverAddress, int serverPort, String startId, NotEOFEvent event) throws ActionFailedException {
        String applicationId = null;
        String applicationPath = null;
        try {
            applicationPath = event.getAttribute("applicationPath");
            applicationId = String.valueOf(event.getAttribute("workApplicationId"));
        } catch (Exception ex) {
            LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
        }

        if (Util.isEmpty(applicationId))
            throw new ActionFailedException(10650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new ActionFailedException(10650L, "applicationPath");

        List<String> arguments = new ArrayList<String>();
        for (int i = 0; i < 999; i++) {
            String arg = event.getAttribute("internal:ARG(" + i + ")");
            if (Util.isEmpty(arg)) {
                break;
            }
            arguments.add(arg);
        }

        List<String> callArgs = new ArrayList<String>();
        callArgs.add(applicationPath);
        callArgs.addAll(arguments);
        callArgs.add("--startId=" + startId.trim());
        callArgs.add("--applicationId=" + applicationId.trim());
        callArgs.add("--serverAddress=" + serverAddress.trim());
        callArgs.add("--serverPort=" + String.valueOf(serverPort));

        ProcessBuilder pb = new ProcessBuilder(callArgs);
        Map<String, String> env = pb.environment();
        for (String key : event.getAttributes().keySet()) {
            if (key.startsWith("internal:ENV->")) {
                String val = event.getAttributes().get(key);
                int pos = key.indexOf("internal:ENV->") + "internal:ENV->".length();
                key = key.substring(pos);
                env.put(key, val);
            }
        }

        Bla bla = new Bla(pb);
        new Thread(bla).start();
        // System.gc();
        // Process proc = null;
        // try {
        // proc = pb.start();
        //
        // // File newFile = new File("c:\\temp\\subout.txt");
        // // FileWriter out = new FileWriter(newFile);
        //
        // InputStream in = proc.getInputStream();
        // in.read();
        //
        // DataInputStream dis = new DataInputStream(in);
        // String input;
        //
        // // PipedOutputStream ps = new PipedOutputStream();
        // // PipedInputStream is = new PipedInputStream(ps);
        // // PrintStream os = new PrintStream(ps);
        //
        // while ((input = dis.readLine()) != null) {
        // System.out.println(input);
        // // // // out.write(input);
        // }
        // System.out.println("ExternalCalls. Loop beendet...");
        // // os.close();
        //
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        return bla.getProc();
    }

    private class Bla implements Runnable {
        ProcessBuilder pb;
        Process proc = null;

        protected Bla(ProcessBuilder pb) {
            this.pb = pb;
        }

        protected Process getProc() {
            return proc;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {

            System.gc();
            proc = null;
            try {
                proc = pb.start();

                // File newFile = new File("c:\\temp\\subout.txt");
                // FileWriter out = new FileWriter(newFile);

                InputStream in = proc.getInputStream();
                in.read();

                DataInputStream dis = new DataInputStream(in);
                String input;

                // PipedOutputStream ps = new PipedOutputStream();
                // PipedInputStream is = new PipedInputStream(ps);
                // PrintStream os = new PrintStream(ps);

                while ((input = dis.readLine()) != null) {
                    System.out.println(input);
                    // // // out.write(input);
                }
                System.out.println("ExternalCalls. Loop beendet...");
                // os.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Process startAppl(String applicationPath, String[] arguments, String[] environment, boolean windowsSupport) throws ActionFailedException {
        Process proc;

        String cmdLine = applicationPath + " ";
        for (String arg : arguments) {
            cmdLine += arg + " ";
        }
        if (windowsSupport) {
            cmdLine = "cmd /c start /wait \"\" " + cmdLine;
        }
        cmdLine.trim();

        System.out.println("ExternalCalls. startAppl. cmdLine: " + cmdLine);

        try {
            Runtime runtime = Runtime.getRuntime();
            if (!Util.isEmpty(environment)) {
                proc = runtime.exec(cmdLine, environment);
            } else {
                proc = runtime.exec(cmdLine);
            }
        } catch (IOException ioEx) {
            throw new ActionFailedException(10651L, "Application: " + applicationPath, ioEx);
        }
        return proc;
    }

    public Process startWindowsApplication(String serverAddress, int serverPort, String startId, NotEOFEvent event) throws ActionFailedException {
        String applicationId = null;
        String applicationPath = null;
        try {
            applicationPath = event.getAttribute("applicationPath");
            applicationId = String.valueOf(event.getAttribute("workApplicationId"));
        } catch (Exception ex) {
            LocalLog.error("Fehler bei Verarbeitung eines Events.", ex);
        }

        if (Util.isEmpty(applicationId))
            throw new ActionFailedException(10650L, "applicationId");
        if (Util.isEmpty(applicationPath))
            throw new ActionFailedException(10650L, "applicationPath");

        List<String> env = new ArrayList<String>();
        for (String key : event.getAttributes().keySet()) {
            if (key.startsWith("internal:ENV->")) {
                String val = event.getAttributes().get(key);
                int pos = key.indexOf("internal:ENV->") + "internal:ENV->".length();
                key = key.substring(pos);
                env.add(key + "=" + val);
            }
        }
        String[] environment = new String[env.size()];
        for (int i = 0; i < env.size(); i++) {
            environment[i] = env.get(i);
        }

        List<String> args = new ArrayList<String>();
        for (int i = 0; i < 999; i++) {
            String arg = event.getAttribute("internal:ARG(" + i + ")");
            if (Util.isEmpty(arg)) {
                break;
            }
            args.add(arg);
        }

        // size of arguments plus the ones of fixed arguments (s. below)
        int baseSize = 4;
        String[] arguments = new String[args.size() + baseSize];
        for (int i = 0; i < args.size(); i++) {
            arguments[i] = args.get(i);
        }

        // special Happtick parameter for own Java applications
        arguments[arguments.length - 4] = "--startId=" + startId.trim();
        arguments[arguments.length - 3] = "--applicationId=" + applicationId.trim();
        arguments[arguments.length - 2] = "--serverAddress=" + serverAddress.trim();
        arguments[arguments.length - 1] = "--serverPort=" + String.valueOf(serverPort);

        return startAppl(applicationPath, arguments, environment, true);
    }
}