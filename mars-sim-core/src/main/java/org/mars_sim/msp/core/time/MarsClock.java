/*
 * Mars Simulation Project
 * MarsClock.java
 * @date 2022-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;

// References:
// 1. Partially based on previous research from Shaun Moss' Mars calendar.
// 2. NASA Goddard Space Flight Center's Mars24 for determining the time for
//    a given location on Mars, which was primarily based on Allison and McEwen (2000)
//    (henceforth AM2000). See https://www.giss.nasa.gov/tools/mars24/help/algorithm.html

/**
 * The MarsClock class keeps track of Martian time.
 */
public class MarsClock implements Serializable {

	private static final long serialVersionUID = 65894231L;

	// private static final Logger logger = Logger.getLogger(MarsClock.class.getName());

	// Note 1: The Mars tropical year is 686.9725 day or 668.5921 sol.
	// For comparison, the Mars sidereal year, as measured with respect to the
	// fixed stars, is 668.5991 sol. The difference between these values results
	// from the precession of the planet's spin axis.

	// A Mars solar day has a mean period of 24 hours 39 minutes 35.244 seconds,
	// customarily referred to as a "sol" in order to distinguish this from the
	// roughly 3% shorter solar day on Earth.

	// The Mars sidereal day, as measured with respect to the fixed stars, is
	// 24h 37m 22.663s, as compared with 23h 56m 04.0905s for Earth.

	// Martian calendar static members

	static final int SOLS_PER_MONTH_SHORT = 27;

	private static final int SOLS_PER_ORBIT_LEAPYEAR = 669;
	private static final int MONTHS_PER_ORBIT = 24;

	public static final int SOLS_PER_ORBIT_NON_LEAPYEAR = 668;
	public static final int SOLS_PER_MONTH_LONG = 28;
	public static final int NUM_SOLS_SIX_MONTHS = SOLS_PER_MONTH_LONG * 5 + SOLS_PER_MONTH_SHORT;

	public static final double AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR = 668.5921;

	// Mars is at aphelion (its greatest distance from the Sun, 249 million
	// kilometers, where it moves most slowly) at Ls = 70 , near the northern
	// summer solstice,
	// Mars is at perihelion (least distance from the Sun, 207 million
	// kilometers, where it moves fastest) at Ls = 250°, near the southern
	// summer solstice.
	// The Mars dust storm season begins just after perihelion at around Ls =
	// 260°

	// Martian/Gregorian calendar conversion
	// Note: 1 millisol = 88.775244 sec

	/** Number of seconds per millisol. */
	public static final double SECONDS_PER_MILLISOL = 88.775244;
	/** Number of hours per millisol. */
	public static final double HOURS_PER_MILLISOL  = SECONDS_PER_MILLISOL / 3600D;
	/** Number of millisols per hour. */
	public static final double MILLISOLS_PER_HOUR  = 3600D / SECONDS_PER_MILLISOL;
	public static final double MILLISOLS_PER_DAY = MILLISOLS_PER_HOUR * 24;

	// Mars is near perihelion when it is summer in the southern hemisphere and
	// winter in the north, and near aphelion when it is winter in the southern
	// hemisphere and summer in the north. As a result, the seasons in the
	// southern hemisphere are more extreme and the seasons in the northern are
	// milder.

	/** Set the year of the planetfall as orbit 01. */
	public static final int FIRST_ORBIT = 1;
	
	// Data members
	/** The Martian year. Note: the year of landing is considered to be the first year/orbit */
	private int orbit = FIRST_ORBIT;
	/** The Martian month. */
	private int month;
	/** The Martian day. */
	private int solOfMonth;
	/** The mission sol since the start of the sim. */
	private int missionSol;
	/** The truncated integer millisols (NOT a rounded millisol) of the day. */
	private int intMillisol;
	/** The millisol of the day. */
	private double millisol;
	/** The total Millisols */
	private double totalMillisols;

