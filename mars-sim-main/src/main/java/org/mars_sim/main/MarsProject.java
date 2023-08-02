/*
 * Mars Simulation Project
 * MarsProject.java
 * @date 2023-03-30
 * @author Scott Davis
 */
package org.mars_sim.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.mars.sim.console.InteractiveTerm;
import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationBuilder;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;
import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;

/**
* MarsProject is the main class for the application. It creates both the
* simulation and the user interface.
*/
public class MarsProject {
	/** initialized logger for this class. */
	private static final Logger logger = Logger.getLogger(MarsProject.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";
	private static final String NOAUDIO = "noaudio";
	private static final String NOGUI = "nogui";
	private static final String DISPLAY_HELP = "help";
	private static final String GENERATE_HELP = "html";
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

	private SimulationConfig simulationConfig = SimulationConfig.instance();

	private String simFile;

	/**
	 * Constructor
	 */
	public MarsProject() {
		logger.config("Starting " + Simulation.TITLE);
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
		String s = Arrays.toString(args);
		
		logger.config("List of input args : " + s);
		
		SimulationBuilder builder = new SimulationBuilder();
		builder.printJavaVersion();
		
		checkOptions(builder, args);

		// Do it
		try {
			if (useGUI) {
				// Start the splash window
				if (!useSiteEditor) {
					SwingUtilities.invokeLater(MainWindow::startSplash);
				}
				
				// Use opengl. 
				
				// This is very fragile logic
				String os = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
				if (!os.contains("linux")) {
					System.setProperty("sun.java2d.ddforcevram", "true");
				}
				
			}

			// Preload the Config
			simulationConfig.loadConfig();
			
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
		
				// Ask if running in standard Sandbox mode or Go to Console Menu
				if (!isSandbox && !bypassConsoleMenuDialog()) {
					logger.config("Please go to the Console Main Menu to choose an option.");
					int type = interactiveTerm.startConsoleMainMenu();
					if (type == 1) {
						logger.config("Start the Scenario Editor...");
						startScenarioEditor(builder);
					}

					else if (type == 2) {
						// Load simulation
						logger.config("Load the sim...");
						String filePath = selectSimFile();
						if (filePath != null) {
							builder.setSimFile(filePath);
						}
					}
					else if (type == 3) {
						// Proceed with configuring the society mode
						logger.config("Configuring the society mode...");
						
						builder.startSocietySim();

						// Start the wait layer
						InteractiveTerm.startLayer();

						// Start beryx console
						startConsoleThread();
						
						return;
					}
					else {
						// Check out crew flag
						builder.setUseCrews(interactiveTerm.getUseCrew());
					}
				}
			}

			// Build and run the simulator
			sim = builder.start();

			// Start the wait layer
			InteractiveTerm.startLayer();

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
				.desc("Display help options").build());
		options.addOption(Option.builder(NOAUDIO)
				.desc("Disable the audio").build());
		options.addOption(Option.builder(NOGUI)
				.desc("Disable the main UI").build());
		options.addOption(Option.builder(CLEANUI)
				.desc("Disable loading stored UI configurations").build());
		options.addOption(Option.builder(SANDBOX)
				.desc("Start in Sandbox Mode").build());
		options.addOption(Option.builder(NEW)
				.desc("Enable quick start").build());
		options.addOption(Option.builder(GENERATE_HELP)
				.desc("Generate HTML help").build());
		options.addOption(Option.builder(SITE_EDITOR)
				.desc("Start the Scenario Editor").build());
		options.addOption(Option.builder(PROFILE)
				.desc("Set up the Commander Profile").build());
		options.addOption(Option.builder(LOAD_ARG).argName("path to simulation file").hasArg().optionalArg(true)
				.desc("Load the a previously saved sim. No argument open file selection dialog. '"
									+ DEFAULT_FILE + "' will use default").build());
		
		DefaultParser commandline = new DefaultParser();
		try {
			Properties defaults = new Properties();
			File defaultFile = new File(SimulationFiles.getDataDir(), "default.props");
			if (defaultFile.exists()) {
				try (InputStream defaultInput = new FileInputStream(defaultFile)) {
					defaults.load(defaultInput);
				} catch (IOException e) {
					logger.warning("Problem reading default.props " + e.getMessage());
				}
			}
			CommandLine line = commandline.parse(options, args, defaults);

			builder.parseCommandLine(line);

			if (line.hasOption(NOAUDIO)) {
				// Disable all audio not just the volume
				AudioPlayer.disableAudio();
			}
			if (line.hasOption(DISPLAY_HELP)) {
				usage("See available options below", options);
			}
			if (line.hasOption(NOGUI)) {
				useGUI = false;
			}
			if (line.hasOption(NEW)) {
				useNew = true;
			}
			if (line.hasOption(CLEANUI)) {
				useCleanUI = true;
			}
			if (line.hasOption(LOAD_ARG)) {
				simFile = line.getOptionValue(LOAD_ARG);
				if (simFile == null) {
					simFile = selectSimFile();
				}
				else if (DEFAULT_FILE.equals(simFile)) {
					simFile = Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION;
				}
			}
			if (line.hasOption(SITE_EDITOR)) {
				useSiteEditor = true;
			}
			if (line.hasOption(GENERATE_HELP)) {
				generateHelp();
			}
			if (line.hasOption(PROFILE)) {
				useProfile = true;
			}
			if (line.hasOption(SANDBOX)) {
				isSandbox = true;
			}
			

		}
		catch (Exception e1) {
			usage("Problem with arguments: " + e1.getMessage(), options);
		}
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
			// Set the actual CrewConfig as it has editted entries
			builder.setCrewConfig(crew);
		}

		Scenario scenario = editor.getScenario();
		if (scenario != null) {
			builder.setScenario(scenario);
		}
	}


	/**
	 * Prints the help options.
	 * 
	 * @param message
	 * @param options
	 */
	private void usage(String message, Options options) {
		HelpFormatter format = new HelpFormatter();
		logger.config(message);
		format.printHelp("marsProject", options);
		System.exit(1);
	}

	/**
	 * Performs the process of loading a simulation.
	 */
	private static String selectSimFile() {

		JFileChooser chooser = new JFileChooser(SimulationFiles.getSaveDir());
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
	 * Generates the html help files
	 */
	private void generateHelp() {
		logger.config("Generating help files in headless mode in " + Simulation.OS + ".");

		try {
			simulationConfig.loadConfig();
			// this will generate html files for in-game help
			HelpGenerator.generateHtmlHelpFiles();
			logger.config("Done creating help files.");
			System.exit(1);

		} catch (Exception e) {
			exitWithError("Could not generate help files ", e);
		}
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
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
		// Note: Read the logging configuration from the classloader to make it webstart compatible
		new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();

		try {
			LogManager.getLogManager().readConfiguration(MarsProject.class.getResourceAsStream(LOGGING_PROPERTIES));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			LogManager.getLogManager().readConfiguration();
        }
		
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
