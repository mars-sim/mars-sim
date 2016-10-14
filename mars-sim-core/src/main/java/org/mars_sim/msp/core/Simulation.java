/**
 * Mars Simulation Project
 * Simulation.java
 * @version 3.1.0 2016-09-30
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
import org.reactfx.EventStreams;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

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
    public final static String TEMP_FILE = Msg.getString("Simulation.tempFile"); //$NON-NLS-1$

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

	public static long autosave_minute;// = 15;

	// Categories of loading and saving simulation
	public static final int OTHER = 0; // load other file
	public static final int SAVE_DEFAULT = 1; // save as default.sim
	public static final int SAVE_AS = 2; // save with other name
	public static final int AUTOSAVE_DEFAULT = 3; // save as default.sim
	public static final int AUTOSAVE = 4; // save with build info/date/time stamp
	
	
	public final static int NUM_THREADS = Runtime.getRuntime().availableProcessors();

	public final static String MARS_SIM_DIRECTORY = ".mars-sim";

    @SuppressWarnings("restriction")
    public final static String title = Msg.getString(
            "Simulation.title", VERSION
            + " - Build " + BUILD
            + " - Java SE " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion()
            + " - " + NUM_THREADS + " CPU thread(s)"
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
    private String loadBuild;// = "unknown";

    private String lastSave = null;
    
    // 2016-07-26 Added transient to avoid serialization error
	//private transient Timeline autosaveTimeline;
	private transient Timeline autosaveTimer;
		
    // Transient data members (aren't stored in save file)
    /** All historical info. */
    private transient HistoricalEventManager eventManager;

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

    //private SimulationConfig simulationConfig;// = SimulationConfig.instance();
	//public JConsole jc;

    /**
     * Private constructor for the Singleton Simulation. This prevents instantiation from other classes.
     * */
    private Simulation() {
    	//simulationConfig = SimulationConfig.instance();
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
        //logger.info("Simulation's createNewSimulation() is on " + Thread.currentThread().getName() + " Thread");

        isUpdating = true;

        logger.config(Msg.getString("Simulation.log.createNewSim")); //$NON-NLS-1$

        Simulation sim = instance();

        // Destroy old simulation.
        if (sim.initialSimulationCreated) {
            sim.destroyOldSimulation();
        }

        sim.initialSimulationCreated = true;

        // Initialize intransient data members.
        sim.initializeIntransientData();

        // Initialize transient data members.
        sim.initializeTransientData(); // done in the constructor already (MultiplayerClient needs HistoricalEnventManager)

        // Sleep current thread for a short time to make sure all simulation objects are initialized.
        try {
            Thread.sleep(50L);
        }
        catch (InterruptedException e) {
            // Do nothing.
        }

        isUpdating = false;
        
        //2016-09-30 Copied build version. Usable for comparison when loading a saved sim
        SimulationConfig.instance().build = Simulation.BUILD;
    }


    /**
     * Initialize transient data in the simulation.
     * @throws Exception if transient data could not be loaded.
     */
    public void initializeTransientData() {
       //logger.info("Simulation's initializeTransientData() is on " + Thread.currentThread().getName() + " Thread");
       eventManager = new HistoricalEventManager();
    }

    /**
     * Initialize intransient data in the simulation.
     * @throws Exception if intransient data could not be loaded.
     */
    // 2015-02-04 Added threading
    public void initializeIntransientData() {
        //logger.info("Simulation's initializeIntransientData() is on " + Thread.currentThread().getName() + " Thread");
        //if (eventManager == null)
        //	eventManager = new HistoricalEventManager();
        malfunctionFactory = new MalfunctionFactory(SimulationConfig.instance().getMalfunctionConfiguration());
        mars = new Mars();
        missionManager = new MissionManager();
        relationshipManager = new RelationshipManager();
        medicalManager = new MedicalManager();
        masterClock = new MasterClock();
        unitManager = new UnitManager();
		unitManager.constructInitialUnits(); // unitManager needs to be on the same thread as masterClock
		creditManager = new CreditManager();
		scientificStudyManager = new ScientificStudyManager();
		transportManager = new TransportManager();
	}


    public void runLoadConfigTask() { 	
    	startSimExecutor();
    	simExecutor.execute(new LoadConfigTask());
   	
    }

	public class LoadConfigTask implements Runnable {		
		LoadConfigTask() {}	
		public void run() {
		   	//logger.info("SimConfigTask's run() is on " + Thread.currentThread().getName());
			SimulationConfig.loadConfig();
		}
	}
	
	
	public void runStartTask(boolean autosaveDefault) { 	
    	simExecutor.execute(new StartTask(autosaveDefault));
    }

	public class StartTask implements Runnable {
		boolean autosaveDefault;
	
		StartTask(boolean autosaveDefault) {
			this.autosaveDefault = autosaveDefault;
		}
		
		public void run() {
		   	//logger.info("StartTask's run() is on " + Thread.currentThread().getName());
			start(autosaveDefault);
		}
	}
		
    /**
     * Start the simulation.
     */
    public void start(boolean autosaveDefault) {
        //logger.info("Simulation's start() -- where clockScheduler is initialized -- is on " + Thread.currentThread().getName());
        //nonJavaFX : Simulation's start() is on AWT-EventQueue-0 Thread
        //JavaFX: Simulation's start() is on pool-2-thread-1 Thread

		//SwingUtilities.invokeLater(() -> {
	    //    testConsole();
		//});

    	if (masterClock ==  null)
    		System.out.println("masterClock ==  null");
        masterClock.addClockListener(this);
        masterClock.startClockListenerExecutor();

        if (clockScheduler == null || clockScheduler.isShutdown() || clockScheduler.isTerminated()) {
	        
        	clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
/*        	
        	if (NUM_THREADS <= 3)
        		clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);// newSingleThreadExecutor();// newCachedThreadPool(); //
        	else if (NUM_THREADS <= 6)
        		clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);// newSingleThreadExecutor();// newCachedThreadPool(); //
        	else 
        		clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);// newSingleThreadExecutor();// newCachedThreadPool(); //
*/   
        	clockScheduler.execute(masterClock.getClockThreadTask());
       }
 
        //2016-04-28 Relocated the autosave timer from MainMenu to here
		startAutosaveTimer(autosaveDefault);
		
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

        Simulation sim = instance();
        sim.stop();

        // Use default file path if file is null.
        if (f == null) {
        	//logger.info("Yes file is null");
            /* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. */
            f = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
        	//logger.info("file is " + f);
            sim.defaultLoad = true;
        }
        else {
            sim.defaultLoad = false;
        }

        if (f.exists() && f.canRead()) {
            logger.info(Msg.getString("Simulation.log.loadSimFrom", f)); //$NON-NLS-1$

            try {

                sim.readFromFile(f);
                             
            } catch (ClassNotFoundException e2) {
            	logger.log(Level.SEVERE, "Quitting mars-sim with Class Not Found Exception when loading the simulation! " + " : " + e2.getMessage());
    	        Platform.exit();
    	        System.exit(1);  
    	        
            } catch (IOException e1) {
            	logger.log(Level.SEVERE, "Quitting mars-sim with I/O error when loading the simulation! " + " : " + e1.getMessage());
    	        Platform.exit();
    	        System.exit(1);

	        } catch (Exception e0) {
	        	logger.log(Level.SEVERE, "Quitting mars-sim. Could not create a new simulation " + " : " + e0.getMessage());
    	        Platform.exit();
    	        System.exit(1);
	        }
            
        }
        
        else{
        	logger.log(Level.SEVERE, "Quitting mars-sim. The saved sim cannot be read or found. ");
            //throw new IllegalStateException(Msg.getString("Simulation.log.fileNotAccessible") + //$NON-NLS-1$ //$NON-NLS-2$
            //        f.getPath() + " is not accessible");
            Platform.exit();
            System.exit(1);
            
        }

    }

    /**
     * Reads a serialized simulation from a file.
     * @param file the saved serialized simulation.
     * @throws ClassNotFoundException if error reading serialized classes.
     * @throws IOException if error reading from file.
     */
   	// 2016-03-22 Replace gzip with xz compression (based on LZMA2)
    private synchronized void readFromFile(File file) throws ClassNotFoundException, IOException {
    	//logger.info("Simulation : running readFromFile()");      
        byte[] buf = new byte[8192];
        ObjectInputStream ois = null;
        FileInputStream in = null;

        boolean no_go = false;
        
        try {
        	//System.out.println("Simulation : inside try. starting decompressing");
            in = new FileInputStream(file);

            // Since XZInputStream does some buffering internally
            // anyway, BufferedInputStream doesn't seem to be
            // needed here to improve performance.
            // in = new BufferedInputStream(in);
        	XZInputStream xzin = new XZInputStream(in, 256 * 1024);              
            // limit memory usage to 256 MB
           
            // define a temporary uncompressed file
            File uncompressed = new File(DEFAULT_DIR, TEMP_FILE);
            // Decompress a xz compressed file 
            FileOutputStream fos = new FileOutputStream(uncompressed);                

            int size;
            while ((size = xzin.read(buf)) != -1)
            	fos.write(buf, 0, size);
   
            ois = new ObjectInputStream(new FileInputStream(uncompressed));
                 
            // Load intransient objects.
            SimulationConfig.setInstance((SimulationConfig) ois.readObject());	
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
  
	        if (ois != null) {
	            ois.close();
	        }
            // Close FileInputStream (directly or indirectly via XZInputStream, it doesn't matter).
            in.close();
            xzin.close();
            fos.close();     
            uncompressed.delete();
   
        	// Compute the size of the saved sim
			fileSize = (file.length() / 1000D);
			String fileStr = "";
			//System.out.println("file size is " + fileSize);
			if (fileSize < 1000)
				fileStr = Math.round(fileSize*10.0)/10.0 + " KB";
			else
				fileStr = Math.round(fileSize)/10.0 + " MB";
			
			//logger.info("The saved sim has a size of "+ fileStr);
		
            loadBuild = SimulationConfig.instance().build;
        	if (loadBuild == null)
        		loadBuild = "unknown";
            
			logger.info("  Build : " + loadBuild + "   Size : "+ fileStr);
        	
        	if (instance().BUILD.equals(loadBuild))
        		logger.info("Running mars-sim and loading a saved sim in Build " + loadBuild);
        	else
        		logger.warning("Running mars-sim in Build " + Simulation.BUILD + " but loading a saved sim in Build " + loadBuild);
 
        } catch (FileNotFoundException e) {
        	logger.log(Level.SEVERE, "Quitting mars-sim since " + file + " cannot be found : ", e.getMessage());
            Platform.exit();
            System.exit(1);

        } catch (EOFException e) {
        	logger.log(Level.SEVERE, "Quitting mars-sim with Unexpected End of File error on " + file + " : " + e.getMessage());
            Platform.exit();
            System.exit(1);

        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Quitting mars-sim with I/O rrror when decompressing " + file + " : " + e.getMessage());
            Platform.exit();
            System.exit(1);           
        	
	    } catch (NullPointerException e) {
	    	logger.log(Level.SEVERE, "Quitting mars-sim with null pointer error when loading " + file + " : " + e.getMessage());
	        Platform.exit();
	        System.exit(1);           

	    } catch (Exception e) {
	    	no_go = true;
	    	logger.log(Level.SEVERE, "Quitting mars-sim with errors when loading " + file + " : " + e.getMessage());
	        Platform.exit();
	        System.exit(1);           
	    } 
        
        if (!no_go) {
	        // Initialize transient data.
	        instance().initializeTransientData();
	
	        instance().initialSimulationCreated = true;
	        
	        isUpdating = false;        
        }
	}


    /**
     * Saves a simulation instance to a save file.
     * @param file the file to be saved to.
     * @throws Exception if simulation could not be saved.
     */
    public synchronized void saveSimulation(int type, File file) throws IOException {
        logger.config(Msg.getString("Simulation.log.saveSimTo") + file); //$NON-NLS-1$
    	//System.out.println("file is " + file);
        
    	// 2015-12-18 Check if it was previously on pause
		boolean previous = masterClock.isPaused();
		// Pause simulation.
		if (!previous) {
			masterClock.setPaused(true);
			//System.out.println("previous2 is false. Paused sim");
		}

        Simulation sim = instance();
        sim.halt();

		//2016-09-15 Added lastSave
    	lastSave = new SystemDateTime().getDateTimeStr();

        // 2016-09-22 Use type to differentiate in what name/dir it is saved
        if (type == SAVE_DEFAULT) {
            file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
            logger.info("Saving as " + DEFAULT_FILE + DEFAULT_EXTENSION);
 
        }
        
        else if (type == SAVE_AS) {
        	//System.out.println("file is " + file);
        	String f = file.getName();
        	//System.out.println("f is " + f);
        	String dir = file.getParentFile().getAbsolutePath();
        	//System.out.println("dir is " + dir);
        	if (!f.contains(".sim"))
        		file = new File(dir, f + DEFAULT_EXTENSION);
        	//System.out.println("file is " + file);
        	logger.info("Saving as " + file);
        }
        
        else if (type == AUTOSAVE_DEFAULT) {
            file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
            logger.info("Autosaving as " + DEFAULT_FILE + DEFAULT_EXTENSION);
        	
        }

        else if (type == AUTOSAVE) {
            String autosaveFilename = lastSave
            		+ "_sol" + masterClock.getMarsClock().getSolElapsedFromStart() 
            		+ "_r" + BUILD
            		+ DEFAULT_EXTENSION;
            file = new File(AUTOSAVE_DIR, autosaveFilename);
            logger.info("Autosaving as " + autosaveFilename);
        	
        }

        // if the autosave/default save directory does not exist, create one now
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        ObjectOutputStream oos = null;
        FileInputStream fis = null;
        FileOutputStream fos = new FileOutputStream(file);
        XZOutputStream xzout = null;
        File uncompressed = null;
        
        try {
        	
            // 2016-03-22 Replace gzip with xz compression (based on LZMA2)
            // See (1) http://stackoverflow.com/questions/5481487/how-to-use-lzma-sdk-to-compress-decompress-in-java
            //     (2) http://tukaani.org/xz/xz-javadoc/
            
            // STEP 1: combine all objects into one single uncompressed file, namely "default"
            uncompressed = new File(DEFAULT_DIR, TEMP_FILE);
            
            // if the default save directory does not exist, create one now
            if (!uncompressed.getParentFile().exists()) {
            	uncompressed.getParentFile().mkdirs();
            }
                     
        	oos = new ObjectOutputStream(new FileOutputStream(uncompressed));           
 
            // Store the in-transient objects.
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
            //oos.close();
            
            // STEP 2: convert the uncompressed file into a fis
            fis = new FileInputStream(uncompressed);           
			LZMA2Options options = new LZMA2Options();		
			// Set to 6. For mid sized archives (>8mb), 7 works better. 		
			options.setPreset(6); 
			xzout = new XZOutputStream(fos, options);
			
			// STEP 3: set up buffer and create outxz and save as a .sim file
			byte[] buf = new byte[8192];
			int size;
			while ((size = fis.read(buf)) != -1)
			   xzout.write(buf, 0, size);	
			
			xzout.finish();				
			

        } catch (IOException e){
            logger.log(Level.SEVERE, Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$
            throw e;
            
        //} finally {
        //    if (oos != null) {
        //        oos.close();
        //    }
        //}
        }

        //uncompressed.delete(); // cannot be deleted;
		uncompressed = null;
        //fis.close(); // fis closed automatically
        //fos.close(); // fos closed automatically
        if (oos != null) {
            oos.close();
        } 
		xzout.close();
		 
        sim.proceed();

		// 2015-12-18 Check if it was previously on pause       
		boolean now = masterClock.isPaused();
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



    /**
     * Ends the current simulation
     */
    public void endSimulation() {
        instance().defaultLoad = false;
        instance().stop();
        masterClock.endClockListenerExecutor();
        clockScheduler.shutdownNow();
    }

    public void endMasterClock() {
    	masterClock = null;
    }

    /**
     * Stop the simulation.
     */
    // called when loading a sim
    public void stop() {
        if (masterClock != null) {
            //simExecutor.shutdown();
            masterClock.stop();
            masterClock.removeClockListener(this);
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
	public void startAutosaveTimer(boolean autosaveDefault) {
        //logger.info("Simulation's startAutosaveTimer() is on " + Thread.currentThread().getName());
		autosave_minute = SimulationConfig.instance().getAutosaveInterval();
		// Note: should call masterClock's saveSimulation() to first properly interrupt the masterClock, 
		// instead of directly call saveSimulation() here in Simulation
		
		if (autosaveTimer != null) {
			autosaveTimer.stop();
			autosaveTimer = null;
		}
		
		if (autosaveDefault) {
			autosaveTimer = new Timeline(
				new KeyFrame(Duration.seconds(60 * autosave_minute),
						ae -> masterClock.saveSimulation(AUTOSAVE_DEFAULT, null)));
			//autosaveTimer = FxTimer.runLater(
    		//		java.time.Duration.ofMinutes(60 * autosave_minute),
    		//        () -> masterClock.saveSimulation(null));
			//EventStreams.ticks(java.time.Duration.ofMinutes(60 * autosave_minute))
	        //.subscribe(tick -> masterClock.saveSimulation(null));
		}
		else {
			autosaveTimer = new Timeline(
				new KeyFrame(Duration.seconds(60 * autosave_minute),
						ae -> masterClock.saveSimulation(AUTOSAVE, null)));
			//autosaveTimer = FxTimer.runLater(
    		//		java.time.Duration.ofMinutes(60 * autosave_minute),
    		//        () -> masterClock.autosaveSimulation());
			//EventStreams.ticks(java.time.Duration.ofMinutes(60 * autosave_minute))
	        //.subscribe(tick -> masterClock.autosaveSimulation());
		}
		
		// Note1: Infinite Timeline might result in a memory leak if not stopped properly.
		// Note2: All the objects with animated properties would NOT be garbage collected.
		
		autosaveTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
		autosaveTimer.play();

	}

	/**
	 * Gets the Timer instance of the autosave timer.
	 * @return autosaveTimeline
	 */
	public Timeline getAutosaveTimer() {
		return autosaveTimer;
	}
	
	public String getLastSave() {
		if (lastSave == null || lastSave.equals(""))
			return "None";
		else {
			int l = lastSave.length();
			String s = lastSave.substring(l-8, l);
			String new_s = s.substring(0, 2) 
					+ ":" + s.substring(2, 4) 
					//+ ":" + s.substring(4, 6) 
					+ " "
					+ s.substring(6, 8)
					+ " (local time)";
			return new_s;	
		}
	}
	
    /**
     * Destroys the current simulation to prepare for creating or loading a new simulation.
     */
	
    public void destroyOldSimulation() {
    	//logger.info("starting Simulation's destroyOldSimulation()");

		autosaveTimer = null;

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