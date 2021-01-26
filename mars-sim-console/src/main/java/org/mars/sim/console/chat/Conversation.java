package org.mars.sim.console.chat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars_sim.msp.core.Simulation;

/**
 * Establishes a Converation with a user.
 */
public class Conversation implements UserOutbound {
	
	public static final String AUTO_COMPLETE_KEY = "tab";
	public static final String HISTORY_BACK_KEY = "up";
	public static final String HISTORY_FORWARD_KEY = "down";
	public static final String CANCEL_KEY = "escape";
	
    private static final Logger LOGGER = Logger.getLogger(Conversation.class.getName());
	
	private List<String> inputHistory;
	private int historyIdx = 0;
	
	private Stack<InteractiveChatCommand> previous;
	private InteractiveChatCommand current;
	private CancellableCommand activeCommand;
	private UserChannel comms;

	private boolean active;
	private List<String> options;
	private int optionsIdx;
	private Object optionsPartial;

	private Simulation sim;
	private boolean admin;
	
	/**
	 * Start a conversation with the user using a Comms Channel starting with a certain command.
	 * @param in
	 * @param out
	 * @param initial
	 */
	public Conversation(UserChannel comms, InteractiveChatCommand initial, boolean admin, Simulation sim) {
		this.current = initial;
        this.active = true;
        this.comms = comms;
        this.admin = admin;
        this.previous = new Stack<>();
        this.inputHistory = new ArrayList<>();
        
        this.sim = sim;

        comms.registerHandler(AUTO_COMPLETE_KEY, this, false);
        comms.registerHandler(HISTORY_BACK_KEY, this, false);
        comms.registerHandler(HISTORY_FORWARD_KEY, this, false);
        comms.registerHandler(CANCEL_KEY, this, true);
	}
	
	/**
	 * The list of stacked commands in the current conversation. It does not include
	 * the current command; only those stacked.
	 */
	public List<InteractiveChatCommand> getCommandStack() {
		return previous;
	}
	
	public InteractiveChatCommand getCurrentCommand() {
		return current;
	}
	
	/**
	 * Update the current chat command and potentially remober it for later.
	 * @param newCommand New chat
	 * @param remember Push this in the stack of previous commands
	 */
	public void setCurrentCommand(InteractiveChatCommand newCommand, boolean remember) {
		if (remember) {
			previous.push(this.current);
		}
		this.current = newCommand;
		LOGGER.fine("Current chat set to " + current);
	}
	
    public void setCompleted() {
    	active = false;
    }
    
	/**
	 * Interact with the end user.
	 */
    public void interact() {
    	if (current == null) {
    		throw new IllegalStateException("There is no current command");
    	}
    	
    	InteractiveChatCommand lastCurrent = null;
		while (active) {
			// A new chat so let it welcome itself
			if (current != lastCurrent) {
	        	String preamble = current.getIntroduction();
	        	if (preamble != null) {
	        		println(preamble);
				}
	        	lastCurrent = current;
			}
			
        	// Get input
			String prompt = current.getPrompt() + " > ";
        	String input = getInput(prompt);
        	options = null; // Remove any auto complete options once user executes
        	
        	// Update history
        	inputHistory.add(input);
        	historyIdx = inputHistory.size();

        	// Execute and trap exceptino to not break conversation
        	LOGGER.fine("Entered " + input);
        	try {
        		current.execute(this, input);
        	}
        	catch (RuntimeException rte) {
        		LOGGER.log(Level.SEVERE, "Problem executing command " + input, rte);
        		
        		StringWriter writer = new StringWriter();
        		PrintWriter out = new PrintWriter(writer);
        		rte.printStackTrace(out);
        		println("Sorry I had a problem doing that " + rte.getMessage());
        		println(writer.toString());
        	}
        }
		
		comms.close();
    }

	/**
	 * This reset the current command to the previous one.
	 */
	public void resetCommand() {
		current = previous.pop();
		LOGGER.fine("Current chat popped to " + current);
	}

	public String getInput(String prompt) {
		return comms.getInput(prompt);
	}


	public void println(String text) {
		comms.println(text);
	}


	public void print(String text) {
		comms.print(text);
	}

	public CancellableCommand getActiveCommand() {
		return activeCommand;
	}

	public void setActiveCommand(CancellableCommand activeCommand) {
		this.activeCommand = activeCommand;
	}

	@Override
	/**
	 * User has pressed a special key that is listened for.
	 * @param keyStroke Key pressed
	 */
	public void keyStrokeApplied(String key) { 		
		switch(key) {
		case AUTO_COMPLETE_KEY:
			autoComplete();
			break;
			
		case HISTORY_BACK_KEY:
			replayHistory(-1);
			break;
			
		case HISTORY_FORWARD_KEY:
			replayHistory(1);
			break;
			
		case CANCEL_KEY:
			cancelCommand();
			break;
		
		default:
			LOGGER.warning("Unexpected keystroke " + key);
			break;
		}
	}

	/**
	 * Cancel the current command that is registered
	 */
	private void cancelCommand() {
		if (activeCommand != null) {
			println("Cancelling command please wait......");
			activeCommand.cancel();
			activeCommand = null;
		}
	}

	private void replayHistory(int offset) {
		historyIdx += offset;
		historyIdx = Math.max(historyIdx, 0);
		historyIdx = Math.min(historyIdx, inputHistory.size()-1);
		
		// Check within range and replace
		if ((historyIdx >= 0) && (historyIdx < inputHistory.size())) {
			comms.replaceUserInput(inputHistory.get(historyIdx));	
		}
	}

	/**
	 * User wants to do auto complete on partial input.
	 */
	private void autoComplete() {
		String partialInString = comms.getPartialInput();
		
		// So no complete option or user has changed partial input
		if ((options == null) || !partialInString.equals(optionsPartial)) {
			options = current.getAutoComplete(this, partialInString);
			Collections.sort(options);
			
			// Reset pointer
			optionsIdx = 0;
			optionsPartial = partialInString;
		}
		
		// If there are options
		if (!options.isEmpty()) {
			if (optionsIdx >= options.size()) {
				optionsIdx = 0;
			}
			
			// Replace the user input
			comms.replaceUserInput(options.get(optionsIdx));
			
			optionsIdx++; // ready for next one
		}
	}

	public boolean isAdmin() {
		return admin;
	}
	
	public Simulation getSim() {
		return sim;
	}

	/**
	 * Gte a integer input
	 * @param prompt
	 * @return
	 */
	public int getIntInput(String prompt) {
		String response = getInput(prompt);

		int newLevel = -1;
		if (!response.isBlank()) {
			try {
				newLevel = Integer.parseInt(response);
			}
			catch (NumberFormatException e) {
				println("Sorry must neter a valid input");
			}
		}
		return newLevel;
	}
}
