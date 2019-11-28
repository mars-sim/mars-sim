/**
 * Mars Simulation Project
 * SimuLoggingFormatter.java
 * @version 3.1.0 2017-10-18
 * @author Sebastien Venot
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.headless;

import java.time.LocalDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;

public class SimuLoggingFormatter extends Formatter {

	private final static String LOGCON = "logconsolidated"; 
	private final static String CONTEXT1 = "[CONTEXT";
	private final static String SKIPPED = "skipped";
	private final static String CONTEXT2 = "[CONTEXT ratelimit_period=\"";
	private final static String BRACKET1 = "]\" ]";
	private final static String MILLISECONDS = " MILLISECONDS [skipped:";
	private final static String CONTEXT3 = "[CONTEXT";
	private final static String LINEFEED = System.getProperty("line.separator");
	private final static String O_PAREN = " (";
	private final static String C_PAREN = ") ";
	private final static String BRAC_X1 = "[x1] ";
	private final static String C_BRAC = "] ";
	private final static String O_BRAC_X = "[x";
	private final static String O_BRAC = "[";
	private final static String PERIOD = ".";
	private final static String COLON = " : ";
    
//  public final static DateFormat df = DateFormat.getDateTimeInstance();
//  private Date date = new Date();
    
    private StringBuffer sb = new StringBuffer();
    
    private static MasterClock masterClock;
    
    public String format(LogRecord record) {

    	if (masterClock == null)
    		masterClock = Simulation.instance().getMasterClock();
    	
		String msg = formatMessage(record);
		if (msg != null) {
			boolean context = msg.contains(CONTEXT1);
			
			if (context) {
				if (LogConsolidated.showRateLimit()) {
					// Remove the rate limit comment from google flogger
					boolean skip = msg.contains(SKIPPED);
					if (skip) {
						msg = fastReplace(msg, CONTEXT2, "[");
		//				msg = fastReplace(msg, " MILLISECONDS\" ", "");
						msg = fastReplace(msg, MILLISECONDS, ",");
						msg = fastReplace(msg, BRACKET1, "]");
					}
					
					else {
						int index = msg.indexOf(CONTEXT3);
						if (index > 0)
							msg = msg.substring(0, msg.indexOf(CONTEXT3)-1);
					}
				}
				
				else {
					int index = msg.indexOf(CONTEXT3);
					if (index > 0)
						msg = msg.substring(0, msg.indexOf(CONTEXT3)-1);
				}
			}
	
			sb.delete(0,sb.length());
			
			int timeStamp = LogConsolidated.getTimeStampType();
			
			if (masterClock == null || timeStamp == 0) {
				appendLocalTime();
			}
			
			else if (timeStamp == 1 && LogConsolidated.getEarthClock() != null) {
				sb.append(LogConsolidated.getEarthClock().getTimeStampF0());
			}
			
			else if (timeStamp == 2 && LogConsolidated.getMarsClock() != null && isMarsClockValid()) {
				sb.append(MarsClock.getDateTimeStamp(LogConsolidated.getMarsClock()));
			}
			
			else {
				appendLocalTime();
			}
	
			// Get the level name and add it to the buffer
			sb.append(O_PAREN);
			sb.append(Conversion.capitalize(record.getLevel().getName().toLowerCase()));
			sb.append(C_PAREN);
			
			// If not using LogConsolidated class to generate the log statement
			// do the following to extract the source class name
			String path = null;
			String source = null;
			path = record.getSourceClassName();
			source = path.substring(path.lastIndexOf(PERIOD) + 1, path.length());
			if (!source.equalsIgnoreCase(LOGCON)) {
				sb.append(source);
				sb.append(COLON);
			}
			
			if (msg != null) {
				
				if (msg.contains(BRAC_X1)) {
					msg = msg.substring(msg.indexOf(C_BRAC) + 2, msg.length());
					sb.append(msg);
				}
				
				else if (msg.contains(O_BRAC_X) && msg.contains(C_BRAC)) {
					sb.append(msg);
				}
				
				else if (msg.contains(O_BRAC) && msg.contains(C_BRAC)) {
					sb.append(msg);
				}
				
				else {
					msg = msg.substring(msg.indexOf(C_BRAC) + 1, msg.length());
					sb.append(msg);
				}
				
				sb.append(LINEFEED);
			}
		}
	
		return sb.toString();
    }
    
    /**
     * Checks if the mars clock is different from the starting clock
     * 
     * @return
     */
    private boolean isMarsClockValid() { 
    	return !MarsClock.getDateTimeStamp(LogConsolidated.getMarsClock()).equalsIgnoreCase(MarsClock.START_CLOCK);
    }
    
    
    /**
     * Append the machine's local time
     */
    private void appendLocalTime() {
		// Gets the local time
		String dt = LocalDateTime.now().toString();
		// Show only one decimal place in seconds
		dt = dt.substring(0, dt.lastIndexOf(".")+2);
		// Append the local time to the log
		sb.append(dt);
    }
    
    /**
     * Replace characters in a string quickly without using regex
     * 
     * @param str
     * @param target
     * @param replacement
     * @return
     */
    static String fastReplace(String str, String target, String replacement) {
        int targetLength = target.length();
        if( targetLength == 0 ) {
            return str;
        }
        int idx2 = str.indexOf( target );
        if( idx2 < 0 ) {
            return str;
        }
        StringBuilder buffer = new StringBuilder( targetLength > replacement.length() ? str.length() : str.length() * 2 );
        int idx1 = 0;
        do {
            buffer.append( str, idx1, idx2 );
            buffer.append( replacement );
            idx1 = idx2 + targetLength;
            idx2 = str.indexOf( target, idx1 );
        } while( idx2 > 0 );
        buffer.append( str, idx1, str.length() );
        return buffer.toString();
    }
    
    public void destroy() {
    	//df = null;
        //date = null;
        sb = null;
    }

}
