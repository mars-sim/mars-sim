/**
 * Mars Simulation Project
 * MarsClock.java
 * @version 2.72 2001-03-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.text.*;

/** The MarsClock class keeps track of Martian time.
 *  It uses the Martian Desynchronized Sol Calendar.
 *  It should be synchronized with the Earth clock. 
 */
public class MarsClock {

    // Static members
    private static final double SOLS_IN_ORBIT = 669.60104;
    private static final int DEGREES_IN_SOL = 360;

    // Data members
    private double orbit;
    private double sol;

    /** Constructs a MarsClock object */
    public MarsClock() {
    
        // Set initial date to 0
        orbit = 0D;
        sol = 0D;
    }

    public double getOrbitDouble() {
        return orbit;
    }

    public int getOrbitInt() {
        return (int) orbit;
    }

    public int getMensis() {
        return (int) ((orbit - getOrbitInt()) * 10);
    }

    public double getSolDouble() {
        return sol;
    }

    public int getSolInt() {
        return (int) sol;
    }

    /** Returns time stamp 
     *  @return timestamp. ex "15.574 - 154.725°"
     */
    public String getTimeStamp() {
        
        // Construct obit portion of time stamp
        double tempOrbit = Math.floor(orbit * 1000D) / 1000D;
        String orbitString = "" + tempOrbit;
        while ((orbitString.length() - orbitString.indexOf('.')) < 4) orbitString += '0';

        // Construct sol portion of time stamp
        double tempSol = Math.floor(sol * 1000D) / 1000D;
        String solString = "" + tempSol;
        while ((solString.length() - solString.indexOf('.')) < 4) solString += '0';

        return orbitString + " - " + solString + "°";
    }

    /** Adds time to the calendar 
     *  param time time measured in units of Degrees 
     *  1 Degree = 4min, 6.23sec (Earth time)
     */
    public void addTime(double time) {

         // Add time to orbit
         double tempSols = time / DEGREES_IN_SOL;
         orbit += tempSols / SOLS_IN_ORBIT;

         // Add time to sol
         sol += time;
         while (sol >= DEGREES_IN_SOL) sol -= DEGREES_IN_SOL;
    }

    /** Adds time to the calendar in units of Earth seconds
     *  param time time measured in units of Earth seconds
     *  1 Degree = 246.23 seconds
     */
    public void addTimeSeconds(double time) {
        
        // Convert to degrees and use addTime method
        addTime(time / 245.23);
    }
}
