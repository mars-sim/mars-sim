/**
 * Mars Simulation Project
 * SimuLoggingFormatter.java
 * @version 3.1.0 2017-08-14
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

    //private int timesRepeated = 1;
    //private int secondStampCache;
    //private String logCache;
    
    private DateFormat df = DateFormat.getDateTimeInstance();
    private Date date = new Date();
    
    private StringBuffer sb = new StringBuffer();

    public String format(LogRecord record) {

		String msg = formatMessage(record);
/*
		if (!recordMap.isEmpty()) {
			if (current < endNum) {
				current++;
			}
			else {
				current = 0;
			}
			recordMap.put(current, msg);
		}
*/

		sb.delete(0,sb.length());
		date.setTime(record.getMillis());

		String dateTimeStamp = df.format(date);//.replaceAll("AM", "").replaceAll("PM", "");
		sb.append(dateTimeStamp);
		//sb.append(" ");

		//int secondStamp = date.getSeconds();	
		//int offset = sb.length() + 1;
		
		// Get the level name and add it to the buffer
		sb.append(" (");
		sb.append(Conversion.capitalize(record.getLevel().getName().toLowerCase()));
		sb.append(") ");
		
		String path = null;
		String source = null;
		
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
		
/*		
		if (msg.contains("exception") || msg.contains("Exception")) {
				source = path
						.replaceAll("org.mars_sim.msp.ui.","ui.")
						.replaceAll("org.mars_sim.msp.core.","core.")
						.replaceAll("org.mars_sim.msp.mapdata.","mapdata.")
						.replaceAll("org.mars_sim.msp.network.","network.")
						.replaceAll("org.mars_sim.msp.android","android.")
						.replaceAll("org.mars_sim.msp.service","service.")
						.replaceAll("org.mars_sim.msp.","main.");
				sb.append(source);
				sb.append(" : ");
		}
*/


		

		// record.getLoggerName()
		//sb.append(record.getSourceClassName());
		//sb.append(record.getClass().getSimpleName());
		//sb.append(source);
		
		// Get the formatted message (includes localization
		// and substitution of paramters) and add it to the buffer
		//sb.append(msg);

		sb.append(LINEFEED);
		
		return sb.toString();

/*		
		String finalLog = sb.toString();
		
		
		if (secondStampCache == secondStamp) {
			if (finalLog.equalsIgnoreCase(logCache)) {
				sb.insert(offset, "[x" + timesRepeated + "] ");
				timesRepeated++;
				return null;
			}
			
			else {
				logCache = sb.toString();
				timesRepeated = 1;
				return logCache;
			}	
		}
		
		else {
			secondStampCache = secondStamp;
			logCache = sb.toString();
			timesRepeated = 1;
			return logCache;
		}
		
*/
	}
    
    
}
