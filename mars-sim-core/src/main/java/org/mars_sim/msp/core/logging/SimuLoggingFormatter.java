/**
 * Mars Simulation Project
 * SimuLoggingFormatter.java
 * @version 3.2.0 2021-06-20
 * @author Sebastien Venot
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.msp.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;

public class SimuLoggingFormatter extends Formatter {

	private static final String LOGSIM = "simlogger";
	private final static String LINEFEED = System.getProperty("line.separator");
	private final static String O_PAREN = " (";
	private final static String C_PAREN = ") ";
	private final static String BRAC_X1 = "[x1] ";
	private final static String C_BRAC = "] ";
	private final static String O_BRAC_X = "[x";
	private final static String O_BRAC = "[";
	private final static String PERIOD = ".";
	private final static String COLON = " : ";

    // Cache Mars Timestamp as text can be expensive
	private static double lastClock = -1D;
	private static String lastMarsTimestamp = null;
    private static MasterClock masterClock;
	private static int timeStampType = 2;

	private static final DateTimeFormatter DATE_TIME_FORMATTER
						= DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    
    public SimuLoggingFormatter() {
		super();
	}

	public String format(LogRecord record) {
    	
		String msg = formatMessage(record);

		// Build the messge output using a fixed format
	    StringBuffer sb = new StringBuffer();
	    if (masterClock != null) {
			if (timeStampType == 0) {
				sb.append(getLocalTime());
			}
			else if (timeStampType == 1) {
				sb.append(masterClock.getEarthTime().format(DATE_TIME_FORMATTER));
			}
			else if (timeStampType == 2  && isMarsClockValid()) {
				String marsTime = getMarsTimestamp();
				sb.append(marsTime);
			}
	    }
		else {
			sb.append(getLocalTime());
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
		if (!source.equalsIgnoreCase(LOGSIM)) {
			sb.append(source);
			sb.append(COLON);
		}
		
		// Add original content
		if (msg != null) {	
			if (msg.contains(BRAC_X1)) {
//					msg = msg.substring(msg.indexOf(C_BRAC) + 2, msg.length());
				msg = msg.replace(BRAC_X1, "");
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
			
		// Add any exception
	    Throwable throwable = record.getThrown();
	    if (throwable != null) {
	    	StringWriter sink = new StringWriter();
	    	throwable.printStackTrace(new PrintWriter(sink, true));
	    	sb.append(sink.toString());
			sb.append(LINEFEED);
	    }
		

		return sb.toString();
    }

    private static String getMarsTimestamp() {
		// Let's cache the format to save processing of recreating the timestamp as text
    	MarsClock mc = masterClock.getMarsClock();
		if ((lastMarsTimestamp == null) || (lastClock != mc.getTotalMillisols())) {
			lastClock = mc.getTotalMillisols();
			lastMarsTimestamp = MarsClockFormat.getDateTimeStamp(mc);
		}
		
		return lastMarsTimestamp;
	}

	/**
     * Checks if the mars clock is different from the starting clock
     * 
     * @return
     */
    private static boolean isMarsClockValid() { 
    	// More efficient to just check if the clock has ticked.
    	return masterClock.getTotalPulses() > 0;
    }
    
    
    /**
     * Append the machine's local time
     */
    private static final String getLocalTime() {
		// Gets the local time
		String dt = LocalDateTime.now().toString();
		// Show only one decimal place in seconds
		return dt.substring(0, dt.lastIndexOf(".")+2);
    }
    
    /**
     * Define the clock to use
     * @param mc
     */
	public static void initializeInstances(MasterClock mc) {
		masterClock = mc;
	}
}
