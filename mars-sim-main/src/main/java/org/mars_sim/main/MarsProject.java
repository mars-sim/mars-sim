/**
 * Mars Simulation Project
 * MarsProject.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.main;

import java.io.File;
//import com.jme3.app.SimpleApplication;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;
//import org.mars_sim.msp.ui.javafx.svg.SvgImageLoaderFactory;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;

/**
* MarsProject is the main class for the application. It creates both the
* simulation and the user interface.
*/
public class MarsProject {
	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProject.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";
	
	private static String[] args;

	/** true if displaying graphic user interface. */
	private boolean useGUI = true;
	/** true if player wants no audio. */
	private boolean noaudio = false;
	/** true if help documents should be generated from config xml files. */
	private boolean generateHelp = false;

	private Simulation sim = Simulation.instance();

	private SimulationConfig simulationConfig = SimulationConfig.instance();
	
	private InteractiveTerm interactiveTerm = new InteractiveTerm(false, false);
	
	private static final String HELP = 

			 "java -jar mars-sim-[$VERSION].jar" + System.lineSeparator()
			 +"                    (Note : start a new sim)" + System.lineSeparator()
			 +"   or" + System.lineSeparator()                           
			 + System.lineSeparator()
			 +" java -jar jarfile [args...]" + System.lineSeparator()
			 +"                   (Note : start mars-sim with arguments)" + System.lineSeparator()
			 + System.lineSeparator()
			 +"  where args include :" + System.lineSeparator()
			 + System.lineSeparator()
			 +"    new             start a new sim (by default" + System.lineSeparator()
			 +"                    (Note : if 'load' is absent, 'new' is automatically appended.)," + System.lineSeparator()
			 +"    headless        run in console mode without an user interface (UI)" + System.lineSeparator()
			 +"    0               256MB Min, 1536MB Max (by default)" + System.lineSeparator()
			 +"    1               256MB Min, 1024MB Max" + System.lineSeparator()
			 +"    2               256MB Min, 1536MB Max" + System.lineSeparator()
			 +"    3               256MB Min, 2048MB Max" + System.lineSeparator()
			 +"    4               256MB Min, 2560MB Max" + System.lineSeparator()
			 +"    5               256MB Min, 3072MB Max" + System.lineSeparator()
			 +"    load            open the File Chooser at the /.mars-sim/saved/" + System.lineSeparator() 
			 +"                    and wait for user to choose a saved sim" + System.lineSeparator()
			 +"    load 123.sim    load the sim with filename '123.sim'" + System.lineSeparator()
			 +"                    (Note : '123.sim' must be located at the same " + System.lineSeparator()
			 +"                            folder as the jarfile)" + System.lineSeparator()
			 +"    noaudio         disable background music and sound effect" + System.lineSeparator()
			 +"    512x            set time ratio to 512x (for headless edition only)" + System.lineSeparator()		
			 +"    1024x           set time ratio to 1024x (for headless edition only)" + System.lineSeparator();                   		

	/**
	 * Constructor 1.
	 * 
	 * @param args command line arguments.
	 */
	public MarsProject(String args[]) {
		// logger.config("MarsProject's constructor is on " + Thread.currentThread().getName() + " Thread");
		logger.config("Starting " + Simulation.title);
		sim.startSimExecutor();
		sim.getSimExecutor().submit(new SimulationTask());
		
	}


	public class SimulationTask implements Runnable {

