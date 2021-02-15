package org.mars_sim.msp.core.time;

/**
 * Provides string formatting for MarsClock.
 */
public class MarsClockFormat {
	
	// Formatting styles
	private static final String DASH = "-";
	private static final char COLON = ':';
	private static final String FULL_TIME_FORMAT = "%07.3f";
	private static final String TRUNCATED_TIME_FORMAT = "%03d";
	private static final String DATE_FORMAT = "%02d-%s-%02d";
	private static final String FULL_DATE_TIME_FORMAT = DATE_FORMAT + COLON + FULL_TIME_FORMAT;
	private static final String TRUNCATED_DATE_TIME_FORMAT = DATE_FORMAT + COLON + TRUNCATED_TIME_FORMAT;

	// Martian calendar static strings
	private static final String[] MONTH_NAMES = { "Adir", "Bora", "Coan", "Detri", "Edal", "Flo", "Geor", "Heliba",
			"Idanon", "Jowani", "Kireal", "Larno", "Medior", "Neturima", "Ozulikan", "Pasurabi", "Rudiakel", "Safundo",
			"Tiunor", "Ulasja", "Vadeun", "Wakumi", "Xetual", "Zungo" };
	
	private static final String[] WEEK_SOL_NAMES = { "Solisol", "Phobosol", "Deimosol", "Terrasol", "Hermesol",
	"Venusol", "Jovisol" };



	/**
	 * Create a MarsClock from a data string
	 *
	 * @param dateString format: "orbit-month-sol millisol"
	 * @throws Exception if dateString is invalid.
	 */
	public static MarsClock fromDateString(String dateString) {

		// Set initial date to dateString. ex: "00-Adir-01 000.000"
		String orbitStr = dateString.substring(0, dateString.indexOf(DASH));
		int orbit = Integer.parseInt(orbitStr);
		if (orbit < 0)
			throw new IllegalArgumentException("Invalid orbit number: " + orbit);

		String monthStr = dateString.substring(dateString.indexOf(DASH) + 1, dateString.lastIndexOf(DASH));
		int month = 0;
		for (int x = 0; x < MONTH_NAMES.length; x++) {
			if (monthStr.equals(MONTH_NAMES[x]))
				month = x + 1;
		}
		if ((month < 1) || (month > MONTH_NAMES.length))
			throw new IllegalStateException("Invalid month: " + monthStr);

		String solStr = dateString.substring(dateString.lastIndexOf(DASH) + 1, dateString.indexOf(COLON));
		int sol = Integer.parseInt(solStr);
		if (sol < 1)
			throw new IllegalStateException("Invalid sol number: " + sol);

		String millisolStr = dateString.substring(dateString.indexOf(COLON) + 1);
		double millisol = Double.parseDouble(millisolStr);
		if (millisol < 0D)
			throw new IllegalStateException("Invalid millisol number: " + millisol);

		int missionSol = 1; // the sol since the start of the sim
		
		return new MarsClock(orbit, month, sol, millisol, missionSol);
	}
	
	/**
	 * Returns the time string in the format of e.g. "056"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return String in millisols
	 */
	public static String getTruncatedTimeString(MarsClock time) {
		return String.format(TRUNCATED_TIME_FORMAT, time.getMillisolInt());
	}

	/**
	 * Returns the time string in the format of e.g. "00000056"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return String in millisols
	 */
	public static String getDecimalTimeString(MarsClock time) {
		return String.format(FULL_TIME_FORMAT, time.getMillisol());
	}

	/**
	 * Returns formatted time stamp string in the format of "03-Adir-05:056.434"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return formatted String
	 */
	public static String getDateTimeStamp(MarsClock time) {
		return String.format(FULL_DATE_TIME_FORMAT, time.getOrbit(), getMonthName(time.getMonth()),
										            time.getSolOfMonth(), time.getMillisol());
	}

	/**
	 * Returns a truncated time stamp string in the format of "03-Adir-05:056"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return formatted String
	 */
	public static String getTruncatedDateTimeStamp(MarsClock time) {
		return String.format(TRUNCATED_DATE_TIME_FORMAT, time.getOrbit(), getMonthName(time.getMonth()),
	            time.getSolOfMonth(), time.getMillisolInt());
	}

	/**
	 * Gets the date string in the format of e.g. "03-Adir-05"
	 * 
	 * @param time {@link MarsClock} instance
	 * @return date string
	 */
	public static String getDateString(MarsClock time) {
		int orbit = time.getOrbit();
		int sol = time.getSolOfMonth();
		String month = getMonthName(time.getMonth());
		
		return String.format(DATE_FORMAT, orbit, month, sol);
	}


	/*
	 * Return the sol name of the week
	 *
	 * @return the sol name of the week as a String
	 */
	public static String getSolOfWeekName(MarsClock clock) {
		int sol = clock.getSolOfMonth();
		int weekOfMonth = ((sol - 1) / 7) ;
		int solOfWeek = sol - (weekOfMonth * 7);
		return WEEK_SOL_NAMES[solOfWeek - 1];
	}

	/**
	 * Returns true if orbit is a leap orbit, false if not.
	 *
	 * @param orbit the orbit number
	 */
	static boolean isLeapOrbit(int orbit) {
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
	 * Returns the number of sols for a given month and orbit.
	 *
	 * @param month the month number
	 * @param orbit the orbit number
	 */
	public static int getSolsInMonth(int month, int orbit) {
	
		// Standard month has 28 sols.
		int result = MarsClock.SOLS_PER_MONTH_LONG;
	
		// If month number is divisible by 6, month has 27 sols
		if ((month % 6) == 0)
			result = MarsClock.SOLS_PER_MONTH_SHORT;
	
		// If leap orbit and month number is 24, month has 28 sols
		if ((month == 24) && MarsClockFormat.isLeapOrbit(orbit))
			result = MarsClock.SOLS_PER_MONTH_LONG;
	
		return result;
	}

	/**
	 * Get the name of a month"
	 * @param month
	 * @return
	 */
	public static String getMonthName(int month) {
		return MONTH_NAMES[month-1];
	}

	public static String[] getMonthNames() {
		return MONTH_NAMES;
	}

}
