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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.lang3.StringUtils;
import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * MarsProjectHeadless is the main class for starting mars-sim in purely
 * headless mode.
 */
public class MarsProjectHeadless {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectHeadless.class.getName());
	
	private static final String LOGGING_PROPERTIES = "/logging.properties";

	private static final String DEFAULT_SIM_FILENAME = "default.sim";
	
	private List<String> argList;

	private Simulation sim = Simulation.instance();
	
	private SimulationConfig simulationConfig = SimulationConfig.instance();
	
	private InteractiveTerm interactiveTerm = new InteractiveTerm(true, false);
	
	private String templatePhaseString;
	
	private String countryString;
	
	private String sponsorString;
	
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
	public MarsProjectHeadless(String[] args) {
//		this.args = args;
		logger.config("Starting " + Simulation.title);
		sim.startSimExecutor();
		sim.getSimExecutor().submit(new SimulationTask(args));		
	}

	public class SimulationTask implements Runnable {
		
		private String[] args;
		
		private SimulationTask(String[] args) {
			this.args = args;
		}
		
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
	
//	/**
//	 * 	Initialize interactive terminal and load menu
//	 */
//	public void initTerminal() {
//		// Initialize interactive terminal 
//		InteractiveTerm.initializeTerminal();	
//	}
	
	/**
	 * Initialize the simulation.
	 * 
	 * @param args the command arguments.
	 * @return true if new simulation (not loaded)
	 */
	boolean initializeSimulation(String[] args) {
//		logger.info("Calling initializeSimulation() ");
//		for (String s: args)
//			System.out.print(s + " ");
//		System.out.println();
		
		boolean useTemplate = false;
		
		boolean result = false;
		int userTimeRatio = -1;

		// Create a simulation
		argList = Arrays.asList(args);

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
		
		else if (argList.contains("-load")) {
			// If load argument, load simulation from file.
			try {
				
				handleLoadSimulation(userTimeRatio);

				// FIXME : make it work
			} catch (Exception e) {
				showError("Could not load the desired simulation. Staring a new Simulation instead. ", e);
				handleNewSimulation(userTimeRatio);
				result = true;
			}
			
		} 
		
		else {//if (argList.contains("-new")) {
//			logger.info("has -new");
			for (String arg: argList) {
				if (arg.contains("-template:")) {
					useTemplate = true;
					break;
				}
			}
		} 
		
//		else {
//			// if there is no args, load default.sim
////                showError("Could not load the default simulation, trying to create a new Simulation...", e);
//			handleNewSimulation(userTimeRatio);
//			result = true;
//		}

		if (useTemplate) {
			// Create a new simulation with the specified settlement template
			createNewSettlement();
			result = true;
		}
//		else {
//			handleNewSimulation(userTimeRatio);
//			result = true;
//		}
		
		return result;
	}

	
	private void loadSettlementTemplate() {
//		logger.config("loadSettlementTemplate()");
		String templateString = "";
		SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
		
		for (String s: argList) {
			if (StringUtils.containsIgnoreCase(s, "-country:")) {
				List<String> countries = UnitManager.getAllCountryList();
//				System.out.println(countries);
				logger.info("has " + s);
				for (String c: countries) {
//					logger.info(c);
					if (s.contains(c) || s.contains(c.toLowerCase())) {
						countryString = c;
						logger.info("Found country string: " + countryString);
					}
				}
			}
			
			if (StringUtils.containsIgnoreCase(s, "-sponsor:")) {
				List<String> sponsors = UnitManager.getAllShortSponsors();
//				System.out.println(sponsors);
				logger.info("has " + s);
				for (String ss: sponsors) {
//					logger.info(ss);
					if (s.contains(ss) || s.contains(ss.toLowerCase())) {
						sponsorString = ss;
						logger.info("Found sponsor string: " + sponsorString);
					}
				}
			}
			
			
			if (StringUtils.containsIgnoreCase(s, "-template:")) {
				settlementConfig.clearInitialSettlements();
				
				Collection<String> templates = settlementConfig.getTemplateMap().values();//MarsProjectHeadlessStarter.getTemplates();
//				System.out.println(templates);
				logger.info("has " + s);
				templatePhaseString = s.substring(s.indexOf(":") + 1, s.length());
				logger.info("Found templatePhaseString: " + templatePhaseString);
				for (String t: templates) {
					if (StringUtils.containsIgnoreCase(t, templatePhaseString)) {
						templateString = t;
					}
				}
			}
		}
		
		SettlementTemplate settlementTemplate = settlementConfig.getSettlementTemplate(templateString);

		String longSponsorName = ReportingAuthorityType.convertSponsorNameShort2Long(sponsorString);
		
		List<String> settlementNames = settlementConfig.getSettlementNameList(longSponsorName);
		
		if (settlementNames.isEmpty()) {
			settlementNames = settlementConfig.getSettlementNameList("Mars Society (MS)");
		}
		
		int size = settlementNames.size();
		String settlementName = "";
		int rand = RandomUtil.getRandomInt(size-1);
		settlementName = settlementNames.get(rand);
			
		settlementConfig.addInitialSettlement(settlementName,
											templateString, 
											settlementTemplate.getDefaultPopulation(),
											settlementTemplate.getDefaultNumOfRobots(),
											longSponsorName,
											"0.0", //latitude,
											"0.0" //longitude
											);
	}
	
	private void createNewSettlement() {
//		logger.info("createNewSettlement()");
		try {
			// Load xml files
			simulationConfig.loadConfig();
			// Clear the default templates and load the specified template
			loadSettlementTemplate();
			// Alert the user to see the interactive terminal 
			logger.config("Please proceed to selecting the type of Game Mode in the popped-up console.");
			// Start interactive terminal 
			int type = interactiveTerm.startConsoleMainMenu(); 
			// Initialize interactive terminal 
			InteractiveTerm.initializeTerminal();	
			
			if (type == 0) {
				// Since SCE is not used, manually set up each of the followings 
				// Create new simulation
				// sim.createNewSimulation(-1, false);
				// Run this class in sim executor
				sim.runCreateNewSimTask();	

				// Start the simulation
				startSimThread(false);
				
				// Start beryx console
				startConsoleThread();
			
//				logger.config("Done with setupMainWindow()");
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}		
		
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
	private void handleLoadSimulation(int userTimeRatio) throws Exception {
		// Initialize the simulation.
		simulationConfig.loadConfig();
		// Create class instances
		sim.createNewSimulation(userTimeRatio, true);
		
		try {
			boolean hasDefault = argList.contains(DEFAULT_SIM_FILENAME);
			int index = argList.indexOf("-load");
			boolean hasSim = argList.contains(".sim");
			
			// Initialize interactive terminal 
			InteractiveTerm.initializeTerminal();	
			
			if (hasDefault || !hasSim) {
				// Prompt to open the file cHooser to select a saved sim
				boolean canLoad = loadSimulationProcess(false);
				if (!canLoad) {
					// Create class instances
					sim.createNewSimulation(userTimeRatio, false);	
				}
				else {			
					// Start simulation clock
					startSimThread(true);				
					
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

	/**
	 * Performs the process of loading a simulation.
	 * 
	 * @param autosave
	 */
	public boolean loadSimulationProcess(boolean autosave) {
		sim.stop();

		String dir = null;
		String title = null;

		// Add autosave
		if (autosave) {
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		} else {
			dir = Simulation.SAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}

		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle(title); // $NON-NLS-1$
		if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			sim.loadSimulation(chooser.getSelectedFile());
			return true;
		}
		
		return false;
	}
	
	/**
	 * Create a new simulation instance without loading the Simulation Configuration Editor
	 */
	private void handleNewSimulation(int userTimeRatio) {
		try {
			// Load xml files
			simulationConfig.loadConfig();
			// Alert the user to see the interactive terminal 
			logger.config("Please proceed to selecting the type of Game Mode in the popped-up console.");
			// Start interactive terminal 
			int type = interactiveTerm.startConsoleMainMenu(); 
			// Initialize interactive terminal 
			InteractiveTerm.initializeTerminal();	
			
			if (type == 0) {
				// Since SCE is not used, manually set up each of the followings 
				// Create new simulation
				// sim.createNewSimulation(-1, false);
				// Run this class in sim executor
				sim.runCreateNewSimTask();	

				// Start the simulation
				startSimThread(false);
				
				// Start beryx console
				startConsoleThread();
			
//				logger.config("Done with setupMainWindow()");
			}
			
			else if (type == 1) {
				// Replace the Site Editor GUI with a CLI Site Editor
				
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
					
					// Start beryx console
					startConsoleThread();
				
				}
//				logger.config("Done with setupMainWindow()");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
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
	 * Start the simulation instance.
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
//			logger.config("StartTask's run() is on " + Thread.currentThread().getName());
			sim.startClock(autosaveDefault);
			// Load the menu choice
//			InteractiveTerm.loadTerminalMenu();
		}
	}
	
	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		Logger.getLogger("").setLevel(Level.ALL);//.FINE);

//		MarsProjectHeadless.args = args;

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

