/*
 * Mars Simulation Project
 * MarsProjectHeadless.java
 * @date 2023-03-30
 * @author Manny Kung
 */

package org.mars_sim.headless;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.mars.sim.console.chat.service.Credentials;
import org.mars.sim.console.chat.service.RemoteChatService;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationBuilder;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.tool.RandomStringUtils;

/**
 * MarsProjectHeadless is the main class for starting mars-sim in purely
 * headless mode.
 */
public class MarsProjectHeadless {

	private static final String REMOTE = "remote";
	private static final String NOREMOTE = "noremote";
	private static final String DISPLAYHELP = "help";
	private static final String RESETADMIN = "resetadmin";
	private static final String LOAD_ARG = "load";


	/** initialized logger for this class. */
	private static final Logger logger = Logger.getLogger(MarsProjectHeadless.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";

	// Location of service files
	private static final String SERVICE_DIR = "service";

	private static final String CREDENTIALS_FILE = "credentials.ser";

	/**
	 * Constructor 1.
	 *
	 * @param args command line arguments.
	 */
	public MarsProjectHeadless(String[] args) {
		logger.config("Starting " + Simulation.TITLE);
		logger.config("List of input args : " + Arrays.toString(args));

		// Initialize the simulation.
		initializeSimulation(args);
	}


	/**
	 * Initialize the simulation.
	 *
	 * @param args the command arguments.
	 * @return true if new simulation (not loaded)
	 */
	private boolean initializeSimulation(String[] args) {

		boolean startServer = true;
		int serverPort = 18080;

		SimulationBuilder builder = new SimulationBuilder();

		Options options = new Options();
		for(Option o : builder.getCmdLineOptions()) {
			options.addOption(o);
		}

		options.addOption(Option.builder(LOAD_ARG).argName("path to simulation file").hasArg().optionalArg(true)
				.desc("Load the a previously saved sim. No argument then the default is used").build());
		options.addOption(Option.builder(DISPLAYHELP)
				.desc("Help of the options").build());
		OptionGroup remoteGrp = new OptionGroup();
		remoteGrp.setRequired(false); // REMOTE is the internal default
		remoteGrp.addOption(Option.builder(REMOTE).argName("port number").hasArg().optionalArg(true)
								.desc("Run the remote console service [default]").build());
		remoteGrp.addOption(Option.builder(NOREMOTE)
				.desc("Do not start a remote console service").build());
		options.addOptionGroup(remoteGrp);
		options.addOption(Option.builder(RESETADMIN)
				.desc("Reset the internal admin password").build());

		CommandLineParser commandline = new DefaultParser();
		boolean resetAdmin = false;
		try {
			CommandLine line = commandline.parse(options, args);

			builder.parseCommandLine(line);

			if (line.hasOption(REMOTE)) {
				startServer = true;
				String portValue = line.getOptionValue(REMOTE);
				if (portValue != null) {
					serverPort = Integer.parseInt(portValue);
				}
			}
			if (line.hasOption(NOREMOTE)) {
				startServer = false;
			}

			if (line.hasOption(DISPLAYHELP)) {
				usage("Available options", options);
			}
			if (line.hasOption(RESETADMIN)) {
				resetAdmin = true;
			}
			if (line.hasOption(LOAD_ARG)) {
				String simFile = line.getOptionValue(LOAD_ARG);
				if (simFile == null) {
					simFile = Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION;
				}
				builder.setSimFile(simFile);
			}
		}
		catch (Exception e1) {
			usage("Problem with arguments: " + e1.getMessage(), options);
		}

		// Do it
		try {
			// Build and run the simulator
			builder.start();

			if (startServer) {
				startRemoteConsole(serverPort, resetAdmin);
			}
		}
		catch(Exception e) {
			// Catch everything
			exitWithError("Problem starting " + e.getMessage(), e);

		}

		return true;
	}

	private void usage(String message, Options options) {
		HelpFormatter format = new HelpFormatter();
		System.out.println();
		System.out.println(message);
		format.printHelp(" [for mars-sim headless edition]", options);
		System.exit(1);
	}

	/**
	 * Exit the simulation with an error message.
	 *
	 * @param message the error message.
	 * @param e       the thrown exception or null if none.
	 */
	private void exitWithError(String message, Exception e) {
		if (e != null) {
			logger.log(Level.SEVERE, message, e);
		} else {
			logger.log(Level.SEVERE, message);
		}
		System.exit(1);
	}



	/**
	 * Start the simulation instance.
	 * @param serverPort
	 * @param changePassword
	 */
	private void startRemoteConsole(int serverPort, boolean changePassword) {
		try {
			File serviceDataDir = new File(SimulationFiles.getDataDir() , SERVICE_DIR);
			if (!serviceDataDir.exists()) {
				logger.info("Build " + serviceDataDir);
				serviceDataDir.mkdirs();
			}

			// Load the credential file
			File credFile = new File(serviceDataDir, CREDENTIALS_FILE);
			Credentials credentials = null;
			String adminPassword;
			if (credFile.exists()) {
				credentials  = Credentials.load(credFile);
				if (changePassword) {
					adminPassword = RandomStringUtils.random(8, true, true);
					credentials.setPassword(Credentials.ADMIN, adminPassword);
				}
				else {
					adminPassword = credentials.getPassword(Credentials.ADMIN);
				}
			}
			else {
				credentials = new Credentials(credFile);
				adminPassword = RandomStringUtils.random(8, true, true);
				credentials.addUser(Credentials.ADMIN, adminPassword);
				credentials.addUser("normal", "test456");

			}

			// This should be dropped eventually
			logger.info("User " + Credentials.ADMIN + " has password " + adminPassword);

			logger.info("Start console service on port " + serverPort);
			RemoteChatService service = new RemoteChatService(serverPort, serviceDataDir, credentials);

			service.start();
		} catch (IOException e) {
			exitWithError("Problem starting remote service", e);
		}
	}


	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		Logger.getLogger("").setLevel(Level.ALL);

		new File(SimulationFiles.getLogDir()).mkdirs();

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
		new MarsProjectHeadless(args);
	}
}

