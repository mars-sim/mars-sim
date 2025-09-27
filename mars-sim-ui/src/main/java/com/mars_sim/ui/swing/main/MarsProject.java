/*
 * Mars Simulation Project
 * MarsProject.java
 * @date 2025-09-15
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationBuilder;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.ui.swing.MainWindow;
import com.mars_sim.ui.swing.configeditor.SimulationConfigEditor;
import com.mars_sim.ui.swing.sound.AudioPlayer;

/**
* MarsProject is the main class for starting mars-sim's UI mode. It creates both the
* simulation and the user interface.
*/
public class MarsProject {
	/** initialized logger for this class. */
	private static final Logger logger = Logger.getLogger(MarsProject.class.getName());

	private static final String NOAUDIO = "noaudio";
	private static final String DISPLAY_HELP = "help";
	private static final String NEW = "new";
	private static final String CLEANUI = "cleanui";
	private static final String SANDBOX = "sandbox";
	private static final String SITE_EDITOR = "site";
	private static final String LOAD_ARG = "load";
	
	/** true if displaying graphic user interface. */
	private boolean useNew = false;
	private boolean useCleanUI = false;
	private boolean useSiteEditor;
	private boolean isSandbox = false;

	private Simulation sim;

	private String simFile;

	/**
	 * Constructor
	 */
	public MarsProject() {
		logger.config("Starting " + SimulationRuntime.LONG_TITLE);
	}

	/**
	 * Parses the argument and start the simulation.
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {
				
		SimulationBuilder builder = new SimulationBuilder();
		
		parseCommandArgs(builder, args);

		// Do it
		try {

			// Use opengl...
			
			// This is very fragile logic
			String os = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
			if (!os.contains("linux")) {
				System.setProperty("sun.java2d.ddforcevram", "true");
			}			

			// Preload the Config
			var simConfig = SimulationConfig.loadConfig();
			
			if (useSiteEditor) {
				logger.config("Start the Scenario Editor...");
				startScenarioEditor(builder, simConfig);
			}
			else if (simFile != null) {
				builder.setSimFile(simFile);
			}	
			
			// Go to console main menu if there is no template well-defined in the startup string
			if (!builder.isFullyDefined() && !useNew) {
				showUserChoice(builder, simConfig);
			}

			// Start the splash window
			var splashWindow = new SplashWindow();
			splashWindow.display();

			// Build and run the simulator
			sim = builder.start(splashWindow::setStatusMessage);

			// Build main window
			splashWindow.setStatusMessage("Starting the Main Window...");
			new MainWindow(useCleanUI, sim);

			// Switch from Splash to main window as one
			SwingUtilities.invokeLater(splashWindow::remove);

			logger.config("Starting the Master Clock...");		
			sim.startClock(false);
		}
		catch(Exception e) {
			// Catch everything
			exitWithError("Problem starting " + e.getMessage(), e);
		}
	}

	/**
	 * Goes to console for building the sim.
	 * 
	 * @param builder
	 * @param simConfig 
	 */
	private void showUserChoice(SimulationBuilder builder, SimulationConfig simConfig) {

		
		var choice = new StartUpChooser(builder.getScenarioConfig(),
										simConfig.getSettlementTemplateConfiguration(),
										simConfig.getReportingAuthorityFactory());

		switch (choice.getChoice()) {
			case StartUpChooser.EDIT_SCENARIO -> {
				logger.config("Start the Scenario Editor...");
				startScenarioEditor(builder, simConfig);
			}
			case StartUpChooser.LOAD_SIM -> {
				// Load simulation
				logger.config("Load the sim...");
				var selected = choice.getSelectedFile();
				if (selected != null) {
					selected = Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION;
				}
				builder.setSimFile(selected);
			}
			case StartUpChooser.NEW_SOCIETY -> {
				// Proceed with configuring the society mode
				logger.config("Configuring the society mode...");
				
				builder.startSocietySim();					
			}
			case StartUpChooser.SCENARIO -> {
				var scenario = choice.getScenario();
				if (scenario != null) {
					builder.setScenarioName(scenario.getName());
				}
			}
			case StartUpChooser.TEMPLATE -> {
				var t = choice.getTemplate();
				if (t != null) {
					builder.setTemplate(t.getName());
					var a = choice.getAuthority();
					if (a != null) {
						builder.setSponsor(a.getName());
					}
				}
			}
			case StartUpChooser.NEW_SIM -> {
				// New simulation with default settings
			}					
			
			case StartUpChooser.EXIT -> System.exit(0);

			default -> {logger.warning("No choice made, exiting...");
						System.exit(0);}
		}
	}
	
