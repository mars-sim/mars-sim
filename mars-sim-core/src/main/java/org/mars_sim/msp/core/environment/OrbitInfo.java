/**
 * Mars Simulation Project
 * OrbitInfo.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.environment;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;

/**
 * The OrbitInfo class keeps track of the orbital position of Mars
 */
public class OrbitInfo implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(OrbitInfo.class.getName());
	
	// Static data members.
	// Reference from http://nssdc.gsfc.nasa.gov/planetary/factsheet/marsfact.html
	/** Mars orbit semimajor axis in au. */
	public static final double SEMI_MAJOR_AXIS = 1.5236915D; // in AU

	/** Mars orbit period in seconds. */
	private static final double ORBIT_PERIOD = 59355072D;
	/** Mars orbit eccentricity. */
	public static final double ECCENTRICITY = .093377D;

	// INSTANTANEOUS_RADIUS_NUMERATOR = 1.510818924D
	// public static final double INSTANTANEOUS_RADIUS_NUMERATOR = SEMI_MAJOR_AXIS
	// *(1 - ECCENTRICITY * ECCENTRICITY);
	public static final double DEGREE_TO_RADIAN = Math.PI / 180D; // convert a number in degrees to radians

	/** Mars tilt in radians. */
	private static final double TILT = 0.4398; // 25.2 deg // 25.2 / 180 *pi = 0.4398
	/** Mars tilt in sine . */
	private static final double SINE_TILT = 0.4258; // sin (25.2 / 180 *pi) = 0.4258
	/** Mars solar day in seconds. */
	public static final double SOLAR_DAY = 88775.244D;
	/** The area of Mars' orbit in au squared. */
	private static final double ORBIT_AREA = 9.5340749D;
	/** Half of PI. */
	private static final double HALF_PI = Math.PI / 2;
	/** Two PIs. */
	private static final double TWO_PIs = Math.PI * 2;
	// On earth, use 15; On Mars, use 14.6 instead.
	private static final double ANGLE_TO_HOURS = 90 / HALF_PI  / 14.6;

	private static final double HRS_TO_MILLISOLS = ClockUtils.MILLISOLS_PER_DAY / 24; //  1.0275 / 24 * ClockUtils.MILLISOLS_PER_DAY;
	
	
	/** Astronomical Dawn is the point at which it becomes possible to detect light in the sky. */
	private static final double ASTRONOMICAL_DAWN_ANGLE = 18; // in degree
	/** Nautical Dawn occurs at 12° below the horizon, when it becomes possible to see the horizon properly and distinguish some objects.  */
	private static final double NAUTICAL_DAWN_ANGLE = 12; // in degree
	/** Civil Dawn occurs when the sun is 6° below the horizon and there is enough light for activities to take place without artificial lighting. */
	private static final double CIVIL_DAWN_ANGLE = 6; // in degree

	/** 
	 * Adopts the nautical dawn as the angle of the sun below the horizon for calculating the zenith angle at dawn.
	 * See http://wordpress.mrreid.org/2013/02/05/dawn-dusk-sunrise-sunset-and-twilight/
	 */
	private static final double ZENITH_ANGLE_AT_DAWN = (90 + NAUTICAL_DAWN_ANGLE) / DEGREE_TO_RADIAN; // in radian
	/** The cosine of the dawn zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_DAWN =  Math.cos(ZENITH_ANGLE_AT_DAWN);

	/** 
	 * Adopts the nautical dusk as the angle of the sun below the horizon for calculating the zenith angle at dawn.
	 * See http://wordpress.mrreid.org/2013/02/05/dawn-dusk-sunrise-sunset-and-twilight/
	 */
	private static final double ZENITH_ANGLE_AT_DUSK = (90 + NAUTICAL_DAWN_ANGLE) / DEGREE_TO_RADIAN; // in radian
	/** The cosine of the dusk zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_DUSK =  Math.cos(ZENITH_ANGLE_AT_DAWN);
	
	
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
	public static final int NORTHERN_HEMISPHERE = 1;
	public static final int SOUTHERN_HEMISPHERE = 2;
	private static final String EARLY = "Early ";
	private static final String MID = "Mid ";
	private static final String LATE = "Late ";
	private static final String SPRING = "Spring";
	private static final String SUMMER = "Summer";
	private static final String AUTUMN = "Autumn";
	private static final String WINTER = "Winter";
	
	private static final String R_IS = "r is ";
	
	/**
	 * The initial areocentric solar longitude (or the orbital position of Mars
	 * around the sun) at the start of the sim on 2043-Sep-30 00:00:00 Note: may
	 * obtain the most accurate value of L_AT_START numerically by running
	 * ClockUtils.getLs()
	 */
