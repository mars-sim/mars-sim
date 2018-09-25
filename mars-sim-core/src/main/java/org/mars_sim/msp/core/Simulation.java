/**
 * Mars Simulation Project
 * Simulation.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.beryx.textio.TextIO;
import org.beryx.textio.system.SystemTextTerminal;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.SystemDateTime;
import org.mars_sim.msp.core.time.UpTimer;

//import org.reactfx.EventStreams;
//import org.reactfx.util.FxTimer;
//import org.reactfx.util.Timer;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

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

	// Categories of loading and saving simulation
	public static final int OTHER = 0; // load other file
	public static final int SAVE_DEFAULT = 1; // save as default.sim
	public static final int SAVE_AS = 2; // save with other name
	private static final int AUTOSAVE_AS_DEFAULT = 3; // save as default.sim
	public static final int AUTOSAVE = 4; // save with build info/date/time stamp
	/** # of thread(s). */
	public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
	/** Version string. */
	public static final String OS = System.getProperty("os.name"); // e.g. 'linux', 'mac os x'
	/** Version string. */
	public final static String VERSION = Msg.getString("Simulation.version"); //$NON-NLS-1$
	/** Build string. */
	public final static String BUILD = Msg.getString("Simulation.build"); //$NON-NLS-1$
	/** Java version string. */
	private final static String JAVA_TAG = System.getProperty("java.version");// VersionInfo.getRuntimeVersion(); //e.g.
																				// "8.0.121-b13 (abcdefg)";
																				// com.sun.javafx.runtime.VersionInfo.getRuntimeVersion();
																				// //
																				// System.getProperty("java.version");/
	/** Java version string. */
	public final static String JAVA_VERSION = (JAVA_TAG.contains("(") ? JAVA_TAG.substring(0, JAVA_TAG.indexOf("(") - 1)
			: JAVA_TAG);
	/** Vendor string. */
	// public final static String VENDOR = System.getProperty("java.vendor");
	/** Vendor string. */
	private final static String OS_ARCH = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");
	/** Default save filename. */
	private final static String DEFAULT_FILE = Msg.getString("Simulation.defaultFile"); //$NON-NLS-1$
	/** Default temp filename. */
	private final static String TEMP_FILE = Msg.getString("Simulation.tempFile"); //$NON-NLS-1$
	/** Default save filename extension. */
	private final static String DEFAULT_EXTENSION = Msg.getString("Simulation.defaultFile.extension"); //$NON-NLS-1$

	private final static String LOCAL_TIME = Msg.getString("Simulation.localTime"); //$NON-NLS-1$ " (Local Time) ";

	private final static String WHITESPACES = "  ";

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

	private static final boolean debug = logger.isLoggable(Level.FINE);
	/** true if displaying graphic user interface. */
	private static boolean useGUI = true;
	/** Flag to indicate that a new simulation is being created or loaded. */
	private static boolean isUpdating = false;

	private static boolean defaultLoad = false;

	private static boolean justSaved = true;

	private static boolean autosaveDefault;

	// private static int autosave_minute;// = 15;

	private boolean initialSimulationCreated = false;

	private boolean changed = true;

	private boolean isFXGL = false;

	private double fileSize;

	/** The name of the player */
	private String user;
	/** The gender of the player */
	private String gender ;
	
	/** The time stamp of the last saved sim */	
	private String lastSaveTimeStamp;
	/** The build version of the SimulationConfig of the loading .sim */
	private String loadBuild;// = "unknown";

	private String lastSaveStr = null;
	
	/** The age of the player */
	private int age;
	
	// Note: Transient data members (aren't stored in save file)
	// Added transient to avoid serialization error
//	private transient Timer autosaveTimer;
	// private transient Timeline timer;
	private transient ScheduledExecutorService autosaveService;// = Executors.newSingleThreadScheduledExecutor();

	// private transient ThreadPoolExecutor clockScheduler;
	private transient ExecutorService clockExecutor;

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
	
