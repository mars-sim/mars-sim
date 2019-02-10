/**
 * Mars Simulation Project
 * MarsProjectHeadless.java
 * @version 3.1.0 2018-06-14
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.headless;

import java.io.File;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.lang.Runnable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

/**
 * MarsProjectHeadless is the main class for starting mars-sim in purely
 * headless mode.
 */
public class MarsProjectHeadless {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectHeadless.class.getName());
	
	private static final String LOGGING_PROPERTIES = "/logging.properties";

	static String[] args;

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
	public MarsProjectHeadless(String args[]) {
		logger.config("Starting " + Simulation.title);
		sim.startSimExecutor();
		sim.getSimExecutor().submit(new SimulationTask());
		
	}

	public class SimulationTask implements Runnable {

		public void run() {
			// new Simulation(); // NOTE: NOT supposed to start another instance of the
			// singleton Simulation
			
			// Decompress map dat files
			//decompressMaps();
			
			// Initialize the simulation.
			initializeSimulation(args);

		}
	}

//	public void decompressMaps() {		
//	}
	
	/**
	 * 	Initialize interactive terminal and load menu
	 */
	public void initTerminal() {
		// Initialize interactive terminal 
		sim.getTerm().initializeTerminal();	
	}
	
	/**
	 * Initialize the simulation.
	 * 
	 * @param args the command arguments.
	 * @return true if new simulation (not loaded)
	 */
	boolean initializeSimulation(String[] args) {
		boolean result = false;
		int userTimeRatio = -1;

		// Create a simulation
		List<String> argList = Arrays.asList(args);

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

		if (argList.contains("-help")) {
			System.out.println(HELP);
			System.exit(1);
		}
		
		else if (argList.contains("-new")) {
			// If new argument, create new simulation.
			handleNewSimulation(userTimeRatio); // if this fails we always exit, continuing is useless
			result = true;
		} 
		
		else if (argList.contains("-load")) {
			// If load argument, load simulation from file.
			try {
				// Initialize the simulation.
				SimulationConfig.loadConfig();
				
				Simulation.createNewSimulation(userTimeRatio, true);
				
				handleLoadDefaultSimulation();

				// FIXME : make it work
			} catch (Exception e) {
				showError("Could not load the desired simulation. Staring a new Simulation instead. ", e);
				handleNewSimulation(userTimeRatio);
				result = true;
			}
			

		} 
		
		else {
			// if there is no args, load default.sim
//                showError("Could not load the default simulation, trying to create a new Simulation...", e);
			handleNewSimulation(userTimeRatio);
			result = true;
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
	}

	/**
	 * Loads the simulation from the default save file.
	 * 
	 * @throws Exception if error loading the default saved simulation.
	 */
	private void handleLoadDefaultSimulation() throws Exception {
		try {
			// Load the default simulation
			sim.loadSimulation(null);
			// Start simulation.
			startSimulation(true);
			// Initialize interactive terminal and load menu
			initTerminal();
			
		} catch (Exception e) {
			// logger.log(Level.WARNING, "Could not load default simulation", e);
//			 throw e;
			exitWithError("Problem loading the default simulation.", e);
		}
	}

	/**
	 * Create a new simulation instance.
	 */
	private void handleNewSimulation(int userTimeRatio) {
		try {
			// Load xml files
			SimulationConfig.loadConfig();
			// Alert the user to see the interactive terminal 
			logger.config("Please proceed to selecting the type of Game Mode in the popped-up console.");
			// Start interactive terminal 
			sim.getTerm().startModeSelection(); 
			
			sim.destroyOldSimulation();

			Simulation.createNewSimulation(userTimeRatio, false);
			
			startSimulation(true);
			// Load the menu choice
//			sim.getTerm().loadTerminalMenu();
			
		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}
	}

	/**
	 * Start the simulation instance.
	 */
	public void startSimulation(boolean useDefaultName) {
		// Start the simulation.
		sim.runStartTask(useDefaultName);
	}

	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		Logger.getLogger("").setLevel(Level.ALL);//.FINE);

		MarsProjectHeadless.args = args;

		new File(Simulation.USER_HOME, Simulation.MARS_SIM_DIR + File.separator + Simulation.LOGS_DIR).mkdirs();

		try {
			LogManager.getLogManager()
					.readConfiguration(MarsProjectHeadless.class.getResourceAsStream(LOGGING_PROPERTIES));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Could read logging default config", e);
			}
		}
	
		// Add command prompt console 
//		Console console = System.console(); 
//		if (console == null && !GraphicsEnvironment.isHeadless()){
//			String filename = MarsProject.class.getProtectionDomain().getCodeSource().getLocation().
//			toString().substring(6); 
//			Runtime.getRuntime().exec(new
//			String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
//		} else { 
//			MarsProjectHeadless.main(new String[0]); 
//			System.out.println("Program has ended, please type 'exit' to close the console"); 
//		}


		// starting the simulation
//		MarsProjectHeadless mp = 
		new MarsProjectHeadless(args);

	}
}