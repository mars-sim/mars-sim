/*
 * Mars Simulation Project
 * MarsProjectFXGL.java
 * @date 2021-09-29
 * @author Manny Kung
 */

package org.mars_sim.base;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.fxgl.First;
import org.mars_sim.msp.core.Msg;
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

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class MarsProjectFXGL extends GameApplication {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectFXGL.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";
	private static final String NOAUDIO = "noaudio";
	private static final String NOGUI = "nogui";
	private static final String DISPLAYHELP = "help";
	private static final String GENERATEHELP = "html";

	/** true if displaying graphic user interface. */
	private boolean useGUI = true;

	private Simulation sim = Simulation.instance();

	private SimulationConfig simulationConfig = SimulationConfig.instance();

	private InteractiveTerm interactiveTerm = new InteractiveTerm(false);

	private First first = new First();

	/**
	 * Constructor
	 */
	public MarsProjectFXGL() {
		logger.config("Starting " + Simulation.title);
	}

	@Override
	protected void initSettings(GameSettings settings) {
		first.initSettings(settings);
	}

    @Override
    protected void initGameVars(Map<String, Object> vars) {
    	first.initGameVars(vars);
    }

	 @Override
	 protected void initGame() {
		 first.initGame();
	}

	 @Override
	 protected void initInput() {
		 first.initInput();
	 }

	 @Override
	 protected void initUI() {
		 first.initUI();
	 }

	 @Override
	 protected void initPhysics() {
		 first.initPhysics();
	 }

	 @Override
	 protected void onUpdate(double tpf) {
		 first.onUpdate(tpf);
	 }

	/**
	 * Parse the argument and start the simulation.
	 * @param args
	 */
	public void parseArgs(String[] args) {
		logger.config("List of input args : " + Arrays.toString(args));

		SimulationBuilder builder = new SimulationBuilder();

		checkOptions(builder, args);

		// Do it
		try {
			if (useGUI) {
				// Start the splash window
				MainWindow.startSplash();
				// Use opengl
				// Question: How compatible are linux and macos with opengl ?
				// System.setProperty("sun.java2d.opengl", "true"); // not compatible with
				if (!MainWindow.OS.contains("linux")) {
					System.setProperty("sun.java2d.ddforcevram", "true");
				}
			}

			// Preload the Config
			SimulationConfig.instance().loadConfig();

			// Get user choices if there is no template defined or a preload
			if (!builder.isFullyDefined()) {
				logger.config("Please go to the console's Main Menu to choose an option.");

				int type = interactiveTerm.startConsoleMainMenu();
				if (type == 1) {
					// Start sim config editor
					logger.config("Start the Site Editor...");
					SimulationConfigEditor editor = new SimulationConfigEditor(SimulationConfig.instance());
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

				else if (type == 2) {
					// Load simulation
					logger.config("Load sim...");
					String filePath = selectSimFile(false);
					if (filePath != null) {
						builder.setSimFile(filePath);
					}
				}
				else {
					// Check out crew flag
					builder.setUseCrews(interactiveTerm.getUseCrew());
				}
			}

			// Build and run the simulator
			builder.start();

			// Start the wait layer
			InteractiveTerm.startLayer();

			// Start beryx console
			startConsoleThread();

			if (useGUI) {
//				setupMainWindow(false);
				launch(args);
			}
		}
		catch(Exception e) {
			// Catch everything
			exitWithError("Problem starting " + e.getMessage(), e);
		}
	}

	private void checkOptions(SimulationBuilder builder, String[] args) {

		Options options = new Options();
		for(Option o : builder.getCmdLineOptions()) {
			options.addOption(o);
		}

		options.addOption(Option.builder(DISPLAYHELP)
				.desc("Help of the options").build());
		options.addOption(Option.builder(NOAUDIO)
				.desc("Disable the audio").build());
		options.addOption(Option.builder(NOGUI)
				.desc("Disable the main UI").build());
		options.addOption(Option.builder(GENERATEHELP)
				.desc("Generate HTML help").build());

		CommandLineParser commandline = new DefaultParser();
		try {
			CommandLine line = commandline.parse(options, args);

			builder.parseCommandLine(line);

			if (line.hasOption(NOAUDIO)) {
				// Disable all audio not just the volume
				AudioPlayer.disableVolume();
			}
			if (line.hasOption(DISPLAYHELP)) {
				usage("Available options", options);
			}
			if (line.hasOption(NOGUI)) {
				useGUI = false;
			}
			if (line.hasOption(GENERATEHELP)) {
				generateHelp();
			}

		}
		catch (Exception e1) {
			usage("Problem with arguments: " + e1.getMessage(), options);
		}
	}

	private void usage(String message, Options options) {
		HelpFormatter format = new HelpFormatter();
		System.out.println();
		System.out.println(message);
		format.printHelp(" [for mars-sim Swing dition]", options);
		System.exit(1);
	}

	/**
	 * Performs the process of loading a simulation.
	 *
	 * @param autosave
	 */
	private String selectSimFile(boolean autosave) {

		String dir = null;
		String title = null;

		// Add autosave
		if (autosave) {
			dir = SimulationFiles.getAutoSaveDir();
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		} else {
			dir = SimulationFiles.getSaveDir();
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}

		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle(title); // $NON-NLS-1$
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}

		return null;
	}

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

		if (useGUI) {
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
		System.exit(1);
	}

	/**
	 * Start the simulation instance.
	 */
	private void startConsoleThread() {
		Thread consoleThread = new Thread(new ConsoleTask());
		consoleThread.setName("ConsoleThread");
		consoleThread.start();
	}

	class ConsoleTask implements Runnable {

		ConsoleTask() {
		}

		public void run() {
			// Load the menu choice
			InteractiveTerm.loadTerminalMenu();
		}
	}

	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * [landrus, 27.11.09]: Read the logging configuration from the classloader, so
		 * that this gets webstart compatible. Also create the logs dir in user.home
		 */
		new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();

		try {
			LogManager.getLogManager().readConfiguration(MarsProjectFXGL.class.getResourceAsStream(LOGGING_PROPERTIES));
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
		new MarsProjectFXGL().parseArgs(args);
		logger.config("Simulation running");
	}

}
