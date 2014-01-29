/**
 * Mars Simulation Project
 * SimuLoggingFilter.java
 * @version 3.06 2014-01-29
 * @author Sebastien Venot
 */
package org.mars_sim.msp;

import java.util.logging.Filter;
import java.util.logging.LogRecord;


public class SimuLoggingFilter implements Filter{

 
    public boolean isLoggable(LogRecord record) {
	if(record.getLoggerName() == null ||
	   record.getLoggerName().startsWith("org.mars_sim")) {
	    return true;
	}
	
	return false;
    }

}
