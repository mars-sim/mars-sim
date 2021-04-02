package org.mars_sim.msp.core.logging;
/**
 * Mars Simulation Project
 * LogConsolidated.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This is a logger class similar to Java Logger that is Simulation aware
 * to handle common formatting.
 * This actor as an Adapter to the underlying Java Logger.
 */
public class SimLogger {

	private static Map<String, SimLogger> loggers = new HashMap<>();
	private static Map<String, TimeAndCount> lastLogged = new ConcurrentHashMap<>();

	private static final String OPEN_BRACKET = " [x";
	private static final String CLOSED_BRACKET = "]";
	private static final String CLOSED_BRACKET_SPACE = "] ";
	private static final String COLON = " : [";
	private static final String DASH = " - ";
	private static final String COLON_2 = ":";
	private static final String QUESTION = "?";

	private String sourceName;

	private Logger rootLogger;


	public static SimLogger getLogger(String name) {
		SimLogger result = null;
		synchronized (loggers) {
			result = loggers.computeIfAbsent(name, k -> new SimLogger(name));
			
		}
		return result;
	}
	
	private SimLogger(String name) {
		rootLogger = Logger.getLogger(name);

		sourceName = name.substring(name.lastIndexOf(".") + 1, name.length());
	}

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
	 * @param actor           Unit that is the Actor in the message.
	 * @param level           Level to log.
	 * @param timeBetweenLogs Milliseconds to wait between similar log messages.
	 * @param message         The actual message to log.
	 */
	public void log(Loggable actor, Level level, long timeBetweenLogs, String message)
	{
		log(null, actor, level, timeBetweenLogs, message, null);
	}
	
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
	 * @param location		  Where does the message occur? If Null actor settlement is used.
	 * @param actor           Unit that is the Actor in the message.
	 * @param level           Level to log.
	 * @param timeBetweenLogs Milliseconds to wait between similar log messages.
	 * @param message         The actual message to log.
	 * @param t               Can be null. Will log stack trace if not null.
	 */
	public void log(Unit location, Loggable actor, Level level, long timeBetweenLogs, String message,
			Throwable t) {
		if (!rootLogger.isLoggable(level)) {
			return;
		}
		
		long dTime = timeBetweenLogs;
	
		String uniqueIdentifier = getFileAndLine();
		TimeAndCount lastTimeAndCount = lastLogged.get(uniqueIdentifier);
		StringBuilder outputMessage = null;
		if (lastTimeAndCount != null) {
			synchronized (lastTimeAndCount) {
				long now = System.currentTimeMillis();
				if (now - lastTimeAndCount.startTime < dTime) {
					// Increment count only since the message in the same and is within the time prescribed
					lastTimeAndCount.count++;
					return;
				} 
				
				// Print the log statement with counts
				outputMessage = new StringBuilder(sourceName);
				outputMessage.append(OPEN_BRACKET).append(lastTimeAndCount.count).append(CLOSED_BRACKET);
			}
		}
		else {
			// First time for this message
			outputMessage = new StringBuilder(sourceName);
		}
	

		// Add body, contents Settlement, Unit nickname message"
		outputMessage.append(COLON);
		if (actor instanceof Settlement) {
			// Actor in bracket; it's top level
			outputMessage.append(actor.getNickName()).append(CLOSED_BRACKET_SPACE);
		}
		else {
			// Need container hierarchy in brackets
			if (location == null) {
				if (actor instanceof Building) {
					location = actor.getAssociatedSettlement();
				}
				else {
					location = actor.getContainerUnit();
				}
			}
			
			// On the surface
			if (location.getIdentifier() == Unit.MARS_SURFACE_UNIT_ID) {
				// On the surface use coordinate
				Coordinates coords = actor.getCoordinates();
				outputMessage.append(coords.getFormattedLatitudeString());
				outputMessage.append(' ');
				outputMessage.append(coords.getFormattedLongitudeString());
			}
			else {
				locationDescription(location, outputMessage);
			}
			outputMessage.append(CLOSED_BRACKET_SPACE).append(actor.getNickName()).append(DASH);
		}

		outputMessage.append(message);

		if (t == null) {
			rootLogger.log(level, outputMessage.toString());
		}
		else {
			rootLogger.log(level, outputMessage.toString(), t);
		}

		// Register the message
		lastLogged.put(uniqueIdentifier, new TimeAndCount());
	}

	/**
	 * THhis method will be moved into Unit as part of Issue #296
	 * @param location
	 * @param outputMessage
	 */
	private static void locationDescription(Unit location, StringBuilder outputMessage) {
		Unit next = null;
		if (location instanceof Building) {
			next = location.getAssociatedSettlement();
		}
		else {
			next = location.getContainerUnit();
		}
		
		// Go up the chain if not surface
		if (next != null && (next.getIdentifier() != Unit.MARS_SURFACE_UNIT_ID)) {
			locationDescription(next, outputMessage);
			outputMessage.append("->");
		}
		outputMessage.append(location.getNickName());
	}


	/**
	 * Returns the line
	 * 
	 * @return
	 */
	private static String getFileAndLine() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		boolean enteredLogConsolidated = false;
		for (StackTraceElement ste : stackTrace) {
			if (ste.getClassName().equals(SimLogger.class.getName())) {
				enteredLogConsolidated = true;
			} else if (enteredLogConsolidated) {
				// We have now file/line before entering LogConsolidated.
				return ste.getFileName() + COLON_2 + ste.getLineNumber();
			}
		}
		return QUESTION;
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
	
	/**
	 * log directly without formatting
	 * @param level
	 * @param message
	 */
	public void log(Level level, String message) {
		rootLogger.log(level, message);
	}

	/**
	 * log directly without formatting
	 * @param level
	 * @param message
	 * @param e Exception
	 */
	public void log(Level level, String message, Exception e) {
		rootLogger.log(level, message, e);
	}
}
