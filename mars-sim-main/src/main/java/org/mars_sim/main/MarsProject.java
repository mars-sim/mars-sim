/**
 * Mars Simulation Project
 * MarsProject.java
* @version 3.1.0 2016-10-03
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
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
	
	private static final String DEFAULT_SIM_FILENAME = "default.sim";
	
	static String[] args;

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
			 +"    0               256MB Min, 1024MB Max (by default)" + System.lineSeparator()
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
		sim.startSimExecutor();
		sim.getSimExecutor().submit(new SimulationTask());
		
	}


	public class SimulationTask implements Runnable {

		public void run() {
			// new Simulation(); // NOTE: NOT supposed to start another instance of the
			// singleton Simulation
			logger.config("Starting " + Simulation.title);

			List<String> argList = Arrays.asList(args);
			
			if (argList.contains("-headless")) 
				useGUI = false;
			
			generateHelp = argList.contains("-html");
			noaudio = argList.contains("-noaudio");
			
			if (noaudio) {
				logger.config("noaudio argument detected. Turn off sound.");
				// Disable the sound in AudioPlayer
				AudioPlayer.disableSound();
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

		// Create a simulation
		List<String> argList = Arrays.asList(args);

		if (argList.contains("-new")) {

			// If new argument, create new simulation.
			handleNewSimulation();
			
			result = true;

		} else if (argList.contains("-load")) {
			// If load argument, load simulation from file.

			try {
				handleLoadSimulation(argList);
				
				// FIXME : make it work
			} catch (Exception e) {
				showError("Could not load the saved simulation, trying to create a new Simulation...", e);
				
				handleNewSimulation();

				result = true;
			}
			
		} else {
			// if there is no args, load default.sim in GUI mode
			
			try {
				handleLoadDefaultSimulation();
//				logger.config("Done with handleLoadDefaultSimulation()");
				
			} catch (Exception e) {
                showError("Could not load the default simulation, trying to create a new Simulation...", e);
				
                handleNewSimulation();

				result = true;
			}
		}

		return result;
	}

	/**
	 * 	Initialize interactive terminal and load menu
	 */
	public void initTerminalLoadMenu() {
		// Initialize interactive terminal 
		InteractiveTerm.initializeTerminal();	
		// Load the menu choice
		InteractiveTerm.loadTerminalMenu();
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
	 * Loads the simulation from the default save file.
	 * 
	 * @throws Exception if error loading the default saved simulation.
	 */
	private void handleLoadDefaultSimulation() throws Exception {
		// Initialize the simulation.
		simulationConfig.loadConfig();
		// Create serializable class 
		sim.createNewSimulation(-1, true);
		
		try {
			// Prompt to open the file cHooser to select a saved sim
			boolean canLoad = MainWindow.loadSimulationProcess(false);
			if (!canLoad) {
				// Create class instances
				sim.createNewSimulation(-1, false);	
			}
			else {		
				// Start simulation.
				startSimThread(true);
				
				if (useGUI) {
					// Create main window
					setupMainWindow();
				} 
				
				else {
					// Go headless				
				}
				
				// Start beryx console
				startConsoleThread();
			}
			

//			logger.config("useGUI is " + useGUI);

			
			// Initialize interactive terminal and load menu
//			initTerminalLoadMenu();
			
		} catch (Exception e) {
			// logger.log(Level.WARNING, "Could not load default simulation", e);
			// throw e;
			exitWithError("Problem loading the default simulation.", e);
		}

	}

	/**
	 * Loads the simulation from a save file.
	 * 
	 * @param argList the command argument list.
	 * @throws Exception if error loading the saved simulation.
	 */
	private void handleLoadSimulation(List<String> argList) throws Exception {
//		logger.config("MarsProject's handleLoadSimulation() is on " + Thread.currentThread().getName() + " Thread");
		// Initialize the simulation.
		simulationConfig.loadConfig();
		// Create class instances
		sim.createNewSimulation(-1, true);
		
		try {
			boolean hasDefault = argList.contains(DEFAULT_SIM_FILENAME);
			int index = argList.indexOf("-load");
			boolean hasSim = argList.contains(".sim");
			
			// Initialize interactive terminal 
			InteractiveTerm.initializeTerminal();	
			
			if (hasDefault || !hasSim) {
				// Prompt to open the file cHooser to select a saved sim
				boolean canLoad = MainWindow.loadSimulationProcess(false);
				if (!canLoad) {
					// Create class instances
					sim.createNewSimulation(-1, false);	
				}
				else {			
					// Start simulation clock
					startSimThread(true);
					
					if (useGUI) {
						// Create main window
						setupMainWindow();
					} 
					
					else {
						// Go headless				
					}
					
					// Start beryx console
					startConsoleThread();
				
				}

				// Initialize interactive terminal and load menu
//				initTerminalLoadMenu();
			}

			else if (!hasDefault && hasSim) {
				// Get the next argument as the filename.
				File loadFile = new File(argList.get(index + 1));
				if (loadFile.exists() && loadFile.canRead()) {
					sim.loadSimulation(loadFile);

					// Start simulation.
					startSimThread(false);	
					
					if (useGUI) {
//						logger.config("useGUI is " + useGUI);
						setupMainWindow();
					} 
					
					else {
						// Go headless				
					}
					
					// Start beryx console
					startConsoleThread();
				
				}
				else {
//					logger.config("Invalid param.");
					exitWithError("Problem loading simulation. No valid saved sim is found.", null);
				}
			}

		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Problem loading existing simulation", e);
			exitWithError("Problem loading the default simulation.", e);
		}
	}

	public void setupMainWindow() {
//		new Timer().schedule(new WindowDelayTimer(), 100);
		while (true) {
			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {
			}
			
			if (!sim.isUpdating()) {
				new MainWindow(true);
				break;
			}
		}
		Simulation.delay(1000);
	}
	
//	/**
//	 * Defines the delay timer class
//	 */
//	class WindowDelayTimer extends TimerTask {
//		public void run() {
//			// Create main window
//			SwingUtilities.invokeLater(() -> new MainWindow(true));
//		}
//	}
	
	
	/**
	 * Create a new simulation instance.
	 */
	private void handleNewSimulation() {
//		logger.config("handleNewSimulation() is on " + Thread.currentThread().getName());
		// Alert the user to see the interactive terminal 
		logger.config("Please proceed to selecting the type of Game Mode in the popped-up console.");

		try {
			if (useGUI) {
				// Initialize the simulation.
				simulationConfig.loadConfig();
				// Start interactive terminal
				int type = interactiveTerm.startConsoleMainMenu();
				// Initialize interactive terminal 
				InteractiveTerm.initializeTerminal();	
				// Start sim config editor
//				logger.config("type is " + type);
				if (type == 0) {		
					// Since SCE is not used, manually set up each of the followings 
					// Create new simulation
					// sim.createNewSimulation(-1, false);
					// Run this class in sim executor
					sim.runCreateNewSimTask();	

					// Start the simulation
					startSimThread(false);
					
					// Create main window
					setupMainWindow();
					
					// Start beryx console
					startConsoleThread();
				
//					logger.config("Done with setupMainWindow()");
				}
				
				else if (type == 1) {
					SwingUtilities.invokeLater(() -> {
						new SimulationConfigEditor(SimulationConfig.instance());
					});
				}
			
				else if (type == 2) {
					// initialize class instances but do NOT recreate simulation
					sim.createNewSimulation(-1, true);

					// Prompt to open the file cHooser to select a saved sim
					boolean canLoad = interactiveTerm.loadSimulationProcess();
					
					if (!canLoad) {
						// initialize class instances
						sim.createNewSimulation(-1, false);
					}
					else {
						// Start simulation.
						startSimThread(false);
						
						// Create main window
						setupMainWindow();
						
						// Start beryx console
						startConsoleThread();
					
					}
//					logger.config("Done with setupMainWindow()");
				}
			}
			
			else {
				// Go headless
				// Initialize the simulation.
				simulationConfig.loadConfig();
				// Create serializable class 
				sim.createNewSimulation(-1, true);
				// Start interactive terminal
				interactiveTerm.startConsoleMainMenu();
				// Initialize interactive terminal 
				InteractiveTerm.initializeTerminal();	
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