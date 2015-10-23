/**
 * Mars Simulation Project
 * SimuLoggingFormatter.java
 * @version 3.07 2014-12-06

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

		String log = df.format(date).replaceAll("AM", "").replaceAll("PM", "");
		sb.append(log);
		//sb.append(" ");

		// Get the level name and add it to the buffer
		sb.append("(");
		sb.append(record.getLevel().getName());
		sb.append(") ");

		String source = (record.getSourceClassName())
				.replaceAll("org.mars_sim.msp.ui.","ui.")
				.replaceAll("org.mars_sim.msp.core.","core.")
				.replaceAll("org.mars_sim.msp.mapdata.","mapdata.")
				.replaceAll("org.mars_sim.msp.network.","network.")
				.replaceAll("org.mars_sim.msp.","main.");
		
		// record.getLoggerName()
		//sb.append(record.getSourceClassName());
		//sb.append(record.getClass().getSimpleName());
		sb.append(source);
		sb.append(" ");

		// Get the formatted message (includes localization
		// and substitution of paramters) and add it to the buffer
		sb.append(formatMessage(record));
		sb.append(LINEFEED);

		return sb.toString();

	}
}
