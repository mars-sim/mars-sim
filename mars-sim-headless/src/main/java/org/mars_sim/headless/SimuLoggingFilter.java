/**
 * Mars Simulation Project
 * SimuLoggingFilter.java
 * @version 3.1.0 2018-09-29
 * @author Sebastien Venot
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.headless;

import java.util.logging.Filter;
import java.util.logging.LogRecord;


public class SimuLoggingFilter implements Filter {

    private final static String PREFIX = "org.mars_sim";
 
    public boolean isLoggable(LogRecord record) {
//		if (record.getLoggerName() == null) {
//			if (record.getMessage().contains("SLF4J:")
//					|| record.getMessage().contains("xstream")
//					|| record.getMessage().contains("reflective")
//					|| record.getMessage().contains("All illegal access operations")) {
//				return false;
//			}
//			return true;
//		}
		if (record.getLoggerName().startsWith(PREFIX)) {
			return true;
		}
	
		return false;
    }

}
