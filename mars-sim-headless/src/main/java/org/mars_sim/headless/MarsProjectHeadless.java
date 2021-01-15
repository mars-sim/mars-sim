/**
 * Mars Simulation Project
 * MarsProjectHeadless.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.headless;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.mars.sim.console.chat.service.Credentials;
import org.mars.sim.console.chat.service.RemoteChatService;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
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

	private static final String LOAD = "load";

	private static final String NEW = "new";

	private static final String REMOTE = "remote";

	private static final String TIMERATIO = "timeratio";
	
	private static final String TEMPLATE = "template";
	
	private static final String DISPLAYHELP = "help";                   		

	private static final String DATADIR = "datadir";                   		
	
	/** initialized logger for this class. */
	private static final Logger logger = Logger.getLogger(MarsProjectHeadless.class.getName());
	
	private static final String LOGGING_PROPERTIES = "/logging.properties";

	private Simulation sim = Simulation.instance();
	
	private SimulationConfig simulationConfig = SimulationConfig.instance();
	
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
			
			String str = "";
			for (String s : args) {
				str = str + "[" + s + "] "; 
			}
			
			logger.config("List of input args : " + str);
			
			// Initialize the simulation.
			initializeSimulation(args);
		}
	}
	
	/**
	 * Initialize the simulation.
	 * 
	 * @param args the command arguments.
	 * @return true if new simulation (not loaded)
	 */
	boolean initializeSimulation(String[] args) {

		int userTimeRatio = -1;
		boolean startServer = false;
		int serverPort = 18080;
		boolean loadSim = false;
		boolean loadNew = false;
		boolean loadTemplate = false;
		String simPath = null;
		
		Options options = new  Options();
		
		options.addOption(Option.builder(DISPLAYHELP)
				.desc("Help of the options").build());
		options.addOption(Option.builder(TIMERATIO).argName("Ratio (power of 2)").hasArg()
								.desc("Define the time ratio of the simulation").build());
		options.addOption(Option.builder(REMOTE).argName("port number").hasArg().optionalArg(true)
								.desc("Run the remote console service").build());
		options.addOption(Option.builder(DATADIR).argName("path to data directory").hasArg().optionalArg(false)
				.desc("Path to the data directory for simulation files (defaults to user.home)").build());
		
		options.addOption(Option.builder(NEW)
						.desc("Create a new simulation if one is not present").build());
		options.addOption(Option.builder(LOAD).argName("path to simulation file").hasArg().optionalArg(true)
						.desc("Load the a previously saved sim, default is used if none specifed").build());
		options.addOption(Option.builder(TEMPLATE).argName("template sponsor").numberOfArgs(2).optionalArg(false)
						.desc("New simualtion from a template for a Sponsor").build());
		
		CommandLineParser commandline = new DefaultParser();
		String template = null;
		String sponsor = null;
		try {
			CommandLine line = commandline.parse(options, args);
			if (line.hasOption(TIMERATIO)) {
				userTimeRatio = Integer.parseInt(line.getOptionValue(TIMERATIO));
			}
			if (line.hasOption(REMOTE)) {
				startServer = true;
				String portValue = line.getOptionValue(REMOTE);
				if (portValue != null) {
					serverPort = Integer.parseInt(portValue);
				}
			}
			if (line.hasOption(DISPLAYHELP)) {
				usage("Available options", options);
			}
			if (line.hasOption(NEW)) {
				loadNew = true;
			}
			if (line.hasOption(TEMPLATE)) {
				loadTemplate = true;
				String [] details = line.getOptionValues(TEMPLATE);
				template = details[0];
				sponsor = details[1];
			}
			if (line.hasOption(LOAD)) {
				loadSim = true;
				simPath = line.getOptionValue(LOAD);
			}
			if (line.hasOption(DATADIR)) {
				SimulationFiles.setDataDir(line.getOptionValue(DATADIR));
			}
		}
		catch (ParseException e1) {
			usage(e1.getMessage(), options);
		}

		if (!loadSim && !loadTemplate && !loadNew) {
			// Should never be here
			usage("Must specifed a startup option", options);
		}
		// Do it
		boolean loaded = false;
		if (loadSim) {
			loaded = handleLoadSimulation(userTimeRatio, simPath);
		}
		if (loadTemplate) {
			loaded = createNewSettlement(userTimeRatio, template, sponsor);
		}
		if (loadNew && !loaded) {
			handleNewSimulation(userTimeRatio);
			loaded = true;
		}
		if (startServer && loaded) {
			startRemoteConsole(serverPort);
		}

		if (!loaded) {
			exitWithError("No simulation started", null);
		}
		
		return true;
	}

	private void usage(String message, Options options) {
		HelpFormatter format = new HelpFormatter();
		System.out.println(message);
		format.printHelp("marssim headless", options);
		System.exit(1);
	}

	/**
	 * Loads the prescribed settlement template
	 * TODO; this must be common code somewhere else ?????
	 */
	private void loadSettlementTemplate(String template, String sponsor) {
//		logger.config("loadSettlementTemplate()");
		SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
		String templateString = null;
		String sponsorString = null;

		List<String> sponsors = UnitManager.getAllShortSponsors();
		for (String ss: sponsors) {
			if (sponsor.contains(ss) || sponsor.contains(ss.toLowerCase())) {
				sponsorString = ss;
				logger.info("Found sponsor string: " + sponsorString);
			}
		}
			
		settlementConfig.clearInitialSettlements();
			
		Collection<String> templates = settlementConfig.getTemplateMap().values();
		for (String t: templates) {
			if (StringUtils.containsIgnoreCase(t, template)) {
				templateString  = t;
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
	
	private boolean createNewSettlement(int userTimeRatio, String template, String sponsor) {
		try {
			// Load xml files
			simulationConfig.loadConfig();
			// Clear the default templates and load the specified template
			loadSettlementTemplate(template, sponsor);

			// Since SCE is not used, manually set up each of the followings 
			// Create new simulation
			// sim.createNewSimulation(-1, false);
			// Run this class in sim executor
			sim.runCreateNewSimTask(userTimeRatio);	

			// Start the simulation
			startSimThread(false);		
		}
		catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}		
		
		return true;
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
	 * @return 
	 * 
	 * @throws Exception if error loading the default saved simulation.
	 */
	private boolean handleLoadSimulation(int userTimeRatio, String simStr) {
		// Initialize the simulation.
		simulationConfig.loadConfig();
		// Create class instances
		sim.createNewSimulation(userTimeRatio, true);
		
		boolean result = false;
		try {
			File loadFile = null;
	
			if (simStr != null) {
				loadFile = new File(SimulationFiles.getSaveDir(), simStr);
			}
			else {
				loadFile = new File(SimulationFiles.getSaveDir(), Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION);
			}
					
			if (loadFile.exists()) {
				if (!loadFile.canRead()) {
					exitWithError("Problem: simulation file can not be opened " + loadFile.getAbsolutePath(), null);
				}
				
				logger.config("Loading from " + loadFile.getAbsolutePath());
				sim.loadSimulation(loadFile);

				// Start simulation.
				startSimThread(false);
				
				result  = true;
			}
		}
		catch (Exception e) {
			exitWithError("Problem loading the default simulation.", e);
		}
		
		return result;
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

			// Since SCE is not used, manually set up each of the followings 
			// Create new simulation
			// sim.createNewSimulation(-1, false);
			// Run this class in sim executor
			sim.runCreateNewSimTask(userTimeRatio);	

			// Start the simulation
			startSimThread(false);
		}
		catch (Exception e) {
			e.printStackTrace();
			exitWithError("Could not create a new simulation, startup cannot continue", e);
		}
	}

	/**
	 * Start the simulation instance.
	 * @param serverPort 
	 */
	private void startRemoteConsole(int serverPort) {
		logger.info("Start console service on port " + serverPort);
		RemoteChatService service = new RemoteChatService(serverPort, new Credentials());
		try {
			service.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Start the simulation instance.
	 */
	private void startSimThread(boolean useDefaultName) {
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

		// starting the simulation
//		MarsProjectHeadless mp = 
		new MarsProjectHeadless(args);

	}
	

}