		public void run() {
			// new Simulation(); // NOTE: NOT supposed to start another instance of the
			// singleton Simulation

			List<String> argList = Arrays.asList(args);
			
			String str = "";
			for (String s : args) {
				str = str + "[" + s + "] "; 
			}
			
			logger.config("List of input args : " + str);
			
			if (argList.contains("-headless")) 
				useGUI = false;
			
			generateHelp = argList.contains("-html");
			noaudio = argList.contains("-noaudio");
			
			if (noaudio) {
				logger.config("noaudio argument detected. Turn off sound.");
				// Disable the sound in AudioPlayer
				AudioPlayer.disableVolume();
			}
			
			if (argList.contains("-help")) {
				System.out.println(HELP);
				System.exit(1);
			}
			
			// this will generate html files for in-game help based on config xml files
			else if (generateHelp) {
				logger.config("Generating help files in headless mode in " + Simulation.OS);

				try {
					simulationConfig.loadConfig();
					// this will generate html files for in-game help based on config xml files
					// Relocate the following to handleNewSimulation() right before calling
					// ScenarioConfigEditorFX.
					HelpGenerator.generateHtmlHelpFiles();
					logger.config("Done creating help files.");
					System.exit(1);

				} catch (Exception e) {
					e.printStackTrace();
					exitWithError("Could not generate help files ", e);
				}
			}
			
			else if (useGUI) {
				MainWindow.startSplash();
				// System.setProperty("sun.java2d.opengl", "true"); // not compatible with
				// SplashWindow and SimulationConfigEditor
				if (!MainWindow.OS.contains("linux")) {
					System.setProperty("sun.java2d.ddforcevram", "true"); // question: is this compatible with opengl in
																		// linux and macos ?
				}
				// Enable capability of loading of svg image using regular method
				// SvgImageLoaderFactory.install();

				// Create a splash window
				// SplashWindow splashWindow = new SplashWindow();
				// splashWindow.display();

				initializeSimulation(args);
							
				// Dispose the splash window.
				// splashWindow.remove();
			} else {
				// headless mode
				// Initialize the simulation.
				initializeSimulation(args);
			}
		}
	}

	/**
	 * Initialize the simulation.
	 * 
	 * @param args the command arguments.
	 * @return true if new simulation (not loaded)
	 */
	boolean initializeSimulation(String[] args) {
		boolean result = false;
		boolean useTemplate = false;
		
		// Create a simulation
		List<String> argList = Arrays.asList(args);

		int userTimeRatio = -1;
		
		if (argList.contains("-512x"))
			userTimeRatio = 512;
		if (argList.contains("-1024x"))
			userTimeRatio = 1024;
		else if (argList.contains("-2048x"))
			userTimeRatio = 2048;
		if (argList.contains("-4096x"))
			userTimeRatio = 4096;
		else if (argList.contains("-8192x"))
			userTimeRatio = 8192;
				
		if (argList.contains("-load")) {
			// If load argument, load simulation from file.

			try {
				handleLoadSimulation(userTimeRatio, argList);
				
				// FIXME : make it work
			} catch (Exception e) {
				showError("Could not load the saved simulation, trying to create a new Simulation...", e);
				
				handleNewSimulation(userTimeRatio);

				result = true;
			}
			
		} else {
			for (String arg: argList) {
				if (arg.contains("-template:")) {
					useTemplate = true;
					break;
				}
			}	
				
			if (useTemplate) {
				// Create a new simulation with the specified settlement template
				createNewSettlement(userTimeRatio);
				result = true;
			}
			else {
				handleNewSimulation(userTimeRatio);
				result = true;
			}
		}
		
		return result;
	}

	private void createNewSettlement(int userTimeRatio) {
//		logger.info("createNewSettlement()");
		try {
			// Load xml files
			simulationConfig.loadConfig();
			// Clear the default templates and load the specified template
			loadSettlementTemplate();
			// Alert the user to see the interactive terminal 
			logger.config("Please proceed to selecting Game Mode in the pop-up console...");
			// Start interactive terminal 
			int type = interactiveTerm.startConsoleMainMenu(); 
//			System.out.println("type: " + type);
			if (type == 0) {
				// 0: New Simulation
				// Since the Site Editor is not used, manually set up each of the followings 
				// Create new simulation
				// sim.createNewSimulation(-1, false);
				// Run this class in sim executor
				sim.runCreateNewSimTask(userTimeRatio);
				// Start the simulation
				startSimThread(false);
				// Start the wait layer
				InteractiveTerm.startLayer();
				// Start beryx console
				startConsoleThread();
				// Create main window
				setupMainWindow(true);
//				logger.config("Done with setupMainWindow()");
			}

			else if (type == 1) {
				// 1: Site Editor
				SwingUtilities.invokeLater(() -> {
					new SimulationConfigEditor(SimulationConfig.instance(), userTimeRatio);
				});
			}

			else if (type == 2) {
				// 2: Load saved Sim
				// initialize class instances but do NOT recreate simulation
				sim.createNewSimulation(userTimeRatio, true);

				// Prompt to open the file chooser to select a saved sim
				boolean canLoad = interactiveTerm.loadSimulationProcess();
				
				if (!canLoad) {
					// initialize class instances
					sim.createNewSimulation(userTimeRatio, false);
				}
				else {
					// Start simulation.
					startSimThread(false);					
					// Start the wait layer
					InteractiveTerm.startLayer();
					// Start beryx console
					startConsoleThread();	
					// Create main window
					setupMainWindow(true);
				}
//				logger.config("Done with setupMainWindow()");
			}
					
		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}
	}
	
