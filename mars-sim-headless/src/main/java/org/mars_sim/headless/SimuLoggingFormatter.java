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
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;

public class SimuLoggingFormatter extends Formatter {

    public final static String LINEFEED = System.getProperty("line.separator");
    public final static String O_PAREN = " (";
    public final static String C_PAREN = ") ";
    public final static String BRAC_X1 = "[x1] ";
    public final static String C_BRAC = "] ";
    public final static String O_BRAC_X = "[x";
    public final static String O_BRAC = "[";
    public final static String PERIOD = ".";
    public final static String COLON = " : ";
    
//  public final static DateFormat df = DateFormat.getDateTimeInstance();
//  private Date date = new Date();
    
    private StringBuffer sb = new StringBuffer();
    
    private MasterClock masterClock;// = Simulation.instance().getMasterClock();
    private MarsClock marsClock;// = masterClock.getMarsClock();
    private EarthClock earthClock;// = masterClock.getEarthClock();

    public String format(LogRecord record) {

    	if (masterClock == null) {
    		masterClock = Simulation.instance().getMasterClock();
    		if (masterClock != null) {
		    	if (marsClock == null)
		    		marsClock = masterClock.getMarsClock();
		    	if (earthClock == null)
		    		earthClock = masterClock.getEarthClock();
    		}
    	}
    	
		String msg = formatMessage(record);
//		System.out.println(msg);
		boolean context = msg.contains("[CONTEXT");
		
		if (context) {
			if (LogConsolidated.showRateLimit()) {
				// Remove the rate limit comment from google flogger
				boolean skip = msg.contains("skipped");
				if (skip) {
					msg = fastReplace(msg, "[CONTEXT ratelimit_period=\"", "[");
	//				msg = fastReplace(msg, " MILLISECONDS\" ", "");
					msg = fastReplace(msg, " MILLISECONDS [skipped:", ",");
					msg = fastReplace(msg, "]\" ]", "]");
				}
				
				else {
					int index = msg.indexOf("[CONTEXT");
					if (index > 0)
						msg = msg.substring(0, msg.indexOf("[CONTEXT")-1);
				}
			}
			
			else {
				int index = msg.indexOf("[CONTEXT");
				if (index > 0)
					msg = msg.substring(0, msg.indexOf("[CONTEXT")-1);
			}
		}
		
//		System.out.println(msg);
		
		sb.delete(0,sb.length());
		
		int timeStamp = LogConsolidated.getTimeStampType();
		
		if (timeStamp == 1) {
			sb.append(earthClock.getTimeStampF0());
		}
		
		else if (timeStamp == 2) {
			sb.append(marsClock.getDateTimeStamp());
		}
		
		else {//if (timeStamp == 0) {
			// Gets the local time
			String dt = LocalDateTime.now().toString();
			// Show only one decimal place in seconds
			dt = dt.substring(0, dt.lastIndexOf(".")+2);
			
			sb.append(dt);
		}
		

		// Get the level name and add it to the buffer
		sb.append(O_PAREN);
		sb.append(Conversion.capitalize(record.getLevel().getName().toLowerCase()));
		sb.append(C_PAREN);
		
		String path = null;
		String source = null;
		path = record.getSourceClassName();
		source = path.substring(path.lastIndexOf(PERIOD) + 1, path.length());		
		sb.append(source);
		sb.append(COLON);
		
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
	
		return sb.toString();
		
    }
    
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
