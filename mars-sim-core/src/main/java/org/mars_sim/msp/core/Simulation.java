/**
 * Mars Simulation Project
 * Simulation.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.WriteAbortedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.mars.DustStorm;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.HeatSource;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.SalvageValues;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.time.AutosaveScheduler;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.SystemDateTime;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.core.tool.CheckSerializedSize;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZFormatException;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import com.google.common.io.ByteStreams;

/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation implements ClockListener, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = -631308653510974249L;

	private static Logger logger = Logger.getLogger(Simulation.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	public enum SaveType {
		/** Do not save */
		NONE, 
		/** Save as default.sim. */
		SAVE_DEFAULT, 
		/** Save as other name. */
		SAVE_AS, 
		/** Autosave as default.sim. */
		AUTOSAVE_AS_DEFAULT, 
		/** Autosave with build info and timestamp. */
		AUTOSAVE;
	};

	/** # of thread(s). */
	public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
	/** User's home directory string. */
	public static final String USER_HOME = System.getProperty("user.home");
	/** User's mars-sim directory string. */
	public static final String MARS_SIM_DIR = ".mars-sim";
	/** User's logs directory string. */
	public static final String LOGS_DIR = "logs";
	/** OS string. */
	public static final String OS = System.getProperty("os.name"); // e.g. 'linux', 'mac os x'
	/** Version string. */
	public final static String VERSION = Msg.getString("Simulation.version"); //$NON-NLS-1$
	/** Build string. */
	public final static String BUILD = Msg.getString("Simulation.build").trim(); //$NON-NLS-1$
	/** Java version string. */
	private final static String JAVA_TAG = System.getProperty("java.version");
	// VersionInfo.getRuntimeVersion() e.g. "8.0.121-b13 (abcdefg)";																			
	/** Java version string. */
	public final static String JAVA_VERSION = (JAVA_TAG.contains("(") ? 
			JAVA_TAG.substring(0, JAVA_TAG.indexOf("(") - 1) : JAVA_TAG);
	/** Vendor string. */
	// public final static String VENDOR = System.getProperty("java.vendor");
	/** OS architecture string. */
	private final static String OS_ARCH = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");
	/** Default save filename. */
	private final static String SAVE_FILE = Msg.getString("Simulation.saveFile"); //$NON-NLS-1$
	/** Default temp filename. */
//	private final static String TEMP_FILE = Msg.getString("Simulation.tempFile"); //$NON-NLS-1$
	/** Default save filename extension. */
	private final static String SAVE_FILE_EXTENSION = Msg.getString("Simulation.saveFile.extension"); //$NON-NLS-1$
	/** JSON save filename extension. */
//	private final static String JSON_EXTENSION = Msg.getString("Simulation.jsonFile.extension"); //$NON-NLS-1$
	/** local time string */
	private final static String LOCAL_TIME = Msg.getString("Simulation.localTime"); //$NON-NLS-1$ " (Local Time) ";
	/** 2 whitespaces. */
	private final static String WHITESPACES = "  ";
	/** Console directory for saving/loading console related files. */
	public final static String CONSOLE_DIR = "/console";
	

	/** Home directory. */
	public final static String HOME_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.homeFolder");
	/** Backup directory. */
	public final static String BACKUP_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.homeFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.backupFolder"); //$NON-NLS-1$
	/** Save directory. */
	public final static String SAVE_DIR = HOME_DIR + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.saveDir"); //$NON-NLS-1$
	/** xml files directory. */
	public final static String XML_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.homeFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.xmlFolder"); //$NON-NLS-1$
	/** music files directory. */
	public final static String MUSIC_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.homeFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.musicFolder"); //$NON-NLS-1$
	/** The version.txt denotes the xml build version. */	
	public final static String VERSION_FILE = Msg.getString("Simulation.versionFile"); //$NON-NLS-1$
	/** autosave directory. */
	public final static String AUTOSAVE_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.homeFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.saveDir.autosave"); //$NON-NLS-1$

	public final static String MARS_SIM_DIRECTORY = ".mars-sim";

	public final static String title = Msg.getString("Simulation.title", VERSION + " - Build " + BUILD
	// + " - " + VENDOR
			+ " - " + OS_ARCH + " " + JAVA_VERSION + " - " + NUM_THREADS
			+ ((NUM_THREADS == 1) ? " CPU thread" : " CPU threads")); // $NON-NLS-1$

	/** The mininum size of heap space in bytes */
	public final static int MIN_HEAP_SPACE = 128*1024*1024;
	
//	private static final boolean debug = false; // logger.isLoggable(Level.FINE);
	/** true if displaying graphic user interface. */
	private transient boolean useGUI = true;
	/** Flag to indicate that a new simulation is being created or loaded. */
	private transient boolean isUpdating = false;
	/** Flag to keep track of whether the initial state of simulation has been initialized. */
	private transient boolean doneInitializing = false;
	
	private transient boolean defaultLoad = false;

	private transient boolean justSaved = true;

	private transient boolean autosaveDefault;
	
	private transient boolean clockOnPause = false;
	
	private boolean initialSimulationCreated = false;

	private boolean changed = true;

	private boolean isFXGL = false;

	/** The modified time stamp of the last saved sim */	
	private String lastSaveTimeStampMod;
	/** The time stamp of the last saved sim. */
	private String lastSaveTimeStamp = null;

	// Note: Transient data members aren't stored in save file
	/** The clock thread executor service. */
	private transient ExecutorService clockThreadExecutor;
	/** The simulation thread executor service. */
	private transient ExecutorService simExecutor;

	// Intransient data members (stored in save file)
	/** Planet Mars. */
	private static Mars mars;
	/** All historical info. */
	private static HistoricalEventManager eventManager;
	/** The malfunction factory. */
	private static MalfunctionFactory malfunctionFactory;
	/** Manager for all units in simulation. */
	private static UnitManager unitManager;
	/** Mission controller. */
	private static MissionManager missionManager;
	/** Manages all personal relationships. */
	private static RelationshipManager relationshipManager;
	/** Medical complaints. */
	private static MedicalManager medicalManager;
	/** Master clock for the simulation. */
	private static MasterClock masterClock;
	/** Manages trade credit between settlements. */
	private static CreditManager creditManager;
	/** Manages scientific studies. */
	private static ScientificStudyManager scientificStudyManager;
	/** Manages transportation of settlements and resupplies from Earth. */
	private static TransportManager transportManager;
	/** The SimulationConfig instance. */
	private static SimulationConfig simulationConfig;
	/** The GameWorld instance for FXGL frameworld */
	// private GameWorld gameWorld;

	private UpTimer ut;
//	private ObjectMapper objectMapper;
	
	/**
	 * Private constructor for the Singleton Simulation. This prevents instantiation
	 * from other classes.
	 */
	private Simulation() {
		// INFO Simulation's constructor is on both JavaFX-Launcher Thread

//		// Create ObjectMapper instance
//		objectMapper = new ObjectMapper();
//		// Configure Object mapper for pretty print
//		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//		// to allow serialization of "empty" POJOs (no properties to serialize)
//		// (without this setting, an exception is thrown in those cases)
//		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		
	}

//	/** (NOT USED) Eager Initialization Singleton instance. */
//	private static final Simulation instance = new Simulation();
	