	/**
	 * Loads the prescribed settlement template
	 * e.g. -8192x -template:1-X -sponsor:SpaceX -country:USA
	 */
	private void loadSettlementTemplate() {
		SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
		String templateString = "";
		String countryString = "";
		String latitude = "";
		String longitude = "";
		
		ReportingAuthorityType authority = null;
		
		for (String s: args) {
			
			if (StringUtils.containsIgnoreCase(s, "-country:")) {
				
				List<String> countries = UnitManager.getAllCountryList();

				for (String c: countries) {

					if (s.contains(c) || s.contains(c.toLowerCase())) {
						countryString = c;
						logger.info(s + " -> " + countryString);
					}
				}
			}
			
			if (StringUtils.containsIgnoreCase(s, "-sponsor:")) {
				
				String sponsor = "";
				for (ReportingAuthorityType ra: ReportingAuthorityType.values()) {
					sponsor = ra.name();
					if (s.contains(sponsor)) {
						authority = ReportingAuthorityType.valueOf(sponsor);
						break;
					}
				}
				logger.info(s + " -> " + sponsor);
			}
			
			
			if (StringUtils.containsIgnoreCase(s, "-template:")) {
				
				settlementConfig.clearInitialSettlements();
				
				Collection<String> templates = settlementConfig.getTemplateMap().values();

				String temp = s.substring(s.indexOf(":") + 1, s.length());
				
				for (String t: templates) {
					if (StringUtils.containsIgnoreCase(t, temp)) {
						templateString = t;
					}
				}
				
				if (!templateString.equals(""))  {
					logger.info(s + " -> " + templateString);
				}
				else {
					logger.severe(templateString + " not found.");
				}
			}
			
			if (StringUtils.containsIgnoreCase(s, "-x:")) {
				
				String lat = s.substring(s.indexOf(":") + 1, s.length());
				if (!lat.contains("_"))
					lat = lat.substring(0, lat.length() - 1) + " " + lat.substring(lat.length() - 1, lat.length());
				else
					lat = lat.replace("_", " ");
				
				String error = Coordinates.checkLat(lat);
				if (error != null) {
					logger.severe("Error: " + error);
				}
				else {
					latitude = lat;
					logger.info(s + " -> " + latitude);
				}
			}
			
			if (StringUtils.containsIgnoreCase(s, "-y:")) {

				String lon = s.substring(s.indexOf(":") + 1, s.length());
				if (!lon.contains("_"))
					lon = lon.substring(0, lon.length() - 1) + " " + lon.substring(lon.length() - 1, lon.length());
				else
					lon = lon.replace("_", " ");
				
				String error = Coordinates.checkLon(lon);
				if (error != null) {
					logger.severe("Error: " + error);
				}
				else {
					longitude = lon;
					logger.info(s + " -> " + longitude);
				}
			}	
		}
		
		SettlementTemplate settlementTemplate = settlementConfig.getSettlementTemplate(templateString);

		if (authority == null) {
			authority = ReportingAuthorityType.MS;
		}
		
		List<String> settlementNames = settlementConfig.getSettlementNameList(authority);
		
		int size = settlementNames.size();
		String settlementName = "";
		int rand = RandomUtil.getRandomInt(size-1);
		settlementName = settlementNames.get(rand);
			
		settlementConfig.addInitialSettlement(settlementName,
											templateString, 
											settlementTemplate.getDefaultPopulation(),
											settlementTemplate.getDefaultNumOfRobots(),
											authority,
											latitude,
											longitude
											);
	}
	
