/*
 * Mars Simulation Project
 * Simulation.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.mars_sim.msp.core.data.DataLogger;
import org.mars_sim.msp.core.environment.Environment;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.SolarHeatSource;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.construction.SalvageValues;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.time.AutosaveScheduler;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.SystemDateTime;
import org.mars_sim.msp.core.tool.CheckSerializedSize;


/**
 * The Simulation class is the primary singleton class in the MSP simulation.
 * It's capable of creating a new simulation or loading/saving an existing one.
 */
public class Simulation implements ClockListener, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = -631308653510974249L;

	private static final Logger logger = Logger.getLogger(Simulation.class.getName());

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
	public final static String JAVA_VERSION = "Java " + (JAVA_TAG.contains("(") ? 
			JAVA_TAG.substring(0, JAVA_TAG.indexOf("(") - 1) : JAVA_TAG);
	/** Vendor string. */
	// public final static String VENDOR = System.getProperty("java.vendor");
	/** OS architecture string. */
	private final static String OS_ARCH = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");
	/** Default save filename. */
	public final static String SAVE_FILE = Msg.getString("Simulation.saveFile"); //$NON-NLS-1$
	/** Default save filename extension. */
	public final static String SAVE_FILE_EXTENSION = Msg.getString("Simulation.saveFile.extension"); //$NON-NLS-1$
	/** local time string */
	private final static String LOCAL_TIME = Msg.getString("Simulation.localTime"); //$NON-NLS-1$ " (Local Time) ";
	/** 2 whitespaces. */
	private final static String WHITESPACES = "  ";
	
	public final static String title = Msg.getString("Simulation.title", VERSION + " - Build " + BUILD
	// + " - " + VENDOR
			+ " - " + OS_ARCH + " " + JAVA_VERSION + " - " + NUM_THREADS
			+ ((NUM_THREADS == 1) ? " CPU thread" : " CPU threads")); // $NON-NLS-1$

	/** The minimum size of heap space in bytes */
	public final static int MIN_HEAP_SPACE = 64*1024*1024;
	
	/** true if displaying graphic user interface. */
	private transient boolean useGUI = true;
	/** Flag to indicate that a new simulation is being created or loaded. */
	private transient boolean isUpdating = false;
	/** Flag to keep track of whether the initial state of simulation has been initialized. */
	private transient boolean doneInitializing = false;

	private transient boolean justSaved = true;

	private transient boolean autosaveDefault;
	
	private transient boolean clockOnPause = false;
	
	private boolean initialSimulationCreated = false;

	private boolean changed = true;

	/** Mission sol at the time of starting this sim. */
	public static int MISSION_SOL = 0;
	/** msols at the time of starting this sim. */
	public static int MSOL_CACHE = 0;
	
	/** The modified time stamp of the last saved sim */	
	private String lastSaveTimeStampMod;
	/** The time stamp of the last saved sim. */
	private String lastSaveTimeStamp = null;

	// Intransient data members (stored in save file)
	/** Planet Mars. */
	private Environment mars;
	/** Planet Mars. */	
	private OrbitInfo orbit;
	/** All historical info. */
	private HistoricalEventManager eventManager;
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
	/** The SimulationConfig instance. */
	private SimulationConfig simulationConfig;

	
	/**
	 * Private constructor for the Singleton Simulation. This prevents instantiation
	 * from other classes.
	 */
	private Simulation() {
		// INFO Simulation's constructor is on both JavaFX-Launcher Thread
	}

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
	public void createNewSimulation(int timeRatio) {
		isUpdating = true;
		
		logger.config(Msg.getString("Simulation.log.createNewSim")); //$NON-NLS-1$
		
		Simulation sim = instance();

		// Destroy old simulation.
		if (sim.initialSimulationCreated) {
			sim.destroyOldSimulation();
		}
				
		sim.initialSimulationCreated = true;

		// Initialize intransient data members.
		sim.initializeIntransientData(timeRatio);

		isUpdating = false;

		// Preserve the build version tag for future build 
		// comparison when loading a saved sim
		unitManager.originalBuild = Simulation.BUILD;
	}


	public void testRun() {		
		Simulation sim = Simulation.instance();
		ResourceUtil.getInstance();

		// Create marsClock instance
		masterClock = new MasterClock(256);
		MarsClock marsClock = masterClock.getMarsClock();
		EarthClock earthClock = masterClock.getEarthClock();
		
		mars = new Environment(marsClock);
		unitManager = new UnitManager();
		
		// Build plantary objects
		MarsSurface marsSurface = new MarsSurface();
		unitManager.addUnit(marsSurface);
		
		// Gets the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
		
		Inventory.initializeInstances(unitManager);
        RoleUtil.initialize();

		medicalManager = new MedicalManager();
		
		// Set instances for logging
		LogConsolidated.initializeInstances(marsClock, earthClock);
		
		
		Unit.setUnitManager(unitManager);
		Unit.initializeInstances(masterClock, marsClock, earthClock, sim, mars, 
				 mars.getWeather(), surfaceFeatures, new MissionManager());

	}
	
	/**
	 * Initialize intransient data in the simulation.
	 */
	private void initializeIntransientData(int timeRatio) {
		// Initialize resources
		ResourceUtil.getInstance();
		
		// Clock is always first
		masterClock = new MasterClock(timeRatio);
		MarsClock marsClock = masterClock.getMarsClock();
		EarthClock earthClock = masterClock.getEarthClock();
		
		// Initialize serializable objects
		malfunctionFactory = new MalfunctionFactory();
		mars = new Environment(marsClock);
		orbit = new OrbitInfo(marsClock);

		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		scientificStudyManager = new ScientificStudyManager();
	
		// Gets the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
		surfaceFeatures.initializeTransientData();
				
		// Initialize units prior to starting the unit manager
		Unit.initializeInstances(masterClock, marsClock, earthClock, this, mars, 
				mars.getWeather(), surfaceFeatures, missionManager);
		TaskManager.initializeInstances(marsClock);
		
		// Initialize serializable managers
		unitManager = new UnitManager(); 
		
		// Build plantary objects
		MarsSurface marsSurface = new MarsSurface();
		unitManager.addUnit(marsSurface);
				
		Inventory.initializeInstances(unitManager);
		Airlock.initializeInstances(unitManager, marsSurface);
	
		// Gets the MarsSurface instance
		Unit.setUnitManager(unitManager);
		
		// Gets config file instances
		simulationConfig = SimulationConfig.instance();
		BuildingConfig bc = simulationConfig.getBuildingConfiguration();
		PersonConfig pc = simulationConfig.getPersonConfig();
		CropConfig cc = simulationConfig.getCropConfiguration();
		
		ResourceProcess.initializeInstances(marsClock);
		Function.initializeInstances(bc, marsClock, pc, cc, surfaceFeatures,
								     mars.getWeather(), unitManager);
		// Initialize meta tasks
		MetaTaskUtil.initializeMetaTasks();
		
		eventManager = new HistoricalEventManager();
		creditManager = new CreditManager();
		transportManager = new TransportManager();

        // Initialize ManufactureUtil
        new ManufactureUtil();
        RoleUtil.initialize();

		// Set instances for logging
		LogConsolidated.initializeInstances(marsClock, earthClock);

		// Initialize instances prior to UnitManager initiatiation		
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory, medicalManager, eventManager);
		RelationshipManager.initializeInstances(unitManager);
		RadiationExposure.initializeInstances(marsClock);
		
		//  Re-initialize the GameManager
		GameManager.initializeInstances(unitManager);
							
		// Set instances for classes that extend Unit and Task and Mission
		Mission.initializeInstances(this, marsClock, eventManager, unitManager, scientificStudyManager, 
				surfaceFeatures, missionManager, relationshipManager, pc, creditManager);
		Task.initializeInstances(marsClock, eventManager, relationshipManager, unitManager, 
				scientificStudyManager, surfaceFeatures, orbit, missionManager, pc);

		doneInitializing = true;
	}

	/**
	 * Starts the simulation.
	 * 
	 * @param autosaveDefault True if default is used for autosave
	 */
	public void startClock(boolean autosaveDefault) {
		masterClock.addClockListener(this);

		this.autosaveDefault = autosaveDefault;
		AutosaveScheduler.defaultStart();
		
		masterClock.start();
	}
	
	/**
	 * Loads a simulation instance from a save file.
	 * 
	 * @param file the file to be loaded from.
	 */
	public void loadSimulation(final File file) {
		isUpdating = true;

		File f = file;
		
		Simulation sim = instance();
		if (f == null) {		
			// Try the default file path if file is null.
			f = new File(SimulationFiles.getSaveDir(), SAVE_FILE + SAVE_FILE_EXTENSION);
		} 
		
		logger.config("The file to be loaded is " + f);
		
		if (f.exists() && f.canRead()) {

			try {
				sim.readFromFile(f);
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "Problem loading file: ", e);	
			}
		}

		else {
			logger.log(Level.SEVERE, "Quitting mars-sim. The saved sim cannot be read/found.");
			System.exit(1);
		}
		
		// Call up garbage collector. But it's up to the gc what it will do.
		System.gc();
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
    private void deserialize(File file) throws IOException, ClassNotFoundException {

		FileInputStream in = null;
	    ObjectInputStream ois = null;

		try {
			in = new FileInputStream(file);

			// Stream the file directly into the Object stream to reduce memory
			ois = new ObjectInputStream(new GZIPInputStream(in));

			// Load remaining serialized objects
			malfunctionFactory = (MalfunctionFactory) ois.readObject();
			mars = (Environment) ois.readObject();
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
		} finally {
			
			if (ois != null) {
				ois.close();
			}

			if (in != null) {
				in.close();
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
		logger.config("Loading and processing the saved sim. Please wait...");
		
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
			logger.config(" Note : The two builds are identical.");
		} else {
			logger.config(" Note : The two builds are NOT identical.");
			logger.warning("Attempting to load a simulation made in build " + loadBuild
				+ " (older) under core engine build " + Simulation.BUILD + " (newer).");
		}		
		
		MISSION_SOL = masterClock.getMarsClock().getMissionSol();
		MSOL_CACHE = masterClock.getMarsClock().getMillisolInt();
		
		logger.config("  - - - - - - - - - Sol " + MISSION_SOL
				+ " (Cont') - - - - - - - - - - - ");
		
		instance().initialSimulationCreated = true;
		
		// Re-initialize instances
		reinitializeInstances();
		// Set this flag to false
		isUpdating = false;
	}
	
	/**
	 *  Re-initialize instances after loading from a saved sim
	 */
	private void reinitializeInstances() {
		// Re-initialize the utility class for getting lists of meta tasks.
		MetaTaskUtil.initializeMetaTasks();
		
		// Restart the autosave scheduler
		AutosaveScheduler.defaultStart();
		// Set save type to NONE
		masterClock.setSaveType();	
		// Re-initialize the resources for the saved sim
		ResourceUtil.getInstance().initializeInstances();
		// Re-initialize the MarsSurface instance
		MarsSurface marsSurface = unitManager.getMarsSurface();

		Airlock.initializeInstances(unitManager, marsSurface);
		
		Inventory.initializeInstances(unitManager);
	
		//  Re-initialize the GameManager
		GameManager.initializeInstances(unitManager);
		// Re-initialize the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
		// Gets the Weather instance
		Weather weather = mars.getWeather();
		// Gets the orbitInfo instance
		orbit = mars.getOrbitInfo();
		
		// Gets he MarsClock instance
		MarsClock marsClock = masterClock.getMarsClock();
		// Gets he MarsClock instance
		EarthClock earthClock = masterClock.getEarthClock();
		
		// Re-initialize the instances in LogConsolidated
		DataLogger.changeTime(marsClock);
		LogConsolidated.initializeInstances(marsClock, earthClock);
	
		SurfaceFeatures.initializeInstances(this); 
	
		// Gets config file instances
		simulationConfig = SimulationConfig.instance();
		BuildingConfig bc = simulationConfig.getBuildingConfiguration();
		PersonConfig pc = simulationConfig.getPersonConfig();
		CropConfig cc = simulationConfig.getCropConfiguration();
	
		// Re-initialize units prior to starting the unit manager
		Unit.initializeInstances(masterClock, marsClock, earthClock, this, mars, weather, surfaceFeatures, missionManager);	
		Unit.setUnitManager(unitManager);
		
		// Re-initialize Building function related class
		Function.initializeInstances(bc, marsClock, pc, cc, surfaceFeatures, weather, unitManager);
	
		// Rediscover the MissionControls
		ReportingAuthorityFactory.discoverReportingAuthorities(unitManager);
		
		RelationshipManager.initializeInstances(unitManager);
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory, medicalManager, eventManager);
		TransportManager.initializeInstances(eventManager);
		ScientificStudy.initializeInstances(marsClock);
		ScientificStudyUtil.initializeInstances(relationshipManager, unitManager);
				
		Resupply.initializeInstances(bc, unitManager);
	
		// Re-initialize Unit related class
//		Vehicle.initializeInstances();
		SalvageValues.initializeInstances(unitManager);
			
		// Re-initialize Person/Robot related class
		Mind.initializeInstances(missionManager, relationshipManager);		
		PhysicalCondition.initializeInstances(this, masterClock, marsClock, medicalManager);
		RadiationExposure.initializeInstances(marsClock);
		Role.initializeInstances(marsClock);
		TaskManager.initializeInstances(marsClock);
		HealthProblem.initializeInstances(medicalManager, eventManager);

		// Re-initialize Structure related class
		BuildingManager.initializeInstances(this, masterClock, marsClock, eventManager, relationshipManager, unitManager);
		Settlement.initializeInstances(unitManager);		// loadDefaultValues()
		GoodsManager.initializeInstances(this, marsClock, missionManager, unitManager, pc);
			
		// Miscs.
		CompositionOfAir.initializeInstances(pc, unitManager);
		Crop.initializeInstances(surfaceFeatures, unitManager);
		SolarHeatSource.initializeInstances(surfaceFeatures);
		PowerSource.initializeInstances(mars, surfaceFeatures, orbit, weather);
		ResourceProcess.initializeInstances(marsClock);
		Job.initializeInstances(unitManager, missionManager);
		RobotJob.initializeInstances(unitManager, missionManager);
//		CreditEvent.initializeInstances(unitManager, missionManager);
			
		// Re-initialize Task related class 
		Walk.initializeInstances(unitManager);	
		Task.initializeInstances(marsClock, eventManager, relationshipManager, unitManager, 
				scientificStudyManager, surfaceFeatures, orbit, missionManager, pc);
	
		// Re-initialize Mission related class
		Mission.initializeInstances(this, marsClock, eventManager, unitManager, scientificStudyManager, 
				surfaceFeatures, missionManager, relationshipManager, pc, creditManager);
		MissionPlanning.initializeInstances(marsClock);

		// Start a chain of calls to set instances
		unitManager.reinit(marsClock);
		
		doneInitializing = true;

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

		// Checks to see if the simulation is on pause
		boolean isPause = masterClock.isPaused();
		
		Simulation sim = instance();
		// Stops the master clock and removes the Simulation clock listener
		sim.halt(isPause);

		// Experiment with saving in JSON format
//		writeJSON();
		
		lastSaveTimeStamp = new SystemDateTime().getDateTimeStr();
		changed = true;

		File backupFile = new File(SimulationFiles.getSaveDir(), "previous" + SAVE_FILE_EXTENSION);
		FileSystem fileSys = null;
		Path destPath = null;
		Path srcPath = null;

		// Use type to differentiate in what name/dir it is saved
		if (type == SaveType.SAVE_DEFAULT) {

			file = new File(SimulationFiles.getSaveDir(), SAVE_FILE + SAVE_FILE_EXTENSION);

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

			file = new File(SimulationFiles.getSaveDir(), SAVE_FILE + SAVE_FILE_EXTENSION);

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
			file = new File(SimulationFiles.getAutoSaveDir(), autosaveFilename);
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
		
		logger.config(
		"heapSize: " + formatSize(heapSize) 
		+ "    heapMaxSize: " + formatSize(heapMaxSize)
		+ "    heapFreeSize: " + formatSize(heapFreeSize) + "");

		// Call up garbage collector. But it's up to the gc what it will do.
		System.gc();
		
		if (heapFreeSize > MIN_HEAP_SPACE){
			// Serialize the file
			serialize(type, file, srcPath, destPath);
		}
		else {
			logger.config("Not enough free memory in Heap Space. Please try saving the sim later.");
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
          	logger.log(Level.SEVERE, "Cannot sleep : " + e.getMessage());
        }
    }
    
    private static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.2f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
    
    /**
     * Serialize the given object and save it to a given file.
     */
    private void serialize(SaveType type, File file, Path srcPath, Path destPath)
            throws IOException {

	    ObjectOutputStream oos = new ObjectOutputStream(
	    			new GZIPOutputStream(new FileOutputStream(file)));
		
		try {
	
			// Set a delay for 200 millis to avoid java.util.ConcurrentModificationException
			delay(500L);
			
			// Store the in-transient objects.
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

			// Print the size of the saved sim
			logger.config("           File size : " + computeFileSize(file));
			logger.config("Done saving. The simulation resumes.");

		// Note: see https://docs.oracle.com/javase/7/docs/platform/serialization/spec/exceptions.html
		} catch (WriteAbortedException e) {
			// Thrown when reading a stream terminated by an exception that occurred while the stream was being written.
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with WriteAbortedException when saving " + file + " : " + e.getMessage());	

		} catch (OptionalDataException e) {
			// Thrown by readObject when there is primitive data in the stream and an object is expected. The length field of the exception indicates the number of bytes that are available in the current block.
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with OptionalDataException when saving " + file + " : " + e.getMessage());
		
		} catch (InvalidObjectException e) {
			// Thrown when a restored object cannot be made valid.
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with InvalidObjectException when saving " + file + " : " + e.getMessage());	

		} catch (NotActiveException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with NotActiveException when saving " + file + " : " + e.getMessage());

		} catch (StreamCorruptedException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with StreamCorruptedException when saving " + file + " : " + e.getMessage());
		
		} catch (NotSerializableException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with NotSerializableException when saving " + file + " : " + e.getMessage());

		} catch (ObjectStreamException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with ObjectStreamException when saving " + file + " : " + e.getMessage());

		} catch (IOException e0) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": " + Msg.getString("Simulation.log.saveError"), e0); //$NON-NLS-1$

			if (type == SaveType.AUTOSAVE_AS_DEFAULT || type == SaveType.SAVE_DEFAULT) {

				if (file.exists() && !file.isDirectory()) {
					// Backup the existing default.sim
					Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": " + Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$

			if (type == SaveType.AUTOSAVE_AS_DEFAULT || type == SaveType.SAVE_DEFAULT) {

				if (file.exists() && !file.isDirectory()) {
					// backup the existing default.sim
					Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		}

		finally {

			if (oos != null)
				oos.close();
			
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
		
		sb.append("      Serializable object | Serialized Size");
		sb.append("  | Object Size");
		sb.append(System.lineSeparator());
		sb.append(" ---------------------------------------------------------"
				+ System.lineSeparator());		
		int max0 = 25;
		int max1 = 10;
		
		String SPACE = " ";
		
		double sumFileSize = 0;
		
		String unit = "";

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
  
			if (type == 0) {
				fileSize = CheckSerializedSize.getSerializedSize(o);

			}
			else if (type == 1) {
				fileSize = CheckSerializedSize.getSerializedSizeByteArray(o);
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

			sb.append(sizeStr);

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

		String sizeStr = String.format("%.2f", sumFileSize) + unit;
		int size2 = max1 - sizeStr.length();
		for (int i=0; i<size2; i++) {
			sb.append(SPACE);
		}
		
		sb.append(sizeStr + System.lineSeparator());
		
		masterClock.setPaused(false, false);
		
		return sb;
	}
	
	
	/**
	 * Ends the current simulation
	 */
	public void endSimulation() {
		logger.log(Level.CONFIG, "Exiting the simulation. Good Bye !");

		instance().stop();
		// Ends the clock listener executor in master clock
		if (masterClock != null)
			masterClock.shutdown();

		// Ends the unitmanager's executor thread pools
		if (unitManager != null) {
			unitManager.endSimulation();
		}
	}

	public void endMasterClock() {
		masterClock = null;
	}

	/**
	 * Stop the simulation.
	 */
	public void stop() {
		if (masterClock != null) {
			masterClock.stop();
			masterClock.removeClockListener(this);
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
		
		// Call up garbage collector. But it's up to the gc what it will do.
		System.gc();
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
	public Environment getMars() {
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

	public boolean getJustSaved() {
		return justSaved;
	}

	public void setJustSaved(boolean value) {
		justSaved = value;
	}

	/**
	 * Clock pulse from master clock
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 */
	@Override
	public void clockPulse(ClockPulse pulse) {
		if (doneInitializing && !clockOnPause) {
			// Refresh all Data loggers; this can be refactored later to a Manager class
			DataLogger.changeTime(pulse.getMarsTime());
			mars.timePassing(pulse);

			missionManager.timePassing(pulse);

			unitManager.timePassing(pulse);

			transportManager.timePassing(pulse);
		}
	}

	public boolean getAutosaveDefault() {
		return autosaveDefault;
	}
	
	
	@Override
	public void uiPulse(double time) {
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
        clockOnPause = isPaused;
	}

	/**
	 * Destroys the current simulation to prepare for creating or loading a new
	 * simulation.
	 */
	public void destroyOldSimulation() {
		logger.config("Starting destroyOldSimulation()");

		AutosaveScheduler.cancel();

		malfunctionFactory = null;

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

		 logger.config("Done with Simulation's destroyOldSimulation()");
	}

}