//	private Terminal terminal;

	/**
	 * Private constructor for the Singleton Simulation. This prevents instantiation
	 * from other classes.
	 */
	private Simulation() {
		// simulationConfig = SimulationConfig.instance();
		// logger.info("Simulation's constructor is on " +
		// Thread.currentThread().getName() + " Thread");
		
		// INFO Simulation's constructor is on both JavaFX-Launcher Thread
//        initializeTransientData();
	}

	/** (NOT USED) Eager Initialization Singleton instance. */
	// private static final Simulation instance = new Simulation();
	/**
	 * Gets a Eager Initialization Singleton instance of the simulation.
	 * 
	 * @return Simulation instance
	 */
	// public static Simulation instance() {
	// return instance;
	// }

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
		// logger.info("Simulation's instance() is on " +
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
		// logger.info("Simulation's startSimExecutor() is on " +
		// Thread.currentThread().getName() + " Thread");
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
	public static void createNewSimulation(int timeRatio) {
		// logger.info("Simulation's createNewSimulation() is on " +
		// Thread.currentThread().getName() + " Thread");

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

	/**
	 * Initialize transient data in the simulation.
	 */
//    private void initializeTransientData() {
//       //logger.info("Simulation's initializeTransientData() is on " + Thread.currentThread().getName() + " Thread");
//       eventManager = new HistoricalEventManager();
//    }

	/**
	 * Initialize intransient data in the simulation.
	 */
	private void initializeIntransientData(int timeRatio) {
		// logger.info("Simulation's initializeIntransientData() is on " +
		// Thread.currentThread().getName() + " Thread");
		malfunctionFactory = new MalfunctionFactory(SimulationConfig.instance().getMalfunctionConfiguration());
		mars = new Mars();
		missionManager = new MissionManager();
		relationshipManager = new RelationshipManager();
		medicalManager = new MedicalManager();
		masterClock = new MasterClock(isFXGL, timeRatio);
		unitManager = new UnitManager();
		unitManager.constructInitialUnits(); // unitManager needs to be on the same thread as masterClock
		eventManager = new HistoricalEventManager();
		creditManager = new CreditManager();
		scientificStudyManager = new ScientificStudyManager();
		transportManager = new TransportManager();

		// System.out.println("running Simulation's initializeIntransientData()");
		// ResourceUtil.getInstance().initializeNewSim();
		// ResourceUtil.printID();

		ut = masterClock.getUpTimer();
	}

	public void runLoadConfigTask() {
		startSimExecutor();
		simExecutor.execute(new LoadConfigTask());

	}

	public class LoadConfigTask implements Runnable {
		LoadConfigTask() {
		}

		public void run() {
			// logger.info("SimConfigTask's run() is on " +
			// Thread.currentThread().getName());
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
			// logger.info("StartTask's run() is on " + Thread.currentThread().getName());
			start(autosaveDefault);
		}
	}

	/**
	 * Start the simulation.
	 * @param autosaveDefault. True if default is used for autosave
	 */
	public void start(boolean autosaveDefault) {
		// SwingUtilities.invokeLater(() -> testConsole());
		
		masterClock.addClockListener(this);
		masterClock.startClockListenerExecutor();

		if (clockExecutor == null || clockExecutor.isShutdown() || clockExecutor.isTerminated()) {

			clockExecutor = Executors.newSingleThreadExecutor();

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
				clockExecutor.execute(masterClock.getClockThreadTask());
		}

		this.autosaveDefault = autosaveDefault;
		startAutosaveTimer();

		ut = masterClock.getUpTimer();
	}

	/**
	 * Initialize the text-io terminal.
	 */
	public void startTerminal() {
//		try {
//			terminal = TerminalBuilder.terminal();
	//		terminal = TerminalBuilder.builder()
	//                .system(true)
	//                .signalHandler(Terminal.SignalHandler.SIG_IGN)
	//                .build();
//			
//	        terminal.writer().print("Activating Jline3 in this terminal...\n");
//	        terminal.writer().flush();
//	        
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
        SystemTextTerminal sysTerminal = new SystemTextTerminal();
        TextIO textIO = new TextIO(sysTerminal);
        
        // Construct a terminal based on Java Swing 
//		TextIO textIO = TextIoFactory.getTextIO();
		
		user = textIO.newStringInputReader()
//		        .withDefaultValue("admin")
		        .read("Do you want to be added as the commander of a settlement? [y/n]");	
//		System.out.println("user is " + user + " and has " + user.length() + " character(s).");
		
		if (user.equals("y") || user.equals("Y")) {
//		if (!user.equals("n") && !user.equals("N")) {	
			String firstN = textIO.newStringInputReader()
//		        .withDefaultValue("admin")
		        .read("First Name");		
		
			String lastN = textIO.newStringInputReader()
//		        .withDefaultValue("admin")
		        .read("Last Name/Surname");	
		
			user = firstN + " " + lastN;

//		String password = textIO.newStringInputReader()
//		        .withMinLength(6)
//		        .withInputMasking(true)
//		        .read("Password");
			gender = textIO.newStringInputReader()
		        .withMinLength(1)
//		        .withDefaultValue("M")
//		        .withInputMasking(true)
		        .read("Gender [M/F]");
			age = textIO.newIntInputReader()
		        .withMinVal(21)
		        .read("Age");

//		Month month = textIO.newEnumInputReader(Month.class)
//		        .read("What month were you born in?");
			textIO.getTextTerminal().printf("Commander %s, your profile has been set up.\n", user);
		
//        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to terminate...");
//        textIO.dispose("User '" + user + "' has left the building.");
		}
		else {
			user = null;
		}
		
	}
	
	/**
	 * Checks if the user's name is provided
	 * 
	 * @return true if the name is not null
	 */
	public boolean isUserCommander() {
//		System.out.println("isUserCommander() : " + user);
		if (user != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Resets the user's name back to null
	 */
	public void resetUserCommander() {
		user = null;
	}
	
	/** Gets the name of the user */
	public String getUser() {
		return user;
	}
	
	
	/** Gets the gender of the user */
	public String getGender() {
		return gender;
	}
	
	/** Gets the age of the player */
	public int getAge() {
		return age;
	}
	
	/*
	 * Obtains the size of the file
	 * 
	 * @return fileSize in megabytes
	 */
	public double getFileSize() {
		return fileSize;
	}

	/**
	 * Loads a simulation instance from a save file.
	 * 
	 * @param file the file to be loaded from.
	 */
	public void loadSimulation(final File file) {
		// logger.info("Simulation's loadSimulation() is on " +
		// Thread.currentThread().getName());
		isUpdating = true;

		File f = file;

		Simulation sim = instance();
		sim.stop();

		// Use default file path if file is null.
		if (f == null) {
			// logger.info("Yes file is null");
			/* [landrus, 27.11.09]: use the home dir instead of unknown relative paths. */
			f = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
			// logger.info("file is " + f);
			sim.defaultLoad = true;
		} else {
			sim.defaultLoad = false;
		}

		if (f.exists() && f.canRead()) {
			logger.info(Msg.getString("Simulation.log.loadSimFrom", f)); //$NON-NLS-1$

			try {

				sim.readFromFile(f);

			} catch (ClassNotFoundException e2) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim with Class Not Found Exception when loading the simulation! " + " : "
								+ e2.getMessage());
//    	        Platform.exit();
				System.exit(1);

			} catch (IOException e1) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim with I/O error when loading the simulation! " + " : " + e1.getMessage());
//    	        Platform.exit();
				System.exit(1);

			} catch (Exception e0) {
				logger.log(Level.SEVERE,
						"Quitting mars-sim. Could not create a new simulation " + " : " + e0.getMessage());
//    	        Platform.exit();
				System.exit(1);
			}

		}

		else {
			logger.log(Level.SEVERE, "Quitting mars-sim. The saved sim cannot be read or found. ");
			// throw new
			// IllegalStateException(Msg.getString("Simulation.log.fileNotAccessible") +
			// //$NON-NLS-1$ //$NON-NLS-2$
			// f.getPath() + " is not accessible");
//            Platform.exit();
			System.exit(1);

		}

	}

	/**
	 * Reads a serialized simulation from a file.
	 * 
	 * @param file the saved serialized simulation.
	 * @throws ClassNotFoundException if error reading serialized classes.
	 * @throws IOException            if error reading from file.
	 */
	private synchronized void readFromFile(File file) throws ClassNotFoundException, IOException {
		// logger.info("Simulation : running readFromFile()");
		byte[] buf = new byte[8192];
		ObjectInputStream ois = null;
		// FileInputStream in = null;
		FileInputStream in = null;
		// boolean no_go = false;

		try {
			// System.out.println("Simulation : inside try. starting decompressing");
			// in = new FileInputStream(file);
			in = new FileInputStream(file);

			// Replace gzip with xz compression (based on LZMA2)

			// Since XZInputStream does some buffering internally
			// anyway, BufferedInputStream doesn't seem to be
			// needed here to improve performance.
			// in = new BufferedInputStream(in);
			XZInputStream xzin = new XZInputStream(in, 256 * 1024);
			// limit memory usage to 256 MB

			// define a temporary uncompressed file
			File uncompressed = new File(DEFAULT_DIR, TEMP_FILE);
			// Decompress a xz compressed file
			// FileOutputStream fos = new FileOutputStream(uncompressed);
			FileOutputStream fos = new FileOutputStream(uncompressed);

			int size;
			while ((size = xzin.read(buf)) != -1)
				fos.write(buf, 0, size);

			ois = new ObjectInputStream(new FileInputStream(uncompressed));

//            InputStream is;
//
//            try {
//                File simConfig = new File(DEFAULT_DIR, "simulationConfig.json");
//                is = new FileInputStream(simConfig);
//                //String sconfig = fileSimConfig.to"c:\\simulationConfig.json"; // some JSON content
//                SimulationConfig.setInstance((SimulationConfig) JsonReader.jsonToJava(is, null));//.jsonToJava(is, true));
////                is.close(); 
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }

//            String simConfig = "c:\\simulationConfig.json";
//            SimulationConfig.setInstance((SimulationConfig) JsonReader.jsonToJava(simConfig));
//            
//            String resourceUtil = "c:\\resourceUtil.json"; // some JSON content
//            ResourceUtil.setInstance((ResourceUtil) JsonReader.jsonToJava(resourceUtil));
//            
//            String malfunction = "c:\\malfunction.json"; // some JSON content
//            malfunctionFactory = (MalfunctionFactory) JsonReader.jsonToJava(malfunction);

			// Load intransient objects.
			SimulationConfig.setInstance((SimulationConfig) ois.readObject());
			ResourceUtil.setInstance((ResourceUtil) ois.readObject());

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

			if (ois != null) {
				ois.close();
			}
			// Close FileInputStream (directly or indirectly via XZInputStream, it doesn't
			// matter).
			in.close();
			xzin.close();
			fos.close();
			uncompressed.delete();

			// Compute the size of the saved sim
			fileSize = (file.length() / 1000D);
			String fileStr = "";
			// System.out.println("file size is " + fileSize);
			if (fileSize < 1000)
				fileStr = Math.round(fileSize * 10.0) / 10.0 + " KB";
			else
				fileStr = Math.round(fileSize) / 10.0 + " MB";

			// logger.info("The saved sim has a size of "+ fileStr);

			loadBuild = SimulationConfig.instance().build;
			if (loadBuild == null)
				loadBuild = "unknown";

			logger.info("This sim file was made in build " + loadBuild + " (size : " + fileStr + ")");

			if (instance().BUILD.equals(loadBuild)) {
				logger.info("Proceed to loading the saved sim.");
				logger.info("Last Saved Martian Date/Time : " + masterClock.getMarsClock().getDateTimeStamp());
			} else
				logger.warning("Attempting to load the saved sim made in build " + loadBuild
						+ " while running mars-sim in build " + Simulation.BUILD);

		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Quitting mars-sim since " + file + " cannot be found : ", e.getMessage());
//            Platform.exit();
			System.exit(1);

		} catch (EOFException e) {
			logger.log(Level.SEVERE,
					"Quitting mars-sim. Unexpected End of File error on " + file + " : " + e.getMessage());
//            Platform.exit();
			System.exit(1);

		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"Quitting mars-sim. I/O error when decompressing " + file + " : " + e.getMessage());
