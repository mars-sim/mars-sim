/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

/** The EarthClock class keeps track of Earth Universal Time.
 *  It should be synchronized with the Mars clock. 
 */
public class EarthClock extends GregorianCalendar implements Serializable {

    // Data members
    SimpleDateFormat formatter;

    /** 
     * Constructor
     * @param dateString the UT date string in format: "MM/dd/yyyy hh:mm:ss".
     * @throws Exception if date string is invalid. 
     */
    public EarthClock(String dateString) throws Exception {
        
        // Use GregorianCalendar constructor
        super();

        // Set GMT timezone for calendar
        SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");
        setTimeZone(zone);

        // Initialize formatter
        formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        formatter.setTimeZone(zone);

        // Set Earth clock to Martian Zero-orbit date-time. 
        // This date may need to be adjusted if it is inaccurate.
        clear();
        DateFormat tempFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        tempFormatter.setTimeZone(zone);
       	setTime(tempFormatter.parse(dateString));
    }

    /** Returns the date/time formatted in a string 
     *  @return date/time formatted in a string. ex "2055-05-06 03:37:22 UT"
     */
    public String getTimeStamp() {
        return formatter.format(getTime()) + " UT";
    }

    /** Returns the date formatted in a string 
     *  @return date formatted in a string. ex "2055-05-06"
     */
    public String getDateString() {
        return getTimeStamp().substring(0,10);
    }
    
    /** Adds time to the calendar 
     *  @param seconds seconds added to the calendar
     */
    public void addTime(double seconds) {
        add(Calendar.MILLISECOND, (int) (seconds * 1000D));
    }
    
    /**
     * Displays the string version of the clock.
     * @return time stamp string.
     */
    public String toString() {
    	return getTimeStamp();
    }
}