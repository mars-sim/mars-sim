/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 2.72 2001-02-29
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;
import java.text.*;

/** The EarthClock class keeps track of Earth Greenwich Mean Time.
 *  It should be synchronized with the Mars clock. 
 */
public class EarthClock extends GregorianCalendar {

    // Data members
    SimpleDateFormat formatter;

    /** Constructs a EarthClock object */
    public EarthClock() {
        
        // Use GregorianCalendar constructor
        super();

        // Set GMT timezone for calendar
        SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");
        setTimeZone(zone);

        // Initialize formatter
        formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z");
        formatter.setTimeZone(zone);

        // Set starting date/time to midnight, January 1st, 2035
        clear(); 
        set(2035, Calendar.JANUARY, 1);
        complete();
    }

    /** Returns the date/time formatted in a string 
     *  @return date/time formatted in a string. ex "05/06/2055 03:37:22 PM GMT"
     */
    public String getTimeStamp() {
        return formatter.format(getTime());
    }
    
    /** Adds time to the calendar 
     *  @param seconds seconds added to the calendar
     */
    public void addTime(double seconds) {
        add(Calendar.MILLISECOND, (int) (seconds * 1000D));
    }
}
