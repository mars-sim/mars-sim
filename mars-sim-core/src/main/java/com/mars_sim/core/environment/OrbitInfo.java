/**
 * Mars Simulation Project
 * OrbitInfo.java
 * @date 2023-11-09
 * @author Scott Davis
 */
package com.mars_sim.core.environment;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.Coordinates;

/**
 * The OrbitInfo class keeps track of the orbital position of Mars.
 */
public class OrbitInfo implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(OrbitInfo.class.getName());
	
	// Static data members.
	// See https://nssdc.gsfc.nasa.gov/planetary/factsheet/marsfact.html
	/** Mars orbit semi-major axis in au. */
	public static final double SEMI_MAJOR_AXIS = 1.5236915D; // in AU

	/** Mars orbit period in seconds. */
	private static final double ORBIT_PERIOD = 59355072D;
	/** Mars orbit eccentricity. */
	public static final double ECCENTRICITY = .093377D;

	// INSTANTANEOUS_RADIUS_NUMERATOR = 1.510818924D
	// public static final double INSTANTANEOUS_RADIUS_NUMERATOR = SEMI_MAJOR_AXIS
	public static final double DEGREE_TO_RADIAN = Math.PI / 180D; // convert a number in degrees to radians

	public static final double RADIANS_TO_MILLISOLS = 1000 / (2 * Math.PI);

	/** Mars tilt in radians. */
	private static final double TILT = 0.4397D; // 25.1918 deg / 180 *pi = 0.4397
	/** Mars tilt in sine [unit-less] . */
	private static final double SINE_TILT = 0.42565D; // sin (25.1918 deg / 180 deg *pi) = 0.42565
	/** Mars solar day in seconds. */
	public static final double SOLAR_DAY = 88775.244D;
	/** The area of Mars' orbit in au squared. */
	private static final double ORBIT_AREA = 9.5340749D;
	/** Half of PI. */
	private static final double HALF_PI = Math.PI / 2D;
	/** Two PIs. */
	private static final double TWO_PI = Math.PI * 2D;
	// On earth, use 15; On Mars, use 14.6 instead.
