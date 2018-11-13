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

import org.mars_sim.msp.core.tool.Conversion;

public class SimuLoggingFormatter extends Formatter {

    public final static String LINEFEED = System.getProperty("line.separator");
    public final static String O_PAREN = " (";
    public final static String C_PAREN = ") ";
    public final static String BRAC_X1 = "[x1] ";
    public final static String C_BRAC = "] ";
    public final static String O_BRAC_X = "[x";
    public final static String O_BRAC = "[x";
    public final static String PERIOD = ".";
    public final static String COLON = " : ";
    
//  public final static DateFormat df = DateFormat.getDateTimeInstance();
//  private Date date = new Date();
    
    private StringBuffer sb = new StringBuffer();

    public String format(LogRecord record) {

		String msg = formatMessage(record);

		sb.delete(0,sb.length());
		//date.setTime(record.getMillis());

		//String dateTimeStamp = df.format(date);//.replaceAll("AM", "").replaceAll("PM", "");
		
		String dt = LocalDateTime.now().toString();
		
		dt = dt.substring(0, dt.lastIndexOf(".")+2);
		
		sb.append(dt);//(df.format(date).replace(",", "")));

		// Get the level name and add it to the buffer
		sb.append(O_PAREN);
		sb.append(Conversion.capitalize(record.getLevel().getName().toLowerCase()));
		sb.append(C_PAREN);
		
		String path = null;
		String source = null;
		
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
				path = record.getSourceClassName();
				source = path.substring(path.lastIndexOf(PERIOD) + 1, path.length());		
				sb.append(source);
				sb.append(COLON);
				sb.append(msg);
			}
			
			sb.append(LINEFEED);
			
		}
		
//		String newString = sb.toString();
//		if (!cacheString.equals(newString)) {
//			cacheString = sb.toString();
//		}
		
		return sb.toString();
		
    }
    
    public void destroy() {
    	//df = null;
        //date = null;
        sb = null;
    }

}
