/**
 * Mars Simulation Project
 * MarsClock.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.util.Arrays;

import org.mars_sim.msp.core.Simulation;

/** The MarsClock class keeps track of Martian time.
 *  This uses Shaun Moss's Mars Calendar, which is
 *  described at http://www.virtualmars.net/Time.asp.
 */
public class MarsClock implements Serializable {

    // Martian calendar static members
    public static final int SOLS_IN_ORBIT_NON_LEAPYEAR = 668;
    public static final int SOLS_IN_ORBIT_LEAPYEAR = 669;
    private static final int MONTHS_IN_ORBIT = 24;
    private static final int SOLS_IN_MONTH_SHORT = 27;
    public static final int SOLS_IN_MONTH_LONG = 28;
    // private static final int WEEKS_IN_ORBIT = 96;
    // private static final int WEEKS_IN_MONTH = 4;
    private static final int SOLS_IN_WEEK_SHORT = 6;
    private static final int SOLS_IN_WEEK_LONG = 7;
    // private static final int MILLISOLS_IN_SOL = 1000;
    public static final int NORTHERN_HEMISPHERE = 1;
    public static final int SOUTHERN_HEMISPHERE = 2;
    
	private static final int SUMMER_SOLSTICE = 142;
	private static final int AUTUMN_EQUUINOX= 309;
	private static final int WINTER_SOLSTICE = 476;
	private static final int SPRING_EQUUINOX = 643;
	
    // Martian/Gregorian calendar conversion
    private static final double SECONDS_IN_MILLISOL = 88.775244;

    public static final int THE_FIRST_SOL = 9353;
    
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

    /**
     * Constructor with date string parameter.
     * @param dateString format: "orbit-month-sol:millisol"
     * @throws Exception if dateString is invalid.
     */
    public MarsClock(String dateString) {
    
        // Set initial date to dateString. ex: "15-Adir-01:000.000"
        String orbitStr = dateString.substring(0, dateString.indexOf("-"));
        orbit = Integer.parseInt(orbitStr);
        if (orbit < 0) throw new IllegalStateException("Invalid orbit number: " + orbit);
        	
        String monthStr = dateString.substring(dateString.indexOf("-") + 1, dateString.lastIndexOf("-"));
        month = 0;
        for (int x=0; x < MONTH_NAMES.length; x++) {
        	if (monthStr.equals(MONTH_NAMES[x])) month = x + 1;
        }
        if ((month < 1) || (month > MONTH_NAMES.length)) throw new IllegalStateException("Invalid month: " + monthStr);
        	
        String solStr = dateString.substring(dateString.lastIndexOf("-") + 1, dateString.indexOf(":"));
        sol = Integer.parseInt(solStr);
        if (sol < 1) throw new IllegalStateException("Invalid sol number: " + sol);
        	
        String millisolStr = dateString.substring(dateString.indexOf(":") + 1);
        millisol = Double.parseDouble(millisolStr);
        if (millisol < 0D) throw new IllegalStateException("Invalid millisol number: " + millisol);
        
    }
    
    /** Constructs a MarsClock object with a given time
     *  param orbit current orbit
     *  param month current month
     *  param sol current sol
     *  param millisol current millisol
     */
    public MarsClock(int orbit, int month, int sol, double millisol) {
        // Set date/time to given parameters.
        this.orbit = orbit;
        this.month = month;
        this.sol = sol;
        this.millisol = millisol;
    }

    /** Converts seconds to millisols
     *  @param seconds decimal number of seconds
     *  @return equivalent number of millisols
     */
    public static double convertSecondsToMillisols(double seconds) {
        return seconds / SECONDS_IN_MILLISOL;
    }

    /** Converts millisols to seconds 
     *  @param millisols decimal number of millisols 
     *  @return equivalent number of seconds 
     */
    public static double convertMillisolsToSeconds(double millisols) {
        return millisols * SECONDS_IN_MILLISOL;
    }
    
    /** Returns the time difference between two Mars clock instances.
     *  @param firstTime first Mars clock instance
     *  @param secondTime second Mars clock instance
     *  @return time difference in millisols
     */
    public static double getTimeDiff(MarsClock firstTime, MarsClock secondTime) {
        return getTotalMillisols(firstTime) - getTotalMillisols(secondTime);
    }
    
    /**
     * Gets the names of the Martian months.
     * @return array of month names.
     */
    public static String[] getMonthNames() {
        return Arrays.copyOf(MONTH_NAMES, MONTH_NAMES.length);
    }
    
