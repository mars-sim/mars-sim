/**
 * Mars Simulation Project
 * SimuLoggingFormatter.java
 * @version 2.83 2008-08-08
 * @author Sebastien Venot
 */
package org.mars_sim.msp;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;



public class SimuLoggingFormatter extends Formatter {
    
    public final static String LINEFEED =
	System.getProperty("line.separator");
    
    private DateFormat df = DateFormat.getDateTimeInstance();
    private Date date = new Date();
    private StringBuffer sb = new StringBuffer();
    

    public String format(LogRecord record)
	{
		sb.delete(0,sb.length());
		date.setTime(record.getMillis());
			
		sb.append(df.format(date));
		sb.append(" ");
			
		// Get the level name and add it to the buffer
		sb.append(record.getLevel().getName());
		sb.append(" ");
			
		sb.append(record.getLoggerName());
		sb.append(" ");
			 
		// Get the formatted message (includes localization 
		// and substitution of paramters) and add it to the buffer
		sb.append(formatMessage(record));
		sb.append(LINEFEED);

		return sb.toString();
		
	}
}