	/**
	 * Exit the simulation with an error message.
	 * 
	 * @param message the error message.
	 * @param e       the thrown exception or null if none.
	 */
	private void exitWithError(String message, Exception e) {
		showError(message, e);
		System.exit(1);
	}

	/**
	 * Show a modal error message dialog.
	 * 
	 * @param message the error message.
	 * @param e       the thrown exception or null if none.
	 */
	private void showError(String message, Exception e) {
		if (e != null) {
			logger.log(Level.SEVERE, message, e);
		} else {
			logger.log(Level.SEVERE, message);
		}

		if (useGUI) {
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Loads the simulation from a save file.
	 * 
	 * @param argList the command argument list.
	 * @throws Exception if error loading the saved simulation.
	 */
	private void handleLoadSimulation(int userTimeRatio, List<String> argList) throws Exception {
//		logger.config("MarsProject's handleLoadSimulation() is on " + Thread.currentThread().getName() + " Thread");
		// Initialize the simulation.
		simulationConfig.loadConfig();
		// Create class instances
		sim.createNewSimulation(userTimeRatio, true);
			
		try {	
			boolean hasDefault = argList.contains(Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION);
			boolean hasSim = argList.contains(Simulation.SAVE_FILE_EXTENSION);
			
			String simStr = "";
			for (String s : argList) {
				if (s.contains(Simulation.SAVE_FILE_EXTENSION))
					simStr = s;
			}
			
			if (hasDefault) {
				File loadFile = new File(SimulationFiles.getSaveDir(), Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION);
				if (loadFile.exists() && loadFile.canRead()) {
					sim.loadSimulation(loadFile);
					// Start simulation.
					startSimThread(false);		
					// Start the wait layer
					InteractiveTerm.startLayer();
					// Start beryx console
					startConsoleThread();
					
					if (useGUI) {
//							startSplash();
//							logger.config("useGUI is " + useGUI);
						setupMainWindow(false);
					} 
					
					else {
						// Go headless				
					}			
				}
				
				else {
//						logger.config("Invalid param.");
					exitWithError("Problem loading simulation. default.sim is found but can't be loaded.", null);
				}
			}
			
			else if (hasSim) {
				File loadFile = new File(SimulationFiles.getSaveDir(), simStr);
				if (loadFile.exists() && loadFile.canRead()) {
					sim.loadSimulation(loadFile);
					// Start simulation.
					startSimThread(false);	
					// Start the wait layer
					InteractiveTerm.startLayer();
					// Start beryx console
					startConsoleThread();	
				}
				
				else {
//						logger.config("Invalid param.");
					exitWithError("Problem loading simulation. default.sim is found but can't be loaded.", null);
				}
			}
			
			else {
				// Prompt to open the file chooser to select a saved sim
				boolean canLoad = MainWindow.loadSimulationProcess(false);
				
				if (!canLoad) {
					// Create class instances
					sim.createNewSimulation(userTimeRatio, false);	
				}
				
				else {			
					// Start simulation clock
					startSimThread(true);
					// Start the wait layer
					InteractiveTerm.startLayer();
					// Start beryx console
					startConsoleThread();
					
					if (useGUI) {
//							startSplash();
						// Create main window
						setupMainWindow(false);
					} 
					
					else {
						// Go headless				
					}			
				}
			}
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Problem loading existing simulation", e);
			exitWithError("Problem loading the default simulation.", e);
		}
	}

	public void setupMainWindow(boolean cleanUI) {
//		new Timer().schedule(new WindowDelayTimer(), 100);
		while (true) {
			Simulation.delay(250);
			
			if (!sim.isUpdating()) {
				new MainWindow(cleanUI).stopLayerUI();
				break;
			}
		}
	}
	
	/**
	 * Create a new simulation instance.
	 */
	private void handleNewSimulation(int userTimeRatio) {
//		logger.config("handleNewSimulation() is on " + Thread.currentThread().getName());
		// Alert the user to see the interactive terminal 
		logger.config("Please proceed to selecting the type of Game Mode in the popped-up console.");

		try {
			if (useGUI) {
//				startSplash();
				// Initialize the simulation.
				simulationConfig.loadConfig();
				// Start interactive terminal
				int type = interactiveTerm.startConsoleMainMenu();
//				logger.config("type is " + type);
				if (type == 0) {		
					// Since SCE is not used, manually set up each of the followings
					// Run this class in sim executor
					sim.runCreateNewSimTask(userTimeRatio);
					// Start the simulation
					startSimThread(false);
					// Start beryx console
					startConsoleThread();
					// Create main window
					setupMainWindow(true);
				
//					logger.config("Done with setupMainWindow()");
				}

				else if (type == 1) {
					// Start sim config editor
					SwingUtilities.invokeLater(() -> {
						new SimulationConfigEditor(SimulationConfig.instance(), userTimeRatio);
					});
				}
			
				else if (type == 2) {
					// Initialize class instances but do NOT recreate simulation
					sim.createNewSimulation(userTimeRatio, true);

					// Prompt to open the file chooser to select a saved sim
					boolean canLoad = interactiveTerm.loadSimulationProcess();
					
					if (!canLoad) {
						// initialize class instances
						sim.createNewSimulation(userTimeRatio, false);
					}
					else {
						// Start simulation.
						startSimThread(false);
						// Start beryx console
						startConsoleThread();
						// Create main window
						setupMainWindow(true);
					}
//					logger.config("Done with setupMainWindow()");
				}
			}
			
			else {
				// Go headless
				// Initialize the simulation.
				simulationConfig.loadConfig();
				// Create serializable class 
				sim.createNewSimulation(userTimeRatio, true);
				// Start interactive terminal
				interactiveTerm.startConsoleMainMenu();
				// Start beryx console
				startConsoleThread();
				// Start the simulation.
				startSimThread(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}
	}
	
	/**
	 * Start a thread for starting the simulation clock instance.
	 * 
	 * @param useDefaultName
	 */
	public void startSimThread(boolean useDefaultName) {
		// Start the simulation.
		ExecutorService e = sim.getSimExecutor();
		if (e == null || (e != null && (e.isTerminated() || e.isShutdown())))
			sim.startSimExecutor();
		e.submit(new StartTask(useDefaultName));
	}
	
	class StartTask implements Runnable {
	boolean autosaveDefault;

		StartTask(boolean autosaveDefault) {
			this.autosaveDefault = autosaveDefault;
		}
	
		public void run() {
//			while (true) {
//				try {
//					Thread.sleep(250L);
//				} catch (InterruptedException e) {
//				}
//				
//				if (!sim.isUpdating()) {
//					logger.config("StartTask's run() is on " + Thread.currentThread().getName());
					sim.startClock(autosaveDefault);
//					break;
//				}
//			}
		}
	}
	
	/**
	 * Start the simulation instance.
	 */
	public void startConsoleThread() {
		// Start the simulation.
		ExecutorService e = sim.getSimExecutor();
		if (e == null || (e != null && (e.isTerminated() || e.isShutdown())))
			sim.startSimExecutor();
		e.submit(new ConsoleTask());
	}
	
	class ConsoleTask implements Runnable {

		ConsoleTask() {
		}
		
		public void run() {
//			while (true) {
//				try {
//					Thread.sleep(100L);
//				} catch (InterruptedException e) {
//				}
//				
//				if (!sim.isUpdating()) {
//					logger.config("ConsoleTask run() is on " + Thread.currentThread().getName());

					// Load the menu choice
					InteractiveTerm.loadTerminalMenu();
//					break;
//				}
//			}
		}
	}
	
	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		Logger.getLogger("").setLevel(Level.FINE);

		MarsProject.args = args;

		/*
		 * [landrus, 27.11.09]: Read the logging configuration from the classloader, so
		 * that this gets webstart compatible. Also create the logs dir in user.home
		 */
		new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();

		try {
			LogManager.getLogManager().readConfiguration(MarsProject.class.getResourceAsStream(LOGGING_PROPERTIES));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Could read logging default config", e);
			}
		}

		// general text antialiasing
		System.setProperty("swing.aatext", "true");
		System.setProperty("awt.useSystemAAFontSettings", "lcd"); // for newer VMs

		// starting the simulation
		new MarsProject(args);
	}

}