	/**
	 * Constructor 2 : create a MarsClock instance with the given mission sol.
	 * Note that time will NOT increment in this clock.
	 *
	 * @param newSols the sols to be added to the calendar
	 */
	public MarsClock(int newSols) {

		// Set missionSol first
		this.missionSol = newSols;

		// Initialize params
		int orbit = 0;
		int month = 1;
		int sol = 0;

		int numSolsInOrbit = SOLS_PER_ORBIT_NON_LEAPYEAR;

		boolean isLeapOrbit = MarsClockFormat.isLeapOrbit(orbit);

		// For mission sols larger than an orbit
		while (newSols > numSolsInOrbit) {
			newSols = newSols - numSolsInOrbit;
			orbit++;

			// Update numSolsInOrbit
			isLeapOrbit = MarsClockFormat.isLeapOrbit(orbit);

			if (isLeapOrbit)
				numSolsInOrbit = SOLS_PER_ORBIT_NON_LEAPYEAR;
			else
				numSolsInOrbit = SOLS_PER_ORBIT_LEAPYEAR;

		}

		// For mission sols larger than 6 months
		while (newSols > NUM_SOLS_SIX_MONTHS) {
			newSols = newSols - NUM_SOLS_SIX_MONTHS;
			month = month + 6;

			if (month > 24) {
				orbit++;
				month = month - 24;
			}

		}

		// Update numSolsInOrbit
		isLeapOrbit = MarsClockFormat.isLeapOrbit(orbit);

		if (isLeapOrbit)
			numSolsInOrbit = SOLS_PER_ORBIT_NON_LEAPYEAR;
		else
			numSolsInOrbit = SOLS_PER_ORBIT_LEAPYEAR;


		// Update numSolsPerMonth
		boolean is27 = (month % 6 == 0) && !(isLeapOrbit && month % 24 == 0);
		int numSolsPerMonth = 0;

		if (is27)
			numSolsPerMonth = SOLS_PER_MONTH_SHORT;
		else
			numSolsPerMonth = SOLS_PER_MONTH_LONG;

		// If month number is divisible by 6, month has 27 sols
		// A standard month has 28 sols
		// However, if it's in a leap orbit and month number is 24,
		// then that month only has 28 sols, not 27 sols

		boolean lessThanOneMonth = true;


		// mission sols larger than 27 or 28 sols
		while (newSols > numSolsPerMonth) {
			lessThanOneMonth = false;
			month++;
			if (month > 24) {
				orbit++;
				month = month - 24;
			}
			newSols = newSols - numSolsPerMonth;
			//sol = sol + numSolsPerMonth;
			if (sol > numSolsPerMonth) {
				sol = sol - numSolsPerMonth;
				month++;
				if (month > 24) {
					orbit++;
					month = month - 24;
				}
			}

			// Update numSolsPerMonth
			is27 = (month % 6 == 0) && !(isLeapOrbit && month % 24 == 0);

			if (is27)
				numSolsPerMonth = SOLS_PER_MONTH_SHORT;
			else
				numSolsPerMonth = SOLS_PER_MONTH_LONG;

		}

		if (lessThanOneMonth) {
			sol = sol + newSols;
			if (sol > numSolsPerMonth) {
				sol = sol - numSolsPerMonth;
				month++;
				if (month > 24) {
					orbit++;
					month = month - 24;
				}
			}
		}


		this.orbit = orbit;
		this.month = month;
		this.solOfMonth = sol;
		this.millisol = 0;
		this.totalMillisols = calculateTotalMillisols(orbit, month, sol, millisol);
	}