//	/**
//	 * Gets a Eager Initialization Singleton instance of the simulation.
//	 * 
//	 * @return Simulation instance
//	 */
//	public static Simulation instance() {
//		return instance;
//	}

	/**
	 * Initializes an inner static helper class for Bill Pugh Singleton Pattern
	 * Note: as soon as the instance() method is called the first time, the class is
	 * loaded into memory and an instance gets created. Advantage: it supports
	 * multiple threads calling instance() simultaneously with no synchronized
	 * keyword needed (which slows down the VM)
	 */
	private static class SingletonHelper {
		private static final Simulation INSTANCE = new Simulation();
	}

	/**
	 * Gets a Bill Pugh Singleton instance of the simulation.
	 * 
	 * @return Simulation instance
	 */
	public static Simulation instance() {
		// logger.config("Simulation's instance() is on " +
		// Thread.currentThread().getName() + " Thread");
		// NOTE: Simulation.instance() is accessible on any threads or by any threads
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Prevents the singleton pattern from being destroyed at the time of
	 * serialization
	 * 
	 * @return Simulation instance
	 */
	protected Object readResolve() throws ObjectStreamException {
		return instance();
	}

	public void startSimExecutor() {
//		logger.config("startSimExecutor() is on " + Thread.currentThread().getName());
		simExecutor = Executors.newSingleThreadExecutor();
	}

	public ExecutorService getSimExecutor() {
		return simExecutor;
	}

	public void runCreateNewSimTask() {
		startSimExecutor();
		simExecutor.submit(new CreateNewSimTask());
	}
	
	public class CreateNewSimTask implements Runnable {

		public void run() {
			createNewSimulation(-1, false);
		}
	}
	
	/**
	 * Checks if the simulation is in a state of creating a new simulation or
	 * loading a saved simulation.
	 * 
	 * @return true is simulation is in updating state.
	 */
	public boolean isUpdating() {
		return isUpdating;
	}

	/**
	 * Creates a new simulation instance.
	 */
	public void createNewSimulation(int timeRatio, boolean loadSaveSim) {
		isUpdating = true;

		logger.config(Msg.getString("Simulation.log.createNewSim")); //$NON-NLS-1$

		Simulation sim = instance();

		// Destroy old simulation.
		if (sim.initialSimulationCreated) {
			sim.destroyOldSimulation();
		}
				
		sim.initialSimulationCreated = true;

		// Initialize intransient data members.
		sim.initializeIntransientData(timeRatio, loadSaveSim);

		// Initialize transient data members.
//        sim.initializeTransientData(); // done in the constructor already (MultiplayerClient needs HistoricalEnventManager)

		// Sleep current thread for a short time to make sure all simulation objects are
		// initialized.
		try {
			Thread.sleep(50L);
		} catch (InterruptedException e) {
			// Do nothing.
		}

		isUpdating = false;

		// Preserve the build version tag for future build 
		// comparison when loading a saved sim
		unitManager.originalBuild = Simulation.BUILD;

//		masterClock.start();
		
//		logger.config("Done with createNewSimulation()");
	}

//	/**
//	 * Initialize transient data in the simulation.
//	 */
//    private void initializeTransientData() {
//       //logger.config("Simulation's initializeTransientData() is on " + Thread.currentThread().getName() + " Thread");
//       eventManager = new HistoricalEventManager();
//    }

	public void testRun() {
//		PersonConfig pc = SimulationConfig.instance().getPersonConfig();
		
		Simulation sim = Simulation.instance();
		ResourceUtil.getInstance();
		mars = Mars.createTest();
		masterClock = new MasterClock(false, 256);
		unitManager = new UnitManager();
		
		// Gets the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
		// Re-initialize the MarsSurface instance
		MarsSurface marsSurface = unitManager.getMarsSurface();
		
		Inventory.initializeInstances(unitManager, marsSurface);
		
		unitManager.constructInitialUnits(true);
		
		medicalManager = new MedicalManager();
		
	
		
		// Create marsClock instance
		MarsClock marsClock = masterClock.getMarsClock();
		EarthClock earthClock = masterClock.getEarthClock();
		
		// Set instances for logging
		LogConsolidated.initializeInstances(marsClock, earthClock);
		
		// Set instance for Inventory
//		Inventory.initializeInstances(mars.getMarsSurface());
		
		Unit.setUnitManager(unitManager);
		Unit.initializeInstances(masterClock, marsClock, earthClock, sim, mars, 
				marsSurface, mars.getWeather(), surfaceFeatures, new MissionManager());

	}
	
	/**
	 * Initialize intransient data in the simulation.
	 */
	private void initializeIntransientData(int timeRatio, boolean loadSaveSim) {
//		logger.config("initializeIntransientData() is on " + Thread.currentThread().getName());
		// Initialize resources
		ResourceUtil.getInstance();
		
		// Initialize serializable objects
		malfunctionFactory = new MalfunctionFactory();
		mars = new Mars();
		mars.createInstances();
	
//		logger.config("Done with Mars");
		
		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		masterClock = new MasterClock(isFXGL, timeRatio);
		
//		logger.config("Done with MasterClock");
		
		// Note : marsSurface is needed before creating Inventory and Unit
		// When loading from saved sim, it's at unitManager
		// Gets the MarsSurface instance 
		MarsSurface marsSurface = mars.getMarsSurface();
		
		// Gets the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
		surfaceFeatures.initializeTransientData();
		
//		logger.config("Done with SurfaceFeatures");
		
		// Create clock instances
		MarsClock marsClock = masterClock.getMarsClock();
		EarthClock earthClock = masterClock.getEarthClock();
		
		OrbitInfo.initializeInstances(marsClock, earthClock);
		
		// Initialize Mars environmental objects
//		mars.initializeTransientData(); // requires terrain, weather, orbit
		mars.getWeather().initializeTransientData();
		
		// Initialize units prior to starting the unit manager
		Unit.initializeInstances(masterClock, marsClock, earthClock, this, mars, null, 
				mars.getWeather(), surfaceFeatures, missionManager);
		
//		logger.config("Done with Unit");

		// Initialize serializable managers
		unitManager = new UnitManager(); 
		
//		logger.config("Done with UnitManager");
		
		Inventory.initializeInstances(unitManager, marsSurface);
		Airlock.initializeInstances(unitManager, marsSurface);
		
//		logger.config("Done with Airlock.initializeInstances()");
		
		// Gets the MarsSurface instance
//		MarsSurface marsSurface = unitManager.getMarsSurface();//mars.getMarsSurface();
		Unit.setMarsSurface(marsSurface);
		Unit.setUnitManager(unitManager);
		
//		logger.config("Done with Unit.setUnitManager()");
		
		unitManager.constructInitialUnits(loadSaveSim); // unitManager needs to be on the same thread as masterClock
		
//		logger.config("Done with unitManager.constructInitialUnits()");
		
		eventManager = new HistoricalEventManager();
		creditManager = new CreditManager();
		scientificStudyManager = new ScientificStudyManager();
		transportManager = new TransportManager();

//		logger.config("Done with TransportManager()");

		// Initialize meta tasks
		new MetaTaskUtil();
        // Initialize ManufactureUtil
        new ManufactureUtil();
		
		// Gets config file instances
		simulationConfig = SimulationConfig.instance();
//		BuildingConfig bc = simulationConfig.getBuildingConfiguration();
		PersonConfig pc = simulationConfig.getPersonConfig();
	
		// Set instances for logging
		LogConsolidated.initializeInstances(marsClock, earthClock);

		// Initialize instances prior to UnitManager initiatiation		
		MalfunctionFactory.initializeInstances(this, marsClock, unitManager);
		MissionManager.initializeInstances(marsClock);
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory, medicalManager, eventManager);
		RelationshipManager.initializeInstances(unitManager);
//		MedicalManager.initializeInstances();		
		
		//  Re-initialize the GameManager
		GameManager.initializeInstances(unitManager);
					
//		logger.config("Done with GameManager()");
		
		// Set instances for classes that extend Unit and Task and Mission
		Mission.initializeInstances(this, marsClock, eventManager, unitManager, scientificStudyManager, 
				surfaceFeatures, missionManager, relationshipManager, pc, creditManager);
		Task.initializeInstances(marsClock, eventManager, relationshipManager, unitManager, 
				scientificStudyManager, surfaceFeatures, missionManager, pc);
		
		ut = masterClock.getUpTimer();

		doneInitializing = true;
//		logger.config("Done initializing intransient data.");
	}


	/**
	 * Start the simulation instance.
	 */
	public void startSimThread(boolean useDefaultName) {
		// Start the simulation.
		ExecutorService e = getSimExecutor();
		if (e == null || (e != null && (e.isTerminated() || e.isShutdown())))
			startSimExecutor();
		e.submit(new StartTask(useDefaultName));
	}
	
	class StartTask implements Runnable {
		boolean autosaveDefault;

		StartTask(boolean useDefaultName) {
			this.autosaveDefault = useDefaultName;
		}

		public void run() {
//			logger.config("StartTask's run() is on " + Thread.currentThread().getName());
			startClock(autosaveDefault);
		}
	}
	
	/**
	 * Starts the simulation.
	 * 
	 * @param autosaveDefault. True if default is used for autosave
	 */
	public void startClock(boolean autosaveDefault) {
//		logger.config("Simulation's startClock() is on " + Thread.currentThread().getName());
		// SwingUtilities.invokeLater(() -> testConsole());
		
		masterClock.addClockListener(this);
		masterClock.startClockListenerExecutor();

		restartClockExecutor();

		this.autosaveDefault = autosaveDefault;
		AutosaveScheduler.defaultStart();
		ut = masterClock.getUpTimer();
		
		masterClock.start();
	}

	/**
	 * Starts or restarts the executive service thread that the MasterClock's ClockThreadTask runs on.
	 */
	public void restartClockExecutor() {
		clockThreadExecutor = Executors.newSingleThreadExecutor();

		if (masterClock.getClockThreadTask() != null)
			clockThreadExecutor.execute(masterClock.getClockThreadTask());
	}
	
	/**
	 * Loads a simulation instance from a save file.
	 * 
	 * @param file the file to be loaded from.
	 */
	public void loadSimulation(final File file) {
		logger.config("Simulation's loadSimulation() is on " + Thread.currentThread().getName());
		isUpdating = true;

		File f = file;
		
		Simulation sim = instance();
//		if (file != null)
//			sim.stop();

//		try {
//			sim.readJSON();
//		} catch (JsonParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// Use default file path if file is null.
		if (f == null) {
			// logger.config("Yes file is null");
			// [landrus, 27.11.09]: use the home dir instead of unknown relative paths.
			f = new File(SAVE_DIR, SAVE_FILE + SAVE_FILE_EXTENSION);
//			 logger.config("file is " + f);
			defaultLoad = true;
		} else {
			defaultLoad = false;
		}

		if (f.exists() && f.canRead()) {
//			logger.config(" - - - - - - - - - - - - - -");
//			logger.config(Msg.getString("Simulation.log.loadSimFrom", f)); //$NON-NLS-1$

			try {
				sim.readFromFile(f);

//				logger.config("Done readFromFile()");
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
//				logger.log(Level.SEVERE,
//						"Quitting mars-sim with ClassNotFoundException when loading the simulation : "
//								+ e.getMessage());
//    	        Platform.exit();
				System.exit(1);

			} catch (IOException e) {
				e.printStackTrace();
//				logger.log(Level.SEVERE,
//						"Quitting mars-sim with IOException when loading the simulation : " + e.getMessage());
//    	        Platform.exit();
				System.exit(1);

			} catch (NullPointerException e) {
				e.printStackTrace();
//				logger.log(Level.SEVERE,
//						"Quitting mars-sim with NullPointerException when loading the simulation : " + e.getMessage());
//    	        Platform.exit();
//				System.exit(1);
			
			} catch (Exception e) {
				e.printStackTrace();
//				logger.log(Level.SEVERE,
//						"Quitting mars-sim. Could not load the simulation : " + e.getMessage());
//    	        Platform.exit();
				System.exit(1);
			}
		}

		else {
			logger.log(Level.SEVERE, "Quitting mars-sim. The saved sim cannot be read/found.");
			System.exit(1);
		}
	}

