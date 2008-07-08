package de.noteof.core.logging;

/**
 * There are two ways of logging: <br>
 * - One part of the framework is the implementation of a special log server /
 * log service in the net. <br>
 * When a log server is reachable the log-message must be send to it. <br>
 * - When a log server is not available respectively there is no communication
 * within the noteof-net alternately local writing in logfiles (log4j) is
 * possible.
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

	public static void error(Exception ex, String additionalInfo) {
	}

	public static void warn(Exception ex, String additionalInfo) {
	}

	public static void debug(Exception ex, String additionalInfo) {
	}

	public static void info(Exception ex, String additionalInfo) {
	}

}
