/**
 * Mars Simulation Project
 * OrbitInfo.java
 * @version 2.72 2001-04-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The OrbitInfo class keeps track of the orbital position of Mars 
 */
public class OrbitInfo {

    // Static data members
    private static final double ORBIT_PERIOD = 59355072D; // Mars orbit period in seconds
    private static final double ECCENTRICITY = .093D;     // Mars orbit eccentricity
    private static final double SEMIMAJOR_AXIS = 1.524D;  // Mars orbit semimajor axis in au
    private static final double TILT=.4396D;              // Mars tilt in radians
    private static final double SOLAR_DAY = 88775.244D;   // Mars solar day in seconds

    // Data members
    private double theta;   // The angle of Mars's position to the Sun (in radians)
    private double radius;  // The distance from the Sun to Mars (in au).
    private Coordinates sunDirection; // The point on the surface of Mars perpendicular to the Sun as Mars rotates.

    /** Constructs an OrbitInfo object */
    public OrbitInfo() {
    
	// Initialize data members
        // Set orbit coordinates to start of orbit.
        theta = 0D;
        radius = 1.665732D;
        sunDirection = new Coordinates((Math.PI / 2D) + TILT, 0D);
    }

    /** Adds time (in seconds) to the orbit
     * @param seconds seconds of time added
     */
    public void addTime(double seconds) {

        // Determine new theta
        // (the correct equation needs to be implemented later)
        theta += (2D * Math.PI) * (seconds / ORBIT_PERIOD);
        if (theta >= (2 * Math.PI)) theta -= (2 * Math.PI);

        // Determine new radius
        radius = 1.510818924D / (1 + (ECCENTRICITY * Math.cos(theta)));

        // Determine Sun direction

        // Determine Sun theta
        double sunTheta = sunDirection.getTheta();
        sunTheta += (2D * Math.PI) * (seconds / SOLAR_DAY);
        while (sunTheta >= (2D * Math.PI)) sunTheta -= 2D * Math.PI;
        sunDirection.setTheta(sunTheta);        

        // Determine Sun phi
        double sunPhi = (Math.PI / 2D) + (Math.sin(theta + (Math.PI / 2D)) * TILT);
        sunDirection.setPhi(sunPhi);
    } 

    /** Returns the theta angle of Mars's orbit.
     *  Angle is clockwise starting at aphelion.
     *  @return the theta angle of Mars's orbit
     */
    public double getTheta() { return theta; }

    /** Returns the radius of Mars's orbit in au.
     * @return the radius of Mars's orbit
     */
    public double getRadius() { return radius; }

    /** The point on the surface of Mars perpendicular to the Sun as Mars rotates. 
     *  @return the surface point on Mars perpendicular to the sun
     */
    public Coordinates getSunDirection() { return sunDirection; }
}
