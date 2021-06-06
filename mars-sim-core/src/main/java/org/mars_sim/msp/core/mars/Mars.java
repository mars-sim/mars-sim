/**
 * Mars Simulation Project
 * Mars.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;


/**
 * Mars represents the planet Mars in the simulation.
 */
public class Mars implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Mars average radius in km. */
	public static final double MARS_RADIUS_KM = 3393D;

	public static final double MARS_CIRCUMFERENCE = MARS_RADIUS_KM * 2D * Math.PI;

	// Data members
	/** Martian weather. */
	private Weather weather;
	/** Surface features. */
	private SurfaceFeatures surfaceFeatures;
	/** Orbital information. */
	private OrbitInfo orbitInfo;
	/** Mars Surface as a unit container */
	//private transient MarsSurface marsSurface;
	
	/**
	 * Constructor.
	 * 
	 * @param clock The Mars clock
	 * @throws Exception if Mars could not be constructed.
	 */
	public Mars(MarsClock clock) {
		// Initialize mars surface		
		//marsSurface = new MarsSurface();
		// Initialize orbit info
		orbitInfo = new OrbitInfo(clock);
		// Initialize weather
		weather = new Weather(clock, orbitInfo);
		// Initialize surface features
		surfaceFeatures = new SurfaceFeatures(clock, orbitInfo, weather);
		// Cyclic dependency !!!!
		weather.initializeInstances(surfaceFeatures);
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
	 * @return {@Link OrbitInfo}
	 */
	public OrbitInfo getOrbitInfo() {
		return orbitInfo;
	}

	/**
	 * Returns surface features
	 * 
	 * @return {@Link SurfacesFeatures}
	 */
	public SurfaceFeatures getSurfaceFeatures() {
		return surfaceFeatures;
	}
	
	/**
	 * Returns Martian weather
	 * 
	 * @return {@Link Weather}
	 */
	public Weather getWeather() {
		return weather;
	}

	/**
	 * Time passing in the simulation.
	 * 
	 * @param pulse The pulse of the simulation clock
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		orbitInfo.timePassing(pulse);
		surfaceFeatures.timePassing(pulse);
		weather.timePassing(pulse);
		
		return true;
	}

	/**
	 * Returns Mars surface instance
	 * 
	 * @return {@Link MarsSurface}
	 */
//	public MarsSurface getMarsSurface() {
//		return marsSurface;
//	}

	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		surfaceFeatures.destroy();
		orbitInfo.destroy();
		weather.destroy();
		surfaceFeatures = null;
		orbitInfo = null;// .destroy();
		weather = null;// .destroy();
	}
}