//	private static final double L_AT_START = 12.72010790886634; // 252.5849107170493;
	// Note : An areocentric orbit is an orbit around the planet Mars.

	/**
	 * The initial distance from the Sun to Mars (in au) on 2043-Sep-30 00:00:00
	 * Note: may obtain the most accurate value numerically by running
	 * ClockUtils.getHeliocentricDistance()
	 */
//	private static final double SUN_MARS_DIST_AT_START = 1.587624202793963;// 1.5876367428334057;//1.3817913894302327;
																			// //1.665732D; //

	// Data members
	/** The difference between the L_s and the true anomaly v in degree. */
	private double offsetL_s;// = 251.0001;
	/** The total time in the current orbit (in seconds). */
	private double orbitTime;
	/** The angle of Mars's position to the Sun (in radians). */
	private double theta;

	// To calculate the approximate distance between Earth and Mars, see
	// https://www.johndcook.com/blog/2015/10/24/distance-to-mars/

	/** The current distance from the Sun to Mars (in au). */
	private double instantaneousSunMarsDistance;
	/**
	 * The current areocentric longitude of the Sun (or the orbital position of
	 * Mars).
	 */
	private double L_s;

	// Note 1 : The apparent seasonal advance of the Sun at Mars is commonly
	// measured in terms of the areocentric
	// longitude L_s, as referred to the planet's vernal equinox (the ascending node
	// of the apparent seasonal
	// motion of the Sun on the planet's equator).

	// Note 2: Because of Mars's orbital eccentricity, L_s advances somewhat
	// unevenly with time,
	// but can be evaluated as a trigonometric power series for the orbital
	// eccentricity and
	// the orbital mean anomaly measured with respect to the perihelion.
	// The areocentric longitude at perihelion, L_s = 251 + 0.0064891 * (yr - 2000),

	/** The areocentric longitude at perihelion. */
//	private double L_s_perihelion;// = 251D + 0.0064891 * (2043- 2000); when L_s at ~250
	// e.g. At year 2000, L_s_perihelion = 251.2790
	// e.g. At year 2043, L_s_perihelion = 251

	/** The areocentric longitude at aphelion. */