	/**
	 * Checks what switches or arguments have been provided.
	 * 
	 * @param builder
	 * @param args
	 */
	private void parseCommandArgs(SimulationBuilder builder, String[] args) {

		Options options = new Options();
		for(Option o : builder.getCmdLineOptions()) {
			options.addOption(o);
		}

		options.addOption(Option.builder(DISPLAY_HELP)
				.desc("Display help options").get());
		options.addOption(Option.builder(NOAUDIO)
				.desc("Disable the audio").get());
		options.addOption(Option.builder(CLEANUI)
				.desc("Disable loading stored UI configurations").get());
		options.addOption(Option.builder(SANDBOX)
				.desc("Start in Sandbox Mode").get());
		options.addOption(Option.builder(NEW)
				.desc("Enable quick start").get());
		options.addOption(Option.builder(SITE_EDITOR)
				.desc("Start the Scenario Editor").get());
		options.addOption(Option.builder(LOAD_ARG).argName("path to simulation file").hasArg().optionalArg(true)
				.desc("Load the a previously saved sim. No argument default will be used.").get());
		
		DefaultParser commandline = new DefaultParser();
		try {
			Properties defaults = readDefaults();
			CommandLine line = commandline.parse(options, args, defaults);

			builder.parseCommandLine(line);

			if (line.hasOption(NOAUDIO)) {
				// Disable all audio not just the volume
				AudioPlayer.disableAudio();
			}
			if (line.hasOption(DISPLAY_HELP)) {
				usage("See available options below", options);
			}
			useNew = line.hasOption(NEW);
			useCleanUI = line.hasOption(CLEANUI);
			
			if (line.hasOption(LOAD_ARG)) {
				simFile = line.getOptionValue(LOAD_ARG);
				if (simFile == null) {
					simFile = Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION;
				}
			}
			
			useSiteEditor = line.hasOption(SITE_EDITOR);
			isSandbox = line.hasOption(SANDBOX);
		}
		catch (ParseException e) {
			usage("Problem with arguments: " + e.getMessage(), options);
		}
	}

	/**
	 * Reads the default properties file.
	 */
	private Properties readDefaults() {
		var props = new Properties();
		File defaultFile = new File(SimulationRuntime.getDataDir(), "default.props");
		if (defaultFile.exists()) {
			try (InputStream defaultInput = new FileInputStream(defaultFile)) {
				props.load(defaultInput);
			} catch (IOException e) {
				logger.warning("Problem reading default.props " + e.getMessage());
			}
		}
		return props;
	}

	/**
	 * Starts the scenario editor.
	 * 
	 * @param builder
	 * @param simConfig 
	 */
	private void startScenarioEditor(SimulationBuilder builder, SimulationConfig config) {
		// Start sim config editor
		SimulationConfigEditor editor = new SimulationConfigEditor(config, builder.getScenarioConfig(),
							builder.getCrewConfig());
		logger.config("Starting the Scenario Editor...");

		Scenario scenario = editor.getScenario();
		if (scenario != null) {
			builder.setScenarioName(scenario.getName());
		}
	}


	/**
	 * Prints the help options.
	 * 
	 * @param message
	 * @param options
	 */
	private void usage(String message, Options options) {
        // New non-deprecated HelpFormatter (Commons CLI 1.10+)
        final HelpFormatter fmt = HelpFormatter.builder().get();
        final String header = "\n" + message + "\n";
        final String footer = "";
        try {
            fmt.printHelp("mars-sim-ui [options]", header, options, footer, true);
        } catch (IOException ioe) {
            // Fallback if printing help fails
            logger.severe(message);
            logger.severe("usage: mars-sim-ui [options]");
        }
        System.exit(1);
	}

	/**
	 * Exits the simulation with an error message.
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

		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}


	/**
	 * Runs the main window.
	 * 
	 * @param cleanUI
	 */
	// private void setupMainWindow(boolean cleanUI) {
	// 	while (true) {
	//         try {
	// 			TimeUnit.MILLISECONDS.sleep(1000);
	// 			if (!sim.isUpdating()) {
	// 				logger.config("Starting the Main Window...");
	// 				new MainWindow(cleanUI, sim);
	// 				break;
	// 			}
	//         } catch (InterruptedException e) {
	// 			logger.log(Level.WARNING, "Trouble starting Main Window. ", e); 
	// 			// Restore interrupted state...
	// 		    Thread.currentThread().interrupt();
	//         }
	// 	}
	// }

	/**
	 * The main starting method for the application.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		SimulationRuntime.initialseLogging();
		
		// Sets text antialiasing
		System.setProperty("swing.aatext", "true");
		System.setProperty("awt.useSystemAAFontSettings", "lcd"); // for newer VMs

		// Starts the simulation
		MarsProject project = new MarsProject();
		
		// Processes the arguments
		project.parseArgs(args);
		
		logger.config("Finish processing MarsProject.");
	}

}
