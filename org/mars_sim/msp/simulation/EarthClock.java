/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 2.72 2001-02-25
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

    /** Constructs a EarthClock object */
    public EarthClock() {
        
        // Use GregorianCalendar constructor
        super();

        // Set GMT timezone for calendar
        SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");
        setTimeZone(zone);

        // Set starting date/time to midnight, January 1st, 2050
        clear(); 
        set(2050, Calendar.JANUARY, 1);
        complete();
    }

    /** Returns the date/time formatted in a string 
     *  @return date/time formatted in a string. ex "5/6/2055 3:37 PM"
     */
    public String getDateTimeString() {
        int month = get(Calendar.MONTH) + 1;
        int date = get(Calendar.DATE);
        int year = get(Calendar.YEAR);
        int hour = get(Calendar.HOUR);
        if (hour == 0) hour = 12;
        int minute = get(Calendar.MINUTE);
        int am_pm = get(Calendar.AM_PM);
       
        String am_pmString;
        if (am_pm == Calendar.AM) am_pmString = "AM";
        else am_pmString = "PM"; 
        
        String minuteString;
        if (minute < 10) minuteString = "0" + minute;
        else minuteString = "" + minute;
        return new String("" + month + "/" + date + "/" + year + " " + hour + ":" + minuteString + " " + am_pmString);
    }
    
    /** Adds time to the calendar 
     *  @param seconds seconds added to the calendar
     */
    public void addTime(double seconds) {
        add(Calendar.MILLISECOND, (int) (seconds * 1000D));
    }
}
