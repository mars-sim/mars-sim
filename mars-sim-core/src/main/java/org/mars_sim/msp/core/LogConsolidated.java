/**
 * Mars Simulation Project
 * LogConsolidated.java
 * @version 3.1.0 2017-08-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;

import com.google.common.flogger.FluentLogger;
//  See https://stackoverflow.com/questions/9132193/log4j-standard-way-to-prevent-repetitive-log-messages#37619797
//	Do
// 		LogConsolidated.log(logger, Level.WARN, 5000, "File: " + f + " not found.", e);
//	Instead of:
//		logger.warn("File: " + f + " not found.", e);

public class LogConsolidated {

	/* Google fluent logger. */
	private static final FluentLogger flogger = FluentLogger.forEnclosingClass();
	
	private static HashMap<String, TimeAndCount> lastLogged = new HashMap<>();

	private static final String OPEN_BRACKET = "[x";
	private static final String CLOSED_BRACKET = "] ";
	private static final String COLON = " : ";
	private static final String COLON_2 = ":";
	private static final String ONCE = "[x1] ";
	private static final String QUESTION = "?";
	private static final String PERIOD = ".";

	private static boolean showRateLimit = false;
	
	/** 0 = Local time. 1 = Simulation Earth time. 2 = Simulation Martian time. */
	private static int timeStampType = 2;
	
	private static EarthClock earthClock;// = Simulation.instance().getMasterClock().getEarthClock();
	private static MarsClock marsClock;// = Simulation.instance().getMasterClock().getMarsClock();
	

	/**
	 * Logs given <code>message</code> to given <code>logger</code> as long as:
	 * <ul>
	 * <li>A message (from same class and line number) has not already been logged
	 * within the past <code>timeBetweenLogs</code>.</li>
	 * <li>The given <code>level</code> is active for given
	 * <code>logger</code>.</li>
	 * </ul>
	 * Note: If messages are skipped, they are counted. When
	 * <code>timeBetweenLogs</code> has passed, and a repeat message is logged, the
	 * count will be displayed.
	 * 
	 * @param logger          Where to log.
	 * @param level           Level to log.
	 * @param timeBetweenLogs Milliseconds to wait between similar log messages.
	 * @param message         The actual message to log.
	 * @param t               Can be null. Will log stack trace if not null.
	 */
	public static void log(Logger logger, Level level, long timeBetweenLogs, String sourceName, String message,
			Throwable t) {
		long dTime = timeBetweenLogs;
		String className = sourceName.substring(sourceName.lastIndexOf(PERIOD) + 1, sourceName.length());

//		if (logger.isEnabledFor(level)) {
		String uniqueIdentifier = getFileAndLine();
		TimeAndCount lastTimeAndCount = lastLogged.get(uniqueIdentifier);
		if (lastTimeAndCount != null) {
			synchronized (lastTimeAndCount) {
				long now = System.currentTimeMillis();
				if (now - lastTimeAndCount.startTime < dTime) {
					// Increment count only since the message in the same and is within the time prescribed
					lastTimeAndCount.count++;
					return;
				} else {
					// Print the log statement with counts
					log(logger, level,
							OPEN_BRACKET + lastTimeAndCount.count + CLOSED_BRACKET + className + COLON + message, t);
				}
			}
		}

		else {
			// Print the log statement
			log(logger, level, ONCE + className + COLON + message, t);
		}

		// Register the message
		lastLogged.put(uniqueIdentifier, new TimeAndCount());
	}
//	}

//	public static void go(Level level, int timeBetweenLogs, StringBuffer sb) {
//		flogger.at(level).atMostEvery(timeBetweenLogs, TimeUnit.SECONDS).log(sb.toString());
//	}
	
	public static void log(Level level, int timeBetweenLogs, String sourceName, String message) {
//		if (sourceName.contains("."))
//			sourceName = sourceName.substring(sourceName.lastIndexOf(PERIOD) + 1, sourceName.length());
		flogger.at(level).atMostEvery(timeBetweenLogs, TimeUnit.MILLISECONDS).log(sourceName + COLON + message);
	}
	
	public static void log(Level level, int timeBetweenLogs, String sourceName, String message, Throwable t) {
//		if (sourceName.contains("."))
//			sourceName = sourceName.substring(sourceName.lastIndexOf(PERIOD) + 1, sourceName.length());
		flogger.at(level).atMostEvery(timeBetweenLogs, TimeUnit.MILLISECONDS).log(sourceName + COLON + message, t);
	}
	
//	public static void info(int timeBetweenLogs, String sourceName, String message) {
//		flogger.atInfo().atMostEvery(timeBetweenLogs, TimeUnit.SECONDS)
//			.log(sourceName.substring(sourceName.lastIndexOf(PERIOD) + 1, sourceName.length()) + COLON + message);
//	}
//	
//	public static void info(int timeBetweenLogs, String sourceName, String message, Throwable t) {
//		flogger.atInfo().atMostEvery(timeBetweenLogs, TimeUnit.SECONDS)
//			.log(sourceName.substring(sourceName.lastIndexOf(PERIOD) + 1, sourceName.length()) + COLON + message, t);
//	}
	
	/**
	 * Returns the line
	 * 
	 * @return
	 */
	private static String getFileAndLine() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		boolean enteredLogConsolidated = false;
		for (StackTraceElement ste : stackTrace) {
			if (ste.getClassName().equals(LogConsolidated.class.getName())) {
				enteredLogConsolidated = true;
			} else if (enteredLogConsolidated) {
				// We have now file/line before entering LogConsolidated.
				return ste.getFileName() + COLON_2 + ste.getLineNumber();
			}
		}
		return QUESTION;
	}

	private static void log(Logger logger, Level level, String message, Throwable t) {

//    	java.util.logging.Level l2 = null;
//    	if (level == Level.INFO)
//    		l2 =  java.util.logging.Level.INFO;
//    	else if (level == Level.WARN || level == Level.ERROR)
//    		l2 =  java.util.logging.Level.WARNING;
//    	else if (level == Level.FATAL)
//    		l2 =  java.util.logging.Level.SEVERE;

		if (t == null) {
			logger.log(level, message);

		} else {
			logger.log(level, message, t);
		}

	}

	public static boolean showRateLimit() {
		return showRateLimit;
	}
	
	public static void setRateLimit(boolean value) {
		showRateLimit = value;
	}
	
	public static int getTimeStampType() {
		return timeStampType;
	}

	public static void setTimeStampChoice(int type) {
		timeStampType = type;
	}

	public static MarsClock getMarsClock() {
		return marsClock;
	}
	
	public static EarthClock getEarthClock() {
		return earthClock;
	}
	
	/**
	 * Initialize transient data in the simulation.
	 * 
	 * @param m {@link MarsClock}
	 * @param e {@link EarthClock}
	 */
	public static void initializeInstances(MarsClock m, EarthClock e) {
		marsClock = m;
		earthClock = e;
	}
	
	public static void setMarsClock(MarsClock m) {
		marsClock = m;
	}
	
	public static void setEarthClock(EarthClock e) {
		earthClock = e;
	}
	
	/**
	 * TimeAndCount keeps track of the between time and the number of times the message has appeared
	 */
	private static class TimeAndCount {
		protected long startTime;
		protected int count;

		TimeAndCount() {
			this.startTime = System.currentTimeMillis();
			this.count = 1;
		}
	}
}