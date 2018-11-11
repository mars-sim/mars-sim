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

	// public void submitWork(Runnable task) {
	// worker.submit(task);
	// }

	public class SimulationTask implements Runnable {

		public void run() {
			// new Simulation(); // NOTE: NOT supposed to start another instance of the
			// singleton Simulation
			logger.config("Starting " + Simulation.title);

			List<String> argList = Arrays.asList(args);
			useGUI = !argList.contains("-headless");
			generateHelp = argList.contains("-generateHelp");

			if (useGUI) {
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

				// Dispose the splash window.
				// splashWindow.remove();
			} else {
				// headless mode
				// Initialize the simulation.
				initializeSimulation(args);
			}

			// this will generate html files for in-game help based on config xml files
			if (generateHelp) {
				HelpGenerator.generateHtmlHelpFiles();
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
			handleNewSimulation(); // if this fails we always exit, continuing is useless
			result = true;

		} else if (argList.contains("-load")) {
			// If load argument, load simulation from file.
			try {
				// Initialize the simulation.
				SimulationConfig.loadConfig();
				
				Simulation.createNewSimulation(-1);
				
				handleLoadSimulation(argList);

				// FIXME : make it work
			} catch (Exception e) {
				showError("Could not load the desired simulation, trying to create a new Simulation...", e);
				
				handleNewSimulation();
				
				result = true;
			}
		} else {
			// if there is no args, load default.sim
			try {
				// Initialize the simulation.
				SimulationConfig.loadConfig();
				
				Simulation.createNewSimulation(-1);
				
				handleLoadDefaultSimulation();
			} catch (Exception e) {
//                showError("Could not load the default simulation, trying to create a new Simulation...", e);
				handleNewSimulation();
				
				result = true;
			}
		}

		return result;
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
		logger.config(
				"MarsProject's handleLoadDefaultSimulation() is on " + Thread.currentThread().getName() + " Thread");

		try {
			// Load the default simulation
			sim.loadSimulation(null);

		} catch (Exception e) {
			// logger.log(Level.WARNING, "Could not load default simulation", e);
			// throw e;
			exitWithError("Problem loading the default simulation.", e);
		}

		if (useGUI) {
			// Create the main desktop window.
			new MainWindow(false);

			// Start simulation.
			startSimulation(false);
		} else {
			// go headless
			
			// Input user info
			sim.getTerm().startCommanderMode();
			// Start simulation.
			startSimulation(true);
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
			int index = argList.indexOf("-load");
			// Get the next argument as the filename.
			File loadFile = new File(argList.get(index + 1));
			if (loadFile.exists() && loadFile.canRead()) {
				Simulation.instance().loadSimulation(loadFile);

			} else {
				exitWithError("Problem loading simulation. " + argList.get(index + 1) + " not found.", null);
			}
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Problem loading existing simulation", e);
			exitWithError("Problem loading the default simulation.", e);
		}

		// Create the main desktop window.
		new MainWindow(false);

		// Start simulation.
		startSimulation(false);

	}

	/**
	 * Create a new simulation instance.
	 */
	private void handleNewSimulation() {
		// logger.config("MarsProject's handleNewSimulation() is on
		// "+Thread.currentThread().getName() + " Thread");

		try {
			SimulationConfig.loadConfig();

			SwingUtilities.invokeLater(() -> {
				new SimulationConfigEditor(SimulationConfig.instance(), null);
			});

		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}
	}

	/**
	 * Start the simulation instance.
	 */
	public void startSimulation(boolean useDefaultName) {
		// logger.config("MarsProject's startSimulation() is on
		// "+Thread.currentThread().getName() + " Thread");

		// Start the simulation.
		Simulation.instance().start(useDefaultName);
	}

	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		Logger.getLogger("").setLevel(Level.FINE);

		MarsProject.args = args;
		// logger.config("MarsProject's main() is on "+Thread.currentThread().getName() +
		// " Thread");

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
		MarsProject mp = new MarsProject(args);
		// mp.start(com.jme3.system.JmeContext.Type.Headless);

		/*
		 * @Override public void simpleInitApp() { // TODO Auto-generated method stub }
		 * 
		 * 
		 * @Override public void simpleUpdate(float tpf) { // Interact with game events
		 * in the main loop / }
		 */
	}
}