//	private synchronized void readJSON() throws JsonParseException, JsonMappingException, IOException {
//
//		String name = mars.getClass().getSimpleName();
//		File file = new File(DEFAULT_DIR, name + JSON_EXTENSION);
//			
//		if (file.exists() && file.canRead()) {
//			// Use Jackson json to read json file data to String
//			byte[] jsonData = Files.readAllBytes(file.toPath());//Paths.get("Mars.json"));
//			System.out.println(new String(jsonData));
//			
////	        mars = objectMapper.readValue(FileUtils.readFileToByteArray(file, Mars.class);
////	        System.out.println(mars);
//	        
//			// Use Jackson json to read
////			mars = objectMapper.readValue(file, Mars.class);
////			System.out.println(mars);
//		}
//	}
	
    /**
     * Deserialize to Object from given file.
     */
    public void deserialize(File file) throws IOException, ClassNotFoundException {
		logger.config("deserialize() is on " + Thread.currentThread().getName());
		
//		byte[] buf = new byte[8192];
		FileInputStream in = null;
	    XZInputStream xzin = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    InputStream is = null;
	    ObjectInputStream ois = null;

		try {
			in = new FileInputStream(file);
			// Replace gzip with xz compression (based on LZMA2)
			// Since XZInputStream does some buffering internally
			// anyway, BufferedInputStream doesn't seem to be
			// needed here to improve performance.
			// in = new BufferedInputStream(in);
			
			try {
				// Limit memory usage to 256 MB
				xzin = new XZInputStream(new BufferedInputStream(in), 256 * 1024);
			} catch (XZFormatException e) {
				e.printStackTrace();
				// Thrown when reading a stream terminated by an exception that occurred while the stream was being written.
				logger.log(Level.SEVERE, "Quitting mars-sim with XZFormatException when loading " + file + " : " + e.getMessage());
				System.exit(1);	
			}
			
//			int size;
//			while ((size = xzin.read(buf)) != -1)
//				baos.write(buf, 0, size);			
			ByteStreams.copy(xzin, baos);
			
			//see https://stackoverflow.com/questions/26960997/convert-outputstream-to-bytearrayoutputstream
//			byte[] bytes = new byte[8];
//			baos.write(bytes);
//			baos.writeTo(os);

//			is = new ByteArrayInputStream(baos.toByteArray());
//			ois = new ObjectInputStream(is);
			ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

			// Load intransient objects.
//			SimulationConfig.setInstance((SimulationConfig) ois.readObject());	
//			ResourceUtil.setInstance((ResourceUtil) ois.readObject());

			// Load remaining serialized objects
			malfunctionFactory = (MalfunctionFactory) ois.readObject();
			mars = (Mars) ois.readObject();
			mars.initializeTransientData();
			missionManager = (MissionManager) ois.readObject();
			medicalManager = (MedicalManager) ois.readObject();
			scientificStudyManager = (ScientificStudyManager) ois.readObject();
			transportManager = (TransportManager) ois.readObject();
			creditManager = (CreditManager) ois.readObject();
			eventManager = (HistoricalEventManager) ois.readObject();
			relationshipManager = (RelationshipManager) ois.readObject();		
			unitManager = (UnitManager) ois.readObject();		
			masterClock = (MasterClock) ois.readObject();	
	
		// Note: see https://docs.oracle.com/javase/7/docs/platform/serialization/spec/exceptions.html
		} catch (WriteAbortedException e) {
			e.printStackTrace();
			// Thrown when reading a stream terminated by an exception that occurred while the stream was being written.
			logger.log(Level.SEVERE, "Quitting mars-sim with WriteAbortedException when loading " + file + " : " + e.getMessage());
			System.exit(1);		

		} catch (OptionalDataException e) {
			e.printStackTrace();
			// Thrown by readObject when there is primitive data in the stream and an object is expected. The length field of the exception indicates the number of bytes that are available in the current block.
			logger.log(Level.SEVERE, "Quitting mars-sim with OptionalDataException when loading " + file + " : " + e.getMessage());
			System.exit(1);		
		
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			// Thrown when a restored object cannot be made valid.
			logger.log(Level.SEVERE, "Quitting mars-sim with InvalidObjectException when loading " + file + " : " + e.getMessage());
			System.exit(1);		

		} catch (NotActiveException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Quitting mars-sim with NotActiveException when loading " + file + " : " + e.getMessage());
			System.exit(1);		

		} catch (StreamCorruptedException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Quitting mars-sim with StreamCorruptedException when loading " + file + " : " + e.getMessage());
			System.exit(1);		
		
		} catch (NotSerializableException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Quitting mars-sim with NotSerializableException when loading " + file + " : " + e.getMessage());
			System.exit(1);		
			
		} catch (ObjectStreamException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Quitting mars-sim with ObjectStreamException when loading " + file + " : " + e.getMessage());
			System.exit(1);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Quitting mars-sim with FileNotFoundException since " + file + " cannot be found : ", e.getMessage());
			System.exit(1);

		} catch (EOFException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,
					"Quitting mars-sim. Unexpected End of File error on " + file + " : " + e.getMessage());
			System.exit(1);

		} catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,
					"Quitting mars-sim. IOException when decompressing " + file + " : " + e.getMessage());
			System.exit(1);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,
					"Quitting mars-sim. ClassNotFoundException when decompressing " + file + " : " + e.getMessage());
			System.exit(1);
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,
					"Quitting mars-sim. NullPointerException when loading " + file + " : " + e.getMessage());
			System.exit(1);

		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Quitting mars-sim with errors when loading " + file + " : " + e.getMessage());
			System.exit(1);
		
		} finally {
			
			if (ois != null) {
				ois.close();
			}

			if (in != null) {
				in.close();
			}

			if (xzin != null) {
				xzin.close();
			}

			if (is != null) {
				is.close();
			}

			if (baos != null) {
				baos.close();
			}
		}

    }
    
    /**
     * Computes the size of the file
     * 
     * @param file
     * @return the file size with unit in a string
     */
    private String computeFileSize(File file) {
		// Convert from Bytes to KB
		double fileSize = file.length() / 1000D;
		String s = "";
		
		if (fileSize > 1000D) {
			fileSize = fileSize / 1_000.0;
			s = " MB";
		}
		else {
			s = " KB";
		}

		return Math.round(fileSize * 100.0) / 100.0 + s;
    }
 
	/**
	 * Reads a serialized simulation from a file.
	 * 
	 * @param file the saved serialized simulation.
	 * @throws ClassNotFoundException if error reading serialized classes.
	 * @throws IOException            if error reading from file.
	 */
	private void readFromFile(File file) throws ClassNotFoundException, IOException {
//		logger.config("readFromFile() is on " + Thread.currentThread().getName());
		logger.config("Loading and processing the saved sim. Please wait...");
		
//		System.out.println(file.length() / 1000D);
//		// Compute the size of the saved sim
//		String sizeStr = computeFileSize(file);

		
//		logger.config("Proceed to loading the saved sim.");
		String filename = file.getName();
		String path = file.getPath().replace(filename, "");
		
		// Deserialize the file
		deserialize(file);
		
		String loadBuild = unitManager.originalBuild;
		if (loadBuild == null)
			loadBuild = "unknown";
		
		logger.config(" --------------------------------------------------------------------");
		logger.config("                   Info on The Saved Simulation                      ");
		logger.config(" --------------------------------------------------------------------");
		logger.config("                   Filename : " + filename);
		logger.config("                       Path : " + path);
		logger.config("                       Size : " + computeFileSize(file));
		logger.config("              Made in Build : " + loadBuild);
		logger.config("  Current Core Engine Build : " + Simulation.BUILD);
		
		logger.config("    Martian Date/Time Stamp : " + masterClock.getMarsClock().getDateTimeStamp());
		logger.config(" --------------------------------------------------------------------");			
		if (Simulation.BUILD.equals(loadBuild)) {
			logger.config(" Note : Both Builds are matched.");
		} else {
			logger.config(" Note : The Builds are NOT matched.");
			logger.warning("Attempting to load a simulation made in build " + loadBuild
				+ " (older) under core engine build " + Simulation.BUILD + " (newer).");
		}		
		logger.config("  - - - - - - - - - Sol " + masterClock.getMarsClock().getMissionSol() 
				+ " (Cont') - - - - - - - - - - - ");
		
		// Initialize transient data.
//	    instance().initializeTransientData();
		instance().initialSimulationCreated = true;
		
        try {
    		// Re-initialize instances
    		reinitializeInstances();
    		// Set this flag to false
    		isUpdating = false;
    		
//    		logger.config("Done reinitializeInstances");
        } catch (Exception e) {
            e.printStackTrace();
        }

	}
	
	/**
	 *  Re-initialize instances after loading from a saved sim
	 */
	private void reinitializeInstances() {
		// Re-initialize the utility class for getting lists of meta tasks.
		new MetaTaskUtil();		
		// Restart the autosave scheduler
		AutosaveScheduler.defaultStart();
		// Set save type to NONE
		masterClock.setSaveType();	
		// Re-initialize the resources for the saved sim
		ResourceUtil.getInstance().initializeInstances();
		// Re-initialize the MarsSurface instance
		MarsSurface marsSurface = unitManager.getMarsSurface();
//		mars.setMarsSurface(marsSurface);
		Airlock.initializeInstances(unitManager, marsSurface);
		
		Inventory.initializeInstances(unitManager, marsSurface);

//		for (Unit u: inv.getContainedUnits()) {
//			String s = String.format("Simulation : %20s (%4d)", u.getName(), u.getIdentifier());
//			System.out.println(s);
//		}
				
//		logger.config("Done marsSurface");
		
		//  Re-initialize the GameManager
		GameManager.initializeInstances(unitManager);
		// Re-initialize the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
		// Gets the Weather instance
		Weather weather = mars.getWeather();
		// Gets the orbitInfo instance
		OrbitInfo orbit = mars.getOrbitInfo();
	
//		logger.config("Done orbit");
		
		// Re-initialize the Simulation instance
		MasterClock.initializeInstances(this);					
		// Re-initialize the Mars instance
		MarsClock.initializeInstances(this, orbit);	
		
		// Gets he MarsClock instance
		MarsClock marsClock = masterClock.getMarsClock();
		// Gets he MarsClock instance
		EarthClock earthClock = masterClock.getEarthClock();
		
		// Re-initialize the instances in LogConsolidated
		LogConsolidated.initializeInstances(marsClock, earthClock);
		
//		logger.config("Done LogConsolidated");
		
		// Re-initialize Mars environmental instances
		Weather.initializeInstances(masterClock, marsClock, mars, surfaceFeatures, orbit, unitManager); // terrain

		OrbitInfo.initializeInstances(marsClock, earthClock);	
		
		SurfaceFeatures.initializeInstances(masterClock, mars, this, weather, orbit, missionManager);  // sunDirection, landmarks

		DustStorm.initializeInstances(weather);
			
//		logger.config("Done DustStorm");
		
		// Gets config file instances
		simulationConfig = SimulationConfig.instance();
		BuildingConfig bc = simulationConfig.getBuildingConfiguration();
		PersonConfig pc = simulationConfig.getPersonConfig();
		
//		logger.config("Done pc");
		
		// Re-initialize static class
		MalfunctionFactory.initializeInstances(this, marsClock, unitManager);
		MissionManager.initializeInstances(marsClock);
//		MedicalManager.justReloaded();
		
		// Re-initialize units prior to starting the unit manager
		Unit.initializeInstances(masterClock, marsClock, earthClock, this, mars, marsSurface, weather, surfaceFeatures, missionManager);	
		Unit.setUnitManager(unitManager);
		
//		logger.config("Done Unit");
		
		// Start a chain of calls to set instances on each person
		unitManager.reinit(marsClock);
		
		// Update/reset the identifier count for each type of units 
		Person.reinitializeIdentifierCount();
		Robot.reinitializeIdentifierCount();
		Vehicle.reinitializeIdentifierCount();
		Building.reinitializeIdentifierCount();
		Equipment.reinitializeIdentifierCount();
		Settlement.reinitializeIdentifierCount();
		ConstructionSite.reinitializeIdentifierCount();

		RelationshipManager.initializeInstances(unitManager);
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory, medicalManager, eventManager);
		TransportManager.initializeInstances(marsClock, eventManager);
		ScientificStudyManager.initializeInstances(marsClock, unitManager);
		ScientificStudy.initializeInstances(marsClock, unitManager);
		ScientificStudyUtil.initializeInstances(relationshipManager, unitManager);
				
		Resupply.initializeInstances(bc, unitManager);
		
