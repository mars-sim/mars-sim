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

import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.Role;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.Heating;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.structure.building.function.Recreation;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.structure.building.function.SolarHeatSource;
import org.mars_sim.msp.core.structure.building.function.SolarPowerSource;
import org.mars_sim.msp.core.structure.building.function.SolarThermalPowerSource;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.structure.building.function.WindPowerSource;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.terminal.InteractiveTerm;
import org.mars_sim.msp.core.time.AutosaveScheduler;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.SystemDateTime;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.core.tool.CheckSerializedSize;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.ByteStreams;

//import mikera.gui.Frames;
//import mikera.gui.JConsole;

/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation implements ClockListener, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = -631308653510974249L;

	private static Logger logger = Logger.getLogger(Simulation.class.getName());

	/** The mode to load other file. */ 
	public static final int OTHER = 0;
	/** The mode to save as default.sim. */
	public static final int SAVE_DEFAULT = 1;
	/** The mode to save with other name. */
	public static final int SAVE_AS = 2;
	/** # The mode to autosave as default.sim. */
	public static final int AUTOSAVE_AS_DEFAULT = 3;
	/**  The mode to save with build info/date/time stamp. */
	public static final int AUTOSAVE = 4;
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
	public final static String BUILD = Msg.getString("Simulation.build"); //$NON-NLS-1$
	/** Java version string. */
	private final static String JAVA_TAG = System.getProperty("java.version");
	// VersionInfo.getRuntimeVersion() e.g. "8.0.121-b13 (abcdefg)";																			
	/** Java version string. */
	public final static String JAVA_VERSION = (JAVA_TAG.contains("(") ? JAVA_TAG.substring(0, JAVA_TAG.indexOf("(") - 1)
			: JAVA_TAG);
	/** Vendor string. */
	// public final static String VENDOR = System.getProperty("java.vendor");
	/** OS architecture string. */
	private final static String OS_ARCH = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");
	/** Default save filename. */
	private final static String DEFAULT_FILE = Msg.getString("Simulation.defaultFile"); //$NON-NLS-1$
	/** Default temp filename. */