//	private double L_s_aphelion;// = L_s_perihelion - 180D; when L_s at ~70

	// Note 3 : L_s_perihelion indicates a near alignment of the planet's closest
	// approach to the Sun
	// in its orbit with its winter solstice season,
	// as related to the occasional onset of global dust storms within the advance
	// of this season.

	// Note 4 : As defined, Ls = 0°, 90°, 180°, and 270° indicate the Mars northern
	// hemisphere
	// vernal equinox, summer solstice, autumnal equinox, and winter solstice,
	// respectively.

	// Reference : http://www.giss.nasa.gov/tools/mars24/help/notes.html

	/** The Sine of the solar declination angle. */
	private double sineSolarDeclinationAngle;
	/** The cache value of the cos zenith angle. */
	private double cosZenithAngleCache = 0;

	/** The solar zenith angle z */
	// private double solarZenithAngle;

	/**  The point on the surface of Mars perpendicular to the Sun as Mars rotates. */
	private Coordinates sunDirection;

	// static instances
	private MarsClock marsClock;

	/** Constructs an {@link OrbitInfo} object */
	public OrbitInfo(MarsClock clock) {
		// Set orbit coordinates to start of orbit.
	
		orbitTime = 0D;
		theta = 0D;
		marsClock = clock;

		// Compute the initial L_s and initial r based on the earth start date/time in
		// simulation.xml
		EarthClock c = new EarthClock(SimulationConfig.instance().getEarthStartDateTime());

		instantaneousSunMarsDistance = ClockUtils.getHeliocentricDistance(c);
		// instantaneousSunMarsDistance = SUN_MARS_DIST_AT_START;
		L_s = ClockUtils.getLs(c) % 360;
		// L_s = L_AT_START;

		sunDirection = new Coordinates(HALF_PI + TILT, Math.PI);

//		testOrbitData();

		offsetL_s = computePerihelion(2043);
	}

	public void testOrbitData() {

		// Scenario 1
		// Given :
		// (1) v = 21.74558 + 4.44193 = 26.1875 deg;
		// (2) Jan. 6, 2000 00:00:00 (UTC) on Earth

		// Calculate
		// (1) r
		// (2) Ls (should be Ls = 277.18758 in deg)

		double v = 26.1875 * DEGREE_TO_RADIAN;

		double r = getRadius(v);

//		double newL_s = v / DEGREE_TO_RADIAN + OFFSET_Ls_v;
//		if (newL_s > 360D)
//			newL_s = newL_s - 360D;
//		double Ls = newL_s;
		offsetL_s = computePerihelion(2000);
		double Ls = computeL_s(v);

// 		Back calculate the offset between v and Ls
//		x = 277.18758 - v;
//		x = 277.18758 - 26.1875	
		System.out.println("Scenario 1");
		System.out.println(R_IS + Math.round(r * 10000.0) / 10000.0);
		System.out.println("Ls is " + Math.round(Ls * 10000.0) / 10000.0);

		// Scenario 2
		// Given :
		// (1) v = 10.22959 + 66.0686 = 76.2982 deg;
		// (2) 3:46:31 UTC on Jan. 3 2004 on Earth

		// Calculate :
		// (1) r
		// (2) Ls (should be Ls = 327.32416 in deg)

		v = 76.2982 * DEGREE_TO_RADIAN;
		r = getRadius(v);
		offsetL_s = computePerihelion(2004);
		Ls = computeL_s(v);

// 		Back calculate the offset between v and Ls
//		x = 277.18758 - v;
//		x = 277.18758 - 26.1875	
		System.out.println("Scenario 2");
		System.out.println(R_IS + Math.round(r * 10000.0) / 10000.0);
		System.out.println("Ls is " + Math.round(Ls * 10000.0) / 10000.0);

		// Scenario 3
		// Given :
		// (1) instantaneousSunMarsDistance

		// Calculate on Sep 30 2043 00:00:00 (UTC) on Earth :
		// (1) v
		// (2) Ls

		offsetL_s = computePerihelion(2043);
		v = getTrueAnomaly(instantaneousSunMarsDistance);
		Ls = computeL_s(v);
		System.out.println("Scenario 3");
		System.out.println("v (deg) is " + Math.round(v / DEGREE_TO_RADIAN * 10000.0) / 10000.0);
		System.out.println("Ls is " + Math.round(Ls * 10000.0) / 10000.0);

		// Scenario 4
		// Given :
		// (1) L_s = 12.72008961663414
		// (2) v = 121.39623354876494

		// Calculate on Sep 30 2043 00:00:00 (UTC) on Earth :
		// (1) verify v
		// (2) r

		System.out.println("Scenario 4");
		L_s = 12.72008961663414;
		v = 121.39623354876494 * DEGREE_TO_RADIAN;

		r = getRadius(v);

		offsetL_s = computePerihelion(2043);
		v = (L_s - offsetL_s) + 360;

		System.out.println("v_old (deg) is " + Math.round(v * 10000.0) / 10000.0);
		System.out.println(R_IS + r);// Math.round(r * 10000.0)/10000.0);

	}

	/**
	 * Obtain the instantaneous distance (in A.U.) between Mars and Sun
	 * 
	 * @param v the instantaneous truly anomaly in radians
	 */
	public double getRadius(double v) {
		double e = ECCENTRICITY;
		double a = SEMI_MAJOR_AXIS;
		double r = a * (1 - e * e) / (1 + e * Math.cos(v));
		return r;
	}

	/**
	 * Adds time to the orbit.
	 * 
	 * @param millisols time added (millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// Convert millisols into seconds.
		double seconds = MarsClock.convertMillisolsToSeconds(pulse.getElapsed());

		// Determine orbit time
		orbitTime += seconds;
		while (orbitTime > ORBIT_PERIOD)
			orbitTime -= ORBIT_PERIOD;

		// Determine new theta
		double area = ORBIT_AREA * orbitTime / ORBIT_PERIOD;
		// 0.00000016063 = ORBIT_AREA / ORBIT_PERIOD;
		double areaTemp = 0D;

		if (area > (ORBIT_AREA / 2D))
			areaTemp = area - (ORBIT_AREA / 2D);
		else
			areaTemp = (ORBIT_AREA / 2D) - area;

		theta = Math.abs(2D * Math.atan(1.097757562D * Math.tan(.329512059D * areaTemp)));

		if (area < (ORBIT_AREA / 2D))
			theta = 0D - theta;

		theta += Math.PI;

		if (theta >= TWO_PIs)
			theta -= TWO_PIs;

		// Determine new radius
		instantaneousSunMarsDistance = 1.510818924D / (1 + (ECCENTRICITY * Math.cos(theta)));

//		offsetL_s = computePerihelion(earthClock.getYear());
//		double v = getTrueAnomaly(instantaneousSunMarsDistance);

		// Recompute the areocentric longitude of Mars
		L_s = computeL_s(pulse.getEarthTime());

		// Determine Sun theta
		double sunTheta = sunDirection.getTheta();
		sunTheta -= 0.000070774 * seconds;
		// 0.000070774 = 2D * Math.PI / SOLAR_DAY;

		while (sunTheta < 0D)
			sunTheta += TWO_PIs;

		// Determine Sun phi
		double sunPhi = HALF_PI + (Math.sin(theta + HALF_PI) * TILT);

		sunDirection = new Coordinates(sunPhi, sunTheta);
		computeSineSolarDeclinationAngle();

		return true;
	}


	public double computePerihelion(double yr) {
		return 251D + .0064891 * (yr - 2000);
	}

	/**
	 * Returns the theta angle of Mars's orbit. Angle is clockwise starting at
	 * aphelion.
	 * 
	 * @return the theta angle of Mars's orbit
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Gets the current distance to the Sun.
	 * 
	 * @return distance in Astronomical Units (A.U.)
	 */
	public double getDistanceToSun() {
		return instantaneousSunMarsDistance;
	}

	/**
	 * Gets the Sun's angle from a given phi (latitude).
	 * 
	 * @param phi location in radians (0 - PI).
	 * @return angle in radians (0 - PI).
	 */
	public double getSunAngleFromPhi(double phi) {
		// double a = Math.abs(phi - sunDirection.getPhi());
		return Math.abs(phi - sunDirection.getPhi());
	}

	/**
	 * Is the sun rising (at dawn) at this location ?
	 * 
	 * @param location
	 * @param extended true if extending the dawn further (doubling the dawn angle)
	 * @return
	 */
	public boolean isSunRising(Coordinates location, boolean extended) {
		double cosZenith = getCosineSolarZenithAngle(location);	
		cosZenithAngleCache = cosZenith;
		// cosZenith is increasing
		if (cosZenithAngleCache < cosZenith) {
			if (extended && cosZenith <= 0) {
				return true;
			}
			else if
			// See if the solar zenith angle is between 90 and (90 + the dawn angle) 
			// Note: if the sun is below the horizon, the solar zenith angle should be negative
			(cosZenith <= 0 && cosZenith > COSINE_ZENITH_ANGLE_AT_DAWN
			// See if the solar zenith angle is between 90 and (90 - the dawn angle) 
			// Note: if the sun is above the horizon, the solar zenith angle should be positive
			|| cosZenith >= 0 && cosZenith < - COSINE_ZENITH_ANGLE_AT_DAWN
			) {
			return true;
			}
		}
		return false;
	}

	/**
	 * Is the sun setting it (at dusk) at this location ? 
	 * 
	 * @param location
	 * @param extended true if extending the dusk earlier (starting the dusk angle earlier)
	 * @return
	 */
	public boolean isSunSetting(Coordinates location, boolean extended) {
		double cosZenith = getCosineSolarZenithAngle(location);	
		cosZenithAngleCache = cosZenith;
		// cosZenith is decreasing
		if (cosZenithAngleCache < cosZenith) {
			if (extended && cosZenith <= 0) {
				return true;
			}
			else if
			// See if the solar zenith angle is between 90 and (90 + the dusk angle) 
			// Note: if the sun is below the horizon, the solar zenith angle should be negative
			(cosZenith <= 0 && cosZenith > COSINE_ZENITH_ANGLE_AT_DUSK
			// See if the solar zenith angle is between 90 and (90 - the dusk angle) 
			// Note: if the sun is above the horizon, the solar zenith angle should be positive
			|| cosZenith >= 0 && cosZenith < - COSINE_ZENITH_ANGLE_AT_DUSK
			) {
			return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the solar zenith angle from a given coordinate.
	 * 
	 * @param location {@link Coordinates}
	 * @return angle in radians (0 - PI).
	 */
	public double getSolarZenithAngle(Coordinates location) {
		return Math.acos(getCosineSolarZenithAngle(location));
	}

	/**
	 * Gets the cosine solar zenith angle from a given coordinate.
	 * 
	 * @param location
	 * @return cosine of solar zenith angle (from -1 to 1).
	 */
	// Reference : https://en.wiki2.org/wiki/Solar_zenith_angle
	public double getCosineSolarZenithAngle(Coordinates location) {

		double solar_time = marsClock.getMillisol();
		// compute latitude in radians rather than in degree
		double lat = location.getPhi2LatRadian();

		// NOTE: figure out a more compact Equation of Time (EOT) using numerical model
		// of the Mars "Analemma".

		// Note: Mars has an EOT varying between -51.1min and +39.9min, since Mars has
		// more than five times (or 40%)
		// larger orbital eccentricity than the Earth's,

		// This results in a fifty minute variation in the timing of local noon (as
		// measured on a 24 "hour" Mars clock).

		// Mars' Analemma has a pear shape or tear-drop shape. For an explanation of
		// analema,
		// see paper at http://pubs.giss.nasa.gov/docs/1997/1997_Allison_1.pdf

		// See media below for the projection of the location of the sun on Mars
		// Oppoortunity Rover
		// 1. pic 1 at
		// https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Mars_Analemma_Time_Lapse_Opportunity.webm/220px--Mars_Analemma_Time_Lapse_Opportunity.webm.jpg
		// 2. pic 2 at
		// http://www.fromquarkstoquasars.com/wp-content/uploads/2014/05/Analemma-Rendering.jpg
		// 3. video at
		// https://upload.wikimedia.org/wikipedia/commons/1/14/Mars_Analemma_Time_Lapse_Opportunity.webm

		// REFERENCE:
		// 1. http://www.giss.nasa.gov/research/briefs/allison_02/
		// 2. https://en.wiki2.org/wiki/Equation_of_time
		// 3. https://en.wiki2.org/wiki/Analemma
		// 4. http://www.planetary.org/blogs/emily-lakdawalla/2014/a-martian-analemma.html

		computeSineSolarDeclinationAngle();
		double d = getCacheSolarDeclinationAngle();

		double equation_of_time_offset = 0;

		double Ls = getL_s();
		if (Ls == 57.7)
			equation_of_time_offset = 0;
		else if (Ls <= 90)
			equation_of_time_offset = 0.7106 * Ls - 41D; // slope = 41/57.7, b = -41
		else if (Ls <= 180)
			equation_of_time_offset = 0.1803 * Ls + 6.7277;
		else if (Ls <= 190)
			equation_of_time_offset = 39.1817;
		else if (Ls == 258)
			equation_of_time_offset = 39.1817 * Math.cos(90D / 68D * (Ls - 190) * DEGREE_TO_RADIAN);
		else if (Ls <= 326)
			equation_of_time_offset = -51D * Math.sin(90D / 69D * (Ls - 258) * DEGREE_TO_RADIAN);
		else if (Ls <= 360)
			equation_of_time_offset = -41D - 10 * Math.sin(90D / 34D * (Ls - 326) * DEGREE_TO_RADIAN);

		double theta_offset = location.getTheta() * 159.1519;
		// 159.1519 = 1000D / 2D / Math.PI ; // convert theta (longitude) from radians
		// to millisols;

		double EOT_in_millisol = equation_of_time_offset * 0.6759;
		// 0.6759 = 60D / SOLAR_DAY * 1000D; // convert from min to millisols

		double modified_solar_time = theta_offset + solar_time + EOT_in_millisol;
		// The hour angle is measured from the true noon westward, represented by h = 2
		// * pi * t / P, t is time past noon in seconds
		double h = 0.0063 * Math.abs(modified_solar_time - 500D);
		// 0.0063 = 2D * Math.PI / 1000D;

		return Math.sin(lat) * sineSolarDeclinationAngle + Math.cos(lat) * Math.cos(d) * Math.cos(h);

	}

	/**
	 * Returns the instantaneous true anomaly or polar angle of Mars around the sun.
	 * Angle is counter-clockwise starting at perigee.
	 * 
	 * @param r instantaneous radius of Mars
	 * @return radians the true anomaly
	 */
	public double getTrueAnomaly(double r) {
		double e = ECCENTRICITY;
		// r = a (1 - e * e) / ( 1 + e * cos (v) )
		double part1 = SEMI_MAJOR_AXIS * (1 - e * e) / r;
		// radius is in A.U. no need of * 149597871000D
		double part2 = (part1 - 1) / e;
		// double v = Math.acos(part2);
		// System.out.println("true anomally is " + v);
		return Math.acos(part2);
	}

	/**
	 * Returns the instantaneous true anomaly or polar angle of Mars around the sun.
	 * Angle is counter-clockwise starting at perigee.
	 * 
	 * @return radians the true anomaly
	 */
	public double getTrueAnomaly() {
		// r = a (1 - e * e) / ( 1 + e * cos (v) )
		double part1 = SEMI_MAJOR_AXIS * (1 - ECCENTRICITY * ECCENTRICITY) / instantaneousSunMarsDistance;
		// radius is in A.U. no need of * 149597871000D
		double part2 = (part1 - 1) / ECCENTRICITY;
		// double v = Math.acos(part2);
		// System.out.println("true anomally is " + v);
		return Math.acos(part2);
	}

	/**
	 * Computes the instantaneous areocentric longitude.
	 * 
	 * @param v the true anomaly in radians
	 */
	public double computeL_s(double v) {
		double newL_s = v / DEGREE_TO_RADIAN + offsetL_s; // why was the offset 248 in the past ?
		if (newL_s > 360D)
			newL_s = newL_s - 360D;
		return newL_s;
	}


	/**
	 * Computes the instantaneous areocentric longitude numerically using
	 * ClockUtil's methods.
	 */
	private double computeL_s(EarthClock c) {
		double ls = ClockUtils.getLs(c) % 360;
		L_s = ls;
		return ls;
	}

	/**
	 * Gets the instantaneous areocentric longitude.
	 * 
	 * @return angle in degrees (0 - 360).
	 */
	public double getL_s() {
		return L_s;
	}

	/**
	 * Gets the cached solar declination angle of a given areocentric longitude.
	 * 
	 * @return angle in radians (0 - 2 PI).
	 */
	public double getCacheSolarDeclinationAngle() {
		// WRNING: must call computeSineSolarDeclinationAngle() elsewhere to update the value	
		return Math.asin(sineSolarDeclinationAngle);
	}

	/**
	 * Gets the sine of the solar declination angle of a given areocentric
	 * longitude.
	 * 
	 * @return -1 to +1
	 */
	public double getCacheSineSolarDeclinationAngle() {
		return sineSolarDeclinationAngle;
	}

	/**
	 * Compute the sine of the solar declination angle of a given areocentric
	 * longitude.
	 */
	public void computeSineSolarDeclinationAngle() {
		sineSolarDeclinationAngle = - SINE_TILT * Math.sin(L_s * DEGREE_TO_RADIAN);
		// return sineSolarDeclinationAngle;
	}

	/**
	 * Gets the solar declination angle from a given areocentric longitude.
	 * 
	 * @return angle in radians (0 - 2 PI).
	 */
	public double getSolarDeclinationAngleDegree() {
		computeSineSolarDeclinationAngle();
		return getCacheSolarDeclinationAngle() / DEGREE_TO_RADIAN;
	}

	/**
	 * Gets the hour angle from a given location [in millisols]
	 * Reference : Solar radiation on Mars: Stataionary Photovoltaic Array. 
	 * NASA Technical Memo. 1993.
	 * 
	 * @param location.
	 * @return millisols.
	 */
	public double getHourAngle(Coordinates location) {
		computeSineSolarDeclinationAngle();
		double phi = location.getPhi();
		logger.info(location + " phi(latitude): " + phi);
		double geoLat = 0;

		if (phi == 0.0) {
			geoLat = HALF_PI; 
		}
		else if (phi < HALF_PI) {
			logger.info(location + " case 1.");
			geoLat = HALF_PI - phi; 
		}
		else if (phi == HALF_PI) {
			logger.info(location + " case 2.");
			geoLat = 0; 
		}
		else if (phi > HALF_PI) {
			logger.info(location + " case 3.");
			geoLat = - (phi - HALF_PI); 
		}
		
		logger.info(location + " geoLat: " + geoLat);
		double tanPhi = Math.tan(geoLat);
		
		double dec = getCacheSolarDeclinationAngle();
		logger.info(location + " dec: " + dec);

		logger.info(location + " tanPhi: " + tanPhi);
		double tanSDA = Math.tan(dec);
		logger.info(location + " tanSDA: " + tanSDA);
		
		if (- tanSDA * tanPhi > 1) {
			logger.info(location + " The sun will not rise. No daylight. Polar night.");
			return -10;
		}
		else if (tanSDA * tanPhi == 1 || tanSDA * tanPhi == -1) {
			logger.info(location + " The sun will be on the horizon for an instant only.");
		}
		else if (- tanSDA * tanPhi < -1) {
			logger.info(location + " The sun will not set. Daylight all day. Polar day.");
			return 10;
		}
		return - Math.acos(-tanPhi * tanSDA);
	}

	/**
	 * Gets the sunrise and sunset time [in millisols].
	 * See https://www.omnicalculator.com/physics/sunrise-sunset
	 * 
	 * @param location
	 * @return
	 */
	public double[] getSunriseSunsetTime(Coordinates location) {
		double omegaSunrise = getHourAngle(location);
		if (omegaSunrise == 10) {
			return new double[] {-1, -1, 1000};
		}
		if (omegaSunrise == -10) {
			return new double[] {-1, -1, 0};
		}
		logger.info(location.getCoordinateString() + " hour angle in radian: " + omegaSunrise);
		double sunriseMillisol = 0;
		double sunsetMillisol = 0;
		
		double sunriseHrs = 12 + omegaSunrise * ANGLE_TO_HOURS;
		logger.info(location.getCoordinateString() + " sunriseHrs: " + sunriseHrs);
		sunriseMillisol = sunriseHrs * HRS_TO_MILLISOLS;
		logger.info(location.getCoordinateString() + " sunriseMillisol: " + sunriseMillisol);
		if (sunriseMillisol < 0)
			sunriseMillisol = 1000 - sunriseMillisol;
		if (sunriseMillisol > 999)
			sunriseMillisol = sunriseMillisol - 1000;

		double sunsetHrs = 12 - omegaSunrise * ANGLE_TO_HOURS;
		logger.info(location.getCoordinateString() + " sunsetHrs: " + sunsetHrs);
		sunsetMillisol = sunsetHrs * HRS_TO_MILLISOLS;
		logger.info(location.getCoordinateString() + " sunsetMillisol: " + sunsetMillisol);
		if (sunsetMillisol < 0)
			sunsetMillisol = 1000 - sunsetMillisol;
		if (sunsetMillisol > 999)
			sunsetMillisol = sunsetMillisol - 1000;
		
		double duration = 0;
		if (sunsetMillisol > sunriseMillisol)
			duration = sunsetMillisol - sunriseMillisol;
		else {
			duration = 1000 - (sunriseMillisol - sunsetMillisol);
		}
		
		return new double[] {sunriseMillisol, sunsetMillisol, duration};
	}
	
	/**
	 * The point on the surface of Mars perpendicular to the Sun as Mars rotates.
	 * 
	 * @return the surface point on Mars perpendicular to the sun
	 */
	public Coordinates getSunDirection() {
		return sunDirection;
	}

	/**
	 * Returns the current season for the given hemisphere (based on value of L_s)
	 * 
	 * @param hemisphere either NORTHERN_HEMISPHERE or SOUTHERN_HEMISPHERE
	 * @return season String
	 */
	public String getSeason(int hemisphere) {
		StringBuilder season = new StringBuilder();

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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		sunDirection = null;
	}
}