//		logger.config("Done Resupply");
		
		// Re-initialize Unit related class
//		Vehicle.initializeInstances();
		SalvageValues.initializeInstances(unitManager);
			
		// Re-initialize Person/Robot related class
		BotMind.initializeInstances(marsClock);
		CircadianClock.initializeInstances(marsClock);
		Mind.initializeInstances(marsClock, missionManager, relationshipManager);		
		PhysicalCondition.initializeInstances(this, masterClock, marsClock, medicalManager);
		RadiationExposure.initializeInstances(masterClock, marsClock);
		Role.initializeInstances(marsClock);
		TaskManager.initializeInstances(marsClock, missionManager);
		TaskSchedule.initializeInstances(marsClock);
		HealthProblem.initializeInstances(medicalManager, eventManager);
		
//		logger.config("Done HealthProblem");

		// Re-initialize Structure related class
		Building.initializeInstances(bc, unitManager);
		BuildingManager.initializeInstances(this, masterClock, marsClock, bc, eventManager, relationshipManager, unitManager);
		Settlement.initializeInstances(unitManager);		// loadDefaultValues()
		ChainOfCommand.initializeInstances(marsClock, unitManager);
		GoodsManager.initializeInstances(this, marsClock, missionManager, unitManager, pc);
			
		// Re-initialize Building function related class
		Function.initializeInstances(bc, masterClock, marsClock, pc, mars, surfaceFeatures, weather, unitManager);
		Cooking.initializeInstances(); // prepareOilMenu()
		Farming.initializeInstances();  // cropConfig

