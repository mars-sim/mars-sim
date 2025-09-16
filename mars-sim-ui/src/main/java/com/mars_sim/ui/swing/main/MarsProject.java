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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import com.mars_sim.console.InteractiveTerm;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationBuilder;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.UserConfigurableConfig;
import com.mars_sim.core.person.Crew;
import com.mars_sim.core.tool.Msg;
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
	private static final String NOGUI = "nogui";
	private static final String DISPLAY_HELP = "help";
	private static final String NEW = "new";
	private static final String CLEANUI = "cleanui";
	private static final String SANDBOX = "sandbox";
	private static final String SITE_EDITOR = "site";
	private static final String PROFILE = "profile";
	private static final String LOAD_ARG = "load";

	private static final String SANDBOX_MODE_Q = "Do you want to bypass the console menu and start a new default simulation in Sandbox Mode ?";

	private static final String DEFAULT_FILE = "default";
	
	/** true if displaying graphic user interface. */
	private boolean useGUI = true;
	private boolean useNew = false;
	private boolean useCleanUI = false;
	private boolean useSiteEditor;
	private boolean useProfile = false;
	private boolean isSandbox = false;

	private static InteractiveTerm interactiveTerm = new InteractiveTerm(false);

	private Simulation sim;

	private String simFile;

	/**
	 * Constructor
	 */
	public MarsProject() {
		logger.config("Starting " + SimulationRuntime.LONG_TITLE);
		// Set the InteractionTerm instance
		MainWindow.setInteractiveTerm(interactiveTerm);
	}

	/**
	 * Checks for confirmation of bypassing the text console main menu via a dialog box.
	 * 
	 * @return
	 */
	public boolean bypassConsoleMenuDialog() {
		logger.config(SANDBOX_MODE_Q);
		logger.config("To proceed, please choose 'Yes' or 'No' button in the dialog.");
		// Ask the player if wanting to do a 'Quick Start'
		int reply = JOptionPane.showConfirmDialog(interactiveTerm.getTerminal().getFrame(),
				SANDBOX_MODE_Q, 
				"Quick Start", 
				JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
			logger.config("You choose Yes. Go straight to starting a new default simulation in Sandbox Mode.");	
			return true;
        }
        
        return false;
	}

	/**
	 * Parses the argument and start the simulation.
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {
				
		SimulationBuilder builder = new SimulationBuilder();
		
		checkOptions(builder, args);

		// Do it
		try {
			if (useGUI) {
				// Start the splash window
				if (!useSiteEditor) {
					SwingUtilities.invokeLater(MainWindow::startSplash);
				}
				
				// Use opengl...
				
				// This is very fragile logic
				String os = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
				if (!os.contains("linux")) {
					System.setProperty("sun.java2d.ddforcevram", "true");
				}			
			}

			// Preload the Config
			SimulationConfig.loadConfig();
			
			if (useSiteEditor) {
				logger.config("Start the Scenario Editor...");
				startScenarioEditor(builder);
			}
			else if (useProfile) {
				setupProfile();
			}
			else if (simFile != null) {
				builder.setSimFile(simFile);
			}	
			// Go to console main menu if there is no template well-defined in the startup string
			else if (!builder.isFullyDefined() && useNew) {
				goToConsole(builder);
			}

			// Build and run the simulator
			sim = builder.start();

			// Start beryx console
			startConsoleThread();

			if (useGUI) {
				setupMainWindow(useCleanUI);
			}
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
	 */
	private void goToConsole(SimulationBuilder builder) {

		// Ask if running in standard sandbox mode or go to Console Menu
		if (!isSandbox && !bypassConsoleMenuDialog()) {
			logger.config("Please go to the Console Main Menu to choose an option.");
			int type = interactiveTerm.startConsoleMainMenu();
			switch (type) {
				case 1 -> {
					logger.config("Start the Scenario Editor...");
					startScenarioEditor(builder);
				}
				case 2 -> {
					// Load simulation
					logger.config("Load the sim...");
					String filePath = selectSimFile();
					if (filePath != null) {
						builder.setSimFile(filePath);
					}
				}
				case 3 -> {
					// Proceed with configuring the society mode
					logger.config("Configuring the society mode...");
					
					builder.startSocietySim();

					// Start beryx console
					startConsoleThread();
					
					return;
				}
				default ->
					// Check out crew flag
					builder.setUseCrews(interactiveTerm.getUseCrew());
			}
		}
	}
	
	/**
	 * Checks what switches or arguments have been provided.
	 * 
	 * @param builder
	 * @param args
	 */
	private void checkOptions(SimulationBuilder builder, String[] args) {

		Options options = new Options();
		for(Option o : builder.getCmdLineOptions()) {
			options.addOption(o);
		}

		options.addOption(Option.builder(DISPLAY_HELP)
				.desc("Display help options").get());
		options.addOption(Option.builder(NOAUDIO)
				.desc("Disable the audio").get());
		options.addOption(Option.builder(NOGUI)
				.desc("Disable the main UI").get());
		options.addOption(Option.builder(CLEANUI)
				.desc("Disable loading stored UI configurations").get());
		options.addOption(Option.builder(SANDBOX)
				.desc("Start in Sandbox Mode").get());
		options.addOption(Option.builder(NEW)
				.desc("Enable quick start").get());
		options.addOption(Option.builder(SITE_EDITOR)
				.desc("Start the Scenario Editor").get());
		options.addOption(Option.builder(PROFILE)
				.desc("Set up the Commander Profile").get());
		options.addOption(Option.builder(LOAD_ARG).argName("path to simulation file").hasArg().optionalArg(true)
				.desc("Load the a previously saved sim. No argument open file selection dialog. '"
									+ DEFAULT_FILE + "' will use default").get());
		
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
			useGUI = !line.hasOption(NOGUI);
			useNew = line.hasOption(NEW);
			useCleanUI = line.hasOption(CLEANUI);
			
			if (line.hasOption(LOAD_ARG)) {
				simFile = line.getOptionValue(LOAD_ARG);
				if (simFile == null) {
					simFile = selectSimFile();
				}
				else if (DEFAULT_FILE.equals(simFile)) {
					simFile = Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION;
				}
			}
			
			useSiteEditor = line.hasOption(SITE_EDITOR);
			useProfile = line.hasOption(PROFILE);
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
	 */
	private void startScenarioEditor(SimulationBuilder builder) {
		MainWindow.setInteractiveTerm(interactiveTerm);
		// Start sim config editor
		SimulationConfigEditor editor = new SimulationConfigEditor(SimulationConfig.instance());
		logger.config("Starting the Scenario Editor...");
		editor.waitForCompletion();

		UserConfigurableConfig<Crew> crew = editor.getCrewConfig();
		if (crew != null) {
			// Set the actual CrewConfig as it has edited entries
			builder.setCrewConfig(crew);
		}

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
	 * Performs the process of loading a simulation.
	 */
	private static String selectSimFile() {

		JFileChooser chooser = new JFileChooser(SimulationRuntime.getSaveDir());
		chooser.setDialogTitle(Msg.getString("MainWindow.dialogLoadSavedSim")); // $NON-NLS-1$
		if (chooser.showOpenDialog(interactiveTerm.getTerminal().getFrame()) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}

		return null;
	}

	/**
	 * Sets up the commander profile 
	 */
	private void setupProfile() {
		logger.config("Note: direct setup of the Commander Profile has NOT been implemented.");
		logger.config("Choose 'No' to start the Console Main Menu");
		logger.config("Select '1. Start a new Sim'");
		logger.config("Select '1. Command Mode'");
		logger.config("Select '5. Set up a new commander profile'");
		logger.config("When done, select '6. Load an exiting commander profile'");
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

		if (useGUI) {
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
		System.exit(1);
	}


	/**
	 * Runs the main window.
	 * 
	 * @param cleanUI
	 */
	public void setupMainWindow(boolean cleanUI) {
		while (true) {
	        try {
				TimeUnit.MILLISECONDS.sleep(1000);
				if (!sim.isUpdating()) {
					logger.config("Starting the Main Window...");
					new MainWindow(cleanUI, sim);
					break;
				}
	        } catch (InterruptedException e) {
				logger.log(Level.WARNING, "Trouble starting Main Window. ", e); 
				// Restore interrupted state...
			    Thread.currentThread().interrupt();
	        }
		}
	}


	/**
	 * Starts the simulation instance.
	 */
	private void startConsoleThread() {
		Thread consoleThread = new Thread(new ConsoleTask());
		consoleThread.setName("ConsoleThread");
		consoleThread.start();
	}

	/**
	 * The ConsoleTask allows running the beryx console in a thread.
	 */
	class ConsoleTask implements Runnable {

		ConsoleTask() {
		}

		public void run() {
			// Load the menu choice
			InteractiveTerm.loadTerminalMenu();
		}
	}

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
