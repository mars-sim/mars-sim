/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 2.72 2001-04-29
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
        formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss z");
        formatter.setTimeZone(zone);

        // Set Earth clock to Martian Zero-orbit date-time
        // "06/18/2015 07:22:10 GMT" 
        // This date may need to be adjusted if it is inaccurate
        clear();
        try {
            DateFormat tempFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
            tempFormatter.setTimeZone(zone);
       	    setTime(tempFormatter.parse("06/18/2015 07:22:10"));
        }
        catch(ParseException e) { System.out.println(e.toString()); }
 
        // Add 15 Martian orbits to date
        double solsInOrbit = 668.5921;
        double secondsInSol = 88775.244;
        int secondsInOrbit = (int) (solsInOrbit * secondsInSol);
        add(Calendar.SECOND, 15 * secondsInOrbit);
    }

    /** Returns the date/time formatted in a string 
     *  @return date/time formatted in a string. ex "2055-05-06 03:37:22 GMT"
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