	/**
	 * Constructor 3 : create a MarsClock object with a given time.
	 * Note that time will NOT increment in this clock.
	 *
	 * @param orbit    current orbit
	 * @param month    current month
	 * @param sol      current sol
	 * @param millisol current millisol
	 * @param missionSol current missionSol
	 */
	public MarsClock(int orbit, int month, int sol, double millisol, int missionSol) {

		// Set date/time to given parameters.
		this.orbit = orbit;
		this.month = month;
		this.solOfMonth = sol;
		this.millisol = millisol;
		this.missionSol = missionSol;
		this.totalMillisols = calculateTotalMillisols(orbit, month, sol, millisol);
		this.intMillisol = (int) millisol;
	}

	/**
	 * Constructor 3 : the copy constructor.
	 * 
	 * @param marsClock
	 */
	public MarsClock(MarsClock marsClock) {
		this.orbit = marsClock.getOrbit();
		this.month = marsClock.getMonth();
		this.solOfMonth = marsClock.getSolOfMonth();
		this.millisol = marsClock.getMillisol();
		this.missionSol = marsClock.getMissionSol();
		this.totalMillisols = MarsClock.calculateTotalMillisols(orbit, month, solOfMonth, millisol);
		this.intMillisol = marsClock.getMillisolInt();		
	}
	
	/**
	 * Converts seconds to millisols.
	 *
	 * @param seconds decimal number of seconds
	 * @return equivalent number of millisols
	 */
	public static double convertSecondsToMillisols(double seconds) {
		return seconds / SECONDS_PER_MILLISOL;
	}

	/**
	 * Converts millisols to seconds.
	 *
	 * @param millisols decimal number of millisols
	 * @return equivalent number of seconds
	 */
	public static double convertMillisolsToSeconds(double millisols) {
		return millisols * SECONDS_PER_MILLISOL;
	}

	/**
	 * Returns the time difference between two Mars clock instances.
	 *
	 * @param firstTime  first {@link MarsClock} instance
	 * @param secondTime second {@link MarsClock} instance
	 * @return time difference in millisols
	 */
	public static double getTimeDiff(MarsClock firstTime, MarsClock secondTime) {
		return firstTime.getTotalMillisols() - secondTime.getTotalMillisols();
	}


	/**
	 * Returns the mission sol. Note: the first day of the mission is Sol 1.
	 *
	 * @return sol
	 */
	public int getMissionSol() {
		return missionSol;
	}

	/**
	 * Adds time to the calendar. Note: negative time should be used to subtract
	 * time.
	 *
	 * @param addedMillisols millisols to be added to the calendar
	 */
	public void addTime(double addedMillisols) {

		totalMillisols += addedMillisols;
		millisol += addedMillisols;

		if (addedMillisols > 0D) {
			while (millisol >= 1000D) {
				millisol -= 1000D;
				solOfMonth += 1;
				missionSol += 1;
				if (solOfMonth > MarsClockFormat.getSolsInMonth(month, orbit)) {
					solOfMonth = 1;
					month += 1;
				}
				if (month > MONTHS_PER_ORBIT) {
					month = 1;
					orbit += 1;
				}
			}
		} else if (addedMillisols < 0D) {
			while (millisol < 0D) {
				millisol += 1000D;
				solOfMonth -= 1;
				missionSol -= 1;
				if (solOfMonth < 1) {
					month -= 1;
					solOfMonth = MarsClockFormat.getSolsInMonth(month, orbit);
				}
				if (month < 1) {
					month = MONTHS_PER_ORBIT;
					orbit -= 1;
				}
			}
		}

		intMillisol = (int) millisol;
	}

	/**
	 * Returns formatted time stamp string in the format of e.g. "03-Adir-05:056.349".
	 *
	 * @return formatted time stamp string
	 */
	public String getDateTimeStamp() {
		return MarsClockFormat.getDateTimeStamp(this);
	}

	/**
	 * Returns formatted time stamp string in the format of e.g. "03-Adir-05:056".
	 *
	 * @return formatted time stamp string
	 */
	public String getTrucatedDateTimeStamp() {
		return MarsClockFormat.getTruncatedDateTimeStamp(this);
	}

