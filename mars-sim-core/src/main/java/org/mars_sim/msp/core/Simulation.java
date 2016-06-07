/**
 * Mars Simulation Project
 * Simulation.java
 * @version 3.08 2015-03-27

 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.awt.Color;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import javax.swing.SwingUtilities;

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
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
//import mikera.gui.Frames;
//import mikera.gui.JConsole;
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
    public final static String BUILD = Msg.getString("Simulation.build"); //$NON-NLS-1$

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

	public static double autosave_minute;// = 15;
	
    @SuppressWarnings("restriction")
    public final static String WINDOW_TITLE = Msg.getString(
            "Simulation.title", Simulation.VERSION
            + " - Build " + Simulation.BUILD
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

    /* The build version of the SimulationConfig of the loading .sim */
    private String loadBuild = "unknown";

    
	private Timeline autosaveTimeline;
	
    // Transient data members (aren't stored in save file)

    /** All historical info. */
    private transient HistoricalEventManager eventManager;

    //private transient Thread clockThread;
    //private transient ThreadPoolExecutor clockExecutor;
    //private transient ThreadPoolExecutor clockScheduler; //
    private transient ThreadPoolExecutor clockScheduler;

    //private transient ThreadPoolExecutor managerExecutor;

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

	//public JConsole jc;

    /**
     * Private constructor for the Singleton Simulation. This prevents instantiation from other classes.
     * */
    private Simulation() {
        //logger.info("Simulation's constructor is on " + Thread.currentThread().getName() + " Thread");
    	// INFO Simulation's constructor is on both JavaFX-Launcher Thread
        initializeTransientData();
    }


    /** (NOT USED) Eager Initialization Singleton instance. */
    // private static final Simulation instance = new Simulation();
    /**
     * Gets a Eager Initialization Singleton instance of the simulation.
     * @return Simulation instance
     */
    //public static Simulation instance() {
    //    return instance;
    //}

    /**
     * Initializes an inner static helper class for Bill Pugh Singleton Pattern
     * Note: as soon as the instance() method is called the first time, the class is loaded into memory and an instance gets created.
     * Advantage: it supports multiple threads calling instance() simultaneously with no synchronized keyword needed (which slows down the VM)
     * {@link SingletonHelper} is loaded on the first execution of
     * {@link Singleton#instance()} or the first access to
     * {@link SingletonHelper#INSTANCE}, not before.
     */
    private static class SingletonHelper{
    	private static final Simulation INSTANCE = new Simulation();
    }

    /**
     * Gets a Bill Pugh Singleton instance of the simulation.
     * @return Simulation instance
     */
    public static Simulation instance() {
        //logger.info("Simulation's instance() is on " + Thread.currentThread().getName() + " Thread");
        //NOTE: Simulation.instance() is accessible on any threads or by any threads
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

    public void startSimExecutor() {
        //logger.info("Simulation's startSimExecutor() is on " + Thread.currentThread().getName() + " Thread");
    	// INFO: Simulation's startSimExecutor() is on JavaFX-Launcher Thread
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
        logger.info("Simulation's createNewSimulation() is on " + Thread.currentThread().getName() + " Thread");

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
        logger.info("Simulation's initializeTransientData() is on " + Thread.currentThread().getName() + " Thread");

    	eventManager = new HistoricalEventManager();
    }

    /**
     * Initialize intransient data in the simulation.
     * @throws Exception if intransient data could not be loaded.
     */
    // 2015-02-04 Added threading
    private void initializeIntransientData() {
        logger.info("Simulation's initializeIntransientData() is on " + Thread.currentThread().getName() + " Thread");

        if (eventManager == null)
        	eventManager = new HistoricalEventManager();

        //if (managerExecutor == null || managerExecutor.isShutdown()) {
        //    managerExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(); //newSingleThreadExecutor();newFixedThreadPool();

            malfunctionFactory = new MalfunctionFactory(SimulationConfig.instance().getMalfunctionConfiguration());
            //managerExecutor.execute(malfunctionFactory);

            mars = new Mars();

            missionManager = new MissionManager();

            relationshipManager = new RelationshipManager();
            //managerExecutor.execute(relationshipManager);

            medicalManager = new MedicalManager();
            //managerExecutor.execute(medicalManager);

            masterClock = new MasterClock();

            unitManager = new UnitManager();
            unitManager.constructInitialUnits(); // unitManager needs to be on the same thread as masterClock

            creditManager = new CreditManager();
            //managerExecutor.execute(creditManager);

            scientificStudyManager = new ScientificStudyManager();
            //managerExecutor.execute(scientificStudyManager);

            transportManager = new TransportManager();
            //managerExecutor.execute(transportManager);

        //}

    }


    /**
     *
     * Start the simulation.
     */
    public void start(boolean useDefaultName) {
        //logger.info("Simulation's start() -- where clockScheduler is initialized -- is on " + Thread.currentThread().getName());
        //nonJavaFX : Simulation's start() is on AWT-EventQueue-0 Thread
        //JavaFX: Simulation's start() is on pool-2-thread-1 Thread

		//SwingUtilities.invokeLater(() -> {
	    //    testConsole();
		//});

        masterClock.addClockListener(this);
        masterClock.startClockListenerExecutor();

        if (clockScheduler == null || clockScheduler.isShutdown() || clockScheduler.isTerminated()) {
	        clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);// newSingleThreadExecutor();// newCachedThreadPool(); //
	        //logger.info("Simulation's instance() is on " + Thread.currentThread().getName() + " Thread");

	        // 2015-06-24 Replaced with PausableThreadPoolExecutor
        	//clockScheduler =  new PausableThreadPoolExecutor(1, 5);
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
        
        //2016-04-28 Relocated the autosave timer from MainMenu to here
		startAutosaveTimer(useDefaultName);
		
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
        //logger.info("Simulation's loadSimulation() is on " + Thread.currentThread().getName());
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
    			logger.info("loadSimulation() : The Saved Sim has a file size of " + Math.round(fileSize*1000.00)/1000.00 + " MB" );
                simulation.readFromFile(f);

            } catch (ClassNotFoundException ex) {
            	logger.info("Encountering ClassNotFoundException when loading the simulation!");
                throw new IllegalStateException(ex);
            } catch (IOException ex) {
            	logger.info("Encountering IOException when loading the simulation!");
            	throw new IllegalStateException(ex);
            }
        }
        else{
        	logger.info("Encountering an error when loading the simulation!");
        	logger.info("Note : you are running Build " + Simulation.BUILD + "but is loading a sim saved in Build " + loadBuild);
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
        
        //if (managerExecutor != null) {
        //    managerExecutor.shutdownNow();
        //}
        
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
    	//System.out.println("Simulation : running readFromFile()");
/*
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
        //SimulationConfig instance = (SimulationConfig) ois.readObject();
    	//SimulationConfig.setInstance(instance);

        loadBuild = SimulationConfig.instance().getBuild();
    	if (loadBuild == null)
    		loadBuild = "unknown";
    	logger.info("Running MSP Build " + Simulation.BUILD + ". Loading a sim saved in Build " + loadBuild);

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
*/
    	
    	// 2016-03-22 Replace gzip with xz compression (based on LZMA2)
        // Decompress a xz compressed file
        
        byte[] buf = new byte[8192];

        ObjectInputStream ois = null;
        FileInputStream in = null;
 
    	//System.out.println("Simulation : before calling try");

        try {
        	//System.out.println("Simulation : inside try. starting decompressing");
            in = new FileInputStream(file);

            //try {
                // Since XZInputStream does some buffering internally
                // anyway, BufferedInputStream doesn't seem to be
                // needed here to improve performance.
                // in = new BufferedInputStream(in);
            	XZInputStream xzin = new XZInputStream(in, 256 * 1024);              
                // limit memory usage to 256 MB
               
                // define a temporary uncompressed file
                File uncompressed = new File(DEFAULT_DIR, "temp");
                FileOutputStream fos = new FileOutputStream(uncompressed);                

                int size;
                while ((size = xzin.read(buf)) != -1)
                	fos.write(buf, 0, size);
   
                ois = new ObjectInputStream(new FileInputStream(uncompressed));
                
                // Destroy old simulation.
                if (instance().initialSimulationCreated) {
                    destroyOldSimulation();
                }

                // Load intransient objects.
                SimulationConfig.setInstance((SimulationConfig) ois.readObject());
                //SimulationConfig instance = (SimulationConfig) ois.readObject();
            	//SimulationConfig.setInstance(instance);

                loadBuild = SimulationConfig.instance().getBuild();
            	if (loadBuild == null)
            		loadBuild = "unknown";
            	
            	if (Simulation.BUILD.equals(loadBuild))
            		logger.info("readFromFile() : You are both running and loading a sim saved in Build " + loadBuild);
            	else
            		logger.warning("readFromFile() : You are running Build " + Simulation.BUILD + " but loading a sim saved in Build " + loadBuild);
            		
               	//System.out.println("Simulation : inside try. starting loading objects");

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
                //ois.close();
                
            //} finally {           	                           
                // Close FileInputStream (directly or indirectly
                // via XZInputStream, it doesn't matter).
                in.close();
                xzin.close();
                fos.close();                
            //}
                
        } catch (FileNotFoundException e) {
            System.err.println("XZDecDemo: Cannot open " + file + ": "
                               + e.getMessage());
            System.exit(1);

        } catch (EOFException e) {
            System.err.println("XZDecDemo: Unexpected end of input on "
                               + file);
            System.exit(1);

        } catch (IOException e) {
            System.err.println("XZDecDemo: Error decompressing from "
                               + file + ": " + e.getMessage());
            System.exit(1);
 
	    } finally {
	        if (ois != null) {
	            ois.close();
	        }

            
	    }
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

    	// 2015-10-17 Save the current pause state
		//boolean isOnPauseMode = Simulation.instance().getMasterClock().isPaused();

		// 2015-12-18 Check if it was previously on pause
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		// Pause simulation.
		if (!previous) {
			masterClock.setPaused(true);
			//System.out.println("previous2 is false. Paused sim");
		}

        Simulation simulation = instance();
        simulation.halt();

        // Use default file path if file is null.
        /* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. Also check if the dirs
         * exist */
        if (file == null) {
            // 2015-01-08 Added isAutosave
            if (isAutosave) {
                String autosaveFilename = new SystemDateTime().getDateTimeStr()
                		+ "_sol" + masterClock.getMarsClock().getTotalSol() 
                		+ "_build" + BUILD
                		+ DEFAULT_EXTENSION;
                file = new File(AUTOSAVE_DIR, autosaveFilename);
                logger.info("Autosaving " + autosaveFilename);
            }

            else {         	
                file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
                logger.info("Saving " + DEFAULT_FILE + DEFAULT_EXTENSION);
            }
                
            // if the autosave directory does not exist, create one now
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

        }

        //ObjectOutputStream p = null;
        ObjectOutputStream oos = null;

        try {
 /*
        	//oos = new ObjectOutputStream(new FileOutputStream(file));
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            oos = new ObjectOutputStream(gz);
			oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

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
 */
        	
            // 2016-03-22 Replace gzip with xz compression (based on LZMA2)
            // See (1) http://stackoverflow.com/questions/5481487/how-to-use-lzma-sdk-to-compress-decompress-in-java
            //     (2) http://tukaani.org/xz/xz-javadoc/
            
            // STEP 1: combine all objects into one single uncompressed file
            File uncompressed = new File(DEFAULT_DIR, DEFAULT_FILE);            
            oos = new ObjectOutputStream(new FileOutputStream(uncompressed));           
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
            
            // STEP 2: convert the uncompressed file into a fis
            // set up fos and outxz
            FileInputStream fis = new FileInputStream(uncompressed);          
            //File xzFilename = new File(DEFAULT_DIR, DEFAULT_FILE + ".xz");           
            FileOutputStream fos = new FileOutputStream(file);
			LZMA2Options options = new LZMA2Options();		
			options.setPreset(6); 
			// play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)		
			XZOutputStream xzout = new XZOutputStream(fos, options);
			
			// STEP 3: set up buffer and create outxz and save as a .sim file
			byte[] buf = new byte[8192];
			int size;
			while ((size = fis.read(buf)) != -1)
			   xzout.write(buf, 0, size);			
			xzout.finish();       
					
			fis.close();
			xzout.close();
			oos = null;
            //
          
        } catch (IOException e){
            logger.log(Level.WARNING, Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$
            throw e;
            
        } finally {
            if (oos != null) {
                oos.close();
            }
        }

        simulation.proceed();

        // 2015-10-17 Check if it was previously on pause
        //if (isOnPauseMode) {
        //	masterClock.setPaused(true); // do NOT use simulation.halt() or it will
     		//System.out.println("Simulation.java: Yes it was on pause and so we pause again");
     	//}

		// 2015-12-18 Check if it was previously on pause
		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				masterClock.setPaused(false);
	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				masterClock.setPaused(false);
	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}

    }

    /*
     * Stops and removes the master clock and pauses the simulation
     */
    public void halt() {
        if (masterClock != null) {
            masterClock.stop();
            masterClock.setPaused(true);
            masterClock.removeClockListener(this);
        }
    }

    /*
     * Adds and starts the master clock and unpauses the simulation
     */
    public void proceed() {
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
		//logger.info("Simulation's clockPulse() is in " + Thread.currentThread().getName() + " Thread");
		// it's in pool-4-thread-1 Thread
        UpTimer ut = null;
        if (masterClock != null)
        	ut = masterClock.getUpTimer();
        
        if (ut != null && !masterClock.isPaused()) {

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


	public ThreadPoolExecutor getClockScheduler() {
	   return clockScheduler;
	}

    //public PausableThreadPoolExecutor getClockScheduler() {
    //	return clockScheduler;
    //}

/*
    // 2015-10-08 Added testConsole() for outputting text messages to mars-simmers
    public void testConsole() {
    	if (jc == null) {
    		jc = new JConsole(60,30);
	    	jc.setCursorVisible(true);
	    	jc.setCursorBlink(true);
	    	jc.write("Welcome to Mars Simulation Project!\n\n");
	    	jc.write("Dear Mars-simmer,\n\nSee hidden logs below. Have fun!\n\n",Color.GREEN,Color.BLACK);
	    	//System.out.println("Normal output");
	    	//jc.setCursorPos(0, 0);

	    	//jc.captureStdOut();
	    	//System.out.println("Captured output");

	    	Frames.display(jc,"MSP Output Console");

	    	//jc.write("after the fact\n");
    	}
    }

    public JConsole getJConsole() {
    	return jc;
    }
*/
	
	//2015-01-07 Added startAutosaveTimer()
    //2016-04-28 Relocated the autosave timer from MainMenu to here
	public void startAutosaveTimer(boolean useDefaultName) {

		autosave_minute = SimulationConfig.instance().getAutosaveInterval();
			
		// Note: should call masterClock's saveSimulation() to first properly interrupt the masterClock, 
		// instead of directly call saveSimulation() here in Simulation
		
		if (useDefaultName) {
			autosaveTimeline = new Timeline(
				new KeyFrame(Duration.seconds(60 * autosave_minute),
						ae -> masterClock.saveSimulation(null)));
		}
		else {
			autosaveTimeline = new Timeline(
				new KeyFrame(Duration.seconds(60 * autosave_minute),
						ae -> masterClock.autosaveSimulation()));
		}
		
		// Note1: Infinite Timeline might result in a memory leak if not stopped properly.
		// Note2: All the objects with animated properties would NOT be garbage collected.
		
		autosaveTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		autosaveTimeline.play();

	}

	/**
	 * Gets the timeline instance of the autosave timer.
	 * @return autosaveTimeline
	 */
	public Timeline getAutosaveTimeline() {
		return autosaveTimeline;
	}
	
    /**
     * Destroys the current simulation to prepare for creating or loading a new simulation.
     */
    public void destroyOldSimulation() {
    	//logger.info("starting Simulation's destroyOldSimulation()");

		autosaveTimeline = null;

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

/*        
        if (managerExecutor != null) {
            managerExecutor.shutdownNow();
            managerExecutor = null;
        }
*/        
    	//logger.info("Simulation's destroyOldSimulation() is done");
    }

}