//		logger.config("Done Farming");
		
		// Miscs.
		CompositionOfAir.initializeInstances(masterClock, marsClock, pc, unitManager);
		Crop.initializeInstances(masterClock, marsClock, surfaceFeatures, unitManager);
		HeatSource.initializeInstances(mars, surfaceFeatures, orbit, weather);
		Malfunction.initializeInstances();
		PowerSource.initializeInstances(mars, surfaceFeatures, orbit, weather);
		ResourceProcess.initializeInstances(marsClock);
		Job.initializeInstances(unitManager, missionManager);
		RobotJob.initializeInstances(unitManager, missionManager);
//		CreditEvent.initializeInstances(unitManager, missionManager);
		
//		logger.config("Done RobotJob");
				
		// Re-initialize Task related class 
//		LoadVehicleGarage.initializeInstances(pc); 
//		ObserveAstronomicalObjects.initializeInstances(surface);
//		PerformLaboratoryExperiment.initializeInstances(scientificStudyManager);
//		PlayHoloGame.initializeInstances(masterClock, marsClock);
//		ProposeScientificStudy.initializeInstances(scientificStudyManager);
		Walk.initializeInstances(unitManager);	
		Task.initializeInstances(marsClock, eventManager, relationshipManager, unitManager, 
				scientificStudyManager, surfaceFeatures, missionManager, pc);

//		logger.config("Done Task");
		
		// Re-initialize MetaMission class
//		BuildingConstructionMissionMeta.setInstances(marsClock);
//		CollectRegolithMeta.setInstances(missionManager);
//		CollectIceMeta.setInstances(missionManager);
//		MetaMission.setInstances(marsClock, missionManager);
		
		// Re-initialize Mission related class
		Mission.initializeInstances(this, marsClock, eventManager, unitManager, scientificStudyManager, 
				surfaceFeatures, missionManager, relationshipManager, pc, creditManager);
//		RoverMission.justReloaded(eventManager);  // eventManager
//		VehicleMission.justReloaded(missionManager); // missionmgr
//		RescueSalvageVehicle.justReloaded(eventManager);  // eventManager
		MissionPlanning.initializeInstances(marsClock);

		doneInitializing = true;
		
