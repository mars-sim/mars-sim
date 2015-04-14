/**
 * Mars Simulation Project
 * OrbitInfo.java
 * @version 3.08 2014-04-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The OrbitInfo class keeps track of the orbital position of Mars
 */
public class OrbitInfo
implements Serializable {

	/** default serial id.*/
	private static final long serialVersionUID = 1L;
	// /** Mars orbit semimajor axis in au. */
	public static final double SEMI_MAJOR_AXIS = 1.5236915D; // in AU
	// Static data members
	/** Mars orbit period in seconds. */
	private static final double ORBIT_PERIOD = 59355072D;
	/** Mars orbit eccentricity. */
	public static final double ECCENTRICITY = .0934D;

	/** Mars tilt in radians. */
	private static final double TILT = .4396D; // or 25.2 deg
	/** Mars solar day in seconds. */
	private static final double SOLAR_DAY = 88775.244D;
	/** The area of Mars' orbit in au squared. */
	private static final double ORBIT_AREA = 9.5340749D;

	// Data members
	/** The total time in the current orbit (in seconds). */
	private double orbitTime;
	/** The angle of Mars's position to the Sun (in radians). */
	private double theta;
	/** The distance from the Sun to Mars (in au). */
	private double radius;

	/** The areocentric longitude (or the orbital position of Mars).
	 *  0 corresponding to Mars' Northern Spring Equinox.
	 */
	private double L_s;
	// Note 1 : The apparent seasonal advance of the Sun at Mars is commonly measured in terms of the areocentric longitude L_s,
	//as referred to the planet's vernal equinox (the ascending node of the apparent seasonal motion of the Sun on the planet's equator).
	//
	// Note 2: Because of Mars's orbital eccentricity, L_s advances somewhat unevenly with time, but can be evaluated
	// as a trigonometric power series for the orbital eccentricity and the orbital mean anomaly measured with respect to the perihelion.
	// The areocentric longitude at perihelion, L_s = 251.000 + 0.00645 * (yr - 2000),
	// indicates a near alignment of the planet's closest approach to the Sun in its orbit with its winter solstice season,
	// as related to the occasional onset of global dust storms within the advance of this season.
	// see http://www.giss.nasa.gov/tools/mars24/help/notes.html


	/** The solar zenith angle z */
	private double z;

	/** The point on the surface of Mars perpendicular to the Sun as Mars rotates. */
	private Coordinates sunDirection;

	private MarsClock marsClock;

	/** Constructs an {@link OrbitInfo} object */
	public OrbitInfo() {
		// Initialize data members
		// Set orbit coordinates to start of orbit.
		orbitTime = 0D;
		theta = 0D;
		radius = 1.665732D;
		sunDirection = new Coordinates((Math.PI / 2D) + TILT, Math.PI);

	}

	/**
	 * Adds time to the orbit.
	 * @param millisols time added (millisols)
	 */
	public void addTime(double millisols) {

		// Convert millisols into seconds.
		double seconds = MarsClock.convertMillisolsToSeconds(millisols);

		// Determine orbit time
		orbitTime += seconds;
		while (orbitTime > ORBIT_PERIOD) orbitTime -= ORBIT_PERIOD;

		// Determine new theta
		double area = ORBIT_AREA * orbitTime / ORBIT_PERIOD;
		double areaTemp = 0D;
		if (area > (ORBIT_AREA / 2D)) areaTemp = area - (ORBIT_AREA / 2D);
		else areaTemp = (ORBIT_AREA / 2D) - area;
		theta = Math.abs(2D * Math.atan(1.097757562D * Math.tan(.329512059D * areaTemp)));
		if (area < (ORBIT_AREA / 2D)) theta = 0D - theta;
		theta += Math.PI;
		if (theta >= (2 * Math.PI)) theta -= (2 * Math.PI);

		// Determine new radius
		radius = 1.510818924D / (1 + (ECCENTRICITY * Math.cos(theta)));
		//System.out.println("radius is " + radius);

		// Determine Sun direction

		// Determine Sun theta
		double sunTheta = sunDirection.getTheta();
		sunTheta -= (2D * Math.PI) * (seconds / SOLAR_DAY);

		while (sunTheta < 0D) sunTheta += 2D * Math.PI;
		sunDirection.setTheta(sunTheta);
		//System.out.println("sunTheta is " + sunTheta);

		// Determine Sun phi
		double sunPhi = (Math.PI / 2D) + (Math.sin(theta + (Math.PI / 2D)) * TILT);
		sunDirection.setPhi(sunPhi);
		//System.out.println("sunPhi is " + sunPhi);

	}

	/**
	 * Returns the theta angle of Mars's orbit.
	 * Angle is clockwise starting at aphelion.
	 * @return the theta angle of Mars's orbit
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Returns the radius of Mars's orbit in A.U.
	 * @return the radius of Mars's orbit
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Gets the current distance to the Sun.
	 * @return distance in Astronomical Units (A.U.)
	 */
	public double getDistanceToSun() {
		return radius;
	}

	/**
	 * Gets the Sun's angle from a given phi (latitude).
	 * @param phi location in radians (0 - PI).
	 * @return angle in radians (0 - PI).
	 */
	public double getSunAngleFromPhi(double phi) {
		double a = Math.abs(phi - sunDirection.getPhi());
		//System.out.println("getSunAngleFromPhi() : " + a);
		//return Math.abs(phi - sunDirection.getPhi());
		return a;
	}

	/**
	 * Gets the solar zenith angle from a given coordinate
	 * @param location
	 * @return angle in radians (0 - PI).
	 */
	// 2015-03-17 Added getSolarZenithAngle()
	public double getSolarZenithAngle(Coordinates location) {
		// Determine the solar zenith angle
		double z = Math.acos( getCosineSolarZenithAngle(location) );
		//System.out.println("solar zenith angle is " + z);
		return z;
	}

	/**
	 * Gets the cosine solar zenith angle from a given coordinate
	 * @param location
	 * @return angle in radians (0 - PI).
	 */
	// 2015-03-17 Added getCosineSolarZenithAngle()
	public double getCosineSolarZenithAngle(Coordinates location) {
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

		//double theta = location.getTheta();
		double theta_offset = location.getTheta() / Math.PI * 500D; // convert theta in longitude in radian to millisols;
		//System.out.println(" theta_offset: " + theta_offset);
		double lat = location.getPhi2Lat(location.getPhi());
		double dec = getSolarDeclinationAngle(getL_s());
		//System.out.println("solar dec angle is " + dec);
		double solar_time = marsClock.getMillisol() ;
		//System.out.println("solar_time is " + (int) solar_time);
		// TODO: figure out a more compact Equation of Time (EOT) using numerical model of the analemma.
		// Mars has an EOT varying between -51.1min and +39.9min, with its more than five times larger orbital eccentricity than the Earth's,
		// see http://www.giss.nasa.gov/research/briefs/allison_02/  http://www.giss.nasa.gov/tools/mars24/help/notes.html
		double equation_of_time_offset = 0;
		if (L_s <= 90 )
			equation_of_time_offset = (43.4783 - dec / Math.PI *180D) * 23D / 25D ; // y = (-25/23 ) x + 25/23*40
		else if (L_s <= 180)
			equation_of_time_offset = (dec / Math.PI *180D - 21D) * 59D / 25D; // y = 25/59 x + 21
		else if (L_s <= 230)
			equation_of_time_offset = 40D - (L_s-180D)*.15;
		else if (L_s <= 240)
			equation_of_time_offset = 35D - (L_s-230D)*.18;
		else if (L_s <= 250)
			equation_of_time_offset = 28D - (L_s-240D) * .18;
		else if (L_s <= 260)
			equation_of_time_offset = 20D - (L_s-250D) * .18;
		else if (L_s <= 270)
			equation_of_time_offset = -10D + (L_s-260D) * .1;
		else if (L_s <= 280)
			equation_of_time_offset = -25D + (L_s-270D) * .1;
		else if (L_s <= 290)
			equation_of_time_offset = -42D + (L_s-280D) * .1;
		else if (L_s <= 300)
			equation_of_time_offset = -50D + (L_s-290D) * .1;
		else if (L_s <= 310)
			equation_of_time_offset = -50D + (L_s-300D) * .1;
		else if (L_s <= 320)
			equation_of_time_offset = -50D + (L_s-310D) * .1;
		else if (L_s <= 330)
			equation_of_time_offset = -50D + (L_s-320D) * .1;
		else if (L_s <= 340)
			equation_of_time_offset = -48D + (L_s-330D) * .1;
		else if (L_s <= 350)
			equation_of_time_offset = -46D + (L_s-340D) * .1;
		else if (L_s <= 360)
			equation_of_time_offset = -44D + (L_s-350D) * .1;

		double EOT_in_millisol = equation_of_time_offset * 60D /SOLAR_DAY * 1000D;
		double modified_solar_time = solar_time + theta_offset + EOT_in_millisol ;
		double h = Math.PI * Math.abs(modified_solar_time - 500D) / 500D ;
		//System.out.println("h is " + h);
		// Determine the solar zenith angle
		double cosine_z =  Math.sin (lat) * Math.sin (dec) +  Math.cos (lat)  *Math.cos (dec) * Math.cos (h) ;
		//System.out.println("Math.cos (h) is " + Math.cos (h));
		//System.out.println("solar zenith angle is " + z);
		return cosine_z;
	}


	/**
	 * Returns the instantaneous true anomaly or polar angle of Mars around the sun.
	 * Angle is counter-clockwise starting at perigee.
	 * @return the true anomaly
	 */
	// 2015-03-17 Added getTrueAnomaly()
	public double getTrueAnomaly() {
		// r = a (1 - e * e) / ( 1 + e * cos (ta) )
		double part1 = SEMI_MAJOR_AXIS * (1 - ECCENTRICITY *  ECCENTRICITY) / radius;//   radius is in A.U.  no need of * 149597871000D
		//System.out.println("part1 is " + part1);
		double part2 = ( part1 - 1 ) / ECCENTRICITY ;
		double v = Math.acos(part2);
		//System.out.println("true anomally is " + v);
		return v;
	}


	/**
	 * Computes the instantaneous areocentric longitude
	 */
	// 2015-03-17 Added computeL_s()
	public void computeL_s() {
		double v = getTrueAnomaly();
		double L_s = v / Math.PI * 180D + 248D;
		if (L_s >= 360)
			L_s = L_s - 360;
		//System.out.println("L_s is " + L_s);
		//return L_s;
	}

	/**
	 * Gets the instantaneous areocentric longitude
	 * @return angle in radians (0 - 360).
	 */
	// 2015-04-08 Added getL_s()
	public double getL_s() {
		computeL_s();
		return L_s;
	}

	/**
	 * Gets the solar declination angle from a given areocentric longitude.
	 * @param L_s areocentric longitude in degrees (0 -360).
	 * @return angle in radians (0 - PI).
	 */
	// 2015-03-17 Added getSolarZenithAngle()
	public double getSolarDeclinationAngle(double L_s) {
		return Math.asin ( Math.sin (TILT) * Math.sin (L_s * Math.PI / 180D) );
	}

	/**
	 * The point on the surface of Mars perpendicular to the Sun as Mars rotates.
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
	}
}