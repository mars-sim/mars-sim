/**
 * Mars Simulation Project
 * Log.java
 * @version 1.0
 * @author Sebastien venot
 */
package org.mars_sim.msp;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
* Logging facility of Java 
* 
*/
public final class Log implements Serializable
{
	public final static String LINEFEED = 
	System.getProperty("line.separator");
	
	private static Logger logger;
	private static Handler fileHandler;


	
	/**
	 * Set the logging level of this logger
	 * @param level
	 */
	public void setLogLevel(Level level) {
	    logger.setLevel(level);
	}
	
	
	/**
	 * Log to external file
	 * 
	 * @param file File to save log into
	 *       
	 * @throws IOException
	 */
	public void setLogToFile(File file) throws IOException
	{
		final DateFormat df = DateFormat.getDateTimeInstance();
		fileHandler = new FileHandler(file.getAbsolutePath());
		fileHandler.setFormatter(new Formatter()
		{
			public String format(LogRecord record)
			{
				String dt = df.format(new Date(record.getMillis()));
				return dt + "  :  " + record.getMessage() + LINEFEED;
			}
		});
		logger.addHandler(fileHandler);
	}

	/**
	 * End a log to external file session.
	 */
	public void unsetLogToFile()
	{
		logger.removeHandler(fileHandler);
	}

	
	/**
	 * Return this log
	 * 
	 * @return handler to log
	 */
	public static void initialize()
	{
	        logger = Logger.getLogger("");
	        logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(true);
	}

}