//		logger.config("Done MissionPlanning");
	}
	
	public boolean isDoneInitializing() {
		return doneInitializing;
	}
	
	/**
	 * Writes the JSON file
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
//	public void writeJSON() throws JsonGenerationException, JsonMappingException, IOException {	
		// Write to console, can write to any output stream such as file
//		StringWriter stringEmp = new StringWriter();
		
//		Simulation sim = instance();
//		SurfaceFeatures surface = sim.getMars().getSurfaceFeatures();
//		String name = surface.getClass().getSimpleName();	
//		objectMapper.writeValue(stringEmp, surface);
//		System.out.println(name + " JSON representation :\n" + stringEmp);	
//		// Write to the file
//		objectMapper.writeValue(new File(name + "." + JSON_EXTENSION), surface);
		
//		String name = mars.getClass().getSimpleName();
//		objectMapper.writeValue(stringEmp, mars);
//		System.out.println("JSON representation of the Class '" + name + "' :\n" + stringEmp);
//		// Write to the file
//		objectMapper.writeValue(new File(DEFAULT_DIR, name + JSON_EXTENSION), mars);
		
//		Object o = mars;
//		String name = o.getClass().getSimpleName();
		
//		objectMapper.writeValue(stringEmp, o);
//		System.out.println("JSON representation of the Class '" + name + "' :\n" + stringEmp);
//		// Write to the file
//		objectMapper.writeValue(new File(SAVE_DIR, name + JSON_EXTENSION), o);
//		
//		String json = objectMapper.writeValueAsString(o) ; 
//		System.out.println("JSON representation of the Class '" + name + "' :\n" + json);	
//	}
	
	
	/**
	 * Saves a simulation instance to a save file.
	 * 
	 * @param file the file to be saved to.
	 */
	public synchronized void saveSimulation(SaveType type, File file) throws IOException {
//		logger.config("saveSimulation(" + type + ", " + file + ")");
		// Checks to see if the simulation is on pause
		boolean isPause = masterClock.isPaused();
		
		Simulation sim = instance();
		// Stops the master clock and removes the Simulation clock listener
		sim.halt(isPause);

		// Experiment with saving in JSON format
//		writeJSON();
		
		lastSaveTimeStamp = new SystemDateTime().getDateTimeStr();
		changed = true;

		File backupFile = new File(SAVE_DIR, "previous" + SAVE_FILE_EXTENSION);
		FileSystem fileSys = null;
		Path destPath = null;
		Path srcPath = null;

		// Use type to differentiate in what name/dir it is saved
		if (type == SaveType.SAVE_DEFAULT) {

			file = new File(SAVE_DIR, SAVE_FILE + SAVE_FILE_EXTENSION);

			if (file.exists() && !file.isDirectory()) {
				fileSys = FileSystems.getDefault();
				destPath = fileSys.getPath(backupFile.getPath());
				srcPath = fileSys.getPath(file.getPath());
				// Backup the existing default.sim
				Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}

			logger.config("Saving the simulation as " + SAVE_FILE + SAVE_FILE_EXTENSION + ".");

		}

		else if (type == SaveType.SAVE_AS) {
			String f = file.getName();
			String dir = file.getParentFile().getAbsolutePath();
			if (!f.contains(".sim"))
				file = new File(dir, f + SAVE_FILE_EXTENSION);
			logger.config("Saving the simulation as " + file + "...");
		}

		else if (type == SaveType.AUTOSAVE_AS_DEFAULT) {

//            file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//            logger.config("Autosaving as " + DEFAULT_FILE + DEFAULT_EXTENSION);

			file = new File(SAVE_DIR, SAVE_FILE + SAVE_FILE_EXTENSION);

			if (file.exists() && !file.isDirectory()) {
				fileSys = FileSystems.getDefault();
				destPath = fileSys.getPath(backupFile.getPath());
				srcPath = fileSys.getPath(file.getPath());
				// Backup the existing default.sim
				Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}

			logger.config("Autosaving the simulation as " + SAVE_FILE + SAVE_FILE_EXTENSION + ".");

		}

		else if (type == SaveType.AUTOSAVE) {
			int missionSol = masterClock.getMarsClock().getMissionSol();
			
			String autosaveFilename = lastSaveTimeStamp + "_sol" + missionSol + "_r" + BUILD
					+ SAVE_FILE_EXTENSION;
			file = new File(AUTOSAVE_DIR, autosaveFilename);
			logger.config("Autosaving the simulation as " + autosaveFilename + ".");

		}

		// if the autosave/default save directory does not exist, create one now
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory();
		// Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		 // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
		long heapFreeSize = Runtime.getRuntime().freeMemory(); 
		
		logger.config("heapSize: " + formatSize(heapSize) 
		+ "    heapMaxSize: " + formatSize(heapMaxSize) 
//		+ "    heapFreeSize: " + heapFreeSize
		+ "    heapFreeSize: " + formatSize(heapFreeSize) + "");
		
		int counts = 0;
		while (heapFreeSize < MIN_HEAP_SPACE && counts <= 5) {
			counts++;
			logger.config("Not enough free memory in heap space. Wait for 10 seconds and retry...");
			delay(10000);
			// Get current size of heap in bytes
			heapSize = Runtime.getRuntime().totalMemory();
			// Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
			heapMaxSize = Runtime.getRuntime().maxMemory();
			 // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
			heapFreeSize = Runtime.getRuntime().freeMemory(); 
			
			logger.config("heapSize: " + formatSize(heapSize) 
			+ "    heapMaxSize: " + formatSize(heapMaxSize) 
			+ "    heapFreeSize: " + formatSize(heapFreeSize) + "");
		}

		if (counts <= 5) {
			// Serialize the file
			serialize(type, file, srcPath, destPath);
		}
		else {
			logger.config("Please try saving again later.");
		}

		// Restarts the master clock and adds back the Simulation clock listener
		sim.proceed(isPause);
	}

	/**
	 * Delays for a period of time in millis
	 * 
	 * @param millis
	 */
    public static void delay(long millis) {
        try {
			TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.2f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
    
    /**
     * Serialize the given object and save it to a given file.
     */
    public void serialize(SaveType type, File file, Path srcPath, Path destPath)
            throws IOException {

		// Replace gzip with xz compression (based on LZMA2)
		// (1) http://stackoverflow.com/questions/5481487/how-to-use-lzma-sdk-to-compress-decompress-in-java
		// (2) http://tukaani.org/xz/xz-javadoc/

		// STEP 1: combine all objects into one single uncompressed file, namely
		// "default"
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    InputStream is = null;
		XZOutputStream xzout = null;
		
		try {
	
			// Set a delay for 200 millis to avoid java.util.ConcurrentModificationException
			delay(500L);
			
			// Store the in-transient objects.
//			oos.writeObject(SimulationConfig.instance());
//			oos.writeObject(ResourceUtil.getInstance());
			oos.writeObject(malfunctionFactory);
			oos.writeObject(mars); // java.util.ConcurrentModificationException, infinite ObjectOutputStream.java:1510)
			oos.writeObject(missionManager);
			oos.writeObject(medicalManager);
			oos.writeObject(scientificStudyManager);
			oos.writeObject(transportManager);
			oos.writeObject(creditManager);
			oos.writeObject(eventManager);
			oos.writeObject(relationshipManager);
			oos.writeObject(unitManager);
			oos.writeObject(masterClock);
			
			oos.flush();
			oos.close();

		    is = new ByteArrayInputStream(baos.toByteArray());
		    
			// Print the size of each serializable object
//			System.out.println(printObjectSize(0).toString());
			
			// Using the default settings and the default integrity check type (CRC64)
			LZMA2Options lzma2 = new LZMA2Options(5);
			// Set to 6. For mid sized archives (>8mb), 7 works better.
			//lzma2.setPreset(8);
			FilterOptions[] options = {lzma2};
			
			// Using the x86 BCJ filter // 424KB
//			X86Options x86 = new X86Options();
//			LZMA2Options lzma2 = new LZMA2Options();
//			FilterOptions[] options = { x86, lzma2 };
			logger.config("Encoder memory usage : "
		              + Math.round(FilterOptions.getEncoderMemoryUsage(options)/1_000.0*100.00)/100.00 + " MB");
			logger.config("Decoder memory usage : "
		              + Math.round(FilterOptions.getDecoderMemoryUsage(options)/1_000.0*100.00)/100.00 + " MB");
	
			xzout = new XZOutputStream(new BufferedOutputStream(new FileOutputStream(file)), options);
			
			ByteStreams.copy(is, xzout);
			
			xzout.finish();
			// Print the size of the saved sim
			logger.config("           File size : " + computeFileSize(file));
			logger.config("Done saving. The simulation resumes.");

		// Note: see https://docs.oracle.com/javase/7/docs/platform/serialization/spec/exceptions.html
		} catch (WriteAbortedException e) {
			// Thrown when reading a stream terminated by an exception that occurred while the stream was being written.
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with WriteAbortedException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();		

		} catch (OptionalDataException e) {
			// Thrown by readObject when there is primitive data in the stream and an object is expected. The length field of the exception indicates the number of bytes that are available in the current block.
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with OptionalDataException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();	
		
		} catch (InvalidObjectException e) {
			// Thrown when a restored object cannot be made valid.
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with InvalidObjectException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();	

		} catch (NotActiveException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with NotActiveException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();	

		} catch (StreamCorruptedException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with StreamCorruptedException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();	
		
		} catch (NotSerializableException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with NotSerializableException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();	
			
		} catch (ObjectStreamException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with ObjectStreamException when saving " + file + " : " + e.getMessage());
			e.printStackTrace();

		} catch (IOException e0) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": " + Msg.getString("Simulation.log.saveError"), e0); //$NON-NLS-1$
			e0.printStackTrace();

			if (type == SaveType.AUTOSAVE_AS_DEFAULT || type == SaveType.SAVE_DEFAULT) {
//	            backupFile = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//	            backupFile.renameTo(file);

				if (file.exists() && !file.isDirectory()) {
					// Backup the existing default.sim
					Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": " + Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$
			e.printStackTrace();

			if (type == SaveType.AUTOSAVE_AS_DEFAULT || type == SaveType.SAVE_DEFAULT) {
//	            backupFile = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//	            backupFile.renameTo(file);

				if (file.exists() && !file.isDirectory()) {
					// backup the existing default.sim
					Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		}

		finally {
		
			if (xzout != null) {
				xzout.close();
				xzout.finish();
			}
			
			if (oos != null)
				oos.close();
			
			if (is != null)
				is.close();
			
			if (baos != null)
				baos.close();
			
			justSaved = true;

		}
    }
    
	/**
	 * Prints the object and its size
	 * @throws IOException 
	 */
	public StringBuilder printObjectSize(int type) {
      	StringBuilder sb = new StringBuilder();
		
//      	System.out.println("# of collection sites : " + mars.getSurfaceFeatures().getSites().size());
      	
		List<Serializable> list = Arrays.asList( 
				SimulationConfig.instance(),
				ResourceUtil.getInstance(),
				malfunctionFactory,
				mars,
				missionManager,
				medicalManager,
				scientificStudyManager,
				transportManager,
				creditManager,
				eventManager,
				relationshipManager,
				unitManager,
				masterClock
		);
			
		list.sort((Serializable d1, Serializable d2) -> d1.getClass().getSimpleName().compareTo(d2.getClass().getSimpleName())); 
		
		sb.append("      Serializable object | Serialized Size");
		sb.append("  | Object Size");
		sb.append(System.lineSeparator());
		sb.append(" ---------------------------------------------------------"
				+ System.lineSeparator());		
		int max0 = 25;
		int max1 = 10;
		
		String SPACE = " ";
		
		double sumFileSize = 0;
//		double sumSize1 = 0;
		
		String unit = "";
//		String unit1 = "";
		
//		halt();
		masterClock.setPaused(true, false);
		
		for (Serializable o : list) {
			String name = o.getClass().getSimpleName();
			int size0 = max0 - name.length();
			for (int i=0; i<size0; i++) {
				sb.append(SPACE);
			}
			sb.append(name);
			sb.append(SPACE + ":" + SPACE);

			// Get size
			double fileSize = 0;
			
//			long objectSize = 0;
					
//			MemoryMeter meter = new MemoryMeter();
		    
			if (type == 0) {
				fileSize = CheckSerializedSize.getSerializedSize(o);
//				objectSize = meter.countChildren(o);
//				System.out.println("Object Size : " + objectSize);
			}
			else if (type == 1) {
				fileSize = CheckSerializedSize.getSerializedSizeByteArray(o);
//				objectSize = meter.countChildren(o);
//				System.out.println("Object Size : " + objectSize);
			}
			
			sumFileSize += fileSize;
		
			if (fileSize < 1_000) {
				unit = SPACE + "B" + SPACE;
			}
			else if (fileSize < 1_000_000) {
				fileSize = fileSize/1_000D;
				unit = SPACE + "KB";
			}
			else if (fileSize < 1_000_000_000) {
				fileSize = fileSize/1_000_000D;
				unit = SPACE + "MB";
			}
			
			String sizeStr = String.format("%.2f", fileSize) + unit;
			int size = max1 - sizeStr.length();
			for (int i=0; i<size; i++) {
				sb.append(SPACE);
			}
						
//			sumSize1 += objectSize;
//			
//			if (objectSize < 1_000) {
//				unit1 = SPACE + "B" + SPACE;
//			}
//			else if (objectSize < 1_000_000) {
//				objectSize = objectSize/1_000;
//				unit1 = SPACE + "KB";
//			}
//			else if (objectSize < 1_000_000_000) {
//				objectSize = objectSize/1_000_000;
//				unit1 = SPACE + "MB";
//			}
//			
//			String objectSizeStr1 = objectSize + unit1;
//			int objectSize1 = max1 - objectSizeStr1.length();
//			for (int i=0; i<objectSize1; i++) {
//				sb.append(SPACE);
//			}
			
			
			sb.append(sizeStr);
			
//			sb.append("        " + objectSize + unit1);
			
			sb.append(System.lineSeparator());
		}
		
		// Get the total size
		if (sumFileSize < 1_000D) {
			unit = SPACE + "B" + SPACE;
		}
		else if (sumFileSize < 1_000_000D) {
			sumFileSize = sumFileSize/1_000D;
			unit = SPACE + "KB";
		}
		else if (sumFileSize < 1_000_000_000) {
			sumFileSize = sumFileSize/1_000_000D;
			unit = SPACE + "MB";
		}
					
		sb.append(" ---------------------------------------------------------"
				+ System.lineSeparator());	
		
		String name = "Total";
		int size0 = max0 - name.length();
		for (int i=0; i<size0; i++) {
			sb.append(SPACE);
		}
		sb.append(name);
		sb.append(SPACE + ":" + SPACE);

//		sumFileSize = Math.round(sumFileSize*100.0)/100.0;

		String sizeStr = String.format("%.2f", sumFileSize) + unit;
		int size2 = max1 - sizeStr.length();
		for (int i=0; i<size2; i++) {
			sb.append(SPACE);
		}
		
		sb.append(sizeStr + System.lineSeparator());
		
//		proceed();
		masterClock.setPaused(false, false);
		
		return sb;
	}
	
	
	/**
	 * Ends the current simulation
	 */
	public void endSimulation() {
		defaultLoad = false;
		instance().stop();
		if (masterClock != null)
			masterClock.endClockListenerExecutor();
		if (clockThreadExecutor != null)
			clockThreadExecutor.shutdownNow();
	}

	public void endMasterClock() {
		masterClock = null;
	}

	/**
	 * Stop the simulation.
	 */
	public void stop() {
		if (masterClock != null) {
			// simExecutor.shutdown();
			masterClock.stop();
//			 logger.config("just called stop()");
			masterClock.removeClockListener(this);
//			 logger.config("just called removeClockListener()");
		}
	}

	/*
	 * Stops the master clock and removes the Simulation clock listener 
	 * 
	 * @param isPause has it been on pause ?
	 */
	public void halt(boolean isPause) {
		if (masterClock != null) {
			masterClock.stop();
			if (!isPause) masterClock.setPaused(true, false);
			masterClock.removeClockListener(this);
		}
	}

	/*
	 * Restarts the master clock and adds back the Simulation clock listener
	 * 
	 * @param isPause has it been on pause ?
	 */
	public void proceed(boolean isPause) {
		if (masterClock != null) {
			masterClock.addClockListener(this);
			if (!isPause) masterClock.setPaused(false, false);
			masterClock.restart();
		}
	}

	/**
	 * Returns the time string of the last saving or autosaving action
	 */
	public String getLastSaveTimeStamp() {
		if (lastSaveTimeStamp == null || lastSaveTimeStamp.equals(""))
			return "Never     ";
		else if (!changed) {
			return lastSaveTimeStampMod;
		} else {
			changed = false;
			StringBuilder sb = new StringBuilder();
			int l = lastSaveTimeStamp.length();

			// Past : e.g. 03-22-2017_022018PM
			// String s = lastSave.substring(l-8, l);
			// sb.append(s.substring(0, 2)).append(":").append(s.substring(2, 4))
			// .append(" ").append(s.substring(6, 8)).append(" (local time)");

			// Now e.g. 2007-12-03T10.15.30
			// String id = ZonedDateTime.now().getZone().toString();
			String s = lastSaveTimeStamp.substring(lastSaveTimeStamp.indexOf("T") + 1, l).replace(".", ":");
			sb.append(s).append(WHITESPACES).append(LOCAL_TIME);
			lastSaveTimeStampMod = sb.toString();
			return lastSaveTimeStampMod;
		}
	}

	/**
	 * Get the planet Mars.
	 * 
	 * @return Mars
	 */
	public Mars getMars() {
		return mars;
	}

	/**
	 * Get the unit manager.
	 * 
	 * @return unit manager
	 */
	public UnitManager getUnitManager() {
		return unitManager;
	}

	/**
	 * Get the mission manager.
	 * 
	 * @return mission manager
	 */
	public MissionManager getMissionManager() {
		return missionManager;
	}

	/**
	 * Get the relationship manager.
	 * 
	 * @return relationship manager.
	 */
	public RelationshipManager getRelationshipManager() {
		return relationshipManager;
	}

	/**
	 * Gets the credit manager.
	 * 
	 * @return credit manager.
	 */
	public CreditManager getCreditManager() {
		return creditManager;
	}

	/**
	 * Get the malfunction factory.
	 * 
	 * @return malfunction factory
	 */
	public MalfunctionFactory getMalfunctionFactory() {
		return malfunctionFactory;
	}

	/**
	 * Get the historical event manager.
	 * 
	 * @return historical event manager
	 */
	public HistoricalEventManager getEventManager() {
		return eventManager;
	}

	/**
	 * Get the medical manager.
	 * 
	 * @return medical manager
	 */
	public MedicalManager getMedicalManager() {
		return medicalManager;
	}

	/**
	 * Get the scientific study manager.
	 * 
	 * @return scientific study manager.
	 */
	public ScientificStudyManager getScientificStudyManager() {
		return scientificStudyManager;
	}

	/**
	 * Get the transport manager.
	 * 
	 * @return transport manager.
	 */
	public TransportManager getTransportManager() {
		return transportManager;
	}

	/**
	 * Get the master clock.
	 * 
	 * @return master clock
	 */
	public MasterClock getMasterClock() {
		return masterClock;
	}

	/**
	 * Checks if simulation was loaded from default save file.
	 * 
	 * @return true if default load.
	 */
	public boolean isDefaultLoad() {
		return defaultLoad;
	}

	/**
	 * Sets if simulation was loaded with GUI.
	 * 
	 * @param value is true if GUI is in use.
	 */
	public void setUseGUI(boolean value) {
		useGUI = value;
	}

	/**
	 * Checks if simulation was loaded with GUI.
	 * 
	 * @return true if GUI is in use.
	 */
	public boolean getUseGUI() {
		return useGUI;
	}


	public ExecutorService getClockThreadExecutor() {
		return clockThreadExecutor;
	}

	// public PausableThreadPoolExecutor getClockScheduler() {
	// return clockScheduler;
	// }

	public boolean getJustSaved() {
		return justSaved;
	}

	public void setJustSaved(boolean value) {
		justSaved = value;
	}

	// public void setGameWorld(GameWorld gw) {
	// gameWorld = gw;
	// }

	public void setFXGL(boolean isFXGL) {
		this.isFXGL = isFXGL;
	}

	/**
	 * Sends out a clock pulse if using FXGL
	 */
	public void onUpdate(double tpf) {
		if (masterClock != null)
			masterClock.onUpdate(tpf);
	}

//	/**
//	 * Get the interactive terminal instance
//	 * 
//	 * @return {@link InteractiveTerm}
//	 */
//	public InteractiveTerm getTerm() {
//		return interactiveTerm;
//	}
	
	/**
	 * Clock pulse from master clock
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public void clockPulse(double time) {
		if (doneInitializing && ut != null && !clockOnPause && !masterClock.isPaused() && time > Double.MIN_VALUE) {

			ut.updateTime();

//			if (debug) {
//				logger.fine(Msg.getString("Simulation.log.clockPulseMars", //$NON-NLS-1$
//						ut.getUptime(), mars.toString()));
//			}
			mars.timePassing(time);
			ut.updateTime();

//			if (debug) {
//				logger.fine(Msg.getString("Simulation.log.clockPulseMissionManager", //$NON-NLS-1$
//						masterClock.getUpTimer().getUptime(), missionManager.toString()));
//			}
			missionManager.timePassing(time);
			ut.updateTime();

//			if (debug) {
//				logger.fine(Msg.getString("Simulation.log.clockPulseUnitManager", //$NON-NLS-1$
//						masterClock.getUpTimer().getUptime(), unitManager.toString()));
//			}
			unitManager.timePassing(time);
			ut.updateTime();

//			if (debug) {
//				logger.fine(Msg.getString("Simulation.log.clockPulseScientificStudyManager", //$NON-NLS-1$
//						masterClock.getUpTimer().getUptime(), scientificStudyManager.toString()));
//			}
			scientificStudyManager.updateStudies();
			ut.updateTime();

//			if (debug) {
//				logger.fine(Msg.getString("Simulation.log.clockPulseTransportManager", //$NON-NLS-1$
//						masterClock.getUpTimer().getUptime(), transportManager.toString()));
//			}
			transportManager.timePassing(time);
		}
	}

	public boolean getAutosaveDefault() {
		return autosaveDefault;
	}
	
//	/**
//	 * Returns the ObjectMapper instance
//	 * @return {@link ObjectMapper}
//	 */
//	public ObjectMapper getObjectMapper() {
//		return objectMapper; 
//	}
	
	@Override
	public void uiPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		if (isPaused)
			clockOnPause = true;
		else
			clockOnPause = false;
	}

	/**
	 * Destroys the current simulation to prepare for creating or loading a new
	 * simulation.
	 */
	public void destroyOldSimulation() {
		logger.config("Starting destroyOldSimulation()");

//		autosaveService = null;
		AutosaveScheduler.cancel();

		if (malfunctionFactory != null) {
			malfunctionFactory.destroy();
			malfunctionFactory = null;
		}

		if (mars != null) {
			mars.destroy();
			mars = null;
		}
		
		logger.config("Done with mars");

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

		logger.config("Done with medicalManager");
		
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

		 logger.config("Done with destroyOldSimulation()");
	}

}
