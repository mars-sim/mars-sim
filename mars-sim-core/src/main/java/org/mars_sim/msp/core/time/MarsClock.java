/**
 * Mars Simulation Project
 * MarsClock.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.util.Arrays;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;

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

	// private static Logger logger = Logger.getLogger(MarsClock.class.getName());

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
	public static final int SOLS_PER_ORBIT_NON_LEAPYEAR = 668;
	public static final int SOLS_PER_ORBIT_LEAPYEAR = 669;
	private static final int MONTHS_PER_ORBIT = 24;
	private static final int SOLS_PER_MONTH_SHORT = 27;
	public static final int SOLS_PER_MONTH_LONG = 28;
	// private static final int WEEKS_IN_ORBIT = 96;
	// private static final int WEEKS_IN_MONTH = 4;
	private static final int SOLS_PER_WEEK_SHORT = 6;
	private static final int SOLS_PER_WEEK_LONG = 7;
	// private static final int MILLISOLS_IN_SOL = 1000;
	public static final int NORTHERN_HEMISPHERE = 1;
	public static final int SOUTHERN_HEMISPHERE = 2;

	public static final int NUM_SOLS_SIX_MONTHS = SOLS_PER_MONTH_LONG * 5 + SOLS_PER_MONTH_SHORT;
	
	// Note: the summer solstice marks the longest day of the calendar year and
	// the beginning of summer in the "Northern Hemisphere".
	// The solar longitude Ls is the Mars-Sun angle, measured from the Northern
	// Hemisphere spring equinox where L_s=0.
	// Ls=90 corresponds to summer solstice, just as L_s=180 marks the autumn
	// equinox and L_s=270 the winter solstice (all relative to the northern
	// hemisphere).
	// see http://www-mars.lmd.jussieu.fr/mars/time/solar_longitude.html
	// private static final int SUMMER_SOLSTICE = 168; // (Ls = 90°)
	// private static final int AUTUMNAL_EQUINOX = 346; // (Ls = 180°)
	// private static final int WINTER_SOLSTICE = 489; // (Ls = 270°)
	// private static final int SPRING_EQUINOX = 643; // (Ls = 0°) or on the -25th
	// sols

	// Mars is at aphelion (its greatest distance from the Sun, 249 million
	// kilometers, where it moves most slowly) at Ls = 70 , near the northern
	// summer solstice,
	// Mars is at perihelion (least distance from the Sun, 207 million
	// kilometers, where it moves fastest) at Ls = 250°, near the southern
	// summer solstice.
	// The Mars dust storm season begins just after perihelion at around Ls =
	// 260°

	// public static final int THE_FIRST_SOL = 9353;

	// Martian/Gregorian calendar conversion
	public static final double SECONDS_PER_MILLISOL = 88.775244;
	// 1 millisol = 88.775244 sec

	// from https://www.teuse.net/games/mars/mars_dates.html
	// Demios only takes 30hrs, and Phobos 7.6hrs to rotate around mars
	// Spring lasts 193.30 sols
	// Summer lasts 178.64 sols
	// Autumn lasts 142.70 sols
	// Winter lasts 153.94 sols
	// No thats doesnt add up exactly to 668.5921 sols. Worry about that later
	// just like our ancestors did.
	// That gives us 4 "holidays". Round off the numbers for when they occur.
	// Spring Equinox at sol 1,
	// Summer Solstice at sol 193,
	// Autumnal equinox sol 372,
	// Winter solstice at sol 515,
	// Spring again sol 669 or 1 new annus.
	// This gives them 4 periods to use like we do months.
	// They are a bit long so maybe they divide them up more later.

	private static final String DASH = "-";
	private static final String COLON = ":";
	private static final String ONE_ZERO = "0";
	private static final String TWO_ZEROS = "00";
	private static final String THREE_ZEROS = "000";
	private static final String UMST = "(UMST) ";
	private static final String EARLY = "Early ";
	private static final String MID = "Mid ";
	private static final String LATE = "Late ";
	private static final String SPRING = "Spring";
	private static final String SUMMER = "Summer";
	private static final String AUTUMN = "Autumn";
	private static final String WINTER = "Winter";

	// Mars is near perihelion when it is summer in the southern hemisphere and
	// winter in the north, and near aphelion when it is winter in the southern
	// hemisphere and summer in the north. As a result, the seasons in the
	// southern hemisphere are more extreme and the seasons in the northern are
	// milder.

	// Martian calendar static strings
	private static final String[] MONTH_NAMES = { "Adir", "Bora", "Coan", "Detri", "Edal", "Flo", "Geor", "Heliba",
			"Idanon", "Jowani", "Kireal", "Larno", "Medior", "Neturima", "Ozulikan", "Pasurabi", "Rudiakel", "Safundo",
			"Tiunor", "Ulasja", "Vadeun", "Wakumi", "Xetual", "Zungo" };

	private static final String[] WEEK_SOL_NAMES = { "Solisol", "Phobosol", "Deimosol", "Terrasol", "Hermesol",
			"Venusol", "Jovisol" };

	// Data members
	/** The Martian year. */
	private int orbit;
	/** The Martian month. */
	private int month;
	/** The Martian day. */
	private int sol;
	/** The mission sol since the start of the sim. */	
	private int missionSol;
	/** The rounded millisol of the day. */
	private int msolInt;
	/** The millisol of the day. */
	private double millisol;
	/** The millisol of the day in 1 decimal place. */
	private double msol1;

	
	private static OrbitInfo orbitInfo;

