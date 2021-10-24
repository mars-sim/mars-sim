/**
 * Mars Simulation Project
 * SimuLoggingFilter.java
 * @version 3.2.0 2021-06-20
 * @author Sebastien Venot
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.msp.core.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;


public class SimuLoggingFilter implements Filter {

    private final static String PREFIX = "org.mars_sim";
 
    public boolean isLoggable(LogRecord record) {

        return record.getLoggerName().startsWith(PREFIX)
                || record.getLoggerName() == null;
    }

}
