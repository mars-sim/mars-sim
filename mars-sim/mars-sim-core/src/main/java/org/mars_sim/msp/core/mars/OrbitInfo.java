/**
 * Mars Simulation Project
 * OrbitInfo.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.time.MarsClock;

/** 
 * The OrbitInfo class keeps track of the orbital position of Mars 
 */
public class OrbitInfo
implements Serializable {

	/** default serial id.*/
	private static final long serialVersionUID = 1L;

	// Static data members
	/** Mars orbit period in seconds. */
	private static final double ORBIT_PERIOD = 59355072D;
	/** Mars orbit eccentricity. */
	private static final double ECCENTRICITY = .093D;
	// /** Mars orbit semimajor axis in au. */
	// private static final double SEMIMAJOR_AXIS = 1.524D;
	/** Mars tilt in radians. */
	private static final double TILT = .4396D;
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
	/** The point on the surface of Mars perpendicular to the Sun as Mars rotates. */
	private Coordinates sunDirection;

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

		// Determine Sun direction

		// Determine Sun theta
		double sunTheta = sunDirection.getTheta();
		sunTheta -= (2D * Math.PI) * (seconds / SOLAR_DAY);
		while (sunTheta < 0D) sunTheta += 2D * Math.PI;
		sunDirection.setTheta(sunTheta);        

		// Determine Sun phi
		double sunPhi = (Math.PI / 2D) + (Math.sin(theta + (Math.PI / 2D)) * TILT);
		sunDirection.setPhi(sunPhi);
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
		return Math.abs(phi - sunDirection.getPhi());
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