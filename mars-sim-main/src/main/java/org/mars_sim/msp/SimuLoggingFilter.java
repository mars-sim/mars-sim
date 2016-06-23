/**
 * Mars Simulation Project
 * SimuLoggingFilter.java
 * @version 3.07 2014-12-06
 * @author Sebastien Venot
 * $LastChangedDate$
 * $LastChangedRevision$
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
