/**
 * Mars Simulation Project
 * Simulation.java
 * @version 3.08 2015-03-27

 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.medical.MedicalManager;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.SystemDateTime;
import org.mars_sim.msp.core.time.UpTimer;
/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation
implements ClockListener, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = -631308653510974249L;

    private static Logger logger = Logger.getLogger(Simulation.class.getName());

    /** Version string. */
    public final static String VERSION = Msg.getString("Simulation.version"); //$NON-NLS-1$

    /** Build string. */
    public final static String BUILD_VERSION = Msg.getString("Simulation.version.build"); //$NON-NLS-1$

    /** Default save filename. */
    public final static String DEFAULT_FILE = Msg.getString("Simulation.defaultFile"); //$NON-NLS-1$

    /** Default save filename extension. */
    public final static String DEFAULT_EXTENSION = Msg.getString("Simulation.defaultFile.extension"); //$NON-NLS-1$

    /** Save directory. */
    public final static String DEFAULT_DIR =
            System.getProperty("user.home") + //$NON-NLS-1$
            File.separator +
            Msg.getString("Simulation.defaultFolder") + //$NON-NLS-1$
            File.separator +
            Msg.getString("Simulation.defaultDir"); //$NON-NLS-1$

    // 2015-01-08 Added autosave
    /** Autosave directory. */
    public final static String AUTOSAVE_DIR =
            System.getProperty("user.home") + //$NON-NLS-1$
            File.separator +
            Msg.getString("Simulation.defaultFolder") + //$NON-NLS-1$
            File.separator +
            Msg.getString("Simulation.defaultDir.autosave"); //$NON-NLS-1$

    @SuppressWarnings("restriction")
    public final static String WINDOW_TITLE = Msg.getString(
            "Simulation.title", Simulation.VERSION
            + " - Build " + Simulation.BUILD_VERSION
            + " - Java SE " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion()
            + " - " + Runtime.getRuntime().availableProcessors() + " CPU thread(s)"
            ); //$NON-NLS-1$

    private static final boolean debug = logger.isLoggable(Level.FINE);

    /** Flag to indicate that a new simulation is being created or loaded. */
    private static boolean isUpdating = false;

    private double fileSize;
    // 2014-12-26 Added useGUI
    /** true if displaying graphic user interface. */
    private static boolean useGUI = true;

    private boolean defaultLoad = false;

    private boolean initialSimulationCreated = false;

    /** Eager Initialization Singleton instance. */
    // private static final Simulation instance = new Simulation();
    /**
     * Gets a Eager Initialization Singleton instance of the simulation.
     * @return Simulation instance
     */
    //public static Simulation instance() {
    //    return instance;
    //}

    /**
     * Creates an inner static helper class for Bill Pugh Singleton Pattern
     * Note: as soon as the instance() method is called the first time, the class
     * is loaded into memory and an instance gets created.
     * Advantage: it supports multiple threads calling instance() simultaneously with
     * no synchronized keyword needed (which slows down the VM)
     */
    private static class SingletonHelper{
    	private static final Simulation INSTANCE = new Simulation();
    }

    /**
     * Gets a Bill Pugh Singleton instance of the simulation.
     * @return Simulation instance
     */
    public static Simulation instance() {
    	return SingletonHelper.INSTANCE;
    }

    /**
     * Prevents the singleton pattern from being destroyed
     * at the time of serialization
     * @return Simulation instance
     */
    protected Object readResolve() throws ObjectStreamException {
    	return instance();
    }

    // Transient data members (aren't stored in save file)

    /** All historical info. */
    private transient HistoricalEventManager eventManager;

    //private transient Thread clockThread;
    //private transient ThreadPoolExecutor clockExecutor;
    //private transient ThreadPoolExecutor clockScheduler; //
    private transient PausableThreadPoolExecutor clockScheduler;

    private transient ThreadPoolExecutor managerExecutor;

    private transient ExecutorService simExecutor;

    // Intransient data members (stored in save file)
    /** Planet Mars. */
    private Mars mars;
    /** The malfunction factory. */
    private MalfunctionFactory malfunctionFactory;
    /** Manager for all units in simulation. */
    private UnitManager unitManager;
    /** Mission controller. */
    private MissionManager missionManager;
    /** Manages all personal relationships. */
    private RelationshipManager relationshipManager;
    /** Medical complaints. */
    private MedicalManager medicalManager;
    /** Master clock for the simulation. */
    private MasterClock masterClock;
    /** Manages trade credit between settlements. */
    private CreditManager creditManager;
    /** Manages scientific studies. */
    private ScientificStudyManager scientificStudyManager;
    /** Manages transportation of settlements and resupplies from Earth. */
    private TransportManager transportManager;


    /** constructor. */
    public Simulation() {
        //logger.info("Simulation's constructor is on " + Thread.currentThread().getName() + " Thread");
        initializeTransientData();
    }

    public void startSimExecutor() {
    	simExecutor = Executors.newSingleThreadExecutor();
    }

    public ExecutorService getSimExecutor() {
    	return simExecutor;
    }

    /**
     * Checks if the simulation is in a state of creating a new simulation or
     * loading a saved simulation.
     * @return true is simulation is in updating state.
     */
    public static boolean isUpdating() {
        return isUpdating;
    }


    /**
     * Creates a new simulation instance.
     * @throws Exception if new simulation could not be created.
     */
    public static void createNewSimulation() {
        //logger.info("Simulation's createNewSimulation() is on " + Thread.currentThread().getName() + " Thread");

        isUpdating = true;

        logger.config(Msg.getString("Simulation.log.createNewSim")); //$NON-NLS-1$

        Simulation simulation = instance();

        // Destroy old simulation.
        if (simulation.initialSimulationCreated) {
            simulation.destroyOldSimulation();
        }

        // Initialize intransient data members.
        simulation.initializeIntransientData();

        // Initialize transient data members.
        //simulation.initializeTransientData(); // done in the constructor already (MultiplayerClient needs HistoricalEnventManager)

        // Sleep current thread for a short time to make sure all simulation objects are initialized.
        try {
            Thread.sleep(50L);
        }
        catch (InterruptedException e) {
            // Do nothing.
        }

        simulation.initialSimulationCreated = true;

        isUpdating = false;
    }


    /**
     * Initialize transient data in the simulation.
     * @throws Exception if transient data could not be loaded.
     */
    private void initializeTransientData() {
        //logger.info("Simulation's initializeTransientData() is on " + Thread.currentThread().getName() + " Thread");

    	eventManager = new HistoricalEventManager();
    }

    /**
     * Initialize intransient data in the simulation.
     * @throws Exception if intransient data could not be loaded.
     */
    // 2015-02-04 Added threading
    private void initializeIntransientData() {
        //logger.info("Simulation's initializeIntransientData() is on " + Thread.currentThread().getName() + " Thread");

        if (eventManager == null)
        	eventManager = new HistoricalEventManager();

        if (managerExecutor == null || managerExecutor.isShutdown()) {
            managerExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(); //newFixedThreadPool();

            malfunctionFactory = new MalfunctionFactory(SimulationConfig.instance().getMalfunctionConfiguration());
            managerExecutor.execute(malfunctionFactory);

            mars = new Mars();

            missionManager = new MissionManager();

            relationshipManager = new RelationshipManager();
            managerExecutor.execute(relationshipManager);

            medicalManager = new MedicalManager();
            managerExecutor.execute(medicalManager);

            masterClock = new MasterClock();

            unitManager = new UnitManager();
            unitManager.constructInitialUnits(); // unitManager needs to be on the same thread as masterClock

            creditManager = new CreditManager();
            managerExecutor.execute(creditManager);

            scientificStudyManager = new ScientificStudyManager();
            managerExecutor.execute(scientificStudyManager);

            transportManager = new TransportManager();
            managerExecutor.execute(transportManager);

        }

        /*
		try {
			malfunctionFactory.join();
			missionManager.join();
			relationshipManager.join();
			medicalManager.join();
			creditManager.join();
			scientificStudyManager.join();
			transportManager.join();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         */

    }


    /**
     *
     * Start the simulation.
     */
    public void start() {
        //logger.info("Simulation's start() is on " + Thread.currentThread().getName() + " Thread");

        masterClock.addClockListener(this);
        masterClock.startClockListenerExecutor();

        if (clockScheduler == null || clockScheduler.isShutdown() || clockScheduler.isTerminated()) {
	        //clockExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);// newCachedThreadPool(); //
	        // 2015-06-24 Replaced with PausableThreadPoolExecutor
        	clockScheduler =  new PausableThreadPoolExecutor(1, 5);
        	//clockScheduler = (ThreadPoolExecutor) Executors.newCachedThreadPool(); // newSingleThreadExecutor(); newFixedThreadPool(1); //newScheduledThreadPool(1); // newSingleThreadScheduledExecutor(); //
        	//clockScheduler.scheduleAtFixedRate(masterClock.getClockThreadTask(), 0, (long) 16.66667, TimeUnit.MILLISECONDS);
        	//logger.info("Simulation's start() : clockExecutor was null. just made one");
	        clockScheduler.execute(masterClock.getClockThreadTask());
	        //logger.info("Simulation : just loading clockExecutor for masterClock");
        }
        //else if (clockExecutor.isShutdown() || clockExecutor.isTerminated()) {
	    //    logger.info("Simulation : clockExecutor was shutdown or terminated. execute next");
	    //    clockExecutor.submit(masterClock.getClockThreadTask());
	    //    logger.info("Simulation : just loading clockExecutor for masterClock");
        //}
    }


    /**
     * Stop the simulation.
     */
    // called when loading a sim
    public void stop() {
        /*		if (masterClock != null) {
			masterClock.stop();
			masterClock.removeClockListener(this);
		}
		clockThread = null;
         */
        if (masterClock != null) {
            //executor.shutdown();
            masterClock.stop();
            masterClock.removeClockListener(this);
        }
        //executor = null;
    }

    /*
     * Obtains the size of the file
     * @return fileSize in megabytes
     */
    public double getFileSize() {
    	return fileSize;
    }

    /**
     * Loads a simulation instance from a save file.
     * @param file the file to be loaded from.
     * @throws Exception if simulation could not be loaded.
     */
    public void loadSimulation(final File file) {
        //logger.info("Simulation's loadSimulation() is on " + Thread.currentThread().getName() + " Thread");
        isUpdating = true;

        File f = file;

        logger.config(Msg.getString("Simulation.log.loadSimFrom") + file); //$NON-NLS-1$

        Simulation simulation = instance();
        simulation.stop();

        // Use default file path if file is null.
        if (f == null) {
            /* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. */
            f = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
            simulation.defaultLoad = true;
        }
        else {
            simulation.defaultLoad = false;
        }

        if (f.exists() && f.canRead()) {
            try {
    			fileSize = (f.length() / 1024D / 1024D);
                simulation.readFromFile(f);

            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        else{
            throw new IllegalStateException(Msg.getString("Simulation.log.fileNotAccessible") + //$NON-NLS-1$ //$NON-NLS-2$
                    f.getPath() + " is not accessible");
        }

        isUpdating = false;
    }


    /**
     * Ends the current simulation
     */
    public void endSimulation() {
        Simulation sim = instance();
        sim.defaultLoad = false;
        sim.stop();

        masterClock.endClockListenerExecutor();
        clockScheduler.shutdownNow();
        if (managerExecutor != null) {
            managerExecutor.shutdownNow();
        }
        // Wait until current time pulse runs its course
        // we have no idea how long it will take it to
        // run its course. But this might be enough.
        //Thread.yield();
        //worker.shutdown();
        //masterClock = null; // not an option
        //masterClock.exitProgram(); // not exiting main menu

    }

    public void endMasterClock() {
    	masterClock = null;
    }

    /**
     * Reads a serialized simulation from a file.
     * @param file the saved serialized simulation.
     * @throws ClassNotFoundException if error reading serialized classes.
     * @throws IOException if error reading from file.
     */
    private void readFromFile(File file) throws ClassNotFoundException, IOException {

        //ObjectInputStream p = new ObjectInputStream(new FileInputStream(file));
        FileInputStream fin = new FileInputStream(file);
        GZIPInputStream gis = new GZIPInputStream(fin);
        ObjectInputStream ois = new ObjectInputStream(gis);

        // Destroy old simulation.
        if (instance().initialSimulationCreated) {
            destroyOldSimulation();
        }

        // Load intransient objects.
        SimulationConfig.setInstance((SimulationConfig) ois.readObject());
        malfunctionFactory = (MalfunctionFactory) ois.readObject();
        mars = (Mars) ois.readObject();
        mars.initializeTransientData();
        missionManager = (MissionManager) ois.readObject();
        relationshipManager = (RelationshipManager) ois.readObject();
        medicalManager = (MedicalManager) ois.readObject();
        scientificStudyManager = (ScientificStudyManager) ois.readObject();
        transportManager = (TransportManager) ois.readObject();
        creditManager = (CreditManager) ois.readObject();
        unitManager = (UnitManager) ois.readObject();
        masterClock = (MasterClock) ois.readObject();
        ois.close();

        // Initialize transient data.
        initializeTransientData();

        instance().initialSimulationCreated = true;
    }

    /**
     * Saves a simulation instance to a save file.
     * @param file the file to be saved to.
     * @throws Exception if simulation could not be saved.
     */
    public void saveSimulation(File file, boolean isAutosave) throws IOException {
        //logger.config(Msg.getString("Simulation.log.saveSimTo") + file); //$NON-NLS-1$

        Simulation simulation = instance();
        simulation.pause();

        // Use default file path if file is null.
        /* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. Also check if the dirs
         * exist */
        if (file == null) {
            // 2015-01-08 Added isAutosave
            if (isAutosave) {
                String autosaveFilename = new SystemDateTime().getDateTimeStr() + "_Build_" + BUILD_VERSION + DEFAULT_EXTENSION;
                file = new File(AUTOSAVE_DIR, autosaveFilename);
                logger.info("Autosaving into " + autosaveFilename);
            }

            else
                file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);

            // if the autosave directory does not exist, create one now
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

        }

        //else if file != null
            // file should already have the correct dir, name and extension, no need to change it



        //ObjectOutputStream p = null;
        ObjectOutputStream oos = null;

        try {
            //oos = new ObjectOutputStream(new FileOutputStream(file));
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            oos = new ObjectOutputStream(gz);

            // Store the intransient objects.
            oos.writeObject(SimulationConfig.instance());
            oos.writeObject(malfunctionFactory);
            oos.writeObject(mars);
            oos.writeObject(missionManager);
            oos.writeObject(relationshipManager);
            oos.writeObject(medicalManager);
            oos.writeObject(scientificStudyManager);
            oos.writeObject(transportManager);
            oos.writeObject(creditManager);
            oos.writeObject(unitManager);
            oos.writeObject(masterClock);

            oos.flush();
            oos.close();
            oos = null;
        } catch (IOException e){
            logger.log(Level.WARNING, Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$
            throw e;
        } finally {
            if (oos != null) {
                oos.close();
            }
        }

        simulation.unpause();
    }

    public void pause() {
        if (masterClock != null) {
            masterClock.stop();
            masterClock.setPaused(true);
            masterClock.removeClockListener(this);
        }
    }

    public void unpause() {
        if (clockScheduler != null) {
            masterClock.addClockListener(this);
            masterClock.setPaused(false);
            masterClock.restart();
        }
    }

    /**
     * Clock pulse from master clock
     * @param time amount of time passing (in millisols)
     */
    @Override
    public void clockPulse(double time) {
        final UpTimer ut = masterClock.getUpTimer();
        if (!masterClock.isPaused()) {

            ut.updateTime();

            if (debug) {
                logger.fine(
                        Msg.getString(
                                "Simulation.log.clockPulseMars", //$NON-NLS-1$
                                ut.getUptime(),
                                mars.toString()
                                )
                        );
            }
            mars.timePassing(time);
            ut.updateTime();

            if (debug) {
                logger.fine (
                        Msg.getString(
                                "Simulation.log.clockPulseMissionManager", //$NON-NLS-1$
                                masterClock.getUpTimer().getUptime(),
                                missionManager.toString()
                                )
                        );
            }
            missionManager.timePassing(time);
            ut.updateTime();

            if (debug) {
                logger.fine(
                        Msg.getString(
                                "Simulation.log.clockPulseUnitManager", //$NON-NLS-1$
                                masterClock.getUpTimer().getUptime(),
                                unitManager.toString()
                                )
                        );
            }
            unitManager.timePassing(time);
            ut.updateTime();

            if (debug) {
                logger.fine(
                        Msg.getString(
                                "Simulation.log.clockPulseScientificStudyManager", //$NON-NLS-1$
                                masterClock.getUpTimer().getUptime(),
                                scientificStudyManager.toString()
                                )
                        );
            }
            scientificStudyManager.updateStudies();
            ut.updateTime();


            if (debug) {
                logger.fine(
                        Msg.getString(
                                "Simulation.log.clockPulseTransportManager", //$NON-NLS-1$
                                masterClock.getUpTimer().getUptime(),
                                transportManager.toString()
                                )
                        );
            }
            transportManager.timePassing(time);

        }
    }

    @Override
    public void pauseChange(boolean isPaused) {
        // Do nothing
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
     * Get the transport manager.
     * @return transport manager.
     */
    public TransportManager getTransportManager() {
        return transportManager;
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

    /**
     * Sets if simulation was loaded with GUI.
     * @param true if GUI is in use.
     */
    // 2014-12-26 Added setUseGUI()
    public static void setUseGUI(boolean value) {
        useGUI = value;
    }

    /**
     * Checks if simulation was loaded with GUI.
     * @return true if GUI is in use.
     */
    // 2014-12-26 Added getUseGUI()
    public static boolean getUseGUI() {
        return useGUI;
    }


   //public ThreadPoolExecutor getClockExecutor() {
   //	return clockExecutor;
   //}

    public PausableThreadPoolExecutor getClockScheduler() {
    	return clockScheduler;
    }


    /**
     * Destroys the current simulation to prepare for creating or loading a new simulation.
     */
    public void destroyOldSimulation() {
    	//logger.info("starting Simulation's destroyOldSimulation()");

        if (malfunctionFactory != null) {
            malfunctionFactory.destroy();
            malfunctionFactory = null;
        }

        if (mars != null) {
            mars.destroy();
            mars = null;
        }

        if (missionManager != null) {
            missionManager.destroy();
            missionManager = null;
        }

        if (relationshipManager != null) {
            relationshipManager.destroy();
            relationshipManager = null;
        }

        if (medicalManager != null) {
            medicalManager.destroy();
            medicalManager = null;
        }

        if (masterClock != null) {
            masterClock.destroy();
            masterClock = null;
        }

        if (unitManager != null) {
            unitManager.destroy();
            unitManager = null;
        }

        if (creditManager != null) {
            creditManager.destroy();
            creditManager = null;
        }

        if (scientificStudyManager != null) {
            scientificStudyManager.destroy();
            scientificStudyManager = null;
        }

        if (eventManager != null) {
            eventManager.destroy();
            eventManager = null;
        }

        if (managerExecutor != null) {
            managerExecutor.shutdownNow();
            managerExecutor = null;
        }
    	//logger.info("Simulation's destroyOldSimulation() is done");
    }

}