    /**
     * Gets the names of the Martian sols of the week.
     * @return array of week sol names.
     */
    public static String[] getWeekSolNames() {
        return Arrays.copyOf(WEEK_SOL_NAMES, WEEK_SOL_NAMES.length);
    }
    
    /** Returns the total millisols in the Mars clock from orbit 0.
     *  @param time Mars clock instance
     *  @return total millisols
     */
    public static double getTotalMillisols(MarsClock time) {
        double result = 0D;
        
        // Add millisols up to current orbit
        for (int x=1; x < time.orbit; x++) {
            if (MarsClock.isLeapOrbit(x)) result += SOLS_IN_ORBIT_LEAPYEAR * 1000D;
            else result += SOLS_IN_ORBIT_NON_LEAPYEAR * 1000D;
        }
        
        // Add millisols up to current month
        for (int x=1; x < time.month; x++)
            result += MarsClock.getSolsInMonth(x, time.orbit) * 1000D;
            
        // Add millisols up to current sol
        result += (time.sol - 1) * 1000D;
   
        // Add millisols in current sol
        result += time.millisol;
        
		//System.out.println("MarsClock : result : " + result);
		
        return result;
    }


    /** Returns the sol in a Martian year
     *  @return the sol of year as an integer
     */
    // 2015-01-28 Added getSolOfYear()
    public static int getSolOfYear(MarsClock time) {
    	
    	int result = 0;

        // Add sols up to current month
        for (int x=1; x < time.month; x++)
            result += MarsClock.getSolsInMonth(x, time.orbit) ;
            
        // Add sols up to current sol
        result += time.sol  ;
	
        return result;
    }

    /** Returns the total number of sol since the start of the simulation
     *  @return the total number of sol as an integer
     */
    // 2015-02-09 Added getTotalSol
    public static int getTotalSol(MarsClock time) {
    	
    	int result = 0;

        // Add sols up to current orbit
        for (int x=1; x < time.orbit; x++) {
            if (MarsClock.isLeapOrbit(x)) result += SOLS_IN_ORBIT_LEAPYEAR;
            else result += SOLS_IN_ORBIT_NON_LEAPYEAR;
        }
        
        // Add sols up to current month
        for (int x=1; x < time.month; x++)
            result += MarsClock.getSolsInMonth(x, time.orbit) ;
            
        // Add sols up to current sol
        result += time.sol  ;
	
        result = result - THE_FIRST_SOL;
        
        return result;
    }
    