//	private final static String TEMP_FILE = Msg.getString("Simulation.tempFile"); //$NON-NLS-1$
	/** Default save filename extension. */
	private final static String DEFAULT_EXTENSION = Msg.getString("Simulation.defaultFile.extension"); //$NON-NLS-1$
	/** JSON save filename extension. */
	private final static String JSON_EXTENSION = Msg.getString("Simulation.jsonFile.extension"); //$NON-NLS-1$
	/** local time string */
	private final static String LOCAL_TIME = Msg.getString("Simulation.localTime"); //$NON-NLS-1$ " (Local Time) ";
	/** 2 whitespaces. */
	private final static String WHITESPACES = "  ";
	/** Console directory for saving/loading console related files. */
	public final static String CONSOLE_DIR = "/console";
	/** Save directory. */
	public final static String DEFAULT_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.defaultFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.defaultDir"); //$NON-NLS-1$

	/** autosave directory. */
	public final static String AUTOSAVE_DIR = System.getProperty("user.home") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.defaultFolder") + //$NON-NLS-1$
			File.separator + Msg.getString("Simulation.defaultDir.autosave"); //$NON-NLS-1$

	public final static String MARS_SIM_DIRECTORY = ".mars-sim";

	public final static String title = Msg.getString("Simulation.title", VERSION + " - Build " + BUILD
	// + " - " + VENDOR
			+ " - " + OS_ARCH + " " + JAVA_VERSION + " - " + NUM_THREADS
			+ ((NUM_THREADS == 1) ? " CPU thread" : " CPU threads")); // $NON-NLS-1$

	private static final boolean debug = false; // logger.isLoggable(Level.FINE);
	/** true if displaying graphic user interface. */
	private static boolean useGUI = true;
	/** Flag to indicate that a new simulation is being created or loaded. */
	private static boolean isUpdating = false;

	private static boolean defaultLoad = false;

	private static boolean justSaved = true;

	private static boolean autosaveDefault;
	
	private static boolean clockOnPause = false;
	
	private boolean initialSimulationCreated = false;

	private boolean changed = true;

	private boolean isFXGL = false;

	/** The modified time stamp of the last saved sim */	
	private String lastSaveTimeStampMod;
	/** The time stamp of the last saved sim. */
	private String lastSaveTimeStamp = null;
	/** The build version of the SimulationConfig of the loading .sim */
	private String loadBuild;// = "unknown";
	
	// Note: Transient data members aren't stored in save file
	private transient ExecutorService clockThreadExecutor;

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
	/** The GameWorld instance for FXGL frameworld */
	// private GameWorld gameWorld;

	private UpTimer ut;
	
	private static ObjectMapper objectMapper;
	
	private static InteractiveTerm interactiveTerm;
	
	/**
	 * Private constructor for the Singleton Simulation. This prevents instantiation
	 * from other classes.
	 */
	private Simulation() {
		// INFO Simulation's constructor is on both JavaFX-Launcher Thread
//      initializeTransientData();
		// Create Interactive Terminal instance
		interactiveTerm = new InteractiveTerm();
		// Create ObjectMapper instance
		objectMapper = new ObjectMapper();
		// Configure Object mapper for pretty print
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		// to allow serialization of "empty" POJOs (no properties to serialize)
		// (without this setting, an exception is thrown in those cases)
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		
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
		// INFO: Simulation's startSimExecutor() is on JavaFX-Launcher Thread
		simExecutor = Executors.newSingleThreadExecutor();
	}

	public ExecutorService getSimExecutor() {
		return simExecutor;
	}

	/**
	 * Checks if the simulation is in a state of creating a new simulation or
	 * loading a saved simulation.
	 * 
	 * @return true is simulation is in updating state.
	 */
	public static boolean isUpdating() {
		return isUpdating;
	}

	/**
	 * Creates a new simulation instance.
	 */
	public static void createNewSimulation(int timeRatio, boolean loadSaveSim) {
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

		// Copy build version. Usable for comparison when loading a saved sim
		SimulationConfig.instance().build = Simulation.BUILD;
	}

//	/**
//	 * Initialize transient data in the simulation.
//	 */
//    private void initializeTransientData() {
//       //logger.config("Simulation's initializeTransientData() is on " + Thread.currentThread().getName() + " Thread");
//       eventManager = new HistoricalEventManager();
//    }

	/**
	 * Initialize intransient data in the simulation.
	 */
	private void initializeIntransientData(int timeRatio, boolean loadSaveSim) {
		malfunctionFactory = new MalfunctionFactory();
		mars = new Mars();
		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		masterClock = new MasterClock(isFXGL, timeRatio);
		unitManager = new UnitManager();
		unitManager.constructInitialUnits(loadSaveSim); // unitManager needs to be on the same thread as masterClock
		eventManager = new HistoricalEventManager();
		creditManager = new CreditManager();
		scientificStudyManager = new ScientificStudyManager();
		transportManager = new TransportManager();

		// ResourceUtil.getInstance().initializeNewSim();
		// ResourceUtil.printID();
		
		// Initialize mars clock
		MarsClock marsClock = masterClock.getMarsClock();
		MalfunctionFactory.setMarsClock(marsClock);
		MissionManager.setMarsClock(marsClock);
		MalfunctionManager.initializeInstances(masterClock, marsClock);
		
		// Initialize instances
//		MedicalManager.initializeInstances();
		mars.initializeTransientData();
		mars.getOrbitInfo().initializeTransientData();
		mars.getWeather().initializeTransientData();
		Inventory.initializeInstances(mars.getMarsSurface());
		Mission.initializeInstances();
		
		ut = masterClock.getUpTimer();
		
		logger.config("Done initializing intransient data.");
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
			// logger.config("StartTask's run() is on " + Thread.currentThread().getName());
			start(autosaveDefault);
		}
	}

	/**
	 * Starts the simulation.
	 * 
	 * @param autosaveDefault. True if default is used for autosave
	 */
	public void start(boolean autosaveDefault) {
		// SwingUtilities.invokeLater(() -> testConsole());
		
		masterClock.addClockListener(this);
		masterClock.startClockListenerExecutor();

		restartClockExecutor();

		Simulation.autosaveDefault = autosaveDefault;
		AutosaveScheduler.defaultStart();
		ut = masterClock.getUpTimer();
	}

	/**
	 * Starts or restarts the executive service thread that the MasterClock's ClockThreadTask runs on.
	 */
	public void restartClockExecutor() {
		//if (clockExecutor == null || clockExecutor.isShutdown() || clockExecutor.isTerminated()) {

			clockThreadExecutor = Executors.newSingleThreadExecutor();

			// if (NUM_THREADS <= 3)
			// clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			// clockScheduler = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();
			// else if (NUM_THREADS <= 8)
			// clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);//
			// newSingleThreadExecutor();// newCachedThreadPool(); //
			// else if (NUM_THREADS <= 16)
			// clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);//
			// newSingleThreadExecutor();// newCachedThreadPool(); //
			// else
			// clockScheduler = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);//
			// newSingleThreadExecutor();// newCachedThreadPool(); //

			if (masterClock.getClockThreadTask() != null)
				clockThreadExecutor.execute(masterClock.getClockThreadTask());
		//}
	}

	/**
	 * Get the interactive terminal instance
	 * 
	 * @return {@link InteractiveTerm}
	 */
	public InteractiveTerm getITerm() {
		return interactiveTerm;
	}
	