//	private static EarthClock earthClock;

	private Simulation sim;


	/**
	 * Constructor 1 : create an instance of MarsClock with date string parameter.
	 *
	 * @param dateString format: "orbit-month-sol millisol"
	 * @throws Exception if dateString is invalid.
	 */
	public MarsClock(String dateString) {
		// logger.config("MarsClock's constructor is on " +
		// Thread.currentThread().getName() + " Thread");

		sim = Simulation.instance();
//		earthClock = sim.getMasterClock().getEarthClock();

		if (orbitInfo == null)
			orbitInfo = sim.getMars().getOrbitInfo();

		// Set initial date to dateString. ex: "0000-Adir-01 000.000"
		String orbitStr = dateString.substring(0, dateString.indexOf(DASH));
		orbit = Integer.parseInt(orbitStr);
		if (orbit < 0)
			throw new IllegalStateException("Invalid orbit number: " + orbit);

		String monthStr = dateString.substring(dateString.indexOf(DASH) + 1, dateString.lastIndexOf(DASH));
		month = 0;
		for (int x = 0; x < MONTH_NAMES.length; x++) {
			if (monthStr.equals(MONTH_NAMES[x]))
				month = x + 1;
		}
		if ((month < 1) || (month > MONTH_NAMES.length))
			throw new IllegalStateException("Invalid month: " + monthStr);

		String solStr = dateString.substring(dateString.lastIndexOf(DASH) + 1, dateString.indexOf(COLON));
		sol = Integer.parseInt(solStr);
		if (sol < 1)
			throw new IllegalStateException("Invalid sol number: " + sol);

		String millisolStr = dateString.substring(dateString.indexOf(COLON) + 1);
		millisol = Double.parseDouble(millisolStr);
		if (millisol < 0D)
			throw new IllegalStateException("Invalid millisol number: " + millisol);

		missionSol = 1; // the sol since the start of the sim
	}

//	public int getOrbit(int missionSol) {
//		
//		// If an orbit is divisible by 10 it is a leap orbit
//		// If an orbit is divisible by 100, it is not a leap orbit
//		// If an orbit is divisible by 500, it is a leap orbit
//	
//		int orbit = 0;
//		if (missionSol > SOLS_PER_ORBIT_NON_LEAPYEAR) {
//			missionSol = missionSol - SOLS_PER_ORBIT_NON_LEAPYEAR;
//			orbit++;
//		}
//		return orbit;
//	}
	
	
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
		
		sim = Simulation.instance();

		if (orbitInfo == null)
			orbitInfo = sim.getMars().getOrbitInfo();	

		boolean isLeapOrbit = isLeapOrbit(orbit);
		
		// For mission sols larger than an orbit
		while (newSols > numSolsInOrbit) {
			newSols = newSols - numSolsInOrbit;
			orbit++;
					
			// Update numSolsInOrbit
			isLeapOrbit = isLeapOrbit(orbit);
			
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
		isLeapOrbit = isLeapOrbit(orbit);
		
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

//		System.out.println("A. month : " + month + "   sol : " + sol + "   newSols : " + newSols);
		
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
			
//			System.out.println("B. month : " + month + "   sol : " + sol + "   newSols : " + newSols);
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
//			System.out.println("C. month : " + month + "   sol : " + sol + "   newSols : " + newSols);
		}
		

		this.orbit = orbit;
		this.month = month;
		this.sol = sol;
		this.millisol = 0;
		
		
		// Note : the following procedures can be done only if addedMillisols is small 
		// and needs to be done iteratively and thus is very slow
		
