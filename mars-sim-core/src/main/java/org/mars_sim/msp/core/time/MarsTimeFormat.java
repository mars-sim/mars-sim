/*
 * Mars Simulation Project
 * MarsTimeFormat.java
 * @date 2021-12-03
 * @author Barry Evans
 */

package org.mars_sim.msp.core.time;

/**
 * Provides string formatting for MarsTime.
 */
public class MarsTimeFormat {

	// Formatting styles
	private static final String DASH = "-";
	private static final String COLON = ":";
	private static final String FULL_TIME_FORMAT = "%07.3f";
	private static final String TRUNCATED_TIME_FORMAT = "%03d";
	private static final String DATE_FORMAT = "%02d-%s-%02d";
	private static final String FULL_DATE_TIME_FORMAT = DATE_FORMAT + COLON + FULL_TIME_FORMAT;
	private static final String TRUNCATED_DATE_TIME_FORMAT = DATE_FORMAT + COLON + TRUNCATED_TIME_FORMAT;

	// Martian calendar static strings
	private static final String[] MONTH_NAMES = { "Adir", "Bora", "Coan", "Detri", "Edal", "Flo", "Geor", "Heliba",
			"Idanon", "Jowani", "Kireal", "Larno", "Medior", "Neturima", "Ozulikan", "Pasurabi", "Rudiakel", "Safundo",
			"Tiunor", "Ulasja", "Vadeun", "Wakumi", "Xetual", "Zungo" };

	private static final String[] WEEK_SOL_NAMES = 
//		{ "Solisol", "Phobosol", "Deimosol", "Terrasol", "Hermesol", "Venusol", "Jovisol" };	
		{ "Heliosol", "Neriosol", "Libersol", "Terrasol", "Venusol", "Mercusol", "Jovisol" };

	/**
	 * Prevents object creation.
	 */
	private MarsTimeFormat() {
		throw new UnsupportedOperationException("MarsTime should not be instiatated");
	}

	/**
	 * Creates a MarsTime from a data string.
	 *
	 * @param dateString format: "orbit-month-sol:millisol"
	 * @throws Exception if dateString is invalid.
	 */
	public static MarsTime fromDateString(String dateString) {

		String parts[] = dateString.split(DASH);
		int orbit = Integer.parseInt(parts[0]);
		if (orbit < 0)
			throw new IllegalArgumentException("Invalid orbit number: " + orbit);

		String monthStr = parts[1];
		int month = 0;
		for (int x = 0; x < MONTH_NAMES.length; x++) {
			if (monthStr.equals(MONTH_NAMES[x]))
				month = x + 1;
		}
		if ((month < 1) || (month > MONTH_NAMES.length))
			throw new IllegalStateException("Invalid month: " + monthStr);

		String[] subParts = parts[2].split(COLON);
		int sol = Integer.parseInt(subParts[0]);
		if (sol < 1)
			throw new IllegalStateException("Invalid sol number: " + sol);

		double millisol = Double.parseDouble(subParts[1]);
		if (millisol < 0D)
			throw new IllegalStateException("Invalid millisol number: " + millisol);

		int missionSol = 1; // the sol since the start of the sim

		return new MarsTime(orbit, month, sol, millisol, missionSol);
	}

	/**
	 * Returns the time string in the format of e.g. "056".
	 *
	 * @param time {@link MarsTime} instance
	 * @return String in millisols
	 */
	public static String getTruncatedTimeString(MarsTime time) {
		return String.format(TRUNCATED_TIME_FORMAT, time.getMillisolInt());
	}

	/**
	 * Returns the martian time string in the format of e.g. "00000056".
	 *
	 * @param time {@link MarsTime} instance
	 * @return String in millisols
	 */
	public static String getDecimalTimeString(MarsTime time) {
		return String.format(FULL_TIME_FORMAT, time.getMillisol());
	}

	/**
	 * Returns formatted martian time stamp string in the format of "03-Adir-05:056.434".
	 *
	 * @param time {@link MarsTime} instance
	 * @return formatted String
	 */
	public static String getDateTimeStamp(MarsTime time) {
		return String.format(FULL_DATE_TIME_FORMAT, time.getOrbit(), getMonthName(time.getMonth()),
										            time.getSolOfMonth(), time.getMillisol());
	}

	/**
	 * Returns a truncated martian time stamp string in the format of "03-Adir-05:056".
	 *
	 * @param time {@link MarsTime} instance
	 * @return formatted String
	 */
	public static String getTruncatedDateTimeStamp(MarsTime time) {
		return String.format(TRUNCATED_DATE_TIME_FORMAT, time.getOrbit(), getMonthName(time.getMonth()),
	            time.getSolOfMonth(), time.getMillisolInt());
	}

	/**
	 * Gets the martian date string in the format of e.g. "03-Adir-05".
	 *
	 * @param time {@link MarsTime} instance
	 * @return date string
	 */
	public static String getDateString(MarsTime time) {
		int orbit = time.getOrbit();
		int sol = time.getSolOfMonth();
		String month = getMonthName(time.getMonth());

		return String.format(DATE_FORMAT, orbit, month, sol);
	}


	/*
	 * Returns the sol name of the week.
	 *
	 * @return the sol name of the week as a String
	 */
	public static String getSolOfWeekName(MarsTime clock) {
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
		boolean result = (orbit % 10) == 0;

		// If an orbit is divisible by 10 it is a leap orbit

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
		int result = MarsTime.SOLS_PER_MONTH_LONG;

		// If month number is divisible by 6, month has 27 sols
		if ((month % 6) == 0)
			result = MarsTime.SOLS_PER_MONTH_SHORT;

		// If leap orbit and month number is 24, month has 28 sols
		if ((month == 24) && MarsTimeFormat.isLeapOrbit(orbit))
			result = MarsTime.SOLS_PER_MONTH_LONG;

		return result;
	}

	/**
	 * Gets the name of a month.
	 * 
	 * @param month
	 * @return
	 */
	public static String getMonthName(int month) {
		return MONTH_NAMES[month-1];
	}

	/**
	 * Gets an array of month names.
	 * 
	 * @return
	 */
	public static String[] getMonthNames() {
		return MONTH_NAMES;
	}

    public static String getDateStamp(MarsDate date) {
		return String.format(DATE_FORMAT, date.getOrbit(), getMonthName(date.getMonth()),
										            date.getSolOfMonth());
    }

}