    /** Returns the number of sols in a month for
     *  a given month and orbit.
     *  @param month the month number
     *  @param orbit the orbit number
     */
    public static int getSolsInMonth(int month, int orbit) {

        // Standard month has 28 sols.
        int result = SOLS_IN_MONTH_LONG;

        // If month number is divisible by 6, month has 27 sols 
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

    /** 
     * Adds time to the calendar
     * Note: negative time should be used to subtract time.
     * @param addedMillisols millisols to be added to the calendar
     */ 
    public void addTime(double addedMillisols) {
        
        millisol += addedMillisols;
        
        if (addedMillisols > 0D) {
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
        else if (addedMillisols < 0D) {
            while (millisol < 0D) {
                millisol += 1000D;
                sol -= 1;
                if (sol < 1) {
                    month -= 1;
                    if (month < 1) {
                        month = MONTHS_IN_ORBIT;
                        orbit -= 1;
                    }
                    sol = getSolsInMonth(month, orbit);
                }
            }
        }
    }

    /** Returns formatted time stamp string.
     *  ex. "13-Adir-05  056.349"
     *  @return formatted timestamp string
     */
    public String getTimeStamp() {
        return new StringBuilder(getDateString()).append("  ").append(getTimeString()).toString();
    }

    /** 
     * Gets the current date string.
     * ex. "13-Adir-05"
     * @return current date string
     */
    public String getDateString() {
        StringBuilder result = new StringBuilder();

        // Append orbit
        result.append(orbit).append("-").append(getMonthName()).append("-");

        if(sol < 10){
            result.append("0");
        }
        result.append(sol);
//        // Append sol of month
//        String solString = "" + sol;
//        if (solString.length() == 1) solString = "0" + solString;
//        result.append(solString);

        return result.toString();
    }

    /** Return the current time string.
     *  ex. "05:056.349"
     */
    public String getTimeString() {
        StringBuilder b = new StringBuilder();
        double tb = Math.floor(millisol * 1000D) / 1000D;
//        String result = "" + tb;
        b.append(tb);
        if (millisol < 100D) {
            b.insert(0,"0");
//            result = "0" + result;
        }
        if (millisol < 10D) {
            b.insert(0,"0");
//            result = "0" + result;
        }
        while (b.length() < 7){
            b.append("0");
//            result += "0";
        }

        return b.toString();
    }

    /** Returns the name of the current month.
     *  @return name of the current month
     */
    public String getMonthName() {
        return MONTH_NAMES[month - 1];
    }

    /** Returns the orbit
     *  @return the orbit as an integer
     */
    public int getOrbit() { return orbit; }

    /** Returns the month (1 - 24)
     *  @return the month as an integer
     */
    public int getMonth() { return month; }

    /** Returns the sol of month (1 - 28)
     *  @return the sol of month as an integer
     */
    public int getSolOfMonth() { return sol; }

    /** Returns the millisol 
     *  @return the millisol as a double
     */ 
    public double getMillisol() { return millisol; }

    /** Returns the week of the month (1-4)
     *  @return the week of the month as an integer
     */
    public int getWeekOfMonth() {
        return ((sol -1) / 7) + 1;
    }

    /** Returns the sol number of the week (1-7)
     *  @return the sol number of the week as an integer
     */ 
    public int getSolOfWeek() {
        return sol - ((getWeekOfMonth() - 1) * 7);
    }

    /** Return the sol name of the week
     *  @return the sol name of the week as a String
     */
    public String getSolOfWeekName() {
        return WEEK_SOL_NAMES[getSolOfWeek() - 1];
    }
    
    /** Returns the number of sols in the current week
     *  @return the number of osls in the current week as an integer
     */
    public int getSolsInWeek() {
        int result = SOLS_IN_WEEK_LONG;

        if (getSolsInMonth(month, orbit) == SOLS_IN_MONTH_SHORT) {
            if (getWeekOfMonth() == 4) 
                result = SOLS_IN_WEEK_SHORT;
        }
        return result;
    }

    /** Returns the current season for the given hemisphere
     *  @param hemisphere the hemisphere 
     *  NORTHERN_HEMISPHERE or SOUTHERN_HEMISPHERE valid parameters
     *  @return season as String ("Spring", "Summer", "Autumn" or "Winter")
     */
    public String getSeason(int hemisphere) {

    	String season = "0";
    	MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
    	
    	int solElapsed = MarsClock.getSolOfYear(marsClock);
 		
		if (solElapsed > SOLS_IN_ORBIT_LEAPYEAR)
			solElapsed = solElapsed - SOLS_IN_ORBIT_NON_LEAPYEAR; 
		
		if (solElapsed < SUMMER_SOLSTICE){
            if (hemisphere == NORTHERN_HEMISPHERE) season = "Spring";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = "Autumn";
        }
		else if (solElapsed < AUTUMN_EQUUINOX) {
            if (hemisphere == NORTHERN_HEMISPHERE) season = "Summer";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = "Winter";
        }
		else if (solElapsed < WINTER_SOLSTICE){
            if (hemisphere == NORTHERN_HEMISPHERE) season = "Autumn";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = "Spring";
        }
		else if (solElapsed < SPRING_EQUUINOX) {
            if (hemisphere == NORTHERN_HEMISPHERE) season = "Winter";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = "Summer";
        }
		
		return season;
    }
    
    /** 
     * Creates a clone of this MarsClock object, with the time set the same.
     * @return clone of this MarsClock object
     */
    public Object clone() {
        return new MarsClock(orbit, month, sol, millisol);
    }
    
    /**
     * Displays the string version of the clock.
     * @return time stamp string.
     */
    public String toString() {
    	return getTimeStamp();
    }
    
    /**
     * Checks if another object is equal to this one.
     */
    public boolean equals(Object object) {
    	boolean result = true;
    	if (object instanceof MarsClock) {
    		MarsClock otherClock = (MarsClock) object;
    		if (orbit != otherClock.orbit) result = false;
    		else if (month != otherClock.month) result = false;
    		else if (sol != otherClock.sol) result = false;
    		else if (millisol != otherClock.millisol) result = false;
    	}
    	else result = false;
    	return result;
    }
    
    /**
     * Gets the hash code for this object.
     * @return hash code.
     */
    public int hashCode() {
    	return orbit * month * sol + ((int) (millisol * 1000D));
    }
}