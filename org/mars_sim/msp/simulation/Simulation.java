/**
 * Mars Simulation Project
 * Simulation.java
 * @version 2.77 2004-09-01
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation;

import java.io.*;
import org.mars_sim.msp.simulation.events.HistoricalEventManager;
import org.mars_sim.msp.simulation.malfunction.MalfunctionFactory;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.ai.job.JobManager;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.person.medical.MedicalManager;
import org.mars_sim.msp.simulation.time.*;

/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation implements Serializable {

	// Default save file.
	public final static String DEFAULT_FILE = "default.sim";
	
	// Save directory
	public final static String DEFAULT_DIR = "saved";

	// Singleton instance
	private static final Simulation instance = new Simulation();
	
	// Transient data members (aren't stored in save file)
	private transient MalfunctionFactory malfunctionFactory; // The malfunction factory
	private transient HistoricalEventManager eventManager; // All historical info.
	private transient SimulationConfig simConfig; // The simulation configuration.
	private transient Thread clockThread;
	
	// Intransient data members (stored in save file)
	private Mars mars; // Planet Mars
	private UnitManager unitManager; // Manager for all units in simulation.
	private MissionManager missionManager; // Mission controller
	private RelationshipManager relationshipManager; // Manages all personal relationships.
	private MedicalManager medicalManager; // Medical complaints
	private JobManager jobManager; // Job manager
	private MasterClock masterClock; // Master clock for the simulation.

	/**
	 * Constructor
	 */
	private Simulation() {
		
		try {
			// Initialize transient data members.
			initializeTransientDate();
		}
		catch (Exception e) {
			System.err.println("Simulation could not be created: " + e.getMessage());
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
		simulation.stop();
		
		try {
			// Initialize transient data members to reload configuration.
			simulation.initializeTransientDate();
			
			// Initialize intransient data members.
			simulation.initializeIntransientData();
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
	private void initializeTransientDate() throws Exception {
		simConfig = new SimulationConfig();
		malfunctionFactory = new MalfunctionFactory(simConfig.getMalfunctionConfiguration());
		eventManager = new HistoricalEventManager();
	}
	
	/**
	 * Initialize intransient data in the simulation.
	 * @throws Exception if intransient data could not be loaded.
	 */
	private void initializeIntransientData() throws Exception {
		mars = new Mars();
		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		jobManager = new JobManager();
		masterClock = new MasterClock();
		unitManager = new UnitManager();
		unitManager.constructInitialUnits();
	}
	
	/**
	 * Loads a simulation instance from a save file.
	 * @param file the file to be loaded from.
	 * @throws Exception if simulation could not be loaded.
	 */
	public void loadSimulation(File file) throws Exception {
		Simulation simulation = instance();
		simulation.stop();
		
		// Use default file path if file is null.
		if (file == null) file = new File(DEFAULT_DIR + File.separator + DEFAULT_FILE);
		
		try {
			ObjectInputStream p = new ObjectInputStream(new FileInputStream(file));
			
			// Load intransient objects.
			mars = (Mars) p.readObject();
			mars.initializeTransientData();
			missionManager = (MissionManager) p.readObject();
			relationshipManager = (RelationshipManager) p.readObject();
			medicalManager = (MedicalManager) p.readObject();
			jobManager = (JobManager) p.readObject();
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
		Simulation simulation = instance();
		simulation.stop();
		
		// Use default file path if file is null.
		if (file == null) file = new File(DEFAULT_DIR + File.separator + DEFAULT_FILE);
		
		ObjectOutputStream p = new ObjectOutputStream(new FileOutputStream(file));
			
		// Store the intransient objects.
		p.writeObject(mars);
		p.writeObject(missionManager);
		p.writeObject(relationshipManager);
		p.writeObject(medicalManager);
		p.writeObject(jobManager);
		p.writeObject(unitManager);
		p.writeObject(masterClock);
			
		p.flush();
		p.close();
		
		simulation.start();
	}
	
	/**
	 * Gets the simulation configuration DOM document.
	 * @return config doc.
	 */
	public SimulationConfig getSimConfig() {
		return simConfig;
	}
	
	/**
	 * Start the simulation.
	 */
	public void start() {
		if (clockThread == null) {
			clockThread = new Thread(masterClock, "Master Clock"); 
			clockThread.start();
		}
	}

	/**
	 * Stop the simulation.
	 */
	public void stop() {
		if (masterClock != null) masterClock.stop();
		clockThread = null;
	}
	
	/** 
	 * Clock pulse from master clock
	 * @param time amount of time passing (in millisols)
	 */
	public void clockPulse(double time) {
		try {
			mars.timePassing(time);
			unitManager.timePassing(time);
		}
		catch (Exception e) {
			System.err.println("Simulation.clockPulse(): " + e.getMessage());
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
	 * Get the job manager.
	 * @return job manager
	 */
	public JobManager getJobManager() {
		return jobManager;
	}
	
	/**
	 * Get the master clock.
	 * @return master clock
	 */
	public MasterClock getMasterClock() {
		return masterClock;
	}
}