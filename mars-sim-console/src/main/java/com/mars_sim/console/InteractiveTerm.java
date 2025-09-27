/*
 * Mars Simulation Project
 * InteractiveTerm.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.console;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
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
import com.mars_sim.core.Simulation;

/**
 * The InteractiveTerm class builds a text-based console interface and handles
 * the interaction with players
 */
public class InteractiveTerm {

	private static final Logger logger = Logger.getLogger(InteractiveTerm.class.getName());

	// Screen sizes presented to user
	private static final Dimension[] screenSizes = { new Dimension(1920, 1080), new Dimension(1280, 800),
			new Dimension(1280, 1024), new Dimension(1600, 900), new Dimension(1366, 768) };

	private MarsTerminal marsTerminal;
	private Simulation sim;
	private int selectedScreen = -1;

	private TextIO textIO;

	/*
	 * Constructor.
	 * 
	 * @param restart
	 */
	public InteractiveTerm(Simulation sim) {

		this.sim = sim;
		this.marsTerminal = new MarsTerminal();
		this.textIO = new TextIO(marsTerminal);
		new SwingHandler(textIO, new GameManager());

		marsTerminal.init();
		  
		// Prevent allow users from arbitrarily close the terminal by clicking top right
		// close button
		marsTerminal.registerUserInterruptHandler(term -> {
		}, false);


		// Add Mars Terminal to the clock listener
		sim.getMasterClock().addClockListener(marsTerminal, 1000);
		// Update title
		marsTerminal.changeTitle(false);

		Thread consoleThread = new Thread(new ConsoleTask());
		consoleThread.setName("ConsoleThread");
		consoleThread.start();
	}

	/**
	 * The ConsoleTask allows running the beryx console in a thread.
	 */
	private class ConsoleTask implements Runnable {

		@Override
		public void run() {
			// Load the menu choice
			startConsole();
		}
	}

	/**
	 * Loads the terminal menu.
	 */
	private void startConsole() {

		UserChannel channel = new TextIOChannel(textIO);
		// Console is always an admin
		Set<ConversationRole> roles = new HashSet<>();
		roles.add(ConversationRole.ADMIN);
		Conversation conversation = new Conversation(channel, new TopLevel(), roles, sim);

		conversation.interact();
		logger.info("Conversation ended.");

	}

	public MarsTerminal getTerminal() {
		return marsTerminal;
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
}
