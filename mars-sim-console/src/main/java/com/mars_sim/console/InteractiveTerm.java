/*
 * Mars Simulation Project
 * InteractiveTerm.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.console;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.beryx.textio.TextIO;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.TextIOChannel;
import com.mars_sim.console.chat.UserChannel;
import com.mars_sim.console.chat.simcommand.TopLevel;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;

/**
 * The InteractiveTerm class builds a text-based console interface and handles
 * the interaction with players
 */
public class InteractiveTerm {

	private static final Logger logger = Logger.getLogger(InteractiveTerm.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	private static final String EXIT_OPTION_0 = "0. Exit";
	private static final String SCENARIO_EDITOR_OPTION_1 = "1. Open Scenario Editor";
	private static final String ENTER_YOUR_CHOICE = "Enter your choice:";
	private static final String BACK_TO_THE_PREVIOUS_MENU = "Back to the previous menu";
	private static final String PROCEED_TO_START_THE_SIM = "Proceed to start the sim";
	private static final String NOTE_1 = "Note 1: By default the loading of crews is Enabled. Choose 2 to enable/disable.";
	private static final String NOTE_2 = "Note 2: The Console Editon does not come with the Scenario Editor.";

	private static boolean consoleEdition = false;

	// Screen sizes presented to user
	private static final Dimension[] screenSizes = { setDimension(1920, 1080), setDimension(1280, 800),
			setDimension(1280, 1024), setDimension(1600, 900), setDimension(1366, 768) };

	private static GameManager gm = new GameManager();
	private static MarsTerminal marsTerminal = new MarsTerminal();
	private static TextIO textIO = new TextIO(marsTerminal);
	private static SwingHandler handler = new SwingHandler(textIO, gm);

	private static final Simulation sim = Simulation.instance();

	private boolean useCrew = true;

	private int selectedScreen = -1;

	private CommanderProfile profile;

	private static Dimension setDimension(int w, int h) {
		return new Dimension(w, h);
	}
	
	/*
	 * Constructor.
	 * 
	 * @param restart
	 */
	public InteractiveTerm(boolean restart) {

		marsTerminal.init();
		  
		// Prevent allow users from arbitrarily close the terminal by clicking top right
		// close button
		marsTerminal.registerUserInterruptHandler(term -> {
		}, false);

		logger.config("Done with MarsTerminal on " + Thread.currentThread().getName());

		if (restart) {
			loadTerminalMenu();
		}

		logger.config("Done with InteractiveTerm's constructor is on " + Thread.currentThread().getName());
	}

	/**
	 * Initializes functions.
	 */
	public void init() {
		// Add Mars Terminal to the clock listener
		sim.getMasterClock().addClockListener(marsTerminal, 1000);
		// Update title
		marsTerminal.changeTitle(false);
	}
	
	/**
	 * Asks the player to choose in beryx console main menu.
	 */
	public int startConsoleMainMenu() {
		logger.config("Staring startConsoleMainMenu()");

		profile = new CommanderProfile(this);

		return selectMenu();
	}

	/**
	 * Prints the main menu.
	 */
	private void printMainMenu() {
		marsTerminal.print(System.lineSeparator()
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n"
				+ System.lineSeparator() + "                          * * *   Main Menu   * * *\n"
				+ System.lineSeparator() + System.lineSeparator() + EXIT_OPTION_0 + System.lineSeparator()
				+ "1. Start a new Sim" + System.lineSeparator() + "2. Load a saved Sim" + System.lineSeparator()
				+ "3. Change screen resolution" + System.lineSeparator() + System.lineSeparator());
	}

	private void printChoice(String text) {
		marsTerminal.print(System.lineSeparator());
		marsTerminal.print(text);
		marsTerminal.print(System.lineSeparator());
	}

	/**
	 * Selects the game mode.
	 *
	 * @return
	 */
	private int selectMenu() {
		int menuChoice = 0;

		try {
			printMainMenu();

			handler.addStringTask("menu", "Choose an option :", false).addChoices("0", "1", "2", "3")
					.constrainInputToChoices();
			handler.executeOneTask();

			if (GameManager.menu.equals("0")) {
				sim.endSimulation();

				System.exit(0);
				disposeTerminal();
				return menuChoice;
			}

			else if (GameManager.menu.equals("1")) {
				printChoice("Starting a new sim...");

				// Go to selectMode() to choose a mode
				menuChoice = selectMode();

				marsTerminal.print(System.lineSeparator());

				return menuChoice;
			}

			else if (GameManager.menu.equals("2")) {
				printChoice("Loading from a saved sim...");

				menuChoice = 2;

				marsTerminal.print(System.lineSeparator());

				return menuChoice;
			}

			else if (GameManager.menu.equals("3")) {
				StringBuilder buffer = new StringBuilder();
				for (int i = 0; i < screenSizes.length; i++) {
					buffer.append(i).append(". ").append(screenSizes[i].width).append(" x ")
							.append(screenSizes[i].height).append(System.lineSeparator());
				}

				marsTerminal.print(System.lineSeparator());

				printChoice("                        * * *   Resolution Menu   * * *\n");

				marsTerminal.print(System.lineSeparator());
				marsTerminal.print(buffer.toString());

				String oldRes = "Screen size";
				if (selectedScreen >= 0) {
					oldRes = screenSizes[selectedScreen].width + " x " + screenSizes[selectedScreen].height;
				}

				printChoice("Current resolution : " + oldRes);

				marsTerminal.print(System.lineSeparator());

				handler.addStringTask("resolution", "Choose an option :", false).addChoices("0", "1", "2", "3", "4")
						.constrainInputToChoices();
				handler.executeOneTask();

				String userInput = GameManager.resolution;

				selectedScreen = Integer.parseInt(userInput);

				String newRes = screenSizes[selectedScreen].width + " x " + screenSizes[selectedScreen].height;

				printChoice("The screen resolution has been changed from '" + oldRes + "' to '" + newRes + "'");

				return selectMenu();
			}

			else {

				return selectMenu();
			}

		} catch (RuntimeException e) {
			logger.severe(sourceName + ": RuntimeException detected.");
		}

		return menuChoice;
	}

	/**
	 * Prints the command mode menu.
	 */
	private void printModeSelectionMenu() {
		marsTerminal.print(System.lineSeparator()
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n"
				+ System.lineSeparator() + "                        * * *   Mode Selection   * * *\n"
				+ System.lineSeparator() + System.lineSeparator() + EXIT_OPTION_0 + System.lineSeparator()
				+ "1. Command Mode (Experimental only)" + System.lineSeparator() + "2. Sandbox Mode"
				+ System.lineSeparator() + "3. Society Mode (Not Available)" + System.lineSeparator()
				+ "4. Sponsor Mode (Experimental only)" + System.lineSeparator() + System.lineSeparator());
	}

	/**
	 * Selects the game mode.
	 *
	 * @return
	 */
	private int selectMode() {
		int modeChoice = 0;

		printModeSelectionMenu();

		handler.addStringTask("input", "Select the Game Mode:", false).addChoices("0", "1", "2", "3", "4")
				.constrainInputToChoices();
		handler.executeOneTask();

		if (GameManager.input.equals("0")) {
			sim.endSimulation();

			System.exit(0);
			disposeTerminal();
		} else if (GameManager.input.equals("1")) {
			printChoice("Go to Command Mode.");
			modeChoice = configureCommandMode(false);
		}

		else if (GameManager.input.equals("2")) {
			printChoice("Go to Sandbox Mode.");
			modeChoice = configureSandoxMode();
		}

		else if (GameManager.input.equals("3")) {
			printChoice("Go to Society Mode.");
			modeChoice = configureSocietyMode();
		}

		else if (GameManager.input.equals("4")) {
			printChoice("Go to Sponsor Mode.");
			modeChoice = configureSponsorMode();
		}

		marsTerminal.print(System.lineSeparator());

		return modeChoice;
	}

	/**
	 * Prints the command mode menu
	 */
	private void printCommandMenu() {
		marsTerminal.println(System.lineSeparator() + System.lineSeparator()
				+ "            * * *   Command Mode (Experimental only) - Crew Selection   * * *"
				+ System.lineSeparator() + System.lineSeparator() + EXIT_OPTION_0 + System.lineSeparator()
				+ SCENARIO_EDITOR_OPTION_1 + System.lineSeparator() + "2. Enable/disable crew loading (currently "
				+ (useCrew ? "Enabled" : "Disabled") + ")" + System.lineSeparator() + "3. " + PROCEED_TO_START_THE_SIM
				+ System.lineSeparator() + "4. " + BACK_TO_THE_PREVIOUS_MENU + System.lineSeparator()
				+ "5. Set up a new commander profile" + System.lineSeparator() + "6. Load an exiting commander profile"
				+ System.lineSeparator() + System.lineSeparator() + NOTE_1 + System.lineSeparator() + NOTE_2
				+ System.lineSeparator());
	}

	/**
	 * Configures the command mode.
	 *
	 * @param isLoaded
	 * @return
	 */
	private int configureCommandMode(boolean isLoaded) {
		int cfg = 0;
		boolean loaded = isLoaded;

		// Set the Game Mode to Command Mode in GameManager
		GameManager.setGameMode(GameMode.COMMAND);

		printCommandMenu();

		handler.addStringTask("commandCfg", ENTER_YOUR_CHOICE, false).addChoices("0", "1", "2", "3", "4", "5", "6")
				.constrainInputToChoices();
		handler.executeOneTask();

		String choice = GameManager.commandCfg;
		switch (choice) {
			case "0":
				sim.endSimulation();

				System.exit(0);
				disposeTerminal();
			break;

			case "1":
				if (consoleEdition) {
					printChoice("Sorry. The Console Edition of mars-sim does not come with the Site Editor.");

					cfg = configureCommandMode(loaded);
				} else {
					marsTerminal.print(System.lineSeparator());
					marsTerminal.print("Loading the Scenario Editor in Swing-based UI...");

					cfg = 1;
				}
			break;
			
			case "2":
				if (useCrew) {
					useCrew = false;
					printChoice("The crew loading is now disabled.");
				} else {
					useCrew = true;
					printChoice("The crew loading is now enabled.");
				}

				cfg = configureCommandMode(loaded);
			break;
			
			case "3":
				if (loaded) {
					printChoice("Starting a new simulation in Command Mode...");
				} else {
					printChoice("Cannot start the simulation since no commander profile has been loaded up. Try it again.");
					cfg = configureCommandMode(loaded);
				}
			break;
			
			case "4":
				printChoice(BACK_TO_THE_PREVIOUS_MENU + ".");

				return selectMode();

			case "5":
				marsTerminal.print(System.lineSeparator());
				// Set new profile
				profile.accept(textIO, null);

				printChoice("Note: if profiled created successfully, choose 6 to load up this profile.");

				cfg = configureCommandMode(loaded);
			break;
			
			case "6":
				// Load from previously saved profile
				loaded = loadPreviousProfile();

				printChoice("Note: if loaded successfully, will automatically proceed to start the simulation.");

				if (!loaded)
					cfg = configureCommandMode(loaded);
			break;
			
			default:
				cfg = configureCommandMode(loaded);
		}

		marsTerminal.print(System.lineSeparator());

		return cfg;
	}

	/**
	 * Prints the sandbox mode menu.
	 */
	private void printSandoxMenu() {
		marsTerminal.println(System.lineSeparator() + System.lineSeparator()
				+ "           * * *  Sandbox Mode - Crew and Scenario Selection  * * *" + System.lineSeparator()
				+ System.lineSeparator() + EXIT_OPTION_0 + System.lineSeparator() + SCENARIO_EDITOR_OPTION_1
				+ System.lineSeparator() + "2. Enable/disable crew loading (currently "
				+ (useCrew ? "Enabled" : "Disabled") + ")" + System.lineSeparator() + "3. " + PROCEED_TO_START_THE_SIM
				+ System.lineSeparator() + "4. " + BACK_TO_THE_PREVIOUS_MENU + System.lineSeparator()
				+ System.lineSeparator() + NOTE_1 + System.lineSeparator() + NOTE_2 + System.lineSeparator());
	}

	/**
	 * Configures the sandbox mode.
	 *
	 * @return
	 */
	private int configureSandoxMode() {
		int cfg = 0;

		GameManager.setGameMode(GameMode.SANDBOX);

		printSandoxMenu();

		handler.addStringTask("sandboxCfg", ENTER_YOUR_CHOICE, false).addChoices("0", "1", "2", "3", "4")
				.constrainInputToChoices();
		handler.executeOneTask();

		if (GameManager.sandboxCfg.equals("0")) {
			sim.endSimulation();

			System.exit(0);
			disposeTerminal();
		}

		else if ((GameManager.sandboxCfg).equals("1")) {
			if (consoleEdition) {
				printChoice("Sorry. The Console Edition of mars-sim does not come with the Site Editor.");
				cfg = configureSandoxMode();
			} else {
				printChoice("Loading the Scenario Editor in Swing-based UI...");
				cfg = 1;
			}
		}

		else if ((GameManager.sandboxCfg).equals("2")) {
			if (useCrew) {
				useCrew = false;
				printChoice("The crew loading is now disabled.");
			} else {
				useCrew = true;
				printChoice("The crew loading is now enabled.");
			}

			cfg = configureSandoxMode();
		}

		else if ((GameManager.sandboxCfg).equals("3")) {
			printChoice("Starting a new simulation in Sandbox Mode...");
		}

		else if ((GameManager.sandboxCfg).equals("4")) {
			printChoice(BACK_TO_THE_PREVIOUS_MENU + ".");
			return selectMode();
		}

		else {
			cfg = configureSandoxMode();
		}

		return cfg;
	}

	/**
	 * Configures the sponsor mode.
	 *
	 * @return
	 */
	private int configureSponsorMode() {
		int cfg = 4;

		GameManager.setGameMode(GameMode.SPONSOR);

		printChoice("Starting the Simulation in Sponsor Mode.");

		return cfg;
	}

	/**
	 * Configures the society mode.
	 *
	 * @return
	 */
	private int configureSocietyMode() {
		int cfg = 3;

		GameManager.setGameMode(GameMode.SOCIETY);

		printChoice("Starting the Society Simulation.");

		return cfg;
	}

	/**
	 * Loads the previously saved commander profile.
	 */
	private boolean loadPreviousProfile() {
		boolean loaded = false;
		try {
			boolean canLoad = CommanderProfile.loadProfile();
			if (canLoad) {
				StringBuilder details = new StringBuilder();
				profile.getCommander().outputDetails(details);

				printChoice("                * * *    Commander's Profile    * * *");

				marsTerminal.println(details.toString() + System.lineSeparator());

				boolean like = textIO.newBooleanInputReader().withDefaultValue(true)
						.read("Would you like to use this profile ?");

				if (like) {
					printChoice("Just loaded up this commander profile.");

					loaded = true;
				} else {
					printChoice("Cancelled loading this commander profile.");
					printChoice("Back to the Command Mode menu.");
					marsTerminal.print(System.lineSeparator());

					configureCommandMode(loaded);
				}
			}

			else {
				logger.severe("Can't find the 'commander.txt' file.");
				printChoice("Can't find the 'commander.txt' file.");
				printChoice("Back to the Command Mode menu.");

				configureCommandMode(loaded);
			}

		} catch (IOException e) {
			logger.severe("Error loading the commander profile.");
			printChoice("Error loading the commander profile.");
			marsTerminal.print(System.lineSeparator());

			configureCommandMode(loaded);
		}

		return loaded;
	}

	/**
	 * Loads the terminal menu.
	 */
	public static void loadTerminalMenu() {

		UserChannel channel = new TextIOChannel(textIO);
		// Console is always an admin
		Set<ConversationRole> roles = new HashSet<>();
		roles.add(ConversationRole.ADMIN);
		Conversation conversation = new Conversation(channel, new TopLevel(), roles, sim);

		conversation.interact();
		logger.info("Conversation ended.");

	}

	/**
	 * Gets the Commander's profile.
	 *
	 * @return profile
	 */
	public CommanderProfile getProfile() {
		return profile;
	}

	public MarsTerminal getTerminal() {
		return marsTerminal;
	}

	public static void disposeTerminal() {
		marsTerminal.getFrame().setVisible(false);
		marsTerminal.dispose(null);
	}

	public SwingHandler getHandler() {
		return handler;
	}

	public GameManager getGameManager() {
		return gm;
	}

	public boolean getUseCrew() {
		return useCrew;
	}

	/*
	 * Gets the dimension of the screen size selected by the user. This is null if
	 * none has been selected.
	 * 
	 * @param gd
	 * 
	 * @return
	 */
	public Dimension getScreenDimension(GraphicsDevice gd) {
		if (selectedScreen >= 0) {
			return screenSizes[selectedScreen];
		}

		int screenWidth = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();

		for (int i = 0; i < screenSizes.length; i++) {
			if (screenSizes[i].width == screenWidth && screenSizes[i].height == screenHeight) {
				selectedScreen = i;
			}
		}

		return new Dimension(screenWidth, screenHeight);
	}

	public void destroy() {
		profile = null;
	}
}
