/**
 * Mars Simulation Project
 * Simulation.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

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
import org.mars_sim.msp.core.time.UpTimer;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation implements ClockListener, Serializable {

    /** DOCME: documentation is missing */
    private static final long serialVersionUID = -631308653510974249L;
    private static Logger logger = Logger.getLogger(Simulation.class.getName());
    
    // Version string.
    public final static String VERSION = "3.02";
    
    // Default save file.
    public final static String DEFAULT_FILE = "default.sim";
    
    // Save directory
    public final static String DEFAULT_DIR = System.getProperty("user.home") + File.separator + 
            ".mars-sim" + File.separator + "saved";
    
    // Singleton instance
    private static final Simulation instance = new Simulation();
    
    // Transient data members (aren't stored in save file)
    private transient HistoricalEventManager eventManager; // All historical info.
    private transient Thread clockThread;
    private static final boolean debug = logger.isLoggable(Level.FINE);
    
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
    private boolean initialSimulationCreated = false;

    /**
     * Constructor
     */
    private Simulation() {

        initializeTransientData();
    }

    /**
     * Gets a singleton instance of the simulation.
     * @return Simulation instance
     */
    public static Simulation instance() {
        return instance;
    }

    public static void stopSimulation() {
        Simulation simulation = instance();
        simulation.defaultLoad = false;
        simulation.stop();

        // Wait until current time pulse runs it course
        // we have no idea how long it will take it to
        // run its course. But this might be enough.
        Thread.yield();
    }

    /**
     * Creates a new simulation instance.
     * @throws Exception if new simulation could not be created.
     */
    public static void createNewSimulation() {

        logger.config("Creating new simulation");
        
        Simulation simulation = instance();
        
        // Destroy old simulation.
        if (simulation.initialSimulationCreated) {
            simulation.destroyOldSimulation();
        }
        
        // Initialize intransient data members.
        simulation.initializeIntransientData();

        // Initialize transient data members.
        simulation.initializeTransientData();
        
        simulation.initialSimulationCreated = true;
    }
    
    /**
     * Destroys the current simulation to prepare for creating or loading a new simulation.
     */
    private void destroyOldSimulation() {
        malfunctionFactory.destroy();
        mars.destroy();
        missionManager.destroy();
        relationshipManager.destroy();
        medicalManager.destroy();
        masterClock.destroy();
        unitManager.destroy();
        creditManager.destroy();
        scientificStudyManager.destroy();
        eventManager.destroy();
    }

    /**
     * Initialize transient data in the simulation.
     * @throws Exception if transient data could not be loaded.
     */
    private void initializeTransientData() {
        eventManager = new HistoricalEventManager();
    }

    /**
     * Initialize intransient data in the simulation.
     * @throws Exception if intransient data could not be loaded.
     */
    private void initializeIntransientData() {
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
    public static void loadSimulation(final File file) {
        File f = file;

        logger.config("Loading simulation from " + file);

        Simulation simulation = instance();
        simulation.stop();


        // Use default file path if file is null.
        if (f == null) {
            /* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. */
            f = new File(DEFAULT_DIR, DEFAULT_FILE);
            simulation.defaultLoad = true;
        } 
        else {
            simulation.defaultLoad = false;
        }
        
        if (f.exists() && f.canRead()) {
            try {
                simulation.readFromFile(f);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        else{
            throw new IllegalStateException("Load file: " + f.getPath() + " is not accessible");
        }
    }

    private void readFromFile(File file) throws ClassNotFoundException, IOException {
        
        ObjectInputStream p = new ObjectInputStream(new FileInputStream(file));
        
        // Destroy old simulation.
        destroyOldSimulation();
        
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
        
        // Initialize transient data.
        initializeTransientData();
    }

    /**
     * Saves a simulation instance to a save file.
     * @param file the file to be saved to.
     * @throws Exception if simulation could not be saved.
     */
    public void saveSimulation(File file) throws IOException {
        logger.config("Saving simulation to " + file);

        Simulation simulation = instance();
        simulation.stop();

        // Use default file path if file is null.
		/* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. Also check if the dirs
         * exist */
        if (file == null) {
            file = new File(DEFAULT_DIR, DEFAULT_FILE);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }
        ObjectOutputStream p = null;
        try{
            p = new ObjectOutputStream(new FileOutputStream(file));
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
            p = null;
        }catch(IOException e){
            logger.log(Level.WARNING, "Could not save the simulation", e);
            throw e;
        }finally{
            if(p != null){
                p.close();
            }
        }

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
    @Override
    public void clockPulse(double time) {
        final UpTimer ut = masterClock.getUpTimer();

        ut.updateTime();
        
        if (debug) {
            logger.fine(ut.getUptime()
                    + " Master clock sending pulse to object: mars " + mars.toString());
        }
        mars.timePassing(time);
        ut.updateTime();
        if (debug) {
            logger.fine(masterClock.getUpTimer().getUptime()
                    + " Master clock sending pulse to object: missionManager " + missionManager.toString());
        }
        missionManager.timePassing(time);
        ut.updateTime();
        if (debug) {
            logger.fine(masterClock.getUpTimer().getUptime()
                    + " Master clock sending pulse to object: unitManager " + unitManager.toString());
        }
        unitManager.timePassing(time);
        ut.updateTime();
        if (debug) {
            logger.fine(masterClock.getUpTimer().getUptime()
                    + " Master clock sending pulse to object: scientificStudyManager " + scientificStudyManager);
        }
        scientificStudyManager.updateStudies();
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
