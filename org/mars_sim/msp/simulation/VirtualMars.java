/**
 * Mars Simulation Project
 * VirtualMars.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.person.ai.MissionManager;
import org.mars_sim.msp.simulation.person.medical.MedicalManager;
import java.util.*;
import java.io.*;

/** VirtualMars represents Mars in the simulation. It contains all the
 *  units, a master clock, and access to the topography data.
 */
public class VirtualMars implements Serializable {

    /**
     * The name of the state file
     */
    public final static String DEFAULT_FILE = "default.sim";

    /**
     * The default directory to hold simulation files
     */
    public final static String DEFAULT_DIR = "saved";

    static final long serialVersionUID = -4771084409109255152L;

    // Transient Data members
    private transient SurfaceFeatures surfaceFeatures; // Surface features
    private transient SimulationProperties properties; // The user-defined simulation properties
    private transient Thread clockThread;

    // Persistent Data members
    private String stateFile; // Name of file to load/store this simulation.
    private UnitManager units; // Unit controller
    private MissionManager missionManager; // Mission controller
    private MedicalManager medicalManager; // Medical complaints
    private MasterClock masterClock; // Master clock for virtual world
    private OrbitInfo orbitInfo; // Orbital information

    /** Constructs a VirtualMars object */
    public VirtualMars() {

        initialiseTransients();
        setStateFile(DEFAULT_DIR + '/' + DEFAULT_FILE);

        // Initialize the Medical conditions
        medicalManager = new MedicalManager(properties);

        // Initialize mission manager
        missionManager = new MissionManager(this);

        // Initialize all units
        units = new UnitManager(this);

        // Initialize orbit info
        orbitInfo = new OrbitInfo();

        // Initialize and start master clock
        masterClock = new MasterClock(this);

        System.out.println("Create new simulation");
    }

    private void initialiseTransients() {

        // Initialize simulation properties
        properties = new SimulationProperties();

        // Initialize surface features
        surfaceFeatures = new SurfaceFeatures(this);
    }

    private void setStateFile(String fileName) {
        stateFile = fileName;
    }

    /**
     * This method starts the execution of the simulation
     */
    public void start() {

        clockThread = new Thread(masterClock, "Master Clock");
        clockThread.start();
    }

    /**
     * Stop the simulation
     */
    public void stop() {
        masterClock.stop();
        clockThread = null;
    }

    /**
     * This method loads a previous simulation state from the specified file.
     * If no file is specified, then the default file name is used.
     * @param fileName Filename of load file.
     * @return A newly created Virtual Mars.
     */
    public static VirtualMars load(File fileName)
                throws Exception {

        if (fileName == null) {
            fileName = new File(DEFAULT_DIR + '/' + DEFAULT_FILE);
        }

        FileInputStream istream;
        try {
	        istream = new FileInputStream(fileName);
	    }
        catch(FileNotFoundException e) {
            return null;
        }

        ObjectInputStream p = new ObjectInputStream(istream);

        VirtualMars mars = (VirtualMars)p.readObject();

        mars.setStateFile(fileName.getAbsolutePath());
        istream.close();

        return mars;
    }


    /**
     * This method stores the current simulation state to a file. If the
     * specified file is null, then the associated file name is used.
     * @param fileName Name of the file to hold simulation data.
     * @throws IOException Problem saving the file
     */
    public void store(File outFile)
            throws IOException {

        if (outFile == null) {
            outFile = new File(stateFile);
        }

        // Make sure the parent directory is set
        File parentFile = outFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        // Store the state
        FileOutputStream ostream = new FileOutputStream(outFile);
        ObjectOutputStream p = new ObjectOutputStream(ostream);

        p.writeObject(this);
        p.flush();
        ostream.close();
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

    /** Returns the medical manager
     *  @return medical manager for virtual Mars
     */
    public MedicalManager getMedicalManager() {
        return medicalManager;
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
        out.writeObject(orbitInfo);
        out.writeObject(masterClock);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        // Initialise the transient values
        initialiseTransients();

        // Load in the persistent values in sequence
        units = (UnitManager)in.readObject();
        missionManager = (MissionManager)in.readObject();
        orbitInfo = (OrbitInfo)in.readObject();
        masterClock = (MasterClock)in.readObject();
    }
}