//		millisol += addedMillisols;
//		msol1 = Math.round(millisol * 10.0) / 10.0;
//		msolInt = (int) millisol;
//
//		if (addedMillisols > 0D) {
//			while (millisol >= 1000D) {
//				millisol -= 1000D;
//				sol += 1;
//				missionSol += 1;
//				if (sol > getSolsInMonth(month, orbit)) {
//					sol = 1;
//					month += 1; ?
//					if (month > MONTHS_PER_ORBIT) {
//						month = 1;
//						orbit += 1;
//					}
//				}
//			}
//		} else if (addedMillisols < 0D) {
//			while (millisol < 0D) {
//				millisol += 1000D;
//				sol -= 1;
//				missionSol -= 1;
//				if (sol < 1) {
//					month -= 1;
//					if (month < 1) {
//						month = MONTHS_PER_ORBIT;
//						orbit -= 1;
//					}
//					sol = getSolsInMonth(month, orbit);
//				}
//			}
//		}
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
		// logger.config("MarsClock's constructor is on " +
		// Thread.currentThread().getName() + " Thread");

		// Set date/time to given parameters.
		this.orbit = orbit;
		this.month = month;
		this.sol = sol;
		this.millisol = millisol;
		this.missionSol = missionSol;
		
		sim = Simulation.instance();

		if (orbitInfo == null)
			orbitInfo = sim.getMars().getOrbitInfo();

	}

	/**
	 * Converts seconds to millisols
	 *
	 * @param seconds decimal number of seconds
	 * @return equivalent number of millisols
	 */
	public static double convertSecondsToMillisols(double seconds) {
		return seconds / SECONDS_PER_MILLISOL;
	}

	/**
	 * Converts millisols to seconds
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
		return getTotalMillisols(firstTime) - getTotalMillisols(secondTime);
	}

	/**
	 * Gets the names of the Martian months.
	 *
	 * @return array of month names.
	 */
	public static String[] getMonthNames() {
		return Arrays.copyOf(MONTH_NAMES, MONTH_NAMES.length);
	}

	/**
	 * Gets the names of the Martian sols of the week.
	 *
	 * @return array of week sol names.
	 */
	public static String[] getWeekSolNames() {
		return Arrays.copyOf(WEEK_SOL_NAMES, WEEK_SOL_NAMES.length);
	}

	/**
	 * Returns the mission sol. Note: the first day of the mission is Sol 1
	 * 
	 * @return sol
	 */
	public int getMissionSol() {
		return missionSol;
	}

	/**
	 * Returns the number of sols for a given month and orbit.
	 *
	 * @param month the month number
	 * @param orbit the orbit number
	 */
	public static int getSolsInMonth(int month, int orbit) {

		// Standard month has 28 sols.
		int result = SOLS_PER_MONTH_LONG;

		// If month number is divisible by 6, month has 27 sols
		if ((month % 6) == 0)
			result = SOLS_PER_MONTH_SHORT;

		// If leap orbit and month number is 24, month has 28 sols
		if ((month == 24) && isLeapOrbit(orbit))
			result = SOLS_PER_MONTH_LONG;

		return result;
	}

	/**
	 * Returns true if orbit is a leap orbit, false if not.
	 *
	 * @param orbit the orbit number
	 */
	public static boolean isLeapOrbit(int orbit) {
		boolean result = false;

		// If an orbit is divisible by 10 it is a leap orbit
		if ((orbit % 10) == 0)
			result = true;

		// If an orbit is divisible by 100, it is not a leap orbit
		if ((orbit % 100) == 0)
			result = false;

		// If an orbit is divisible by 500, it is a leap orbit
		if ((orbit % 500) == 0)
			result = true;

		return result;
	}

	/**
	 * Adds time to the calendar. Note: negative time should be used to subtract
	 * time.
	 * 
	 * @param addedMillisols millisols to be added to the calendar
	 */
	public void addTime(double addedMillisols) {

		millisol += addedMillisols;

		if (addedMillisols > 0D) {
			while (millisol >= 1000D) {
				millisol -= 1000D;
				sol += 1;
				missionSol += 1;
				if (sol > getSolsInMonth(month, orbit)) {
					sol = 1;
					month += 1;
					if (month > MONTHS_PER_ORBIT) {
						month = 1;
						orbit += 1;
					}
				}
			}
		} else if (addedMillisols < 0D) {
			while (millisol < 0D) {
				millisol += 1000D;
				sol -= 1;
				missionSol -= 1;
				if (sol < 1) {
					month -= 1;
					if (month < 1) {
						month = MONTHS_PER_ORBIT;
						orbit -= 1;
					}
					sol = getSolsInMonth(month, orbit);
				}
			}
		}
		
		msol1 = Math.round(millisol * 10.0) / 10.0;
		msolInt = (int) msol1;//illisol;

	}

	/**
	 * Returns formatted time stamp string in the format of e.g. "0013-Adir-05:056.349"
	 *
	 * @return formatted time stamp string
	 */
	public String getDateTimeStamp() {
		// TODO: are the "two" whitespace intentional? or should we use colon as the
		// separator ?
		return new StringBuilder(getDateString()).append(COLON).append(getDecimalTimeString()).toString();
	}

	/**
	 * Returns formatted time stamp string in the format of "0013-Adir-05:056"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return formatted String
	 */
	public static String getDateTimeStamp(MarsClock time) {
		return new StringBuilder(getDateString(time)).append(COLON).append(getTruncatedTimeString(time)).toString();
	}
	
	/**
	 * Gets the current date string in the format of e.g. "0013-Adir-05"
	 *
	 * @return current date string
	 */
	public String getDateString() {
		StringBuilder result = new StringBuilder();

		// Append padding zeros to orbit
		if (orbit < 10) // then 000x
			result.append(THREE_ZEROS);
		else if (orbit < 100) // then 00xx
			result.append(TWO_ZEROS);
		else if (orbit < 1000) // then 0xxx
			result.append(ONE_ZERO);

		result.append(orbit).append(DASH).append(getMonthName()).append(DASH);

		if (sol < 10)
			result.append(ONE_ZERO);

		result.append(sol);

		return result.toString();
	}

	/**
	 * Gets the current orbit string in the format of e.g. "0013"
	 *
	 * @return current orbit string
	 */
	public String getOrbitString() {
		StringBuilder s = new StringBuilder();

		// Append padding zeros to orbit
		if (orbit < 10) // then 000x
			s.append(THREE_ZEROS);
		else if (orbit < 100) // then 00xx
			s.append(TWO_ZEROS);
		else if (orbit < 1000) // then 0xxx
			s.append(ONE_ZERO);

		s.append(orbit);

		return s.toString();
	}

	/**
	 * Gets the date string in the format of e.g. "0013-Adir-05"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return date string
	 */
	public static String getDateString(MarsClock time) {
		StringBuilder s = new StringBuilder();
		int orbit = time.getOrbit();
		int sol = time.getSolOfMonth();
		String month = time.getMonthName();

		// Append padding zeros to orbit
		if (orbit < 10) // then 000x
			s.append(THREE_ZEROS);
		else if (orbit < 100) // then 00xx
			s.append(TWO_ZEROS);
		else if (orbit < 1000) // then 0xxx
			s.append(ONE_ZERO);

		// Append orbit
		s.append(orbit).append(DASH).append(month).append(DASH);

		if (sol < 10) {
			s.append(ONE_ZERO);
		}
		s.append(sol);

		return s.toString();
	}

	/**
	 * Returns the time string in millisols without decimal in the format of e.g. "056"
	 * 
	 * @return millisols without decimal
	 */
	public String getTrucatedTimeStringUMST() {
		StringBuilder s = new StringBuilder();
		int tb = (int) millisol;
		s.append(tb);

		if (millisol < 10D)
			s.insert(0, TWO_ZEROS);
		else if (millisol < 100D)
			s.insert(0, ONE_ZERO);
//		
//		if (millisol < 10D)
//			s.insert(0, THREE_ZEROS);
//		else if (millisol < 100D)
//			s.insert(0, TWO_ZEROS);
//		else if (millisol < 1000D)
//			s.insert(0, ONE_ZERO);

		return s.append(UMST).toString();
	}

	/**
	 * Returns the time string in millisols with decimals in the format of e.g. "056.349"
	 * 
	 * @return millisols with decimals
	 */
	public String getDecimalTimeString() {
		StringBuilder b = new StringBuilder();
		double tb = Math.floor(millisol * 1000D) / 1000D;
		// String result = "" + tb;
		b.append(tb);
		if (millisol < 100) {
			b.insert(0, ONE_ZERO);
			// result = "0" + result;
		}
		if (millisol < 10) {
			b.insert(0, ONE_ZERO);
			// result = "0" + result;
		}
		if (millisol < 1) {
			b.insert(0, ONE_ZERO);
			// result = "0" + result;
		}
		while (b.length() < 7) {
			b.append(ONE_ZERO);
			// result += "0";
		}

		return b.toString();
	}

	/**
	 * Returns the time string in the format of e.g. "056"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return String in millisols
	 */
	public static String getTruncatedTimeString(MarsClock time) {
		StringBuilder b = new StringBuilder();
		int millisol = time.getMillisolInt();

		// String result = "" + tb;
		b.append(millisol);
		if (millisol < 100D) {
			b.insert(0, ONE_ZERO);
			// result = "0" + result;
		}
		if (millisol < 10D) {
			b.insert(0, ONE_ZERO);
			// result = "0" + result;
		}
//		if (millisol < 1) {
//			b.insert(0, ONE_ZERO);
			// result = "0" + result;
//		}
//		while (b.length() < 3) {
//			b.append(ONE_ZERO);
//			// result += "0";
//		}

		return b.toString();
	}

	/**
	 * Returns the total number of millisols of a given time.
	 *
	 * @param time {@link MarsClock} instance
	 * @return total millisols
	 */
	public static double getTotalMillisols(MarsClock time) {
		double result = 0D;

		// Add millisols up to current orbit
		for (int x = 1; x < time.orbit; x++) {
			if (isLeapOrbit(x))
				result += SOLS_PER_ORBIT_LEAPYEAR * 1000D;
			else
				result += SOLS_PER_ORBIT_NON_LEAPYEAR * 1000D;
		}

		// Add millisols up to current month
		for (int x = 1; x < time.month; x++)
			result += getSolsInMonth(x, time.orbit) * 1000D;

		// Add millisols up to current sol
		result += (time.sol - 1) * 1000D;

		// Add millisols in current sol
		result += time.millisol;

		// System.out.println("MarsClock : result : " + result);

		return result;
	}

	/**
	 * Returns the name of the current month.
	 *
	 * @return name of the current month
	 */
	public String getMonthName() {
		return MONTH_NAMES[month - 1];
	}

	/**
	 * Returns the orbit
	 *
	 * @return the orbit as an integer
	 */
	public int getOrbit() {
		return orbit;
	}

	/**
	 * Returns the month (1 - 24)
	 *
	 * @return the month as an integer
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Returns the sol of month (1 - 28)
	 *
	 * @return the sol of month as an integer
	 */
	public int getSolOfMonth() {
		return sol;
	}

	/**
	 * Returns the millisols
	 * 
	 * @return the millisol as a double
	 */
	public double getMillisol() {
		return millisol;
	}

	/**
	 * Returns the rounded millisols
	 * 
	 * @return the millisol as an int
	 */
	public int getMillisolInt() {
		return msolInt;
	}

	/**
	 * Returns the millisols with 1 decimal place
	 * 
	 * @return the millisols in 1 decimal place
	 */
	public double getMillisolOneDecimal() {
		return msol1;
	}

	/**
	 * Returns the week of the month (1-4)
	 *
	 * @return the week of the month as an integer
	 */
	public int getWeekOfMonth() {
		return ((sol - 1) / 7) + 1;
	}

	/**
	 * Returns the sol number of the week (1-7)
	 *
	 * @return the sol number of the week as an integer
	 */
	public int getSolOfWeek() {
		return sol - ((getWeekOfMonth() - 1) * 7);
	}

	/**
	 * Return the sol name of the week
	 *
	 * @return the sol name of the week as a String
	 */
	public String getSolOfWeekName() {
		return WEEK_SOL_NAMES[getSolOfWeek() - 1];
	}

	/**
	 * Returns the number of sols in the current week
	 *
	 * @return the number of osls in the current week as an integer
	 */
	public int getSolsInWeek() {
		int result = SOLS_PER_WEEK_LONG;

		if (getSolsInMonth(month, orbit) == SOLS_PER_MONTH_SHORT) {
			if (getWeekOfMonth() == 4)
				result = SOLS_PER_WEEK_SHORT;
		}
		return result;
	}

	/**
	 * Returns the current season for the given hemisphere (based on value of L_s)
	 * 
	 * @param hemisphere either NORTHERN_HEMISPHERE or SOUTHERN_HEMISPHERE
	 * @return season String
	 */
	public String getSeason(int hemisphere) {
		StringBuilder season = new StringBuilder();
		if (orbitInfo == null)
			orbitInfo = sim.getMars().getOrbitInfo();
		double L_s = orbitInfo.getL_s();
		// System.out.println(" L_s :" + L_s );

		// SUMMER_SOLSTICE = 168;
		// AUTUMN_EQUINOX = 346;
		// WINTER_SOLSTICE = 489;
		// SPRING_EQUINOX = 643; // or on the -25th sols

		// Spring lasts 193.30 sols
		// Summer lasts 178.64 sols
		// Autumn lasts 142.70 sols
		// Winter lasts 153.94 sols

		if (L_s < 90 || L_s == 360) {
			if (L_s < 30 || L_s == 360)
				season.append(EARLY);
			else if (L_s < 60)
				season.append(MID);
			else
				season.append(LATE);
			if (hemisphere == NORTHERN_HEMISPHERE)
				season.append(SPRING);
			else if (hemisphere == SOUTHERN_HEMISPHERE)
				season.append(AUTUMN);
		} else if (L_s < 180) {
			if (L_s < 120)
				season.append(EARLY);
			else if (L_s < 150)
				season.append(MID);
			else
				season.append(LATE);
			if (hemisphere == NORTHERN_HEMISPHERE)
				season.append(SUMMER);
			else if (hemisphere == SOUTHERN_HEMISPHERE)
				season.append(WINTER);
		} else if (L_s < 270) {
			if (L_s < 210)
				season.append(EARLY);
			else if (L_s < 240)
				season.append(MID);
			else
				season.append(LATE);
			if (hemisphere == NORTHERN_HEMISPHERE)
				season.append(AUTUMN);
			else if (hemisphere == SOUTHERN_HEMISPHERE)
				season.append(SPRING);
		} else if (L_s < 360) {
			if (L_s < 300)
				season.append(EARLY);
			else if (L_s < 330)
				season.append(MID);
			else
				season.append(LATE);
			if (hemisphere == NORTHERN_HEMISPHERE)
				season.append(WINTER);
			else if (hemisphere == SOUTHERN_HEMISPHERE)
				season.append(SUMMER);
		}

		return season.toString();
	}

	/**
	 * Creates a clone of this MarsClock object. Note: its time is set and won't
	 * increment.
	 *
	 * @return clone of this MarsClock object
	 */
	public Object clone() {
		return new MarsClock(orbit, month, sol, millisol, missionSol);
	}

//	/**
//	 * Gets a (random) time on the next day between t1 and t2 millisols. if t2 = 0,
//	 * millisols = t1.
//	 * 
//	 * @param clock {@link MarsClock} instance
//	 * @param t1    the lower millisols
//	 * @param t2    the upper millisols
//	 * @return MarsClock
//	 */
//	public MarsClock getRandomTimeNextSol(MarsClock clock, int t1, int t2) {
//		int millis = 0;
//		if (t2 == 0)
//			millis = t1;
//		else
//			millis = RandomUtil.getRandomInt(t1, t2);
//		int s = clock.getSolOfMonth() + 1;
//		int m = clock.getMonth();
//		int o = clock.getOrbit();
//		if (s == 29) {
//			s = 1;
//			m++;
//		}
//		if (m == 24) {
//			m = 1;
//			o++;
//		}
//		return new MarsClock(o, m, s, millis);
//	}

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
			else if (sol != otherClock.sol)
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
		return orbit * month * sol + ((int) (millisol * 1000D));
	}

	public void destroy() {
		orbitInfo = null;
		sim = null;
	}
}