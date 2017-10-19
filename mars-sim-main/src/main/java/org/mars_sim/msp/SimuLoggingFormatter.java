/**
 * Mars Simulation Project
 * SimuLoggingFormatter.java
 * @version 3.1.0 2017-10-18
 * @author Sebastien Venot
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.msp;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.mars_sim.msp.core.tool.Conversion;


public class SimuLoggingFormatter extends Formatter {

    public final static String LINEFEED = System.getProperty("line.separator");

    private DateFormat df = DateFormat.getDateTimeInstance();
    private Date date = new Date();
    
    private StringBuffer sb = new StringBuffer();

    public String format(LogRecord record) {

		String msg = formatMessage(record);

		sb.delete(0,sb.length());
		date.setTime(record.getMillis());

		String dateTimeStamp = df.format(date);//.replaceAll("AM", "").replaceAll("PM", "");
		sb.append(dateTimeStamp);

		// Get the level name and add it to the buffer
		sb.append(" (");
		sb.append(Conversion.capitalize(record.getLevel().getName().toLowerCase()));
		sb.append(") ");
		
		String path = null;
		String source = null;
		
		if (msg != null) {
			
			if (msg.contains("[x1] ")) {
				msg = msg.substring(msg.indexOf("] ") + 2, msg.length());
				sb.append(msg);
			}
			
			else if (msg.contains("[x") && msg.contains("] ")) {
				sb.append(msg);
			}
			
			else {
				msg = msg.substring(msg.indexOf("] ") + 1, msg.length());
				path = record.getSourceClassName();
				source = path.substring(path.lastIndexOf(".") + 1, path.length());		
				sb.append(source);
				sb.append(" : ");
				sb.append(msg);
			}
			
			sb.append(LINEFEED);
			
		}
		
		return sb.toString();
		
    }
    
    public void destroy() {
    	df = null;
        date = null;
        sb = null;
    }

}
