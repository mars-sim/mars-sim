/**
 * Mars Simulation Project
 * ClockUtil.java
 * @version 3.1.0 2018-06-18
 * @author Manny Kung
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;

/**
 * The ClockUtils class stores methods for clock related computation
 */

public class ClockUtils implements Serializable {

	private static final long serialVersionUID = 65465354252L;

	private static Logger logger = Logger.getLogger(ClockUtils.class.getName());

	// The Mars tropical year is 686.9726 day or 668.5921 sol.
	public static final double SOLS_PER_ORBIT = 668.5921;
	public static final double DAYS_PER_ORBIT = 686.9726;
	
	// A Mars solar day has a mean period of 24 hours 39 minutes 35.244 seconds,
	public static final double SECONDS_PER_SOLAR_DAY_ON_MARS = 24*60*60 + 39*60 + 35.244;
	// customarily referred to as a "sol" in order to distinguish this from the
	// roughly 3% shorter solar day on Earth.
	// The Mars sidereal day, as measured with respect to the fixed stars, is
	// 24h 37m 22.663s, as compared with 23h 56m 04.0905s for Earth.
	public static final double SECONDS_PER_SIDEREAL_DAY_ON_MARS = 24*60*60 + 37*60 + 22.663;
	public static final double SECONDS_PER_SIDEREAL_DAY_ON_EARTH = 23*60*60 + 56*60 + 04.0905;	

	// Note: the summer solstice marks the longest day of the calendar year and
	// the beginning of summer in the "Northern Hemisphere".
	// The solar longitude Ls is the Mars-Sun angle, measured from the Northern
	// Hemisphere spring equinox where L_s=0.

	// see http://www-mars.lmd.jussieu.fr/mars/time/solar_longitude.html
	//private static final int SUMMER_SOLSTICE = 168; // (Ls = 90°)
	//private static final int AUTUMNAL_EQUINOX = 346; // (Ls = 180°)
	//private static final int WINTER_SOLSTICE = 489; // (Ls = 270°)
	//private static final int SPRING_EQUINOX = 643; // (Ls = 0°) or on the -25th sols

	// Martian/Gregorian calendar conversion
	public static final double SECONDS_PER_MILLISOL = 88.775244; 
	
	public static final double MILLISOLS_PER_DAY = 973.2469726;

	private Simulation sim = Simulation.instance();
	
	private OrbitInfo orbitInfo = sim.getMars().getOrbitInfo();

	private MarsClock marsClock = sim.getMasterClock().getMarsClock();

	private EarthClock earthClock = sim.getMasterClock().getEarthClock();

	private GregorianCalendar cal;
	
	private SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	// Set GMT timezone for calendar
	private SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");

	/**
	 * Constructor
	 */
//	public ClockUtils() {
//		sim = Simulation.instance();
//		
//		orbitInfo = sim.getMars().getOrbitInfo();
//		earthClock = sim.getMasterClock().getEarthClock();
//		marsClock = sim.getMasterClock().getMarsClock();
//	}

	public MarsClock convertTimeEarth2Mars() {
		MarsClock marsClock = null;
		//;
		//;
		return marsClock;
	}
	
	/** 
	 * Obtain the first landing date and time 
	 */
	public static void getFirstLandingDateTime() {
//		Date date = null;
//		ZonedDateTime zonedDateTime;
//		LocalDate localDate;
//		GregorianCalendar cal;
//		LocalDateTime ldt;
		
		// Assume starting at orbit 0, at exactly 14 orbits later
		double totalSeconds = 15 * SOLS_PER_ORBIT * SECONDS_PER_MILLISOL * 1000;
		
//		double earthDays = totalSeconds/SECONDS_PER_SIDEREAL_DAY_ON_EARTH;
//		double year = (int)(earthDays/364);
//		double r = earthDays % 364;
//		double month = (int)(r/12);
//		double rr = r % 12;
//		double day = (int)(r/30);	
		
		// convert 2043 Sep 30 0:0 (UTC) to millis
		long s_since_1970 = EarthClock.getDateOfFirstLanding().toEpochSecond();
		
		String output = convert(s_since_1970);
		
		logger.info("15-Adir-01 corresponds to " + output );
		
		long delta = s_since_1970 - (long) (totalSeconds);
		
		output = convert(delta);
		
		logger.info("00-Adir-01 corresponds to " + output ); 
		// 0001-Adir-01 corresponds to Wednesday, May 31, 2017 at 9:13:45 AM Coordinated Universal Time
		// 0000-Adir-01 corresponds to Tuesday, July 14, 2015 at 9:53:18 AM Coordinated Universal Time
		
	}
	
	/**
	 * Convert from the number of seconds since 1970 Jan 1
	 * @return string the date
	 */
	public static String convert(long s) {
		// Based on https://stackoverflow.com/questions/8262333/convert-epoch-seconds-to-date-and-time-format-in-java#
		Instant instant = Instant.ofEpochSecond (s);
		
		ZoneId zoneId = ZoneId.of ("Etc/UTC");
		ZonedDateTime zdt = instant.atZone ( zoneId );
		
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime ( FormatStyle.FULL );
		formatter = formatter.withLocale ( Locale.US );
		String output = zdt.format ( formatter );
		
		return output;
		
	}
	
	public static String convertMissionSol2Date(int missionSol) {

//		int orbit = 0;
//		int sol = missionSol;
//		int month = 0;

		MarsClock clock = new MarsClock(15, 1, 1, 0);
		
		clock.addTime(missionSol*1000);
		String s = MarsClock.getDateString(clock);
		
		return s;
	}
	
	public void destroy() {
		orbitInfo = null;
		sim = null;
		marsClock = null;
	}
}