//            Platform.exit();
			System.exit(1);

		} catch (NullPointerException e) {
			logger.log(Level.SEVERE,
					"Quitting mars-sim. Null pointer error when loading " + file + " : " + e.getMessage());
//	        Platform.exit();
			System.exit(1);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Quitting mars-sim with errors when loading " + file + " : " + e.getMessage());
//	        Platform.exit();
			System.exit(1);
		}

		// if (!no_go) {
		// Initialize transient data.
//	        instance().initializeTransientData();
		instance().initialSimulationCreated = true;
		isUpdating = false;
		// }

		ResourceUtil.getInstance().initializeSavedSim();
	}

	/**
	 * Saves a simulation instance to a save file.
	 * 
	 * @param file the file to be saved to.
	 */
	public synchronized void saveSimulation(int type, File file) throws IOException {
//        logger.info("Simulation's saveSimulation() is on " + Thread.currentThread().getName() + " Thread");
		logger.config(Msg.getString("Simulation.log.saveSimTo") + file); //$NON-NLS-1$
		// System.out.println("file is " + file);

		// Check if it was previously on pause
		boolean previous = masterClock.isPaused();
		// Pause simulation.
		if (!previous) {
			masterClock.setPaused(true, false);
			// System.out.println("previous2 is false. Paused sim");
		}

		Simulation sim = instance();
		sim.halt();

		lastSaveStr = new SystemDateTime().getDateTimeStr();
		changed = true;

		File backupFile = new File(DEFAULT_DIR, "previous" + DEFAULT_EXTENSION);
		FileSystem fileSys = null;
		Path destPath = null;
		Path srcPath = null;

		// Use type to differentiate in what name/dir it is saved
		if (type == SAVE_DEFAULT) {

			file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);

			if (file.exists() && !file.isDirectory()) {
				fileSys = FileSystems.getDefault();
				destPath = fileSys.getPath(backupFile.getPath());
				srcPath = fileSys.getPath(file.getPath());
				// backup the existing default.sim
				Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}

			logger.info("Saving as " + DEFAULT_FILE + DEFAULT_EXTENSION);

		}

		else if (type == SAVE_AS) {
			// System.out.println("file is " + file);
			String f = file.getName();
			// System.out.println("f is " + f);
			String dir = file.getParentFile().getAbsolutePath();
			// System.out.println("dir is " + dir);
			if (!f.contains(".sim"))
				file = new File(dir, f + DEFAULT_EXTENSION);
			// System.out.println("file is " + file);
			logger.info("Saving as " + file);
		}

		else if (type == AUTOSAVE_AS_DEFAULT) {

//            file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//            logger.info("Autosaving as " + DEFAULT_FILE + DEFAULT_EXTENSION);

			file = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);

			if (file.exists() && !file.isDirectory()) {
				fileSys = FileSystems.getDefault();
				destPath = fileSys.getPath(backupFile.getPath());
				srcPath = fileSys.getPath(file.getPath());
				// backup the existing default.sim
				Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}

			logger.info("Autosaving as " + DEFAULT_FILE + DEFAULT_EXTENSION);

		}

		else if (type == AUTOSAVE) {
			String autosaveFilename = lastSaveStr + "_Sol" + masterClock.getMarsClock().getMissionSol() + "_r" + BUILD
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

			// Replace gzip with xz compression (based on LZMA2)
			// See (1)
			// http://stackoverflow.com/questions/5481487/how-to-use-lzma-sdk-to-compress-decompress-in-java
			// (2) http://tukaani.org/xz/xz-javadoc/

			// STEP 1: combine all objects into one single uncompressed file, namely
			// "default"
			uncompressed = new File(DEFAULT_DIR, TEMP_FILE);

			// if the default save directory does not exist, create one now
			if (!uncompressed.getParentFile().exists()) {
				uncompressed.getParentFile().mkdirs();
			}

			oos = new ObjectOutputStream(new FileOutputStream(uncompressed));

//            File fileSimConfig = new File(DEFAULT_DIR, "simulationConfig.json");
//            String sconfig = fileSimConfig.getPath();

			// String simConfig = "c:\\simulationConfig.json";
			// SimulationConfig.setInstance((SimulationConfig)
			// JsonReader.jsonToJava(simConfig));

//            String simConfig = JsonWriter.objectToJson(SimulationConfig.instance()); 
//            System.out.println(simConfig);

//        	String malString = JsonWriter.objectToJson(malfunctionFactory);
//            System.out.println(malString);
//            
//            OutputStream os0 = new ObjectStream(new FileStream(uncompressed));
//            JsonWriter jw = new JsonWriter(os0);       // optional 2nd 'options' argument (see below)
//            jw.write(emp);
//            jw.close();
//            
//        	String marsString = JsonWriter.objectToJson(mars);
//            System.out.println(marsString);
//            
//        	String masterClockString = JsonWriter.objectToJson(masterClock);
//            System.out.println(masterClockString);
//            
//            try{
//                FileWriter file = new FileWriter("c:\\simulationConfig.json", false);
//                file.close();
//            }
//            catch(Exception e){
//                e.getMessage();
//            }

//            String resourceUtil = "c:\\resourceUtil.json"; // some JSON content
//            ResourceUtil.setInstance((ResourceUtil) JsonReader.jsonToJava(resourceUtil));
//
//            String malfunction = "c:\\malfunction.json"; // some JSON content
//            malfunctionFactory = (MalfunctionFactory) JsonReader.jsonToJava(malfunction);

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
			// oos.close();

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

			logger.info("Done saving. Resuming the sim.");

		} catch (NullPointerException e0) {
			logger.log(Level.SEVERE, Msg.getString("Simulation.log.saveError"), e0); //$NON-NLS-1$
			e0.printStackTrace();

			if (fos != null)
				fos.close();
			if (xzout != null)
				xzout.close();

			if (type == AUTOSAVE_AS_DEFAULT || type == SAVE_DEFAULT) {
//	            backupFile = new File(DEFAULT_DIR, DEFAULT_FILE + DEFAULT_EXTENSION);
//	            backupFile.renameTo(file);

				if (file.exists() && !file.isDirectory()) {
					// backup the existing default.sim
					Files.move(destPath, srcPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("Simulation.log.saveError"), e); //$NON-NLS-1$
			e.printStackTrace();

			if (fos != null)
				fos.close();
			if (xzout != null)
				xzout.close();

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
			uncompressed = null;
			// uncompressed.delete(); // cannot be deleted;

			// fis.close(); // fis closed automatically
			// fos.close(); // fos closed automatically
			if (oos != null)
				oos.close();

			if (xzout != null)
				xzout.close();

			sim.proceed();

			justSaved = true;

			// Check if it was previously on pause
			boolean now = masterClock.isPaused();
			if (!previous) {
				if (now) {
					masterClock.setPaused(false, false);
					// System.out.println("previous is false. now is true. Unpaused sim");
				}
			} else {
				if (!now) {
					masterClock.setPaused(false, false);
					// System.out.println("previous is true. now is false. Unpaused sim");
				}
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
		if (clockExecutor != null)
			clockExecutor.shutdownNow();
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
			// simExecutor.shutdown();
			masterClock.stop();
			masterClock.removeClockListener(this);
		}
	}

	/*
	 * Stops and removes the master clock and pauses the simulation
	 */
	private void halt() {
		if (masterClock != null) {
			masterClock.stop();
			masterClock.setPaused(true, false);
			masterClock.removeClockListener(this);
			if (autosaveService != null && !autosaveService.isShutdown() && !autosaveService.isTerminated())
				autosaveService.shutdown();
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
			startAutosaveTimer();
		}
	}

	/**
	 * Returns the time string of the last saving or autosaving action
	 */
	public String getLastSaveTimeStamp() {
		if (lastSaveStr == null || lastSaveStr.equals(""))
			return "Never     ";
		else if (!changed) {
			return lastSaveTimeStamp;
		} else {
			changed = false;
			StringBuilder sb = new StringBuilder();
			int l = lastSaveStr.length();

			// Past : e.g. 03-22-2017_022018PM
			// String s = lastSave.substring(l-8, l);
			// sb.append(s.substring(0, 2)).append(":").append(s.substring(2, 4))
			// .append(" ").append(s.substring(6, 8)).append(" (local time)");

			// Now e.g. 2007-12-03T10.15.30
			// String id = ZonedDateTime.now().getZone().toString();
			String s = lastSaveStr.substring(lastSaveStr.indexOf("T") + 1, l).replace(".", ":");
			sb.append(s).append(WHITESPACES).append(LOCAL_TIME);
			lastSaveTimeStamp = sb.toString();
			return lastSaveTimeStamp;
		}
	}

	/*
	 * Set up a timer for periodically saving the sim
	 */
	public void startAutosaveTimer() {// boolean autosaveDefault) {
		// logger.info("Simulation's startAutosaveTimer() is on " +
		// Thread.currentThread().getName());
		// autosave_minute = SimulationConfig.instance().getAutosaveInterval();
		// Note: should call masterClock's saveSimulation() to first properly interrupt
		// the masterClock,
		// instead of directly call saveSimulation() here in Simulation

		if (autosaveService != null) {
			autosaveService.shutdown();
			autosaveService = null;
		}

		autosaveService = Executors.newSingleThreadScheduledExecutor();

		// AutosaveTask autosaveRunnable = new AutosaveTask();

		if (autosaveDefault) {

			// For headless
//			autosaveTimer = new Timeline(
//				new KeyFrame(Duration.seconds(60 * autosave_minute),
//						ae -> masterClock.setSaveSim(AUTOSAVE_AS_DEFAULT, null)));
//			autosaveTimer = FxTimer.runLater(
//    				java.time.Duration.ofMinutes(60 * autosave_minute),
//    		        () -> masterClock.setSaveSim(AUTOSAVE_AS_DEFAULT, null));
//			EventStreams.ticks(java.time.Duration.ofMinutes(60 * autosave_minute))
//	        .subscribe(tick -> masterClock.setSaveSim(AUTOSAVE_AS_DEFAULT, null));

			setAutosaveDefault();
		} else {
			// for GUI
//			autosaveTimer = new Timeline(
//				new KeyFrame(Duration.seconds(60 * autosave_minute),
//						ae -> masterClock.setAutosave(true)));
//			autosaveTimer = FxTimer.runLater(
//    				java.time.Duration.ofMinutes(60 * autosave_minute),
//    		        () -> masterClock.setAutosave(true));
//			EventStreams.ticks(java.time.Duration.ofMinutes(60 * autosave_minute))
//	        .subscribe(tick -> masterClock.setAutosave(true));

			setAutosave();

			// Note1: Infinite Timeline might result in a memory leak if not stopped
			// properly.
			// Note2: All the objects with animated properties would NOT be garbage
			// collected.

//		autosaveTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
//		autosaveTimer.play();
		}
	}

	public void setAutosaveDefault() {
		Runnable autosaveRunnable = new Runnable() {
			public void run() {
				masterClock.setSaveSim(AUTOSAVE_AS_DEFAULT, null);
			}
		};
		autosaveService.scheduleAtFixedRate(autosaveRunnable, SimulationConfig.instance().getAutosaveInterval(),
				SimulationConfig.instance().getAutosaveInterval(), TimeUnit.MINUTES);

	}

	public void setAutosave() {
		Runnable autosaveRunnable = new Runnable() {
			public void run() {
				masterClock.setAutosave(true);
			}
		};
		autosaveService.scheduleAtFixedRate(autosaveRunnable, SimulationConfig.instance().getAutosaveInterval(),
				SimulationConfig.instance().getAutosaveInterval(), TimeUnit.MINUTES);
	}

	// Add testConsole() for outputting text messages to mars-simmers
//    public void testConsole() {
//    	if (jc == null) {
//    		jc = new JConsole(60,30);
//	    	jc.setCursorVisible(true);
//	    	jc.setCursorBlink(true);
//	    	jc.write("Welcome to Mars Simulation Project!\n\n");
//	    	jc.write("Dear Mars-simmer,\n\nSee hidden logs below. Have fun!\n\n",Color.GREEN,Color.BLACK);
//	    	//System.out.println("Normal output");
//	    	//jc.setCursorPos(0, 0);
//
//	    	//jc.captureStdOut();
//	    	//System.out.println("Captured output");
//
//	    	Frames.display(jc,"MSP Output Console");
//
//	    	//jc.write("after the fact\n");
//    	}
//    }
//
//    public JConsole getJConsole() {
//    	return jc;
//    }

	/**
	 * Gets the Timer instance of the autosave timer.
	 * 
	 * @return autosaveTimeline
	 */
	public ScheduledExecutorService getAutosaveTimer() {
		return autosaveService;
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

	// public ThreadPoolExecutor getClockScheduler() {
	// return clockScheduler;
	// }

	public ExecutorService getClockScheduler() {
		return clockExecutor;
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

	/**
	 * Clock pulse from master clock
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public void clockPulse(double time) {
		// logger.info("Simulation's clockPulse() is in " +
		// Thread.currentThread().getName() + " Thread");
		// it's in pool-4-thread-1 Thread
		// UpTimer ut = null;
		// if (masterClock != null)
		// ut = masterClock.getUpTimer();

		if (ut != null && !masterClock.isPaused()) {

			ut.updateTime();

			if (debug) {
				logger.fine(Msg.getString("Simulation.log.clockPulseMars", //$NON-NLS-1$
						ut.getUptime(), mars.toString()));
			}
			mars.timePassing(time);
			ut.updateTime();

			if (debug) {
				logger.fine(Msg.getString("Simulation.log.clockPulseMissionManager", //$NON-NLS-1$
						masterClock.getUpTimer().getUptime(), missionManager.toString()));
			}
			missionManager.timePassing(time);
			ut.updateTime();

			if (debug) {
				logger.fine(Msg.getString("Simulation.log.clockPulseUnitManager", //$NON-NLS-1$
						masterClock.getUpTimer().getUptime(), unitManager.toString()));
			}
			unitManager.timePassing(time);
			ut.updateTime();

			if (debug) {
				logger.fine(Msg.getString("Simulation.log.clockPulseScientificStudyManager", //$NON-NLS-1$
						masterClock.getUpTimer().getUptime(), scientificStudyManager.toString()));
			}
			scientificStudyManager.updateStudies();
			ut.updateTime();

			if (debug) {
				logger.fine(Msg.getString("Simulation.log.clockPulseTransportManager", //$NON-NLS-1$
						masterClock.getUpTimer().getUptime(), transportManager.toString()));
			}
			transportManager.timePassing(time);

		}
	}

	@Override
	public void uiPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// Do nothing
	}

	/**
	 * Destroys the current simulation to prepare for creating or loading a new
	 * simulation.
	 */
	public void destroyOldSimulation() {
		// logger.info("starting Simulation's destroyOldSimulation()");

		autosaveService = null;

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

		// logger.info("Simulation's destroyOldSimulation() is done");
	}

}
