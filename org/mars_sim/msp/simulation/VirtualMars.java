/**
 * Mars Simulation Project
 * VirtualMars.java
 * @version 2.73 2001-11-29
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.task.MissionManager;
import java.util.*;
import java.io.*;

/** VirtualMars represents Mars in the simulation. It contains all the
 *  units, a master clock, and access to the topography data.
 */
public class VirtualMars implements Serializable {

    /**
     * The name of the state file
     */
    public final static String STATE_FILE = "marssim.ser";
    static final long serialVersionUID = -4771084409109255152L;

    // Transient Data members
    private transient OrbitInfo orbitInfo; // Orbital information
    private transient SurfaceFeatures surfaceFeatures; // Surface features
    private transient SimulationProperties properties; // The user-defined simulation properties
    // Persistent Data members
    private UnitManager units; // Unit controller
    private MissionManager missionManager; // Mission controller
    private MasterClock masterClock; // Master clock for virtual world

    /** Constructs a VirtualMars object */
    public VirtualMars() {

        initialiseTransients();

        // Initialize mission manager
        missionManager = new MissionManager(this);

        // Initialize all units
        units = new UnitManager(this);

        // Initialize and start master clock
        masterClock = new MasterClock(this);
    } 
 
    private void initialiseTransients() { 
 
        // Initialize simulation properties
        properties = new SimulationProperties();

        // Initialize orbit info
        orbitInfo = new OrbitInfo();
 
        // Initialize surface features
        surfaceFeatures = new SurfaceFeatures(this);
    }

    /** 
     * This method starts the execution of the simulation 
     */ 
    public void start() { 
 
        Thread clockThread = new Thread(masterClock);
        clockThread.start();
    }

    /** 
     * This method loads a previous simulation state 
     */ 
    public static VirtualMars load() { 
 
        VirtualMars mars = null;
        try {
	        FileInputStream istream = new FileInputStream(STATE_FILE);
	        ObjectInputStream p = new ObjectInputStream(istream);

	        mars = (VirtualMars)p.readObject();
	        istream.close();
        }
        catch(Exception e) {
            System.out.println("Problem reading state " + e);
        }
        return mars;
    }


    /** 
     * This method stores the current simulation state 
     */ 
    public void store() { 
 
        try {
	        FileOutputStream ostream = new FileOutputStream(STATE_FILE);
	        ObjectOutputStream p = new ObjectOutputStream(ostream);

	        p.writeObject(this);
	        p.flush();
	        ostream.close();
        }
        catch(Exception e) {
            System.out.println("Problem writting state " + e);
        }
    }

    /** Clock pulse from master clock 
     *  @param time amount of time passing (in millisols)
     */
    void clockPulse(double time) {
        orbitInfo.addTime(MarsClock.convertMillisolsToSeconds(time));
        units.timePassing(time);
    }

    /** Returns the simulation properties
     *  @return simulation properties
     */
    public SimulationProperties getSimulationProperties() {
        return properties;
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

    /** Returns the unit manager 
     *  @return unit manager for virtual Mars
     */
    public UnitManager getUnitManager() {
        return units;
    }

    /** Returns the mission manager
     *  @return mission manager for virtual Mars
     */
    public MissionManager getMissionManager() {
        return missionManager;
    }

    /** Returns the master clock
     *  @return master clock instance
     */
    public MasterClock getMasterClock() {
        return masterClock;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        // Store the persistent values in sequence
        out.writeObject(units); 
        out.writeObject(missionManager);
        out.writeObject(masterClock);
    }

    private void readObject(java.io.ObjectInputStream in) 
            throws IOException, ClassNotFoundException { 

        // Initialise the transient values 
        initialiseTransients(); 

        // Load in the persistent values in sequence
        units = (UnitManager)in.readObject();
        missionManager = (MissionManager)in.readObject();
        masterClock = (MasterClock)in.readObject();

        // Set the simulation speed
        masterClock.setRatio(getSimulationProperties().getTimeRatio());
    }
}

