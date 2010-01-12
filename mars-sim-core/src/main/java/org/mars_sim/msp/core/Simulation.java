/**
 * Mars Simulation Project
 * Simulation.java
 * @version 2.90 2010-01-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.medical.MedicalManager;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation implements ClockListener, Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.Simulation";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Version string.
	public final static String VERSION = "2.90";
	
	// Default save file.
	public final static String DEFAULT_FILE = "default.sim";
	
	// Save directory
	public final static String DEFAULT_DIR = System.getProperty("user.home") + File.separator + "mars-sim" +
		File.separator + "saved";

	// Singleton instance
	private static final Simulation instance = new Simulation();
	
	// Transient data members (aren't stored in save file)
	private transient HistoricalEventManager eventManager; // All historical info.
	private transient Thread clockThread;
	
	// Intransient data members (stored in save file)
	private Mars mars; // Planet Mars
	private MalfunctionFactory malfunctionFactory; // The malfunction factory
	private UnitManager unitManager; // Manager for all units in simulation.
	private MissionManager missionManager; // Mission controller
	private RelationshipManager relationshipManager; // Manages all personal relationships.
	private MedicalManager medicalManager; // Medical complaints
	private MasterClock masterClock; // Master clock for the simulation.
	private CreditManager creditManager; // Manages trade credit between settlements.
    private ScientificStudyManager scientificStudyManager; // Manages scientific studies.
	private boolean defaultLoad = false;

	/**
	 * Constructor
	 */
	private Simulation() {
		
		try {
			// Initialize transient data members.
			initializeTransientData();
		}
		catch (Exception e) {
			logger.log(Level.SEVERE,"Simulation could not be created: " + e.getMessage());
		}
	}
	
	/**
	 * Gets a singleton instance of the simulation.
	 * @return Simulation instance
	 */
	public static Simulation instance() {
		return instance;
	}
	
	/**
	 * Creates a new simulation instance.
	 * @throws Exception if new simulation could not be created.
	 */
	public static void createNewSimulation() throws Exception {
		Simulation simulation = instance();
		simulation.defaultLoad = false;
		simulation.stop();
		
		// Wait until current time pulse runs it course.
		Thread.sleep(MasterClock.TIME_PULSE_LENGTH);
		
		try {
			// Initialize intransient data members.
			simulation.initializeIntransientData();
			
			// Initialize transient data members.
			simulation.initializeTransientData();
			
			simulation.start();
		}
		catch (Exception e) {
			throw new Exception("New simulation could not be created: " + e.getMessage());
		}
	}
	
	/**
	 * Initialize transient data in the simulation.
	 * @throws Exception if transient data could not be loaded.
	 */
	private void initializeTransientData() throws Exception {
		eventManager = new HistoricalEventManager();
	}
	
	/**
	 * Initialize intransient data in the simulation.
	 * @throws Exception if intransient data could not be loaded.
	 */
	private void initializeIntransientData() throws Exception {
		SimulationConfig.reloadConfig();
		malfunctionFactory = new MalfunctionFactory(SimulationConfig.instance().getMalfunctionConfiguration());
		mars = new Mars();
		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		masterClock = new MasterClock();
		unitManager = new UnitManager();
		unitManager.constructInitialUnits();
		creditManager = new CreditManager();
        scientificStudyManager = new ScientificStudyManager();
	}
	
	/**
	 * Loads a simulation instance from a save file.
	 * @param file the file to be loaded from.
	 * @throws Exception if simulation could not be loaded.
	 */
	public void loadSimulation(File file) throws Exception {
		logger.config("Loading simulation from " + file);
		
		Simulation simulation = instance();
		simulation.stop();
		
		// Use default file path if file is null.
		if (file == null) {
			/* [landrus, 27.11.09]: use the home dir instead of unknow relative paths. */
			file = new File(DEFAULT_DIR, DEFAULT_FILE);
			defaultLoad = true;
		}
		else defaultLoad = false;
		
		try {
			ObjectInputStream p = new ObjectInputStream(new FileInputStream(file));
			
			// Load intransient objects.
			SimulationConfig.setInstance((SimulationConfig) p.readObject());
			malfunctionFactory = (MalfunctionFactory) p.readObject();
			mars = (Mars) p.readObject();
			mars.initializeTransientData();
			missionManager = (MissionManager) p.readObject();
			relationshipManager = (RelationshipManager) p.readObject();
			medicalManager = (MedicalManager) p.readObject();
            scientificStudyManager = (ScientificStudyManager) p.readObject();
			creditManager = (CreditManager) p.readObject();
			unitManager = (UnitManager) p.readObject();
			masterClock = (MasterClock) p.readObject();
			p.close();
		}
		catch(FileNotFoundException e) {
			throw new Exception("Saved file: " + file.getAbsolutePath() + " not found.");
		}
		
		simulation.start();
	}
	
	/**
	 * Saves a simulation instance to a save file.
	 * @param file the file to be saved to.
	 * @throws Exception if simulation could not be saved.
	 */
	public void saveSimulation(File file) throws Exception {
		logger.config("Saving simulation to " + file);
		
		Simulation simulation = instance();
		simulation.stop();
		
		// Use default file path if file is null.
		/* [landrus, 27.11.09]: use the home dir instead of unknow relative paths. Also check if the dirs
		 * exist */
		if (file == null) {
			file = new File(DEFAULT_DIR, DEFAULT_FILE);
            
            if (!file.getParentFile().exists()) {
            	file.getParentFile().mkdirs();
            }
		}
		
		ObjectOutputStream p = new ObjectOutputStream(new FileOutputStream(file));
			
		// Store the intransient objects.
		p.writeObject(SimulationConfig.instance());
		p.writeObject(malfunctionFactory);
		p.writeObject(mars);
		p.writeObject(missionManager);
		p.writeObject(relationshipManager);
		p.writeObject(medicalManager);
        p.writeObject(scientificStudyManager);
		p.writeObject(creditManager);
		p.writeObject(unitManager);
		p.writeObject(masterClock);
			
		p.flush();
		p.close();
		
		simulation.start();
	}
	
	/**
	 * Start the simulation.
	 */
	public void start() {
		if (clockThread == null) {
			clockThread = new Thread(masterClock, "Master Clock"); 
			masterClock.addClockListener(this);
			clockThread.start();
		}
	}

	/**
	 * Stop the simulation.
	 */
	public void stop() {
		if (masterClock != null) {
			masterClock.stop();
			masterClock.removeClockListener(this);
		}
		clockThread = null;
	}
	
	/** 
	 * Clock pulse from master clock
	 * @param time amount of time passing (in millisols)
	 */
	public void clockPulse(double time) {
		try {
			mars.timePassing(time);
			missionManager.timePassing(time);
			unitManager.timePassing(time);
            scientificStudyManager.updateStudies();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.log(Level.SEVERE,"Simulation.clockPulse(): " + e.getMessage());
		}
	}
	
	/**
	 * Get the planet Mars.
	 * @return Mars
	 */
	public Mars getMars() {
		return mars;
	}
	
	/**
	 * Get the unit manager.
	 * @return unit manager
	 */
	public UnitManager getUnitManager() {
		return unitManager;
	}
	
	/**
	 * Get the mission manager.
	 * @return mission manager
	 */
	public MissionManager getMissionManager() {
		return missionManager;
	}
	
	/**
	 * Get the relationship manager.
	 * @return relationship manager.
	 */
	public RelationshipManager getRelationshipManager() {
		return relationshipManager;
	}
	
	/**
	 * Gets the credit manager.
	 * @return credit manager.
	 */
	public CreditManager getCreditManager() {
		return creditManager;
	}
	
	/**
	 * Get the malfunction factory.
	 * @return malfunction factory
	 */
	public MalfunctionFactory getMalfunctionFactory() {
		return malfunctionFactory;
	}
	
	/**
	 * Get the historical event manager.
	 * @return historical event manager
	 */
	public HistoricalEventManager getEventManager() {
		return eventManager;
	}
	
	/**
	 * Get the medical manager.
	 * @return medical manager
	 */
	public MedicalManager getMedicalManager() {
		return medicalManager;
	}
    
    /**
     * Get the scientific study manager.
     * @return scientific study manager.
     */
    public ScientificStudyManager getScientificStudyManager() {
        return scientificStudyManager;
    }
	
	/**
	 * Get the master clock.
	 * @return master clock
	 */
	public MasterClock getMasterClock() {
		return masterClock;
	}
	
	/**
	 * Checks if simulation was loaded from default save file.
	 * @return true if default load.
	 */
	public boolean isDefaultLoad() {
		return defaultLoad;
	}
}