	/**
	 * Returns formatted time stamp string in the format of e.g. "00-Adir-01:056 Solisol".
	 *
	 * @return formatted time stamp string
	 */
	public String getDisplayTruncatedTimeStamp() {
		return MarsClockFormat.getTruncatedDateTimeStamp(this) + " " + MarsClockFormat.getSolOfWeekName(this);
	}

	/**
	 * Returns formatted time stamp string in the format of e.g. "00-Adir-01:056 Solisol".
	 *
	 * @return formatted time stamp string
	 */
	public String getDisplayDateTimeStamp() {
		return MarsClockFormat.getDateTimeStamp(this) + " " + MarsClockFormat.getSolOfWeekName(this);
	}

	/**
	 * Gets the current date string in the format of e.g. "03-Adir-05".
	 *
	 * @return current date string
	 */
	public String getDateString() {
		return MarsClockFormat.getDateString(this);
	}


	/**
	 * Returns the total number of millisols of a given time.
	 *
	 * @param orbit
	 * @param month
	 * @param sol
	 * @param millisol total millisols
	 * @return
	 */
	private static double calculateTotalMillisols(int orbit, int month, int sol, double millisol) {
		double result = 0D;

		// Add millisols up to current orbit
		for (int x = 0; x < orbit; x++) {
			if (MarsClockFormat.isLeapOrbit(x))
				result += SOLS_PER_ORBIT_LEAPYEAR * 1000D;
			else
				result += SOLS_PER_ORBIT_NON_LEAPYEAR * 1000D;
		}

		// Add millisols up to current month
		for (int x = 1; x < month; x++)
			result += MarsClockFormat.getSolsInMonth(x, orbit) * 1000D;

		// Add millisols up to current sol
		result += (sol - 1) * 1000D;

		// Add millisols in current sol
		result += millisol;

		// System.out.println("MarsClock : result : " + result);

		return result;
	}

	/**
	 * Return the total millisols.
	 * 
	 * @return Total
	 */
	public double getTotalMillisols() {
		return totalMillisols;
	}

	/**
	 * Returns the name of the current month.
	 *
	 * @return name of the current month
	 */
	public String getMonthName() {
		return MarsClockFormat.getMonthName(month);
	}

	/**
	 * Returns the orbit.
	 *
	 * @return the orbit as an integer
	 */
	public int getOrbit() {
		return orbit;
	}

	/**
	 * Returns the month (1 - 24).
	 *
	 * @return the month as an integer
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Returns the sol of month (1 - 28).
	 *
	 * @return the sol of month as an integer
	 */
	public int getSolOfMonth() {
		return solOfMonth;
	}

	/**
	 * Returns the millisols.
	 *
	 * @return the millisol as a double
	 */
	public double getMillisol() {
		return millisol;
	}

	/**
	 * Returns the truncated integer millisols (NOT a rounded millisol).
	 *
	 * @return the millisol as an int
	 */
	public int getMillisolInt() {
		return intMillisol;
	}

	/**
	 * Checks if the mars clock becomes stable enough at the start of the sim.
	 * 
	 * @return
	 */
	public boolean isStable() {
		// Note: for now, after 10 millisols, the clock seems to become stable
        return millisol > 15;
    }

	/**
	 * Displays the string version of the clock.
	 *
	 * @return time stamp string.
	 */
	public String toString() {
		return getDateTimeStamp();
	}

	/**
	 * Checks if another object is equal to this one.
	 *
	 * @param object for comparison
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		boolean result = true;
		if (object instanceof MarsClock) {
			MarsClock otherClock = (MarsClock) object;
			if (orbit != otherClock.orbit)
				result = false;
			else if (month != otherClock.month)
				result = false;
			else if (solOfMonth != otherClock.solOfMonth)
				result = false;
			else if (millisol != otherClock.millisol)
				result = false;
		} else
			result = false;
		return result;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return orbit * month * solOfMonth + ((int) (millisol * 1000D));
	}

}
