/**
 * Mars Simulation Project
 * OrbitInfo.java
 * @version 3.1.0 2018-08-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The OrbitInfo class keeps track of the orbital position of Mars
 */
public class OrbitInfo implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
	private static final double TILT = .4396D; // or 25.2 deg
	/** Mars solar day in seconds. */
	public static final double SOLAR_DAY = 88775.244D;
	/** The area of Mars' orbit in au squared. */
	private static final double ORBIT_AREA = 9.5340749D;
	/** Half of PI. */
	private static final double HALF_PI = Math.PI / 2D;
	/** Two PIs. */
	private static final double TWO_PIs = Math.PI * 2D;
	/**
	 * The initial areocentric solar longitude (or the orbital position of Mars
	 * around the sun) at the start of the sim on 2043-Sep-30 00:00:00 Note: may
	 * obtain the most accurate value of L_AT_START numerically by running
	 * ClockUtils.getLs()
	 */
	private static final double L_AT_START = 12.72010790886634; // 252.5849107170493;
	// Note : An areocentric orbit is an orbit around the planet Mars.

	/**
	 * The initial distance from the Sun to Mars (in au) on 2043-Sep-30 00:00:00
	 * Note: may obtain the most accurate value numerically by running
	 * ClockUtils.getHeliocentricDistance()
	 */
	private static final double SUN_MARS_DIST_AT_START = 1.587624202793963;// 1.5876367428334057;//1.3817913894302327;
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
	private double L_s_perihelion;// = 251D + 0.0064891 * (2043- 2000); when L_s at ~250
	// e.g. At year 2000, L_s_perihelion = 251.2790
	// e.g. At year 2043, L_s_perihelion = 251

	/** The areocentric longitude at aphelion. */
	private double L_s_aphelion;// = L_s_perihelion - 180D; when L_s at ~70

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
	/** The cache value of the cos zenith angele. */
	private double cos_zenith_cache = 0;

	/** The solar zenith angle z */
	// private double solarZenithAngle;

	/**
	 * The point on the surface of Mars perpendicular to the Sun as Mars rotates.
	 */
	private Coordinates sunDirection;

	private Simulation sim = Simulation.instance();
	private MarsClock marsClock;
	private EarthClock earthClock;// = sim.getMasterClock().getEarthClock();

	/** Constructs an {@link OrbitInfo} object */
	public OrbitInfo() {
		// Set orbit coordinates to start of orbit.
//		if (earthClock == null)
//			earthClock = sim.getMasterClock().getEarthClock();

		orbitTime = 0D;
		theta = 0D;

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
		System.out.println("r is " + Math.round(r * 10000.0) / 10000.0);
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
		System.out.println("r is " + Math.round(r * 10000.0) / 10000.0);
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
		System.out.println("r is " + r);// Math.round(r * 10000.0)/10000.0);

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
	public void addTime(double millisols) {
		// Convert millisols into seconds.
		double seconds = MarsClock.convertMillisolsToSeconds(millisols);

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

		if (earthClock == null)
			earthClock = sim.getMasterClock().getEarthClock();

//		offsetL_s = computePerihelion(earthClock.getYear());
//		double v = getTrueAnomaly(instantaneousSunMarsDistance);

		// Recompute the areocentric longitude of Mars
		L_s = computeL_s();

		// Determine Sun theta
		double sunTheta = sunDirection.getTheta();
		sunTheta -= 0.000070774 * seconds;
		// 0.000070774 = 2D * Math.PI / SOLAR_DAY;

		while (sunTheta < 0D)
			sunTheta += TWO_PIs;
		sunDirection.setTheta(sunTheta);

		// Determine Sun phi
		double sunPhi = HALF_PI + (Math.sin(theta + HALF_PI) * TILT);

		sunDirection.setPhi(sunPhi);

		computeSineSolarDeclinationAngle();

	}

	public double computePerihelion() {
		if (earthClock == null)
			earthClock = sim.getMasterClock().getEarthClock();

		L_s_perihelion = 251D + .0064891 * (earthClock.getYear() - 2000);

		return L_s_perihelion;
	}

	public double computePerihelion(double yr) {
		return 251D + .0064891 * (yr - 2000);
	}

	public double computeAphelion() {
		L_s_aphelion = computePerihelion() - 180D;
		return L_s_perihelion;
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

//	/**
//	 * Returns the radius of Mars's orbit in A.U.
//	 * 
//	 * @return the radius of Mars's orbit
//	 */
//	 public double getRadius() {
//	 return instantaneousSunMarsDistance;
//	 }

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
		// System.out.println("getSunAngleFromPhi() : " + a);
		return Math.abs(phi - sunDirection.getPhi());
		// return a;
	}

	public boolean isSunRising(Coordinates location) {
		boolean result = true;
		double cos_zenith = getCosineSolarZenithAngle(location);
		if (cos_zenith_cache > cos_zenith)
			result = false;
		cos_zenith_cache = cos_zenith;
		return result;
	}

	/**
	 * Gets the solar zenith angle from a given coordinate
	 * 
	 * @param location {@link Coordinates}
	 * @return angle in radians (0 - PI).
	 */
	public double getSolarZenithAngle(Coordinates location) {
		return Math.acos(getCosineSolarZenithAngle(location));
	}

	/**
	 * Gets the cosine solar zenith angle from a given coordinate
	 * 
	 * @param location
	 * @return cosine of solar zenith angle (from -1 to 1).
	 */
	// Reference : https://en.wiki2.org/wiki/Solar_zenith_angle
	public double getCosineSolarZenithAngle(Coordinates location) {

		if (marsClock == null)
			marsClock = sim.getMasterClock().getMarsClock();

		double solar_time = marsClock.getMillisol();

		// compute latitude in radians rather than in degree
		double lat = location.getPhi2LatRadian();

		// TODO: figure out a more compact Equation of Time (EOT) using numerical model
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
		// 4.
		// http://www.planetary.org/blogs/emily-lakdawalla/2014/a-martian-analemma.html?referrer=https://www.google.com/

		computeSineSolarDeclinationAngle();
		double d = getSolarDeclinationAngle();

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
	 * Computes the instantaneous areocentric longitude
	 * 
	 * @param v the true anomaly in radians
	 */
	public double computeL_s(double v) {
		double newL_s = v / DEGREE_TO_RADIAN + offsetL_s; // why was the offset 248 in the past ?
		if (newL_s > 360D)
			newL_s = newL_s - 360D;
		return newL_s;
	}

//	/**
//	 * Computes the instantaneous areocentric longitude
//	 */
//	public void computeL_s() {
//		double v = getTrueAnomaly();
//		double newL_s = v / DEGREE_TO_RADIAN + offsetL_s; // why was it 248 before ?
//		if (newL_s > 360D)
//			newL_s = newL_s - 360D;
//		//if (newL_s != L_s) {
//			L_s = newL_s;
//		//}
//	}

	/**
	 * Computes the instantaneous areocentric longitude numerically using
	 * ClockUtil's methods.
	 */
	public double computeL_s() {
		double ls = ClockUtils.getLs(earthClock) % 360;
		L_s = ls;
		return ls;
	}

	/**
	 * Gets the instantaneous areocentric longitude
	 * 
	 * @return angle in degrees (0 - 360).
	 */
	public double getL_s() {
		return L_s;
	}

	/**
	 * Gets the solar declination angle of a given areocentric longitude.
	 * 
	 * @return angle in radians (0 - 2 PI).
	 */
	public double getSolarDeclinationAngle() {
		return Math.asin(sineSolarDeclinationAngle);
	}

	/**
	 * Gets the sine of the solar declination angle of a given areocentric
	 * longitude.
	 * 
	 * @return -1 to +1
	 */
	public double getSineSolarDeclinationAngle() {
		return sineSolarDeclinationAngle;
	}

	/**
	 * Compute the sine of the solar declination angle of a given areocentric
	 * longitude.
	 */
	public void computeSineSolarDeclinationAngle() {
		sineSolarDeclinationAngle = Math.sin(TILT) * Math.sin(L_s * DEGREE_TO_RADIAN);
		// return sineSolarDeclinationAngle;
	}

	/**
	 * Gets the solar declination angle from a given areocentric longitude.
	 * 
	 * @return angle in radians (0 - 2 PI).
	 */
	public double getSolarDeclinationAngleDegree() {
		return getSolarDeclinationAngle() / DEGREE_TO_RADIAN;
	}

	/**
	 * Gets the mars daylight hour angle in millisols from a given location's
	 * latitude.
	 * 
	 * @param location.
	 * @return millisols.
	 */
	public double getDaylightinMillisols(Coordinates location) {
		// double delta = getSolarDeclinationAngle(getL_s());
		// double lat = location.getPhi2Lat(location.getPhi());
		return 318.3152 * Math.acos(-Math.tan(location.getPhi2LatRadian()) * Math.tan(getSolarDeclinationAngle()));
		// Note: 318.3152 = 1000 millisols / 24 hours / DEGREE_TO_RADIAN * 2 / 360
		// degrees * 24 hours
		// TODO: should 24 hours be used ?
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		sunDirection = null;
		marsClock = null;
		sim = null;
		earthClock = null;
	}
}