//	private static final double ANGLE_TO_HOURS = 90D / HALF_PI  / 14.6D; // (or = 24 hrs / (2*pi) * 15 / 14.6)
//	private static final double HRS_TO_MILLISOLS = 1 / MarsTime.HOURS_PER_MILLISOL; //1.0275D * MarsTime.MILLISOLS_PER_DAY / 24D; 

	// Date of the 2000K start second
	private static final LocalDateTime Y2K = LocalDateTime.of(2000,1,1,0,0);
	
	// There's a different between civil and nautical dawn/dusk as the angle of the sun below the horizon 
	// for calculating the zenith angle at dawn/dusk.
	// See https://wordpress.mrreid.org/2013/02/05/dawn-dusk-sunrise-sunset-and-twilight/

	/** The nautical dawn occurs at 12° below the horizon, when it becomes possible to see the horizon properly and distinguish some objects.  */
	private static final double NAUTICAL_DAWN_ANGLE = 12D; // in degree\
	
	/** The zenith angle at nautical dawn. */
	private static final double ZENITH_ANGLE_AT_NAUTICAL_DAWN = (-90 - NAUTICAL_DAWN_ANGLE) * DEGREE_TO_RADIAN; // in radian
	/** The cosine of the nautical dawn zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_NAUTICAL_DAWN = Math.cos(ZENITH_ANGLE_AT_NAUTICAL_DAWN);
	/** The zenith angle at nautical dusk. */
	private static final double ZENITH_ANGLE_AT_NAUTICAL_DUSK = (90 + NAUTICAL_DAWN_ANGLE) * DEGREE_TO_RADIAN; // in radian
	/** The cosine of the nautical dusk zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_NAUTICAL_DUSK = Math.cos(ZENITH_ANGLE_AT_NAUTICAL_DUSK);

	/** The civil dawn occurs at 6° below the horizon, when it becomes possible to see the horizon properly and distinguish some objects.  */
	private static final double CIVIL_DAWN_ANGLE = 6D; // in degree
	
	/** The zenith angle at civil dawn. */
	private static final double ZENITH_ANGLE_AT_CIVIL_DAWN = (-90 - CIVIL_DAWN_ANGLE) * DEGREE_TO_RADIAN; // in radian
	/** The cosine of the civil dawn zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_CIVIL_DAWN = Math.cos(ZENITH_ANGLE_AT_CIVIL_DAWN);
	/** The zenith angle at civil dusk. */
	private static final double ZENITH_ANGLE_AT_CIVIL_DUSK = (90 + CIVIL_DAWN_ANGLE) * DEGREE_TO_RADIAN; // in radian
	/** The cosine of the civil dusk zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_CIVIL_DUSK = Math.cos(ZENITH_ANGLE_AT_CIVIL_DUSK);
	
	/** The early civil dusk occurs at 6° above the horizon, when it becomes blurry to see the horizon properly and distinguish some objects.  */
	private static final double EARLY_CIVIL_DUSK_ANGLE = 6D; // in degree
	
	/** The zenith angle at early civil dusk. */
	private static final double ZENITH_ANGLE_AT_EARLY_CIVIL_DUSK = (90 - EARLY_CIVIL_DUSK_ANGLE) * DEGREE_TO_RADIAN; // in radian
	/** The cosine of the early civil dusk zenith angle. */
	private static final double COSINE_ZENITH_ANGLE_AT_EARLY_CIVIL_DUSK = Math.cos(ZENITH_ANGLE_AT_EARLY_CIVIL_DUSK);

	/** The twilight angle [in radians] is set to the civil dawn angle. */
	private static final double TWILIGHT_RADIANS = CIVIL_DAWN_ANGLE * DEGREE_TO_RADIAN;
	
	// From https://www.teuse.net/games/mars/mars_dates.html
	//
	// Demios only takes 30hrs, and Phobos 7.6 hrs to rotate around mars
	// Spring lasts 193.30 sols
	// Summer lasts 178.64 sols
	// Autumn lasts 142.70 sols
	// Winter lasts 153.94 sols
	// Note that they don't add up exactly to 668.5921 sols.
	
	// We could derive 4 "holidays" for Mars. 
	// Note: round off the fractional sol.
	//
	// Spring Equinox at sol 1,
	// Summer Solstice at sol 193,
	// Autumnal equinox sol 372,
	// Winter solstice at sol 515,
	// Spring again sol 669 or 1 new annus or orbit.

	public static final int NORTHERN_HEMISPHERE = 1;
	public static final int SOUTHERN_HEMISPHERE = 2;
	private static final String EARLY = "Early ";
	private static final String MID = "Mid ";
	private static final String LATE = "Late ";
	private static final String SPRING = "Spring";
	private static final String SUMMER = "Summer";
	private static final String AUTUMN = "Autumn";
	private static final String WINTER = "Winter";

	// Data members
	/** The total time in the current orbit (in seconds). */
	private double orbitTime;
	/** The Equation of Center (EOC). */ 
	private double equationOfCenter;
	
	/** The angle of Mars's position to the Sun (in radians). */
//	private double theta;

	// To calculate the approximate distance between Earth and Mars, see
	// https://www.johndcook.com/blog/2015/10/24/distance-to-mars/

	/** The current distance from the Sun to Mars (in au). */
//	private double instantaneousSunMarsDistance;
	
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
	// e.g. At year 2000, L_s_perihelion = 251.2790
	// e.g. At year 2043, L_s_perihelion = 251

	/** The areocentric longitude at aphelion. */

	// Note 3 : L_s_perihelion indicates a near alignment of the planet's closest
	// approach to the Sun
	// in its orbit with its winter solstice season,
	// as related to the occasional onset of global dust storms within the advance
	// of this season.

	// Note 4 : As defined, L_s = 0°, 90°, 180°, and 270° indicate the Mars northern
	// hemisphere
	// vernal equinox, summer solstice, autumnal equinox, and winter solstice,
	// respectively.

	// Reference : http://www.giss.nasa.gov/tools/mars24/help/notes.html

	/**
	 * The current areocentric longitude of the Sun (or L_s, the orbital position of
	 * Mars).
	 */
	private double sunAreoLongitude;
	
	/** The partial sine solar declination angle. */