//	/*
//	 * Obtains the size of the file
//	 * 
//	 * @return fileSize in megabytes
//	 */
//	public double getFileSize() {
//		return fileSize;
//	}

	/**
	 * Loads a simulation instance from a save file.
	 * 
	 * @param file the file to be loaded from.
	 */
	public void loadSimulation(final File file) {
		// logger.config("Simulation's loadSimulation() is on " +
		// Thread.currentThread().getName());
		isUpdating = true;

		File f = file;

		Simulation sim = instance();
		sim.stop();

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
			f = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
			// logger.config("file is " + f);
			sim.defaultLoad = true;
		} else {
			sim.defaultLoad = false;
		}

		if (f.exists() && f.canRead()) {
//			logger.config(" - - - - - - - - - - - - - -");
//			logger.config(Msg.getString("Simulation.log.loadSimFrom", f)); //$NON-NLS-1$

			try {
				sim.readFromFile(f);

			} catch (ClassNotFoundException e2) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim with ClassNotFoundException when loading the simulation : "
								+ e2.getMessage());
//    	        Platform.exit();
				System.exit(1);

			} catch (IOException e1) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim with IOException when loading the simulation : " + e1.getMessage());
//    	        Platform.exit();
				System.exit(1);

			} catch (NullPointerException e) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim with NullPointerException when loading the simulation : " + e.getMessage());
//    	        Platform.exit();
				System.exit(1);
			
			} catch (Exception e0) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim. Could not load the simulation : " + e0.getMessage());
