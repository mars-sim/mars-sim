/**
 * Mars Simulation Project
 * Mars.java
 * @version 2.75 2004-03-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import org.mars_sim.msp.simulation.events.HistoricalEventManager;
import org.mars_sim.msp.simulation.malfunction.MalfunctionFactory;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.medical.MedicalManager;

/** Mars represents the planet Mars in the simulation. It contains all the
 *  units, a master clock, and access to the topography data.
 */
public class Mars implements Serializable {

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
    private transient MalfunctionFactory malfunctionFactory; // The malfunction factory
    private transient HistoricalEventManager eventManager; // All historical info.
    private transient Thread clockThread;
    private transient SimulationConfig configuration; // The simulation configuration.

    // Persistent Data members
    private String stateFile; // Name of file to load/store this simulation.
    private UnitManager units; // Unit controller
    private MissionManager missionManager; // Mission controller
    private MedicalManager medicalManager; // Medical complaints
    private MasterClock masterClock; // Master clock for virtual world
    private OrbitInfo orbitInfo; // Orbital information
    private Weather weather; // Martian weather

    /** 
     * Constructor
     * @throws Exception if Mars could not be constructed.
     */
    public Mars(SimulationProperties initProps) throws Exception {

		// Initialize transient properties
		initializeTransients(initProps);

        // Initialize the Medical conditions
        medicalManager = new MedicalManager(configuration.getPersonConfiguration());

        // Initialize mission manager
        missionManager = new MissionManager(this);

        // Initialize all units
        units = new UnitManager(properties, this);
        units.constructInitialUnits();

        // Initialize orbit info
        orbitInfo = new OrbitInfo();

	    // Initialize weather
	    weather = new Weather(this);

        // Initialize and start master clock
        masterClock = new MasterClock(this);

        // System.out.println("Create new simulation");
    }

    /**
     * Initialize transient simulation properties.
     * @param initProps simulation properties if any or null.
     */
    private void initializeTransients(SimulationProperties initProps) {

        // Initialize simulation properties
	    if (initProps != null) properties = initProps;
	    else properties = new SimulationProperties();

		try {
			configuration = new SimulationConfig();
		}
		catch (Exception e) {
			System.out.println("Configuration error: " + e.getMessage());
		}

        // Initialize surface features
        surfaceFeatures = new SurfaceFeatures(this);

        // Initialize malfunction factory
	    malfunctionFactory = new MalfunctionFactory();

	    // Set state file
	    setStateFile(DEFAULT_DIR + '/' + DEFAULT_FILE);

        // Create an event manager
        eventManager = new HistoricalEventManager(this);
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
     * @return A newly created Mars.
     */
    public static Mars load(File fileName)
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

        Mars mars = (Mars)p.readObject();

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
    
    /** 
     * Gets the simulation configuration.
     * @return configuration
     */
    public SimulationConfig getSimulationConfiguration() {
    	return configuration;
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

    /** Returns the unit manager
     *  @return unit manager for Mars
     */
    public UnitManager getUnitManager() {
        return units;
    }

    /** Returns the mission manager
     *  @return mission manager for Mars
     */
    public MissionManager getMissionManager() {
        return missionManager;
    }

    /** Returns the medical manager
     *  @return medical manager for Mars
     */
    public MedicalManager getMedicalManager() {
        return medicalManager;
    }

    /** Returns the event manager
     *  @return Event manager for Mars
     */
    public HistoricalEventManager getEventManager() {
        return eventManager;
    }

    /** Returns the malfunction factory
     *  @return malfunction factory for Mars
     */
    public MalfunctionFactory getMalfunctionFactory() {
        return malfunctionFactory;
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
	    out.writeObject(medicalManager);
        out.writeObject(orbitInfo);
	    out.writeObject(weather);
        out.writeObject(masterClock);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        // Initialise the transient values
        initializeTransients(null);

        // Load in the persistent values in sequence
        units = (UnitManager)in.readObject();
        missionManager = (MissionManager)in.readObject();
	    medicalManager = (MedicalManager)in.readObject();
        orbitInfo = (OrbitInfo)in.readObject();
	    weather = (Weather)in.readObject();
        masterClock = (MasterClock)in.readObject();
    }
}

