/**
 * Mars Simulation Project
 * MarsClock.java
 * @version 2.72 2001-03-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.text.*;

/** The MarsClock class keeps track of Martian time.
 *  This uses Shaun Moss's Mars Calendar, which is
 *  described at http://www.virtualmars.net/Time.asp.
 */
public class MarsClock {

    // Martian calendar static members
    private static final int SOLS_IN_ORBIT_NON_LEAPYEAR = 668;
    private static final int SOLS_IN_ORBIT_LEAPYEAR = 669;
    private static final int MONTHS_IN_ORBIT = 24;
    private static final int SOLS_IN_MONTH_SHORT = 27;
    private static final int SOLS_IN_MONTH_LONG = 28;
    private static final int WEEKS_IN_ORBIT = 96;
    private static final int WEEKS_IN_MONTH = 4;
    private static final int SOLS_IN_WEEK_SHORT = 6;
    private static final int SOLS_IN_WEEK_LONG = 7;
    private static final int MILLISOLS_IN_SOL = 1000;
    
    // Martian/Gregorian calendar conversion
    private static final double SECONDS_IN_MILLISOL = 88.775244;

    // Martian calendar static strings
    private static final String[] MONTH_NAMES = { "Adir", "Bora", "Coan", "Detri",
        "Edal", "Flo", "Geor", "Heliba", "Idanon", "Jowani", "Kireal", "Larno",
        "Medior", "Neturima", "Ozulikan", "Pasurabi", "Rudiakel", "Safundo", "Tiunor",
        "Ulasja", "Vadeun", "Wakumi", "Xetual", "Zungo" };

    private static final String[] WEEK_SOL_NAMES = { "Solisol", "Phobosol", "Deimosol", 
        "Terrasol", "Hermesol", "Venusol", "Jovisol" };

    // Data members
    private int orbit;
    private int month;
    private int sol;
    private double millisol; 

    /** Constructs a MarsClock object */
    public MarsClock() {
    
        // Set initial date
        orbit = 0;
        month = 1;
        sol = 1;
        millisol = 0D;
    }

    /** Converts seconds to millisols
     *  @param seconds decimal number of seconds
     *  @return equivalent number of millisols
     */
    public static double convertSecondsToMillisols(double seconds) {
        return seconds / SECONDS_IN_MILLISOL;
    }

    /** Returns the number of sols in a month for
     *  a given month and orbit.
     *  @param month the month number
     *  @param orbit the orbit number
     */
    public static int getSolsInMonth(int month, int orbit) {

        // Standard month has 28 sols.
        int result = SOLS_IN_MONTH_LONG;

        // If month number is divisable by 6, month has 27 sols 
        if ((month % 6) == 0) result = SOLS_IN_MONTH_SHORT;

        // If leap orbit and month number is 24, month has 28 sols 
        if ((month == 24) && isLeapOrbit(orbit)) result = SOLS_IN_MONTH_LONG;

        return result;
    }

    /** Returns true if orbit is a leap orbit, false if not.
     *  @param orbit the orbit number
     */
    public static boolean isLeapOrbit(int orbit) {
        boolean result = false;
        
        // If an orbit is divisable by 10 it is a leap orbit
        if ((orbit % 10) == 0) result = true;
      
        // If an orbit is divisable by 100, it is not a leap orbit
        if ((orbit % 100) == 0) result = false;
       
        // If an orbit is divisable by 500, it is a leap orbit
        if ((orbit % 500) == 0) result = true;

        return result;
    }

    /** Adds time to the calendar
     *  @param addedMillisols millisols to be added to the calendar
     */ 
    public void addTime(double addedMillisols) {
        
        millisol += addedMillisols;
        
        while (millisol >= 1000D) {
            millisol -= 1000D;
            sol += 1;
            if (sol > getSolsInMonth(month, orbit)) {
                sol = 1;
                month += 1;
                if (month > MONTHS_IN_ORBIT) {
                    month = 1;
                    orbit += 1;
                }
            }
        } 
    }

    /** Returns formatted time stamp string
     *  ex. "13-Adir-05:056.349"
     *  @return formatted timestamp string
     */
    public String getTimeStamp() {
       
        StringBuffer result = new StringBuffer(""); 
        
        // Append orbit to timestamp
        result.append("" + orbit + "-");

        // Append month to timestamp
        result.append(getMonthName() + "-");

        // Append sol of month to timestamp
        String solString = "" + sol;
        if (solString.length() == 1) solString = "0" + solString;
        result.append(solString + ":");
       
        // Append millisols to timestamp
        String millisolString = "" + (Math.floor(millisol * 1000D) / 1000D);
        if (millisol < 100D) millisolString = "0" + millisolString;
        if (millisol < 10D) millisolString = "0" + millisolString;
        while (millisolString.length() < 7) millisolString += "0";
        result.append(millisolString);

        return result.toString();
    }
   
    /** Returns the name of the current month
     *  @return name of the current month
     */
    public String getMonthName() {
        return MONTH_NAMES[month - 1];
    }
}