//    	        Platform.exit();
				System.exit(1);
			}
		}

		else {
			logger.log(Level.SEVERE, "Quitting mars-sim. The saved sim cannot be read or is NOT found.");
			System.exit(1);
		}
	}

	private synchronized void readJSON() throws JsonParseException, JsonMappingException, IOException {

		String name = mars.getClass().getSimpleName();
		File file = new File(DEFAULT_DIR, name + JSON_EXTENSION);
			
		if (file.exists() && file.canRead()) {
			// Use Jackson json to read json file data to String
			byte[] jsonData = Files.readAllBytes(file.toPath());//Paths.get("Mars.json"));
			System.out.println(new String(jsonData));
			
//	        mars = objectMapper.readValue(FileUtils.readFileToByteArray(file, Mars.class);
//	        System.out.println(mars);
	        
			// Use Jackson json to read
//			mars = objectMapper.readValue(file, Mars.class);
//			System.out.println(mars);
		}
	}
	
    /**
     * Deserialize to Object from given file.
     */
    public void deserialize(File file) throws IOException,
            ClassNotFoundException {

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
			
			// Limit memory usage to 256 MB
			xzin = new XZInputStream(new BufferedInputStream(in), 256 * 1024);
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
			SimulationConfig.setInstance((SimulationConfig) ois.readObject());		
			ResourceUtil.setInstance((ResourceUtil) ois.readObject());

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
		
			logger.config("    Martian Date/Time Stamp : " + masterClock.getMarsClock().getDateTimeStamp());
			logger.config(" --------------------------------------------------------------------");			
			if (Simulation.BUILD.equals(loadBuild)) {
				logger.config(" Note : Both Builds are matched.");
			} else {
				logger.config(" Note : The Builds are NOT matched.");
				logger.warning("Attempting to load the saved sim made in build " + loadBuild
					+ " while running mars-sim build " + Simulation.BUILD);
			}		
			logger.config("  - - - - - - - - - Sol " + masterClock.getMarsClock().getMissionSol() 
					+ " (Cont') - - - - - - - - - - - ");

		// Note: see https://docs.oracle.com/javase/7/docs/platform/serialization/spec/exceptions.html
		} catch (WriteAbortedException e) {
			// Thrown when reading a stream terminated by an exception that occurred while the stream was being written.
			logger.log(Level.SEVERE, "Quitting mars-sim with WriteAbortedException when loading " + file + " : " + e.getMessage());
			System.exit(1);		

		} catch (OptionalDataException e) {
			// Thrown by readObject when there is primitive data in the stream and an object is expected. The length field of the exception indicates the number of bytes that are available in the current block.
			logger.log(Level.SEVERE, "Quitting mars-sim with OptionalDataException when loading " + file + " : " + e.getMessage());
			System.exit(1);		
		
		} catch (InvalidObjectException e) {
			// Thrown when a restored object cannot be made valid.
			logger.log(Level.SEVERE, "Quitting mars-sim with InvalidObjectException when loading " + file + " : " + e.getMessage());
			System.exit(1);		

		} catch (NotActiveException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with NotActiveException when loading " + file + " : " + e.getMessage());
			System.exit(1);		

		} catch (StreamCorruptedException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with StreamCorruptedException when loading " + file + " : " + e.getMessage());
			System.exit(1);		
		
		} catch (NotSerializableException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with NotSerializableException when loading " + file + " : " + e.getMessage());
			System.exit(1);		
			
		} catch (ObjectStreamException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with ObjectStreamException when loading " + file + " : " + e.getMessage());
			System.exit(1);
			
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with FileNotFoundException since " + file + " cannot be found : ", e.getMessage());
			System.exit(1);

		} catch (EOFException e) {
			logger.log(Level.SEVERE,
					"Quitting mars-sim. Unexpected End of File error on " + file + " : " + e.getMessage());
			System.exit(1);

		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"Quitting mars-sim. IOException when decompressing " + file + " : " + e.getMessage());
			System.exit(1);

		} catch (NullPointerException e) {
			logger.log(Level.SEVERE,
					"Quitting mars-sim. NullPointerException when loading " + file + " : " + e.getMessage());
			System.exit(1);

		} catch (Exception e) {
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
	private synchronized void readFromFile(File file) throws ClassNotFoundException, IOException {
		// logger.config("Simulation : running readFromFile()");
		logger.config("Loading and processing the saved sim. Please wait...");
		
//		System.out.println(file.length() / 1000D);
//		// Compute the size of the saved sim
//		String sizeStr = computeFileSize(file);

		loadBuild = SimulationConfig.instance().build;
		if (loadBuild == null)
			loadBuild = "unknown";
		
//		logger.config("Proceed to loading the saved sim.");
		String filename = file.getName();
		String path = file.getPath().replace(filename, "");
		logger.config(" --------------------------------------------------------------------");
		logger.config("                      Saved Simulation                               ");
		logger.config(" --------------------------------------------------------------------");
		logger.config("                   Filename : " + filename);
		logger.config("                       Path : " + path);
		logger.config("                       Size : " + computeFileSize(file));
		logger.config("              Made in Build : " + loadBuild);
		logger.config("  Current Core Engine Build : " + Simulation.BUILD);
		
		// Deserialize the file
		deserialize(file);

		// Initialize transient data.
//	        instance().initializeTransientData();
		instance().initialSimulationCreated = true;
		isUpdating = false;
		
		// Re-initialize instances
		reinitializeInstances();

	}

	
	/**
	 *  Re-initialize instances after loading from a saved sim
	 */
	private void reinitializeInstances() {
		// Re-initialize the resources for the saved sim
		ResourceUtil.getInstance().justReloaded();
		// Re-initialize the MarsSurface instance
		unitManager.setMarsSurface();
		// Re-initialize the Simulation instance
		MasterClock.justReloaded(this);					
		// Re-initialize the Mars instance
		MarsClock.justReloaded(mars);	
		// Gets he MarsClock instance
		MarsClock marsClock = masterClock.getMarsClock();
		// Gets he MarsClock instance
		EarthClock earthClock = masterClock.getEarthClock();
		// Re-initialize the SurfaceFeatures instance
		SurfaceFeatures surface = mars.getSurfaceFeatures();
		// Gets the Weather instance
		Weather w = mars.getWeather();
		// Gets the orbitInfo instance
		OrbitInfo orbitInfo = mars.getOrbitInfo();
		// Gets MarsSurface instance
		MarsSurface marsSurface = mars.getMarsSurface();
		
		// Re-initialize Mars environmental instances
		Weather.justReloaded(masterClock, marsClock, mars, surface, orbitInfo); // terrain
		SurfaceFeatures.justReloaded(masterClock, mars, this, w, orbitInfo, missionManager);  // sunDirection, landmarks
		OrbitInfo.justReloaded(marsClock, earthClock);			

//		System.out.println("Done with Mars environment instances");
		
		// Gets config file instances
		BuildingConfig bc = SimulationConfig.instance().getBuildingConfiguration();
		PersonConfig pc = SimulationConfig.instance().getPersonConfiguration();
		
		// Re-initialize static class
		MalfunctionFactory.setMarsClock(marsClock);
		MissionManager.setMarsClock(marsClock);
//		MedicalManager.justReloaded();
		unitManager.justReloaded(marsClock);
		MalfunctionManager.initializeInstances(masterClock, marsClock);
		TransportManager.initializeInstances(marsClock, eventManager);

//		System.out.println("Done with Serialized Object instances");
		
		Resupply.initializeInstances(bc);
		
		// Re-initialize Unit related class
		Inventory.initializeInstances(mars.getMarsSurface());
		Unit.justReloaded(mars);
		Equipment.justReloaded(unitManager);
		EVASuit.justReloaded(w);				
		Person.justReloaded(masterClock, marsClock, this, mars, marsSurface, earthClock);
		Robot.justReloaded(masterClock, marsClock);
		Vehicle.justReloaded(missionManager);				//  vehicleconfig 
		GroundVehicle.justReloaded(surface);				//  terrain
		Rover.justReloaded(pc);					
		
//		System.out.println("Done with Unit Object instances");
		
		// Re-initialize Person/Robot related class
		Mind.justReloaded(marsClock, this, missionManager);					// relationship
		BotMind.justReloaded(marsClock);
		TaskManager.justReloaded(marsClock, missionManager);	
		PhysicalCondition.justReloaded(masterClock, marsClock);
		RadiationExposure.justReloaded(masterClock, marsClock);
		Role.justReloaded(marsClock);
		
		// Re-initialize Structure related class
		Building.justReloaded(masterClock, marsClock);
		BuildingManager.justReloaded(masterClock, marsClock);
		Settlement.justReloaded(marsClock, w, unitManager);		// loadDefaultValues()
		ChainOfCommand.justReloaded(marsClock, unitManager);
		GoodsManager.justReloaded(marsClock, missionManager, unitManager);
		
//		System.out.println("Done with Structure instances");
		
		// Re-initialize Building function related class
//		Administration.initializeInstances(bc);
		CircadianClock.justReloaded(marsClock);
		Cooking.justReloaded(marsClock, bc);
		Crop.justReloaded(masterClock, marsClock);
		CompositionOfAir.justReloaded(masterClock, marsClock, pc);
		EVA.justReloaded(bc);
		Farming.justReloaded(marsClock);
		GroundVehicleMaintenance.justReloaded(bc);
		Heating.justReloaded(masterClock, marsClock, mars);
		LivingAccommodations.justReloaded(marsClock);
		Manufacture.justReloaded(marsClock);
		ResourceProcessing.justReloaded(bc);
		ResourceProcess.justReloaded(marsClock);
		RoboticStation.justReloaded(bc);
		PowerGeneration.justReloaded(bc);
		PowerStorage.justReloaded(marsClock, bc);
		Recreation.justReloaded(bc);
		Research.justReloaded(marsClock, bc);
		SolarHeatSource.justReloaded(mars, surface);
		SolarThermalPowerSource.justReloaded(surface); 
		SolarPowerSource.justReloaded(mars, surface);
		Storage.justReloaded(bc);
		ThermalGeneration.justReloaded(bc);
		WindPowerSource.justReloaded(w);
		
//		System.out.println("Done with Building function instances");
		
		// Re-initialize Task related class
		Task.justReloaded(eventManager);		// eventManager, relationshipmanager
		Walk.justReloaded(unitManager);	
		EVAOperation.justReloaded(surface); 
		LoadVehicleGarage.initializeInstances(pc); 
		Relax.justReloaded(marsClock);
		Sleep.justReloaded(masterClock, marsClock);
		
//		System.out.println("Done with Task instances");
		
		// Re-initialize Mission related class
		Mission.justReloaded(marsClock, eventManager, missionManager, surface, pc);  // ScientificStudyManager
//		RoverMission.justReloaded(eventManager);  // eventManager
//		VehicleMission.justReloaded(missionManager); // missionmgr
//		RescueSalvageVehicle.justReloaded(eventManager);  // eventManager

//		System.out.println("Done with mission instances");
	}
	
	
	/**
	 * Writes the JSON file
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void writeJSON() throws JsonGenerationException, JsonMappingException, IOException {	
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
		
		Object o = mars;
		String name = o.getClass().getSimpleName();
		
//		objectMapper.writeValue(stringEmp, o);
//		System.out.println("JSON representation of the Class '" + name + "' :\n" + stringEmp);
//		// Write to the file
		objectMapper.writeValue(new File(DEFAULT_DIR, name + JSON_EXTENSION), o);
		
		String json = objectMapper.writeValueAsString(o) ; 
		System.out.println("JSON representation of the Class '" + name + "' :\n" + json);
		
	}
	
	
	/**
	 * Saves a simulation instance to a save file.
	 * 
	 * @param file the file to be saved to.
	 */
	public synchronized void saveSimulation(int type, File file) throws IOException {
		Simulation sim = instance();
		sim.halt();

		// Experiment with saving in JSON format
//		writeJSON();
		
		lastSaveTimeStamp = new SystemDateTime().getDateTimeStr();
		changed = true;

		File backupFile = new File(DEFAULT_DIR, "previous" + DEFAULT_EXTENSION);
		FileSystem fileSys = null;
		Path destPath = null;
		Path srcPath = null;

		int missionSol = masterClock.getMarsClock().getMissionSol();
		
		// Use type to differentiate in what name/dir it is saved
		if (type == SAVE_DEFAULT) {

			file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);

			if (file.exists() && !file.isDirectory()) {
				fileSys = FileSystems.getDefault();
				destPath = fileSys.getPath(backupFile.getPath());
				srcPath = fileSys.getPath(file.getPath());
				// Backup the existing default.sim
				Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}

			logger.config("Saving the simulation as " + DEFAULT_FILE + DEFAULT_EXTENSION + ".");

		}

		else if (type == SAVE_AS) {
			String f = file.getName();
			String dir = file.getParentFile().getAbsolutePath();
			if (!f.contains(".sim"))
				file = new File(dir, f + DEFAULT_EXTENSION);
			logger.config("Saving the simulation as " + file + "...");
		}

		else if (type == AUTOSAVE_AS_DEFAULT) {

//            file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//            logger.config("Autosaving as " + DEFAULT_FILE + DEFAULT_EXTENSION);

			file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);

			if (file.exists() && !file.isDirectory()) {
				fileSys = FileSystems.getDefault();
				destPath = fileSys.getPath(backupFile.getPath());
				srcPath = fileSys.getPath(file.getPath());
				// Backup the existing default.sim
				Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}

			logger.config("Autosaving the simulation as " + DEFAULT_FILE + DEFAULT_EXTENSION + ".");

		}

		else if (type == AUTOSAVE) {
			String autosaveFilename = lastSaveTimeStamp + "_Sol" + missionSol + "_r" + BUILD
					+ DEFAULT_EXTENSION;
			file = new File(AUTOSAVE_DIR, autosaveFilename);
			logger.config("Autosaving the simulation as " + autosaveFilename + "...");

		}

		// if the autosave/default save directory does not exist, create one now
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		// Serialize the file
		serialize(type, file, srcPath, destPath);
		
		sim.proceed();

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
    
    /**
     * Serialize the given object and save it to a given file.
     */
    public void serialize(int type, File file, Path srcPath, Path destPath)
            throws IOException {

		// Replace gzip with xz compression (based on LZMA2)
		// See (1)
		// http://stackoverflow.com/questions/5481487/how-to-use-lzma-sdk-to-compress-decompress-in-java
		// (2) http://tukaani.org/xz/xz-javadoc/

		// STEP 1: combine all objects into one single uncompressed file, namely
		// "default"
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    InputStream is = null;
		XZOutputStream xzout = null;
		
		try {
	
			// Set a delay for 200 millis to avoid java.util.ConcurrentModificationException
			delay(200L);
			
			// Store the in-transient objects.
			oos.writeObject(SimulationConfig.instance());
			oos.writeObject(ResourceUtil.getInstance());
			oos.writeObject(malfunctionFactory);
			oos.writeObject(mars);
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
			LZMA2Options lzma2 = new LZMA2Options(7);
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
			logger.log(Level.SEVERE, "Quitting mars-sim with WriteAbortedException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();		

		} catch (OptionalDataException e) {
			// Thrown by readObject when there is primitive data in the stream and an object is expected. The length field of the exception indicates the number of bytes that are available in the current block.
			logger.log(Level.SEVERE, "Quitting mars-sim with OptionalDataException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();	
		
		} catch (InvalidObjectException e) {
			// Thrown when a restored object cannot be made valid.
			logger.log(Level.SEVERE, "Quitting mars-sim with InvalidObjectException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();	

		} catch (NotActiveException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with NotActiveException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();	

		} catch (StreamCorruptedException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with StreamCorruptedException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();	
		
		} catch (NotSerializableException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with NotSerializableException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();	
			
		} catch (ObjectStreamException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with ObjectStreamException when loading " + file + " : " + e.getMessage());
			e.printStackTrace();

		} catch (IOException e0) {
			logger.log(Level.SEVERE, Msg.getString("Simulation.log.saveError"), e0); //$NON-NLS-1$
			e0.printStackTrace();

			if (type == AUTOSAVE_AS_DEFAULT || type == SAVE_DEFAULT) {
//	            backupFile = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//	            backupFile.renameTo(file);

				if (file.exists() && !file.isDirectory()) {
					// Backup the existing default.sim
					Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$
			e.printStackTrace();

			if (type == AUTOSAVE_AS_DEFAULT || type == SAVE_DEFAULT) {
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
		
		sb.append("      Serializable object :     Size  (before compression)"
				+ System.lineSeparator());
		sb.append(" ---------------------------------------------------------"
				+ System.lineSeparator());		
		int max0 = 25;
		int max1 = 10;
		String SPACE = " ";
		
		double sumSize = 0;
		String unit = "";
		
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
			double size = 0;
			
			if (type == 0)
				size = CheckSerializedSize.getSerializedSize(o);
			else if (type == 1)
				size = CheckSerializedSize.getSerializedSizeByteArray(o);

			sumSize += size;
			
			if (size < 1_000D) {
				unit = SPACE + "B" + SPACE;
			}
			else if (size < 1_000_000D) {
				size = size/1_000D;
				unit = SPACE + "KB";
			}
			else if (size < 1_000_000_000) {
				size = size/1_000_000D;
				unit = SPACE + "MB";
			}
			
			size = Math.round(size*10.0)/10.0;
			
			String sizeStr = size + unit;
			int size1 = max1 - sizeStr.length();
			for (int i=0; i<size1; i++) {
				sb.append(SPACE);
			}
			
			sb.append(size + unit
					+ System.lineSeparator());
		}
		
		// Get the total size
		if (sumSize < 1_000D) {
			unit = SPACE + "B" + SPACE;
		}
		else if (sumSize < 1_000_000D) {
			sumSize = sumSize/1_000D;
			unit = SPACE + "KB";
		}
		else if (sumSize < 1_000_000_000) {
			sumSize = sumSize/1_000_000D;
			unit = SPACE + "MB";
		}
		
		sumSize = Math.round(sumSize*10.0)/10.0;
		
		
		sb.append(" ---------------------------------------------------------"
				+ System.lineSeparator());	
		
		String name = "Total";
		int size0 = max0 - name.length();
		for (int i=0; i<size0; i++) {
			sb.append(SPACE);
		}
		sb.append(name);
		sb.append(SPACE + ":" + SPACE);

		String sizeStr = sumSize + unit;
		int size2 = max1 - sizeStr.length();
		for (int i=0; i<size2; i++) {
			sb.append(SPACE);
		}
		
		sb.append(sumSize + unit
				+ System.lineSeparator());
		
//		proceed();
		masterClock.setPaused(false, false);
		
		return sb;
	}
	
	
	/**
	 * Ends the current simulation
	 */
	public void endSimulation() {
		interactiveTerm.setKeepRunning(false);
		interactiveTerm.disposeTerminal();
		instance().defaultLoad = false;
		instance().stop();
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
			masterClock.removeClockListener(this);
		}
	}

	/*
	 * Stops and removes the master clock and pauses the simulation
	 */
	public void halt() {
		if (masterClock != null) {
			masterClock.stop();
			masterClock.setPaused(true, false);
			masterClock.removeClockListener(this);
		}
	}

	/*
	 * Adds and starts the master clock and unpauses the simulation
	 */
	public void proceed() {
		if (masterClock != null) {
			masterClock.addClockListener(this);
			masterClock.setPaused(false, false);
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
	public static void setUseGUI(boolean value) {
		useGUI = value;
	}

	/**
	 * Checks if simulation was loaded with GUI.
	 * 
	 * @return true if GUI is in use.
	 */
	public static boolean getUseGUI() {
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

	public InteractiveTerm getTerm() {
		return interactiveTerm;
	}
	
	/**
	 * Clock pulse from master clock
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public void clockPulse(double time) {
		if (ut != null && !clockOnPause && !masterClock.isPaused()) {

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
	
	/**
	 * Returns the ObjectMapper instance
	 * @return {@link ObjectMapper}
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper; 
	}

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
		// logger.config("starting Simulation's destroyOldSimulation()");

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

//        if (managerExecutor != null) {
//            managerExecutor.shutdownNow();
//            managerExecutor = null;
//        }

		// logger.config("Simulation's destroyOldSimulation() is done");
	}

}
