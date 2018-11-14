/**
 * Mars Simulation Project
 * Mars.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

/**
 * Mars represents the planet Mars in the simulation.
 */
public class Mars implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Mars average radius in km. */
	public static final double MARS_RADIUS_KM = 3393D;

	public static final double MARS_CIRCUMFERENCE = MARS_RADIUS_KM * 2D * Math.PI;

	// Data members
	/** Surface features. */
	private static SurfaceFeatures surfaceFeatures;
	/** Orbital information. */
	private static OrbitInfo orbitInfo;
	/** Martian weather. */
	private static Weather weather;

	/**
	 * Constructor.
	 * 
	 * @throws Exception if Mars could not be constructed.
	 */
	public Mars() {
		// logger.info("Mars' constructor is in "+Thread.currentThread().getName() + "
		// Thread");
		// Initialize surface features
		surfaceFeatures = new SurfaceFeatures();
		// Initialize orbit info
		orbitInfo = new OrbitInfo();
		// Initialize weather
		weather = new Weather();
	}

	/**
	 * Initialize transient data in the simulation.
	 * 
	 * @throws Exception if transient data could not be constructed.
	 */
	public void initializeTransientData() {
		// Initialize surface features transient data.
		surfaceFeatures.initializeTransientData();
	}

	/**
	 * Returns the orbital information
	 * 
	 * @return orbital information
	 */
	public OrbitInfo getOrbitInfo() {
		return orbitInfo;
	}

	/**
	 * Returns surface features
	 * 
	 * @return surfaces features
	 */
	public SurfaceFeatures getSurfaceFeatures() {
		return surfaceFeatures;
	}

	/**
	 * Returns Martian weather
	 * 
	 * @return weather
	 */
	public Weather getWeather() {
		return weather;
	}

	/**
	 * Time passing in the simulation.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) {
		orbitInfo.addTime(time);
		surfaceFeatures.timePassing(time);
		weather.timePassing(time);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		surfaceFeatures = null;// .destroy();
		orbitInfo = null;// .destroy();
		weather = null;// .destroy();
	}
}