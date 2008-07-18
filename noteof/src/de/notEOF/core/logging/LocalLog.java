package de.notEOF.core.logging;

/**
 * There are two ways of logging: <br>
 * - One part of the framework is the implementation of a special log server /
 * log service in the net. <br>
 * When a log server is reachable the log-message must be send to it. <br>
 * - When a log server is not available respectively there is no communication
 * within the noteof-net alternately local writing in logfiles (log4j) is
 * possible. Independent of this conclusion local logging can be necessary
 * sometimes.
 * 
 * @author Dirk
 * 
 */
public class LocalLog {

    protected static LocalLog localLog;
    protected Exception ex;
    protected String additionalInfo;

    private LocalLog() {
        localLog = new LocalLog();
    }

    public LocalLog getInstance() {
        if (null == localLog)
            localLog = new LocalLog();
        return localLog;
    }

    public static void error(String additionalInfo, Throwable th) {
    	System.out.println(additionalInfo + ": \n" + th);
    }

    public static void error(String additionalInfo) {
    	System.out.println(additionalInfo);
    }

    public static void warn(String additionalInfo, Throwable th) {
    }

    public static void debug(String additionalInfo, Throwable th) {
    }

    public static void info(String additionalInfo, Throwable th) {
    }

    public static void info(String additionalInfo) {
    }
}
