/**
 * Mars Simulation Project
 * ClockUtil.java
 * @version 3.1.0 2018-06-18
 * @author Manny Kung
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.math.BigDecimal;
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

	public static final int SEC_PER_MIN = 60, SEC_PER_HR = 3600, SEC_PER_DAY = 86400, SEC_PER_YR = 31536000;

	private static final String	HOURS = "h ";
	private static final String	MINUTES = "m ";
	private static final String	ZERO_MINUTES = "00m ";
	private static final String	SECONDS = "s";

	private static Simulation sim = Simulation.instance();
	
	private static OrbitInfo orbitInfo;// = sim.getMars().getOrbitInfo();

	private static MarsClock marsClock;//= sim.getMasterClock().getMarsClock();

	private static EarthClock earthClock;// = sim.getMasterClock().getEarthClock();

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
		
		// Assume starting at orbit 0, at exactly 15 orbits later
		double cumulativeSeconds = 15 * SOLS_PER_ORBIT * SECONDS_PER_MILLISOL * 1000;
		
//		double earthDays = totalSeconds/SECONDS_PER_SIDEREAL_DAY_ON_EARTH;
//		double year = (int)(earthDays/364);
//		double r = earthDays % 364;
//		double month = (int)(r/12);
//		double rr = r % 12;
//		double day = (int)(r/30);	
		
		// convert 2043 Sep 30 00:00 (UTC) to millis
		long s_since_1970 = EarthClock.getDateOfFirstLanding().toEpochSecond();
		
		String output = convertSecs2MarsDate(s_since_1970);
		
		// For sanity check, calculate backward to validate if 0015-Adir-01 corresponds to September 30, 2043
		logger.info("0015-Adir-01 corresponds to " + output );
		// 0015-Adir-01 corresponds to Wednesday, September 30, 2043 at 12:00:00 AM Coordinated Universal Time	
	
		long delta = s_since_1970 - (long) (cumulativeSeconds);
		
		output = convertSecs2MarsDate(delta);
		
		logger.info("0000-Adir-01 corresponds to " + output ); 
		// 0001-Adir-01 corresponds to Wednesday, May 31, 2017 at 9:13:45 AM Coordinated Universal Time
		// 0000-Adir-01 corresponds to Tuesday, July 14, 2015 at 9:53:18 AM Coordinated Universal Time
		
//		earthClock = sim.getMasterClock().getEarthClock();
//
//		double jdut = getJulianDateUT(earthClock);
//		logger.info("jdut at 0015-Adir-01 is " + jdut);       
//		double M = getMarsMeanAnomaly(earthClock);
//		logger.info("M at 0015-Adir-01 is " + M);
//		double EOC = getEOC(earthClock);
//		logger.info("EOC at 0015-Adir-01 is " + EOC);	
//		double v = getTrueAnomaly(earthClock);
//		logger.info("v at 0015-Adir-01 is " + v);
//		double L_s = getLs(earthClock);
//		logger.info("L_s at 0015-Adir-01 is " + L_s);

	}
	
	/**
	 * Convert from the number of seconds since 1970 Jan 1
	 * @return String the Mars date in standard format yyyy-month-dd
	 */
	public static String convertSecs2MarsDate(long s) {
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
	
    /**
     * Returns a date time string in HHh MMm SS.SSs format
     * @param ratio The tiem ratio
     * @return a date time string
     */
    public static String getTimeString(double ratio) {

        //long years = (int) Math.floor(seconds / secsperyear);
        //long days = (int) ((seconds % secsperyear) / secspday);
        int hours = (int) ((ratio % SEC_PER_DAY) / SEC_PER_HR);
        int minutes = (int) ((ratio % SEC_PER_HR) / SEC_PER_MIN);
        double secs = (ratio % SEC_PER_MIN);

        StringBuilder b = new StringBuilder();
/*
        b.append(years);
        if(years>0){
            b.append("yr:");
        }

        if (days > 0) {
            b.append(String.format("%03d", days)).append("mon:");
        } else {
            b.append("0mon:");
        }
*/
        if (hours > 0) {
            b.append(String.format("%02d", hours)).append(HOURS);
        }
        //} else {
        //    b.append("00h ");
        //}

        if (minutes > 0) {
            b.append(String.format("%02d", minutes)).append(MINUTES);
        } else {
            b.append(ZERO_MINUTES);
        }

        //b.append(String.format("%5.3f", secs));
        b.append(String.format("%05.2f", secs) + SECONDS);

        return b.toString();
    }

    /**
     * Returns a truncated string in HHh MMm SSs format
     * @param ratio
     * @return a date time string
     */
    public static String getTimeTruncated(double ratio) {

        //long years = (int) Math.floor(seconds / secsperyear);
        //long days = (int) ((seconds % secsperyear) / secspday);
        int hours = (int) ((ratio % SEC_PER_DAY) / SEC_PER_HR);
        int minutes = (int) ((ratio % SEC_PER_HR) / SEC_PER_MIN);
        double secs = (ratio % SEC_PER_MIN);

        StringBuilder b = new StringBuilder();
/*
        b.append(years);
        if(years>0){
            b.append("yr:");
        }

        if (days > 0) {
            b.append(String.format("%03d", days)).append("mon:");
        } else {
            b.append("0mon:");
        }
*/
        if (hours > 0) {
            b.append(String.format("%02d", hours)).append(HOURS);
        }
        //} else {
        //    b.append("00h ");
        //}

        if (minutes > 0) {
            b.append(String.format("%02d", minutes)).append(MINUTES);
        } else {
            b.append(ZERO_MINUTES);
        }

        //b.append(String.format("%5.3f", secs));
        b.append(String.format("%02.0f", secs) + SECONDS);

        return b.toString();
    }
	
    

	/*
	 * Gets Julian Date (UT), the number of days (rather than milliseconds) since the unix epoch
	 * Note: the Julian Date UT at the Unix epoch is 2,440,587.5.
     * @return days
	*/
	public static double getJulianDateUT(EarthClock clock) {
		double result = 2440587.5 + (EarthClock.getMillis(clock) / 8.64E7);
		return result;
	}

	/*
	 * Determine time offset from J2000 epoch (UT). This step is optional; 
	 * we only need to make this calculation if the date is before Jan. 1, 1972. 
	 * Determine the elapsed time in Julian centuries since 12:00 on Jan. 1, 2000 (UT).
     * @return days
	*/
	public static double getT(EarthClock clock) {
		double result = (getJulianDateUT(clock) - 2451545.0)/36525.0;
		return result;
	}
	
	/*
	 * Determine UTC to TT conversion. (Replaces AM2000, eq. 27)
     * @return days
	 */
	public static double getDeltaUTC_TT(EarthClock clock) {
		double T = getT(clock);
		double T2 = T * T;
//		double T3 = getT(clock) * getT(clock) * getT(clock);		
//		double T4 = getT(clock) * getT(clock) * getT(clock) * getT(clock);	
//		long result = (long) (64.184 + 59L * T - 51.2 * T2 - 67.1 * T3 - 16.4 * T4);
		double result = 64.184 + 59L * T - 51.2 * T2 - 67.1 * T * T2 - 16.4 * T2 * T2;
		return result;
	}
	
	
	/*
	 * Gets Julian Date Terrestrial Time (TT), the number of days (rather than milliseconds) since the unix epoch
	 * Note: add the leap seconds which, since 1 July 2012
     * @return days
	*/
	public static double getJulianDateTT(EarthClock clock) {
		double result = getJulianDateUT(clock) + getDeltaUTC_TT(clock)/ 86400.0;// (35D + 32.184) / 86400D);
		return result;
	}

	/*
	 * Gets the days since J2000 Epoch, the number of (fractional) days since 12:00 on 1 January 2000 in Terrestrial Time (TT).
	 * Note: JD TT was 2,451,545.0 at the J2000 epoch
     * @return days
	*/
	public static double getDaysSinceJ2kEpoch(EarthClock clock) {
		double result = getJulianDateTT(clock) - 2451545.0;
		return result;
	}
    
	/*
	 * Gets Mars Mean Anomaly, a measure of where Mars is in its orbit, namely how far into the full orbit the body is
	 * since its last periapsis (the point in the ellipse closest to the focus).
     * @return degree
     */
	public static double getMarsMeanAnomaly(EarthClock clock) {
		// 0.00096 is a slight adjustment as the midnights by Mars24
		double result = 19.3871 + 0.52402073 *getDaysSinceJ2kEpoch(clock);
		return result;
	}

	/*
	 * Determine angle of Fiction Mean Sun. (AM2000, eq. 17)
     * @return degree AlphaFMS
     */
	public static double getAlphaFMS(EarthClock clock) {
		double result = 270.3871 + 0.524038496 * getDaysSinceJ2kEpoch(clock);
		return result;
	}

	/*
	 * Determine perturbers. (AM2000, eq. 18). 
	 * PBS = Σ(i=1,7) Ai cos [ (0.985626° ΔtJ2000 / τi) + φi]
	 * where 0.985626° = 360° / 365.25, and
     * @return deg PBS
     */
	public static double getPBS(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double d = 360 / 365.25 ;
		double PBS = 
			   0.0071 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.2353) +  49.409)) +
               0.0057 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.7543) + 168.173)) +
               0.0039 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  1.1177) + 191.837)) +
               0.0037 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 15.7866) +  21.736)) +
               0.0021 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.1354) +  15.704)) +
               0.0020 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.4694) +  95.528)) +
               0.0018 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 32.8493) +  49.095));
		return PBS;//OrbitInfo.DEGREE_TO_RADIAN;
	}
	
	/*
	 * Determine Equation of Center. (Bracketed term in AM2000, eqs. 19 and 20)
     * The equation of center is the true anomaly minus mean anomaly.
     * @return degree EOC
     */
	public static double getEOC(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double M = (19.3870 + 0.52402075 * j2000) * OrbitInfo.DEGREE_TO_RADIAN;
		double d = 360 / 365.25 ;
		double PBS = 
			   0.0071 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.2353) +  49.409)) +
               0.0057 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.7543) + 168.173)) +
               0.0039 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  1.1177) + 191.837)) +
               0.0037 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 15.7866) +  21.736)) +
               0.0021 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.1354) +  15.704)) +
               0.0020 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.4694) +  95.528)) +
               0.0018 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 32.8493) +  49.095));
		double EOC =  (10.691 + 3.0 * j2000 /1_000_000) 
				* Math.sin(M) + 0.623 * Math.sin(2*M) + 0.050* Math.sin(3*M) 
				+ 0.005* Math.sin(4*M) + 0.0005* Math.sin(5*M) + PBS;//OrbitInfo.DEGREE_TO_RADIAN;
		return EOC;
	}

	/*
	 *
    * @return
    */
	public static double getTrueAnomaly_Concise(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double Mdeg = getMarsMeanAnomaly(clock);
		double M = Mdeg * OrbitInfo.DEGREE_TO_RADIAN;
		double EOC = (10.691 + 3.0E-7 * j2000) * Math.sin(M) +
               0.623 * Math.sin(2 * M) +
               0.050 * Math.sin(3 * M) +
               0.005 * Math.sin(4 * M) +
               0.0005 * Math.sin(5 * M) +
               getPBS(clock);///OrbitInfo.DEGREE_TO_RADIAN;
		return EOC + Mdeg;
	}
	
	/*
	 * Determine the true anomaly. The equation of center is the true anomaly minus mean anomaly.
     * @return degree true anomaly
     */
	public static double getTrueAnomaly(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double Mdeg = 19.3870 + 0.52402075 * j2000;
		double M = (19.3870 + 0.52402075 * j2000)* OrbitInfo.DEGREE_TO_RADIAN;
		double d = 360 / 365.25 ;
		double PBS = 
			   0.0071 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.2353) +  49.409)) +
               0.0057 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.7543) + 168.173)) +
               0.0039 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  1.1177) + 191.837)) +
               0.0037 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 15.7866) +  21.736)) +
               0.0021 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.1354) +  15.704)) +
               0.0020 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.4694) +  95.528)) +
               0.0018 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 32.8493) +  49.095));
		double EOC =  (10.691 + 3.0E-7 * getDaysSinceJ2kEpoch(clock)) * Math.sin(M) 
				+ 0.623 * Math.sin(2*M) 
				+ 0.050* Math.sin(3*M) 
				+ 0.005* Math.sin(4*M) 
				+ 0.0005* Math.sin(5*M) + PBS;//OrbitInfo.DEGREE_TO_RADIAN;
		double v = EOC + Mdeg;
		return v;
	}
	
	/**
	 * Determine areocentric solar longitude. (AM2000, eq. 19)
	 * @return degree L_s
	 */
	public static double getLs(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double M = (19.3870 + 0.52402075 * j2000) * OrbitInfo.DEGREE_TO_RADIAN;
		double d = 360 / 365.25 ;
		double PBS = 
			   0.0071 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.2353) +  49.409)) +
               0.0057 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.7543) + 168.173)) +
               0.0039 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  1.1177) + 191.837)) +
               0.0037 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 15.7866) +  21.736)) +
               0.0021 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.1354) +  15.704)) +
               0.0020 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 /  2.4694) +  95.528)) +
               0.0018 * Math.cos(OrbitInfo.DEGREE_TO_RADIAN * ((d * j2000 / 32.8493) +  49.095));
		double EOC =  (10.691 + 3.0 * j2000 /1_000_000) 
				* Math.sin(M) + 0.623 * Math.sin(2*M) + 0.050* Math.sin(3*M) 
				+ 0.005* Math.sin(4*M) + 0.0005* Math.sin(5*M) + PBS;//OrbitInfo.DEGREE_TO_RADIAN;
		double alphaFMS = 270.3871 + 0.524038496 * j2000;	
		return alphaFMS + EOC;
	}

	/**
	 * Determine areocentric solar longitude. (AM2000, eq. 19)
	 * @return degree L_s
	 */
	public static double getLs_Concise(EarthClock clock) {
		return getAlphaFMS(clock) + getEOC(clock);
	}

	
	/*
	 * Determine Equation of Time. (AM2000, eq. 20)
     * @return in degrees 
     */
	public static double getEOT_Concise(EarthClock clock) {
		double Ls = getLs(clock) * OrbitInfo.DEGREE_TO_RADIAN;
		double EOT = 2.861 * Math.sin(2*Ls) 
				- 0.071 * Math.sin(4*Ls) 
				+ 0.002 * Math.sin(6*Ls)
				- getEOC(clock);
		return EOT;
	}

	/*
	 * Determine Equation of Time. (AM2000, eq. 20)
     * @return in hour
     */
	public static double getEOTHour_Concise(EarthClock clock) {
		double EOT = getEOT_Concise(clock) * 24/360;
		return EOT;
	}
	

	
	/*
	 * Gets Mars Sol Date (MSD), starting at midnight on 6th January 2000 (when time_J2000 = 4.5), at the Martian prime meridian,
     * Note: by convention, to keep the MSD positive going back to midday December 29th 1873, we add 44,796.
     * @return days
     */
	public static double getMarsSolDate(EarthClock clock) {
		// 0.00096 is a slight adjustment as the midnights by Mars24
		double result = ((getDaysSinceJ2kEpoch(clock) - 4.5) / 1.0274912517) + 44796.0 - 0.000926;
		return result;
	}

	/*
	 * Gets Coordinated Mars Time (MTC), a mean time for Mars, like Earth's UTC
     * Note: Calculated directly from the Mars Sol Date
     * @return hours
     */
	public static double getMTC(EarthClock clock) {
		// 0.00096 is a slight adjustment as the midnights by Mars24
		double result = (24 * getMarsSolDate(clock)) % 24;
		return result;
	}

//	/*
//	 *
//     * @return
//     */
//	public static double getE(EarthClock clock) {
//		double result = 0.09340 + 2.477E-9 * getDaysSinceJ2kEpoch(clock) ;
//		return result;
//	}
	
	public void destroy() {
		orbitInfo = null;
		sim = null;
		marsClock = null;
	}
}