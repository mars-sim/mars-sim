/**
 * Mars Simulation Project
 * Mars.java
 * @version 2.84 2008-03-15
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.mars;

import java.io.Serializable;

/** 
 * Mars represents the planet Mars in the simulation.
 */
public class Mars implements Serializable {
	
	// Mars average radius in km.
	public static final double MARS_RADIUS_KM = 3393D;
	
	public static final double MARS_CIRCUMFERENCE = MARS_RADIUS_KM * 2D * Math.PI;

    // Data members
    private SurfaceFeatures surfaceFeatures; // Surface features
    private OrbitInfo orbitInfo; // Orbital information
    private Weather weather; // Martian weather

    /** 
     * Constructor
     * @throws Exception if Mars could not be constructed.
     */
    public Mars() throws Exception {

		// Initialize surface features
		surfaceFeatures = new SurfaceFeatures();

        // Initialize orbit info
        orbitInfo = new OrbitInfo();

	    // Initialize weather
	    weather = new Weather();
    }
    
	/**
	 * Initialize transient data in the simulation.
	 * @throws Exception if transient data could not be constructed.
	 */
	public void initializeTransientData() throws Exception {
		// Initialize surface features transient data.
		surfaceFeatures.initializeTransientData();
	}

    /** Returns the orbital information
     *  @return orbital information
     */
    public OrbitInfo getOrbitInfo() {
        return orbitInfo;
    }

    /** Returns surface features
     *  @return surfaces features
     */
    public SurfaceFeatures getSurfaceFeatures() {
        return surfaceFeatures;
    }

    /** Returns Martian weather
     *  @return weather
     */
    public Weather getWeather() {
        return weather;
    }
    
    /**
     * Time passing in the simulation.
     * @param time time in millisols
     * @throws Exception if error during time.
     */
    public void timePassing(double time) throws Exception {
    	orbitInfo.addTime(time);
    }
}