//	private double partialSineSolarDecAngle;
	/** The cache value of the cos zenith angle. */
	private double cosZenithAngleCache = 0;

	/**  The point on the surface of Mars perpendicular to the Sun as Mars rotates. */
	private Coordinates sunDirection;

	// static instances
	private MasterClock clock;
	
	private LocalDateTime earthTime;

	/** Constructs an {@link OrbitInfo} object */
	public OrbitInfo(MasterClock clock, SimulationConfig simulationConfig) {
		// Set orbit coordinates to start of orbit.
	
		orbitTime = 0D;
//		theta = 0D;
		this.clock = clock;

		earthTime = simulationConfig.getEarthStartDate();
		// Compute initial L_s based on the earth start date/time in simulation.xml		
		double L_s = computeSunAreoLongitude(earthTime);

		logger.config("Earth Start Time: " + earthTime);
		
		logger.config("Areocentric Longitude (L_s): " + Math.round(L_s * 1_000_000.0)/1_000_000.0 + " deg");
		
		double instantaneousSunMarsDistance = getHeliocentricDistance(earthTime);

		logger.config("Sun-to-Mars Distance: " + Math.round(instantaneousSunMarsDistance * 1_000_000.0)/1_000_000.0 + " km");
		
		sunDirection = new Coordinates(HALF_PI + TILT, Math.PI);
	}

	/**
	 * Adds time to the orbit.
	 * 
	 * @param millisols time added (millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// Convert millisols into seconds.
		double seconds = MarsTime.convertMillisolsToSeconds(pulse.getElapsed());

		// Determine orbit time
		orbitTime += seconds;
		while (orbitTime > ORBIT_PERIOD)
			orbitTime -= ORBIT_PERIOD;

		// Determine new theta
		double area = ORBIT_AREA * orbitTime / ORBIT_PERIOD;
		double areaTemp;

		if (area > (ORBIT_AREA / 2D))
			areaTemp = area - (ORBIT_AREA / 2D);
		else
			areaTemp = (ORBIT_AREA / 2D) - area;

		double theta = Math.abs(2D * Math.atan(1.097757562D * Math.tan(.329512059D * areaTemp)));

		if (area < (ORBIT_AREA / 2D))
			theta = 0D - theta;

		theta += Math.PI;

		if (theta >= TWO_PI)
			theta -= TWO_PI;

		// Determine Sun theta
		double sunTheta = sunDirection.getTheta();
		sunTheta -= 0.000070774 * seconds;

		while (sunTheta < 0D)
			sunTheta += TWO_PI;

		// Determine Sun phi
		double sunPhi = HALF_PI + (Math.sin(theta + HALF_PI) * TILT);

		sunDirection = new Coordinates(sunPhi, sunTheta);
		
		// Note: at L_s = 0, sunTheta is pi, sunPhi is (HALF_PI + TILT) 
		// Thus, sunDirection = new Coordinates(HALF_PI + TILT, Math.PI)
		
		// Determine new radius
		// May add back: double instantaneousSunMarsDistance = getHeliocentricDistance(earthTime)
				
		// Recompute the areocentric longitude of Mars
		sunAreoLongitude = computeSunAreoLongitude(pulse.getMasterClock().getEarthTime());

		// Recompute the Solar Declination Angle *ON DEMAND ONLY*
		// No need to call the method below in each frame
		// May add back: computeSineSolarDeclinationAngle();

		return true;
	}

//	/**
//	 * Is the sun rising (at dawn) at this location ?
//	 * 
//	 * @param location
//	 * @param extended true if extending the dawn further (doubling the dawn angle)
//	 * @return
//	 */
//	public boolean isSunRising(Coordinates location, boolean extended) {
//		boolean result = false;
//		
//		double cosZenith = getCosineSolarZenithAngle(location);	
//
//		// cosZenith is increasing and becomes larger than its previous value
//		if (cosZenithAngleCache < cosZenith) {
//
//			// See if the solar zenith angle is between 90 and (90 + the dawn angle) 
//			// Note: if the sun is below the horizon, the solar zenith angle should be negative
//			if (cosZenith <= 0 && cosZenith > COSINE_ZENITH_ANGLE_AT_DAWN) {
//				result = true;
//			}
//			
//			// See if the solar zenith angle is between 90 and (90 - the dawn angle) 
//			// Note: if the sun is above the horizon, the solar zenith angle should be positive
//			if (extended && cosZenith >= 0 && cosZenith < - COSINE_ZENITH_ANGLE_AT_DAWN) {
//				result = true;
//			}
//		}
//		
//		// Update the cache value
//		cosZenithAngleCache = cosZenith;
//		
//		return result;
//	}

	/**
	 * Is the sun setting (at dusk) at this location ? 
	 * 
	 * @param location
	 * @param extended true if extending the dusk earlier (starting the dusk angle earlier)
	 * @return
	 */
	public boolean isSunSetting(Coordinates location, boolean extended) {
		boolean result = false;
		
		double cosZenith = getCosineSolarZenithAngle(location);	
		
		// Note 0: cosZenith is decreasing and becomes smaller than its previous value
		if (cosZenithAngleCache > cosZenith) {

			// Note 1: cosine of solar zenith angle at 90 is zero
			// Note 2: cosine of solar zenith angle at >90 is -ve
			// Note 3: cosine of solar zenith angle at <90 is +ve
			
			
			// Check if the solar zenith angle is between 90 and (90 + the dusk angle)
			// Note: when the sun is below the horizon, the cosine of solar zenith angle becomes negative
			if (!extended && cosZenith <= 0 && cosZenith >= COSINE_ZENITH_ANGLE_AT_CIVIL_DUSK) {
				// Note 3: if the sun is below the horizon, the cosine of solar zenith angle should be negative
				result = true;
			}
			
			// See if the solar zenith angle is between (90 - the early dusk angle) and (90 + the dusk angle) 
			// Note: when the sun is above the horizon, the cosine of solar zenith angle is still positive
			else if (extended && 
				((cosZenith >= 0 && cosZenith >= COSINE_ZENITH_ANGLE_AT_EARLY_CIVIL_DUSK)
				  || (cosZenith <= 0 && cosZenith >= COSINE_ZENITH_ANGLE_AT_EARLY_CIVIL_DUSK))) {
				result = true;
			}
		}

		// Update the cache value
		cosZenithAngleCache = cosZenith;
					
		return result;
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

		double solarTime = clock.getMarsTime().getMillisol();
		// compute latitude in radians rather than in degree
		double lat = location.getPhi2LatRadian();

		// NOTE: figure out a more compact Equation of Time (EOT) using numerical model
		// of the Mars "Analemma".

		// Note: Mars has an EOT varying between -51.1min and +39.9min, since Mars has
		// more than five times (or 40%)
		// larger orbital eccentricity than the Earth's,

		// This results in a fifty minute variation in the timing of local noon (as
		// measured on a 24 "hour" Mars clock).

		// Mars Analemma has a pear shape or tear-drop shape. 
		// See paper at http://pubs.giss.nasa.gov/docs/1997/1997_Allison_1.pdf

		// See media below for the projection of the location of the sun on Mars
		// Opportunity Rover
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

		// Recompute L_s
		// Note: already done in timePassing()
		
		double L_s = getSunAreoLongitude();
		
		// Find equationTimeOffset (EOT) in degrees
		double equationTimeOffset = 2.861 * Math.sin(2 * L_s) - 0.071 * Math.sin(4 * L_s)
									+ 0.002 * Math.sin(6 * L_s) - equationOfCenter;
//
//		if (L_s == 57.7)
//			equationTimeOffset = 0;
//		else if (L_s <= 90)
//			equationTimeOffset = 0.7106 * L_s - 41D;
//		else if (L_s <= 180)
//			equationTimeOffset = 0.1803 * L_s + 6.7277;
//		else if (L_s <= 190)
//			equationTimeOffset = 39.1817;
//		else if (L_s == 258)
//			equationTimeOffset = 39.1817 * Math.cos(90D / 68D * (L_s - 190) * DEGREE_TO_RADIAN);
//		else if (L_s <= 326)
//			equationTimeOffset = -51D * Math.sin(90D / 69D * (L_s - 258) * DEGREE_TO_RADIAN);
//		else if (L_s <= 360)
//			equationTimeOffset = -41D - 10 * Math.sin(90D / 34D * (L_s - 326) * DEGREE_TO_RADIAN);

		double thetaOffset = location.getTheta() * 159.1519;

		double etoMillisol = equationTimeOffset * 0.6759;

		double modifiedSolarTime = thetaOffset + solarTime + etoMillisol;
		// The hour angle is measured from the true noon westward, represented by h = 2
		// * pi * t / P, t is time past noon in seconds
		double h = 0.0063 * Math.abs(modifiedSolarTime - 500D);
		
		// Recompute Solar Declination Angle in radians
		double dec = getSolarDeclinationAngleInRad();

		return Math.sin(lat) * Math.sin(dec) + Math.cos(lat) * Math.cos(dec) * Math.cos(h);
	}

	/**
	 * Determine areocentric solar longitude L_s, given the Earth's time.(AM2000, eq. 19)
	 *
	 * @return degree L_s
	 */
	private double computeSunAreoLongitude(LocalDateTime earthTime) {
		// Use the steps laid out by NASA GSFC's Mars24 Sunclock in
		// https://www.giss.nasa.gov/tools/mars24/help/algorithm.html
		
		// A. Days since J2000 Epoch
		//    Steps A-2 thru A-6 are abbreviated into the following single method for 
		//    finding the Time Offset from J2000 epoch (TT).
		double timeOffsetJ2000 = getDaysSinceJ2kEpoch(earthTime);

		// B. Mars Parmeters of Date
		//    Step B-1: find Mars Mean Anomaly
		double M = (19.3871 + 0.52402073 * timeOffsetJ2000) * DEGREE_TO_RADIAN;
		//    Step B-2: find Angle of Fiction Mean Sun
		double alphaFMS = 270.3871 + 0.524038496 * timeOffsetJ2000;
		//    Step B-3: find Perturbers
		double d = 360.0 / 365.25;
		double PBS = 
				  0.0071 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 2.2353) + 49.409))
				+ 0.0057 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 2.7543) + 168.173))
				+ 0.0039 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 1.1177) + 191.837))
				+ 0.0037 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 15.7866) + 21.736))
				+ 0.0021 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 2.1354) + 15.704))
				+ 0.0020 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 2.4694) + 95.528))
				+ 0.0018 * Math.cos(DEGREE_TO_RADIAN * ((d * timeOffsetJ2000 / 32.8493) + 49.095));
		//    Step B-4: find Equation of Center (EOC) 
		equationOfCenter = (10.691 + 3.0 * timeOffsetJ2000 / 1_000_000) * Math.sin(M) 
				+ 0.623 * Math.sin(2 * M)
				+ 0.050 * Math.sin(3 * M) 
				+ 0.005 * Math.sin(4 * M) 
				+ 0.0005 * Math.sin(5 * M) 
				+ PBS;
		//    Step B-5: find L_s
		double L_s = alphaFMS + equationOfCenter;
		
		L_s = L_s % 360;
		
		if (L_s < 0) {
			L_s = L_s + 360;
		} 
		else if (L_s >= 360)
			L_s = L_s - 360;
		
		sunAreoLongitude = L_s;
		return L_s;
	}

	/**
	 * Determines heliocentric distance [in A.U.]. (AM2000, eq. 25, corrected).
	 *
	 * @param clock Earth clock
	 *
	 * @return distance in A.U.
	 */
	private static double getHeliocentricDistance(LocalDateTime earthTime) {
		// Use NASA GSFC's Mars24 Sunclock's formula in step D-2 in
		// https://www.giss.nasa.gov/tools/mars24/help/algorithm.html
		
		double M = (19.3871 + 0.52402073 * getDaysSinceJ2kEpoch(earthTime)) * DEGREE_TO_RADIAN;
		return 1.52367934 * (1.00436 - 0.09309 * Math.cos(M) 
				- 0.004336 * Math.cos(2 * M) 
				- 0.00031 * Math.cos(3 * M)
				- 0.00003 * Math.cos(4 * M));
	}

	/**
	 * Gets the time offset since J2k epoch.
	 * 
	 * @param earthTime
	 * @return
	 */
	private static double getDaysSinceJ2kEpoch(LocalDateTime earthTime) {
		return Duration.between(Y2K, earthTime).getSeconds() / 86400D;
	}

	/**
	 * Gets the areocentric longitude [in degrees].
	 * 
	 * @return angle in degrees (0 - 360).
	 */
	public double getSunAreoLongitude() {
		return sunAreoLongitude;
	}

	/**
	 * Gets the solar declination angle (planetographic) of a given areocentric longitude L_s [in radians].
	 * 
	 * @return angle in radians (0 - 2 PI).
	 */
	private double getSolarDeclinationAngleInRad() {
		// Note that d, lsSine and SINE_TILT are unit-less
		double lsSine = Math.sin(sunAreoLongitude * DEGREE_TO_RADIAN);
		double d = SINE_TILT * lsSine;
		// WARNING: must call computeSineSolarDeclinationAngle() first to update partialSineSolarDecAngle
		return Math.asin(d) + 0.25 * DEGREE_TO_RADIAN * lsSine;
	}

	/**
	 * Gets the solar declination angle from a given areocentric longitude [in degrees].
	 * 
	 * @return angle in degree (0 - 360 deg).
	 */
	public double getSolarDeclinationAngleInDeg() {
		return getSolarDeclinationAngleInRad() / DEGREE_TO_RADIAN;
	}

	/**
	 * Gets the hour angle from a given location [in radians].
	 * Note: the hour angle has not been adjusted with longitude yet.
	 * 
	 * @Reference Solar radiation on Mars: Stationary Photovoltaic Array. 
	 * NASA Technical Memo. 1993.
	 * 
	 * @param location
	 * @return radians
	 */
	private double getHourAngle(Coordinates location) {

		double phi = location.getPhi();
		
		// For the geographical latitude geoLat (in radians)
		// Going Northward from equator is positive
		// Going Southward from equator is negative
		double geoLat = 0;
		
		if (phi <= HALF_PI) {
			geoLat = HALF_PI - phi; 
		}
		else {
			geoLat = - (phi - HALF_PI); 
		}
		
		double tanPhi = Math.tan(geoLat);
		
		// Recompute L_s
		// Note: already done in timePassing()
		
		// Get the solar dec angle in radians
		double dec = getSolarDeclinationAngleInRad();

		double tanSDA = Math.tan(dec);
		
		double angle = tanSDA * tanPhi;

		if (0 - angle > 1) {
			logger.info("At " + location + ", the sun will not rise. No daylight. Polar night.");
			return -10;
		}
		else if ((0 - angle) == 1 || (0 - angle) == -1) {
			logger.info("At " + location + ", the sun will be on the horizon for an instant only.");
		}
		else if (0 - angle < -1) {
			logger.info("At " + location + ", the sun will not set. Daylight all day. Polar day.");
			return 10;
		}
		
		// omega = arccos( cos(z-r) - sin(dec) * sin(phi)  
		//				 	/ (cos(dec) * cos(phi)) ) 
		// omega = arccos(0 - sin(dec) * sin(phi) / (cos(dec) * cos(phi)))
		
		// Since z is 90 and assume r (refraction angle) is zero
		// cos(z-r) is 0
		return - Math.acos(0 - angle);
	}

	/**
	 * Gets the sunrise and sunset time [in millisols].
	 * @See also https://www.omnicalculator.com/physics/sunrise-sunset
	 * 
	 * @param location
	 * @return
	 */
	public double[] getSunTimes(Coordinates location) {
		// Gets the omega value [in radians]
		double omega = getHourAngle(location);
		
		if (omega == -10) {
			//  the sun will not rise. No daylight. Polar night.
			logger.info("At " + location + ", the sun will not rise. No daylight. Polar night.");
			return new double[] {-1, -1, 0};
		}
		else if (omega == 1 || omega == -1) {
			logger.info("At " + location + ", the sun will be on the horizon for an instant only.");
		}
		else if (omega == 10) {
			// the sun will not set. Daylight all day. Polar day.
			logger.info("At " + location + ", the sun will not set. Daylight all day. Polar day.");
			return new double[] {-1, -1, 1000};
		}
	
		/** 
		 * OLD METHOD : Please do not delete. Will need it back
		 */
//		// The sunrise and sunset time will need to be adjusted according to the longitude
//		// of the location of interest.
//		double lon = location.getTheta();
//		
//		// Find the time for sunrise 
//		double sunriseHrs = 12 + (omega - lon) * ANGLE_TO_HOURS;
//		if (sunriseHrs < 0)
//			sunriseHrs = 24 + sunriseHrs;
//		
//		double sunriseMillisol = sunriseHrs * HRS_TO_MILLISOLS;
//		
//		// Find the time for sunset 
//		double sunsetHrs = 12 - (omega + lon) * ANGLE_TO_HOURS;
//		if (sunsetHrs > 24)
//			sunsetHrs = sunsetHrs - 24;
//
//		double sunsetMillisol = sunsetHrs * HRS_TO_MILLISOLS;
		
		// Find the millisol time for sunrise 
		double sunriseMillisol = getSunrise(location).getMillisol();
				
		if (sunriseMillisol < 0)
			sunriseMillisol = 1000 + sunriseMillisol;
		if (sunriseMillisol > 999)
			sunriseMillisol = sunriseMillisol - 1000;
		
		// Find the millisol time for sunset 
		double sunsetMillisol = getSunset(location).getMillisol();
		
		if (sunsetMillisol < 0)
			sunsetMillisol = 1000 + sunsetMillisol;
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
	 * Estimates when the sunrise is for this location.
	 * 
	 * @param location
	 * @return
	 */
	public MarsTime getSunrise(Coordinates location) {

		// Find the sun theta when it decrease over time
		double sunTheta = getSunDirection().getTheta();
		// Gets the longitude angle due to the specific location
		double lon = location.getTheta();
	
		// Rotate the globe 90 degree to a location closer to the sun to find the sunrise delta time
		double gapTheta = sunTheta - lon - HALF_PI + TWILIGHT_RADIANS;

		if (gapTheta < 0) {
			// Gone round the planet,
			gapTheta += (2 * Math.PI);
		}

		// Convert time from radian to millisols
		double timeToSunrise = gapTheta * RADIANS_TO_MILLISOLS;

		return clock.getMarsTime().addTime(timeToSunrise);
	}
	
	/**
	 * Estimates when the sunset is for this location.
	 * 
	 * @param location
	 * @return
	 */
	public MarsTime getSunset(Coordinates location) {
		Coordinates sunDirection = getSunDirection();
		// Find the sun theta when it decrease over time
		double sunTheta = sunDirection.getTheta();
		// Gets the longitude angle due to the specific location
		double lon = location.getTheta();
	
		// Rotate the globe 90 degree to a location further away from the sun to find the sunset delta time
		// Future: May add TWILIGHT_RADIANS below ;
		double gapTheta = sunTheta - lon + HALF_PI; 

		if (gapTheta < 0) {
			// Gone round the planet,
			gapTheta += (2 * Math.PI);
		}

		// Convert time from radian to millisols
		double timeToSunset = gapTheta * RADIANS_TO_MILLISOLS;

		return clock.getMarsTime().addTime(timeToSunset);
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
	
		// SUMMER_SOLSTICE is 168
		// AUTUMN_EQUINOX is 346
		// WINTER_SOLSTICE is 489
		// SPRING_EQUINOX is 643 // or on the -25th sols
	
		// Spring lasts 193.30 sols
		// Summer lasts 178.64 sols
		// Autumn lasts 142.70 sols
		// Winter lasts 153.94 sols
		int phaseId = (int)sunAreoLongitude/30; // Convert into 12 phases
		String phase = switch(phaseId % 3) {
			case 0 -> EARLY;
			case 1 -> MID;
			case 2 -> LATE;
			default -> "Unknown"; // Should never reach here
		};
		season.append(phase);
	
		if (sunAreoLongitude < 90 || sunAreoLongitude == 360) {
			season.append(hemisphere == NORTHERN_HEMISPHERE ?
									SPRING : AUTUMN);
		}
		else if (sunAreoLongitude < 180) {
			season.append(hemisphere == NORTHERN_HEMISPHERE ?
									SUMMER : WINTER);
		}
		else if (sunAreoLongitude < 270) {
			season.append(hemisphere == NORTHERN_HEMISPHERE ?
									AUTUMN : SPRING);
		}
		else {
			season.append(hemisphere == NORTHERN_HEMISPHERE ?
									WINTER : SUMMER);
		}
	
		return season.toString();
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		sunDirection = null;
		clock = null;
		earthTime = null;
	}
}
