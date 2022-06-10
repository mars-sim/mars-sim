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
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.mars_sim.msp.core.air.AirComposition;
import org.mars_sim.msp.core.data.DataLogger;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.environment.Environment;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.goods.CreditManager;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.logging.SimuLoggingFormatter;
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
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
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

	private static class AutoSaveTrigger implements ClockListener {
		private Simulation sim;
		private SaveType type;
		
		public AutoSaveTrigger(Simulation sim, SaveType type) {
			super();
			this.sim = sim;
			this.type = type;
		}

		@Override
		public void pauseChange(boolean isPaused, boolean showPane) {
			// placeholder
		}
		
		@Override
		public void clockPulse(ClockPulse currentPulse) {
			// Set the pending save flag for an auto save
			sim.savePending = type;
		}
	}
	
	/** default serial id. */
	private static final long serialVersionUID = -631308653510974249L;

	private static final Logger logger = Logger.getLogger(Simulation.class.getName());

	private enum SaveType {
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
	
	public static final String DASHES = " ---------------------------------------------------------";
	/** OS string. */
	public static final String OS = System.getProperty("os.name"); // e.g. 'linux', 'mac os x'
	/** Version string. */
	public static final String VERSION = Version.getVersion();
	/** Build string. */
	public static final String BUILD = Version.getBuild();
	/** Java version string. */
	private static final String JAVA_TAG = System.getProperty("java.version");
	/** Java version string. */
	public static final String JAVA_VERSION = "Java " + (JAVA_TAG.contains("(") ?
			JAVA_TAG.substring(0, JAVA_TAG.indexOf("(") - 1) : JAVA_TAG);
	/** OS architecture string. */
	private static final String OS_ARCH = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");
	/** Default save filename. */
	public static final String SAVE_FILE = Msg.getString("Simulation.saveFile"); //$NON-NLS-1$
	/** Default save filename extension. */
	public static final String SAVE_FILE_EXTENSION = Msg.getString("Simulation.saveFile.extension"); //$NON-NLS-1$

	public static final String title = Msg.getString("Simulation.title", VERSION + " - Build " + BUILD
			+ " - " + OS_ARCH + " " + JAVA_VERSION + " - " + NUM_THREADS
			+ ((NUM_THREADS == 1) ? " CPU thread" : " CPU threads")); // $NON-NLS-1$

	/** The minimum size of heap space in bytes */
	private static final int MIN_HEAP_SPACE = 64*1024*1024;

	/** true if displaying graphic user interface. */
	private transient boolean useGUI = true;
	/** Flag to indicate that a new simulation is being created or loaded. */
	private transient boolean isUpdating = false;
	/** Flag to keep track of whether the initial state of simulation has been initialized. */
	private transient boolean doneInitializing = false;

	private transient boolean justSaved = true;

	private transient boolean clockOnPause = false;

	private boolean initialSimulationCreated = false;

	/** The time stamp of the last saved sim. */
	private Date lastSaveTimeStamp = null;
	
	/** Clock listener that triggers autosaving **/
	private transient ClockListener autoSaveHandler;

	// Intransient data members (stored in save file)
	/** Planet Mars. */
	private Environment environment;
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

	private transient SaveType savePending = null;
	private transient File savePendingFile = null;

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
		unitManager.setOriginalBuild(Simulation.BUILD);
	}


	public void testRun() {
		Simulation sim = Simulation.instance();
		ResourceUtil.getInstance();

		// Should this method call the initialiseTransient method ?

		// Create marsClock instance
		masterClock = new MasterClock(256);
		MarsClock marsClock = masterClock.getMarsClock();
		EarthClock earthClock = masterClock.getEarthClock();

		environment = new Environment(this, marsClock);
		unitManager = new UnitManager();
		EquipmentFactory.initialise(unitManager);

		// Build planetary objects
		MarsSurface marsSurface = new MarsSurface();
		unitManager.addUnit(marsSurface);

		// Gets the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = environment.getSurfaceFeatures();

        RoleUtil.initialize();

		medicalManager = new MedicalManager();

		// Set instances for logging
		SimuLoggingFormatter.initializeInstances(masterClock);

		simulationConfig = SimulationConfig.instance();
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory,
												medicalManager, eventManager,
												simulationConfig.getPartConfiguration());

		Unit.initializeInstances(masterClock, marsClock, earthClock, sim, environment,
				 environment.getWeather(), surfaceFeatures, new MissionManager());
		Unit.setUnitManager(unitManager);
		
		LocalAreaUtil.initializeInstances(unitManager, marsClock);
		// Initialize instances in Airlock
		Airlock.initializeInstances(unitManager, marsSurface, marsClock);
		// Initialize instances in TaskSchedule
		TaskSchedule.initializeInstances(marsClock);
	}

	/**
	 * Initialize intransient data in the simulation.
	 */
	private void initializeIntransientData(int timeRatio) {

		// Initialize resources
		ResourceUtil.getInstance();

		// Gets config file instances
		simulationConfig = SimulationConfig.instance();
		BuildingConfig bc = simulationConfig.getBuildingConfiguration();
		PersonConfig pc = simulationConfig.getPersonConfig();
		CropConfig cc = simulationConfig.getCropConfiguration();

		// Clock is always first
		masterClock = new MasterClock(timeRatio);
		MarsClock marsClock = masterClock.getMarsClock();
		EarthClock earthClock = masterClock.getEarthClock();

		// Set instances for logging
		SimuLoggingFormatter.initializeInstances(masterClock);

		// Initialize serializable objects
		malfunctionFactory = new MalfunctionFactory();
		environment = new Environment(this, marsClock);
		orbit = new OrbitInfo(marsClock);

		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		scientificStudyManager = new ScientificStudyManager();

		// Gets the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = environment.getSurfaceFeatures();
		SurfaceFeatures.initializeInstances(missionManager, simulationConfig.getLandmarkConfiguration());

		TerrainElevation terrainElevation = surfaceFeatures.getTerrainElevation();
		// Initialize units prior to starting the unit manager
		Unit.initializeInstances(masterClock, marsClock, earthClock, this, environment,
				environment.getWeather(), surfaceFeatures, missionManager);
		TaskManager.initializeInstances(marsClock);

		// Initialize UnitManager instance
		unitManager = new UnitManager();
		EquipmentFactory.initialise(unitManager);

		// Initialize MarsSurface instance
		MarsSurface marsSurface = new MarsSurface();
		// Build objects
		unitManager.addUnit(marsSurface);

		// Initialize instances in Airlock
		Airlock.initializeInstances(unitManager, marsSurface, marsClock);
		AirComposition.initializeInstances(pc);
		// Initialize instances in TaskSchedule
		TaskSchedule.initializeInstances(marsClock);
		
		// Gets the MarsSurface instance
		Unit.setUnitManager(unitManager);

		// Initialise the Building Functions
		ResourceProcess.initializeInstances(marsClock);
		Function.initializeInstances(bc, marsClock, pc, cc, surfaceFeatures,
								     environment.getWeather(), unitManager);
		Crop.initializeInstances(cc);

		// Initialize meta tasks
		MetaTaskUtil.initializeMetaTasks();

		eventManager = new HistoricalEventManager();
		creditManager = new CreditManager();
		transportManager = new TransportManager(eventManager);

        // Initialize ManufactureUtil
        new ManufactureUtil();
        // Initialize RoleUtil
        new RoleUtil();
        RoleUtil.initialize();

		// Initialize instances prior to UnitManager initiatiation
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory,
											medicalManager, eventManager,
											simulationConfig.getPartConfiguration());
		RelationshipManager.initializeInstances(unitManager);
		RadiationExposure.initializeInstances(masterClock, marsClock);

		//  Re-initialize the GameManager
		GameManager.initializeInstances(unitManager);

		// Set instances for classes that extend Unit and Task and Mission
		Mission.initializeInstances(this, marsClock, eventManager, unitManager,
				surfaceFeatures, terrainElevation, missionManager, relationshipManager, pc, creditManager);
		Task.initializeInstances(marsClock, eventManager, relationshipManager, unitManager,
				scientificStudyManager, surfaceFeatures, orbit, missionManager, pc);
		LocalAreaUtil.initializeInstances(unitManager, marsClock);
		
		doneInitializing = true;
	}

	/**
	 * Starts the simulation.
	 *
	 * @param autosaveDefault True if default is used for autosave
	 */
	public void startClock(boolean autosaveDefault) {
		masterClock.addClockListener(this, 0);
		
		// Add a listener to trigger the auto save
		autoSaveHandler = new AutoSaveTrigger(this, autosaveDefault ? SaveType.AUTOSAVE_AS_DEFAULT : SaveType.AUTOSAVE);
		long autoSaveDuration = simulationConfig.getAutosaveInterval() * 60000L;
		logger.config("Adding autosave handled for every " + autoSaveDuration + "ms");
		masterClock.addClockListener(autoSaveHandler, autoSaveDuration);
		
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
			lastSaveTimeStamp = (Date) ois.readObject();
			malfunctionFactory = (MalfunctionFactory) ois.readObject();
			environment = (Environment) ois.readObject();
			missionManager = (MissionManager) ois.readObject();
			medicalManager = (MedicalManager) ois.readObject();
			scientificStudyManager = (ScientificStudyManager) ois.readObject();
			eventManager = (HistoricalEventManager) ois.readObject();
			creditManager = (CreditManager) ois.readObject();
			transportManager = (TransportManager) ois.readObject();
			relationshipManager = (RelationshipManager) ois.readObject();
			unitManager = (UnitManager) ois.readObject();
			masterClock = (MasterClock) ois.readObject();

			UnitSet.reinit(unitManager);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot deserialize : " + e.getMessage());
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

		String loadBuild = unitManager.getOriginalBuild();
		if (loadBuild == null)
			loadBuild = "unknown";

		logger.config(DASHES);
		logger.config("                   Info on The Saved Simulation                      ");
		logger.config(DASHES);
		logger.config("                   Filename : " + filename);
		logger.config("                       Path : " + path);
		logger.config("                       Size : " + computeFileSize(file));
		logger.config("              Made in Build : " + loadBuild);
		logger.config("  Current Core Engine Build : " + Simulation.BUILD);
		logger.config("           Earth Time Stamp : " + masterClock.getEarthClock().getTimeStampF4());
		logger.config("         Martian Time Stamp : " + masterClock.getMarsClock().getDateTimeStamp());
		if (lastSaveTimeStamp != null)
			logger.config("   Machine Local Time Stamp : " + DateFormat.getDateTimeInstance().format(lastSaveTimeStamp));
		logger.config(DASHES);
		if (Simulation.BUILD.equals(loadBuild)) {
			logger.config(" Note : The two builds are identical.");
		} else {
			logger.config(" Note : The two builds are NOT identical.");
			logger.warning("Attempting to load a simulation made in build " + loadBuild
				+ " (older) under core engine build " + Simulation.BUILD + " (newer).");
		}

		int lastSol = masterClock.getMarsClock().getMissionSol();

		logger.config("  - - - - - - - - - Sol " + lastSol
				+ " (Cont') - - - - - - - - - - - ");

		initialSimulationCreated = true;

		// Re-initialize instances
		reinitializeInstances();
		// Set this flag to false
		isUpdating = false;
	}

	/**
	 *  Re-initialize instances after loading from a saved sim
	 */
	private void reinitializeInstances() {
		SimuLoggingFormatter.initializeInstances(masterClock);

		// Re-initialize the utility class for getting lists of meta tasks.
		MetaTaskUtil.initializeMetaTasks();

		// Re-initialize the resources for the saved sim
		ResourceUtil.getInstance().initializeInstances();
		// Re-initialize the MarsSurface instance
		MarsSurface marsSurface = unitManager.getMarsSurface();

		// Gets config file instances
		simulationConfig = SimulationConfig.instance();
		BuildingConfig bc = simulationConfig.getBuildingConfiguration();
		PersonConfig pc = simulationConfig.getPersonConfig();
		CropConfig cc = simulationConfig.getCropConfiguration();

		//  Re-initialize the GameManager
		GameManager.initializeInstances(unitManager);
		// Re-initialize the SurfaceFeatures instance
		SurfaceFeatures surfaceFeatures = environment.getSurfaceFeatures();
		// Gets the Weather instance
		Weather weather = environment.getWeather();
		// Gets the orbitInfo instance
		orbit = environment.getOrbitInfo();
		// Gets he MarsClock instance
		MarsClock marsClock = masterClock.getMarsClock();
		// Gets he MarsClock instance
		EarthClock earthClock = masterClock.getEarthClock();

		// Initialize instances in Airlock
		Airlock.initializeInstances(unitManager, marsSurface, marsClock);
		// Initialize instances in TaskSchedule
		TaskSchedule.initializeInstances(marsClock);
		
		// Re-initialize the instances in LogConsolidated
		DataLogger.changeTime(marsClock);
		SurfaceFeatures.initializeInstances(missionManager, simulationConfig.getLandmarkConfiguration());
		TerrainElevation terrainElevation = surfaceFeatures.getTerrainElevation();

		// Re-initialize units prior to starting the unit manager
		Unit.initializeInstances(masterClock, marsClock, earthClock, this, environment, weather, surfaceFeatures, missionManager);
		Unit.setUnitManager(unitManager);
		EquipmentFactory.initialise(unitManager);

		// Re-initialize Building function related class
		Function.initializeInstances(bc, marsClock, pc, cc, surfaceFeatures, weather, unitManager);

		// Rediscover the MissionControls
		ReportingAuthorityFactory rf  = simulationConfig.getReportingAuthorityFactory();
		rf.discoverReportingAuthorities(unitManager);

		RelationshipManager.initializeInstances(unitManager);
		MalfunctionManager.initializeInstances(masterClock, marsClock, malfunctionFactory,
												medicalManager, eventManager,
												simulationConfig.getPartConfiguration());
		ScientificStudy.initializeInstances(marsClock);
		ScientificStudyUtil.initializeInstances(relationshipManager, unitManager);

		Resupply.initializeInstances(bc, unitManager);

		// Re-initialize Unit related class
		SalvageValues.initializeInstances(unitManager);

		// Re-initialize Person/Robot related class
		Mind.initializeInstances(missionManager, relationshipManager);
		PhysicalCondition.initializeInstances(this, masterClock, marsClock, medicalManager);
		RadiationExposure.initializeInstances(masterClock, marsClock);
		Role.initializeInstances(marsClock);
		TaskManager.initializeInstances(marsClock);
		HealthProblem.initializeInstances(medicalManager, eventManager);

		// Re-initialize Structure related class
		BuildingManager.initializeInstances(this, masterClock, marsClock, eventManager, relationshipManager, unitManager);
		Settlement.initializeInstances(unitManager);		// loadDefaultValues()
		GoodsManager.initializeInstances(this, marsClock, missionManager, unitManager, pc);

		// Miscs.
		AirComposition.initializeInstances(pc);
		Crop.initializeInstances(simulationConfig.getCropConfiguration());
		SolarHeatSource.initializeInstances(surfaceFeatures);
		PowerSource.initializeInstances(environment, surfaceFeatures, orbit, weather);
		ResourceProcess.initializeInstances(marsClock);
		Job.initializeInstances(unitManager, missionManager);
		RobotJob.initializeInstances(unitManager, missionManager);
//		CreditEvent.initializeInstances(unitManager, missionManager);

		// Re-initialize Task related class
		Task.initializeInstances(marsClock, eventManager, relationshipManager, unitManager,
				scientificStudyManager, surfaceFeatures, orbit, missionManager, pc);
		LocalAreaUtil.initializeInstances(unitManager, marsClock);
		
		// Re-initialize Mission related class
		Mission.initializeInstances(this, marsClock, eventManager, unitManager,
				surfaceFeatures, terrainElevation, missionManager, relationshipManager, pc, creditManager);
		MissionPlanning.initializeInstances(marsClock);

		// Start a chain of calls to set instances
		unitManager.reinit();

		doneInitializing = true;

	}

	public boolean isDoneInitializing() {
		return doneInitializing;
	}

	/**
	 * Saves a simulation instance to a save file.
	 *
	 * @param file the file to be saved to.
	 */
	private synchronized void saveSimulation(SaveType type, File file) {

		// Checks to see if the simulation is on pause
		boolean isAlreadyPaused = masterClock.isPaused();

		// Stops the master clock and removes the Simulation clock listener
		masterClock.stop();
		if (!isAlreadyPaused) masterClock.setPaused(true, false);

		// Call up garbage collector. But it's up to the gc what it will do.
		System.gc();

		lastSaveTimeStamp = new Date();

		Path srcPath = null;
		Path destPath = null;

		try {
			// Use type to differentiate in what name/dir it is saved
			switch(type) {
			case AUTOSAVE_AS_DEFAULT:
			case SAVE_DEFAULT:
				file = new File(SimulationFiles.getSaveDir(), SAVE_FILE + SAVE_FILE_EXTENSION);
	
				if (file.exists() && !file.isDirectory()) {
					FileSystem fileSys = FileSystems.getDefault();
					
					// Create the backup file for storing the previous version of default.sim
					File backupFile = new File(SimulationFiles.getSaveDir(), "previous" + SAVE_FILE_EXTENSION);

					destPath = fileSys.getPath(backupFile.getPath());
					srcPath = fileSys.getPath(file.getPath());
					// Backup the existing default.sim
					Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
				}
	
				logger.config("Saving the simulation as " + SAVE_FILE + SAVE_FILE_EXTENSION + ".");
				break;
			
			case SAVE_AS:
				String f = file.getName();
				String dir = file.getParentFile().getAbsolutePath();
				if (!f.contains(".sim"))
					file = new File(dir, f + SAVE_FILE_EXTENSION);
				logger.config("Saving the simulation as " + file + "...");
				break;
			
			case AUTOSAVE:
				int missionSol = masterClock.getMarsClock().getMissionSol();
				String saveTime = new SystemDateTime().getDateTimeStr();
				String autosaveFilename = saveTime + "_sol" + missionSol + "_r" + BUILD
						+ SAVE_FILE_EXTENSION;
				file = new File(SimulationFiles.getAutoSaveDir(), autosaveFilename);
				logger.config("Autosaving the simulation as " + autosaveFilename + ".");
				
				// NOTE: Should purge old auto saved files
				break;
				
			default:
				break;
			}
	
			// if the autosave/default save directory does not exist, create one now
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
	
			// Call up garbage collector. But it's up to the gc what it will do.
			System.gc();

			// Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
			long heapMaxSize = Runtime.getRuntime().maxMemory();
			 // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
			long heapFreeSize = Runtime.getRuntime().freeMemory();
	
			logger.config("Heap Max Size: " + formatSize(heapMaxSize)
						+ ", Heap Free Size: " + formatSize(heapFreeSize));
	
			if (heapFreeSize > MIN_HEAP_SPACE){
				// Save local machine timestamp
				// Serialize the file
				lastSaveTimeStamp = new Date();
				boolean sucessful = serialize(type, file, srcPath, destPath);

				if (sucessful && (type == SaveType.AUTOSAVE)) {
					// Purge old auto backups
					SimulationFiles.purgeAutoSave(simulationConfig.getNumberAutoSaves(), SAVE_FILE_EXTENSION);
				}
			}
			else {
				logger.config("Not enough free memory in Heap Space. Try saving the sim later.");
			}
		}
		catch (IOException ioe) {
			logger.severe("Problem saving simulation " + ioe.getMessage());
		}
		
		// Restarts the master clock and adds back the Simulation clock listener
		if (!isAlreadyPaused) masterClock.setPaused(false, false);
		masterClock.restart();
	}

	/**
	 * Delays for a period of time in millis
	 *
	 * @param millis
	 */
    private static void delay(long millis) {
        try {
			TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
          	logger.log(Level.SEVERE, "Cannot sleep : " + e.getMessage());
          	// Restore interrupted state
            Thread.currentThread().interrupt();
        }
    }

    private static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.2f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    /**
     * Serialize the given object and save it to a given file.
     * @return 
     */
    private boolean serialize(SaveType type, File file, Path srcPath, Path destPath)
            throws IOException {
		boolean success = false;
	    ObjectOutputStream oos = new ObjectOutputStream(
	    			new GZIPOutputStream(new FileOutputStream(file)));
		try {

			// Set a delay for 500 millis to avoid java.util.ConcurrentModificationException
			delay(500L);

			// Store the in-transient objects.
			oos.writeObject(lastSaveTimeStamp);
			oos.writeObject(malfunctionFactory);
			oos.writeObject(environment);
			oos.writeObject(missionManager);
			oos.writeObject(medicalManager);
			oos.writeObject(scientificStudyManager);
			oos.writeObject(eventManager);
			oos.writeObject(creditManager);
			oos.writeObject(transportManager);
			oos.writeObject(relationshipManager);
			oos.writeObject(unitManager);
			oos.writeObject(masterClock);

			oos.flush();
			oos.close();

			// Print the size of the saved sim
			logger.config("           File size : " + computeFileSize(file));
			logger.config("Done saving. The simulation resumes.");
			success = true;

		} catch (NotSerializableException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with NotSerializableException when saving " + file + " : " + e.getMessage());

		} catch (ObjectStreamException e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": Quitting mars-sim with ObjectStreamException when saving " + file + " : " + e.getMessage());

		} catch (IOException e0) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": " + Msg.getString("Simulation.log.saveError"), e0); //$NON-NLS-1$

			if ((type == SaveType.AUTOSAVE_AS_DEFAULT || type == SaveType.SAVE_DEFAULT) 
				&& file.exists() && !file.isDirectory()) {
				// Backup the existing default.sim
				Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, oos.getClass().getSimpleName() + ": " + Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$

			if ((type == SaveType.AUTOSAVE_AS_DEFAULT || type == SaveType.SAVE_DEFAULT)
				&& file.exists() && !file.isDirectory()) {
				// backup the existing default.sim
				Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		finally {

			if (oos != null)
				oos.close();

			justSaved = true;
		}

		return success;
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
				environment,
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
		sb.append(DASHES + System.lineSeparator());
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

		sb.append(DASHES + System.lineSeparator());

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

	/**
	 * Stop the simulation.
	 */
	public void stop() {
		if (masterClock != null) {
			masterClock.stop();
			masterClock.removeClockListener(this);
			masterClock.removeClockListener(autoSaveHandler);
		}
	}


	/**
	 * Get the planet Mars.
	 *
	 * @return Mars
	 */
	public Environment getMars() {
		return environment;
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

	public int getMissionSol() {
		return masterClock.getMarsClock().getMissionSol();
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
			environment.timePassing(pulse);

			missionManager.timePassing(pulse);

			unitManager.timePassing(pulse);

			transportManager.timePassing(pulse);
			
			// Pending save
			if (savePending != null) {
				saveSimulation(savePending, savePendingFile);
				savePending = null;
			}
		}
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
        clockOnPause = isPaused;
	}

	/**
	 * Destroys the current simulation to prepare for creating or loading a new
	 * simulation.
	 */
	private void destroyOldSimulation() {
		logger.config("Starting destroyOldSimulation()");

		// Remove old clock listeners ?
		if (masterClock != null) {
			masterClock.removeClockListener(this);
			if (autoSaveHandler != null) {
				masterClock.removeClockListener(autoSaveHandler);
			}
		}
		malfunctionFactory = null;

		if (environment != null) {
			environment.destroy();
			environment = null;
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

	/**
	 * Request that the simulation is saved on the next idle period
	 * @param saveFile Optional file to save info, null means default file.
	 */
	public void requestSave(File saveFile) {
		savePending = (saveFile == null ? SaveType.SAVE_DEFAULT : SaveType.SAVE_AS);
		savePendingFile = saveFile;		
	}

	/**
	 * Is a save request still pending
	 * @return
	 */
	public boolean isSavePending() {
		return (savePending != null);
	}
}
