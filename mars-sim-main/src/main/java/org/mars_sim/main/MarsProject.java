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
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;
//import org.mars_sim.msp.ui.javafx.svg.SvgImageLoaderFactory;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;

/**
 * MarsProject is the main class for the application. It creates both the
 * simulation and the user interface.
 */
public class MarsProject {
	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProject.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";
	
	static String[] args;

	/** true if displaying graphic user interface. */
	private boolean useGUI = true;

	/** true if help documents should be generated from config xml files. */
	private boolean generateHelp = false;

	private Simulation sim = Simulation.instance();

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
			 +"    1               256MB Min, 512MB Max" + System.lineSeparator()
			 +"    2               256MB Min, 768MB Max" + System.lineSeparator()
			 +"    3               256MB Min, 1024MB Max" + System.lineSeparator()
			 +"    4               256MB Min, 1536MB Max" + System.lineSeparator()
			 +"    5               256MB Min, 2048MB Max" + System.lineSeparator()
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
		// logger.config("MarsProject's constructor is on
		// "+Thread.currentThread().getName() + " Thread");
		sim.startSimExecutor();
		sim.getSimExecutor().submit(new SimulationTask());
	}


	public class SimulationTask implements Runnable {

		public void run() {
			// new Simulation(); // NOTE: NOT supposed to start another instance of the
			// singleton Simulation
			logger.config("Starting " + Simulation.title);

			List<String> argList = Arrays.asList(args);
			boolean headless = argList.contains("-headless");
			if (headless) useGUI = false;
			generateHelp = argList.contains("-generateHelp");
			
			if (argList.contains("-help")) {
				System.out.println(HELP);
				System.exit(1);
			}
			
			// this will generate html files for in-game help based on config xml files
			else if (generateHelp) {
				HelpGenerator.generateHtmlHelpFiles();
			}
			
			else if (useGUI) {
				// System.setProperty("sun.java2d.opengl", "true"); // not compatible with
				// SplashWindow and SimulationConfigEditor
				System.setProperty("sun.java2d.ddforcevram", "true"); // question: is this compatible with opengl in
																		// linux and macos ?

				// Enable capability of loading of svg image using regular method
				// SvgImageLoaderFactory.install();

				// Create a splash window
				// SplashWindow splashWindow = new SplashWindow();
				// splashWindow.display();

				initializeSimulation(args);
				logger.config("Done with initializeSimulation()");

				// Load the menu choice
				sim.getTerm().loadTerminalMenu();
				
				// Create the main desktop window.
//				new MainWindow(false);
//				logger.config("Done with MainWindow()");
				
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
			// Initialize the simulation.
			SimulationConfig.loadConfig();
			// Create serializable class 
			Simulation.createNewSimulation(-1, true);
			
			// If new argument, create new simulation.
			handleNewSimulation(); // if this fails we always exit, continuing is useless
			
			result = true;

		} else if (argList.contains("-load")) {
			// If load argument, load simulation from file.

			// Initialize the simulation.
			SimulationConfig.loadConfig();
			// Create serializable class 
			Simulation.createNewSimulation(-1, true);
			
			try {
				handleLoadSimulation(argList);
				
				// FIXME : make it work
			} catch (Exception e) {
				showError("Could not load the desired simulation, trying to create a new Simulation...", e);
				
				handleNewSimulation();

				result = true;
			}
			
		} else {
			// if there is no args, load default.sim in GUI mode
			
			// Initialize the simulation.
			SimulationConfig.loadConfig();
			// Create serializable class 
			Simulation.createNewSimulation(-1, true);
			
			try {
				handleLoadDefaultSimulation();

				logger.config("Done with handleLoadDefaultSimulation()");
				
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
	public void initTerminal() {
		// Initialize interactive terminal 
		sim.getTerm().initializeTerminal();	
		// Load the menu choice
		sim.getTerm().loadTerminalMenu();
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
		logger.config("handleLoadDefaultSimulation() is on " 
				+ Thread.currentThread().getName() + " Thread");

		try {
//			sim.loadSimulation(null);
			// Prompt to open the file cHooser to select a saved sim
			MainWindow.loadSimulationProcess(false);
			logger.config("Done with MainWindow.loadSimulationProcess(true)");
			// Start simulation.
			startSimulation(true);

			logger.config("useGUI is " + useGUI);
			if (useGUI) {
				// Create main window
				new MainWindow(true);
			} 
			
			else {
				// Go headless				
			}
			// Initialize interactive terminal and load menu
			initTerminal();
			
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
		logger.config("MarsProject's handleLoadSimulation() is on " + Thread.currentThread().getName() + " Thread");

		try {
			boolean hasDefault = argList.contains("default.sim");
			int index = argList.indexOf("-load");
			boolean hasSim = argList.contains(".sim");
			
			if (hasDefault || !hasSim) {
				// Prompt to open the file cHooser to select a saved sim
				MainWindow.loadSimulationProcess(false);
			}

			else if (!hasDefault && hasSim) {
				// Get the next argument as the filename.
				File loadFile = new File(argList.get(index + 1));
				if (loadFile.exists() && loadFile.canRead()) {
					sim.loadSimulation(loadFile);
					logger.config("Done sim.loadSimulation()");
				}
				else {
//					logger.config("Invalid param.");
					exitWithError("Problem loading simulation. No valid saved sim is found.", null);
				}
			}
				
			// Start simulation.
			startSimulation(false);

			logger.config("useGUI is " + useGUI);
			if (useGUI) {
				// Create main window
				new MainWindow(true);
			} 
			
			else {
				// Go headless				
			}
			// Initialize interactive terminal and load menu
			initTerminal();
			
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Problem loading existing simulation", e);
			exitWithError("Problem loading the default simulation.", e);
		}
	}

	/**
	 * Create a new simulation instance.
	 */
	private void handleNewSimulation() {
		 logger.config("MarsProject's handleNewSimulation() is on " + Thread.currentThread().getName());

		try {
			// Alert the user to see the interactive terminal 
			logger.config("Please proceed to selecting the type of Game Mode in the popped-up console.");
			// Start interactive terminal
			sim.getTerm().startModeSelection();
			// Initialize interactive terminal 
			sim.getTerm().initializeTerminal();	
			// Call Sim config editor in a thread
			SwingUtilities.invokeLater(() -> {
				new SimulationConfigEditor(SimulationConfig.instance(), null);
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}
			
		if (useGUI) {

		} 
		
		else {
			// Go headless

			// Start the simulation.
			startSimulation(true);				
			// Load the menu choice
			sim.getTerm().loadTerminalMenu();	
		}

		logger.config("Done handleNewSimulation()");
	}

	/**
	 * Start the simulation instance.
	 */
	public void startSimulation(boolean useDefaultName) {
		// logger.config("MarsProject's startSimulation() is on
		// "+Thread.currentThread().getName() + " Thread");

		// Start the simulation.
		sim.start(useDefaultName);
		logger.config("Done with sim.start()");
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

		// Add command prompt console
//        Console console = System.console();
//        if(console == null && !GraphicsEnvironment.isHeadless()){
//            String filename = MarsProject.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
//            Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
//        }else{
//        	MarsProject.main(new String[0]);
//            System.out.println("Program has ended, please type 'exit' to close the console");
//        }

		// starting the simulation
		new MarsProject(args);
		
//		 mp.start(com.jme3.system.JmeContext.Type.Headless);

//		 @Override 
//		 public void simpleInitApp() { 
//			 // TODO Auto-generated method stub 
//		 }
//		 @Override 
//		 public void simpleUpdate(float tpf) { 
//			 // Interact with game events
//			 //in the main loop 
//		 }

	}
}