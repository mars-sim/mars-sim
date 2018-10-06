/**
 * Mars Simulation Project
 * LogConsolidated.java
 * @version 3.1.0 2017-08-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


//  See https://stackoverflow.com/questions/9132193/log4j-standard-way-to-prevent-repetitive-log-messages#37619797
//	Do
// 		LogConsolidated.log(logger, Level.WARN, 5000, "File: " + f + " not found.", e);
//	Instead of:
//		logger.warn("File: " + f + " not found.", e);
 

public class LogConsolidated {

    private static HashMap<String, TimeAndCount> lastLoggedTime = new HashMap<>();

    private static final String OPEN_BRACKET = "[x";
    private static final String CLOSED_BRACKET = "] ";
    private static final String COLON = " : ";
    private static final String COLON_2 = ":";
    private static final String ONCE = "[x1] "; 
    private static final String QUESTION = "?";
    
	//private static Logger logger = Logger.getLogger(LogConsolidated.class.getName());
	//private static java.util.logging.Logger logj = java.util.logging.Logger.getLogger(LogConsolidated.class.getName());

    
    /**
     * Logs given <code>message</code> to given <code>logger</code> as long as:
     * <ul>
     * <li>A message (from same class and line number) has not already been logged within the past <code>timeBetweenLogs</code>.</li>
     * <li>The given <code>level</code> is active for given <code>logger</code>.</li>
     * </ul>
     * Note: If messages are skipped, they are counted. When <code>timeBetweenLogs</code> has passed, and a repeat message is logged, 
     * the count will be displayed.
     * @param logger Where to log.
     * @param level Level to log.
     * @param timeBetweenLogs Milliseconds to wait between similar log messages.
     * @param message The actual message to log.
     * @param t Can be null. Will log stack trace if not null.
     */
    public static void log(Logger logger, Level level, long timeBetweenLogs, String sourceName, String message, Throwable t) {
    	
		String className = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
		
        //if (logger.isEnabledFor(level)) {
            String uniqueIdentifier = getFileAndLine();
            TimeAndCount lastTimeAndCount = lastLoggedTime.get(uniqueIdentifier);
            if (lastTimeAndCount != null) {
                synchronized (lastTimeAndCount) {
                    long now = System.currentTimeMillis();
                    if (now - lastTimeAndCount.time < timeBetweenLogs) {
                        lastTimeAndCount.count++;
                        return;
                    } else {
                    	//if (lastTimeAndCount.count == 1) {
                        //    log(logger, level, sourceName + " : " + message, t);
                    	//}
                    	//else
                    		log(logger, level, OPEN_BRACKET + lastTimeAndCount.count + CLOSED_BRACKET + className + COLON +  message, t);
                    }
                }
            } 
            
            else {
            	log(logger, level, ONCE + className + COLON + message, t);
            }
            
            lastLoggedTime.put(uniqueIdentifier, new TimeAndCount());
        //}
    }

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

    private static class TimeAndCount {
        protected long time;
        protected int count;
        TimeAndCount() {
            this.time = System.currentTimeMillis();
            this.count = 1;
        }
    }
}