/**
 * Mars Simulation Project
 * SimuLoggingFilter.java
 * @Date 2023-10-17
 * @author Sebastien Venot
 */
package com.mars_sim.core.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;


public class SimuLoggingFilter implements Filter {

    private final static String PREFIX = "com.mars_sim";
 
    public boolean isLoggable(LogRecord record) {

        return record.getLoggerName().startsWith(PREFIX)
                || record.getLoggerName() == null;
    }

}
