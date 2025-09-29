/*
 * Mars Simulation Project
 * TextIOChannel.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.terminal;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.StringInputReader;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;

import com.mars_sim.console.chat.UserChannel;
import com.mars_sim.console.chat.UserOutbound;

/**
 * This binds a TextIO terminal to a UserChannel for the console feature
 */
class TextIOChannel implements UserChannel {

	private static final String PRESSED_PREFIX = "pressed ";
	
	private TextIO textIO;
	private TextTerminal<?> terminal;
	private StringInputReader reader;
	private boolean supportsHandlers = false;
    private boolean userInput = false;
	private String previousPartialInput;

	public TextIOChannel(TextIO term) {
		this.textIO = term;
		this.terminal = textIO.getTextTerminal();
		
		if (terminal instanceof SwingTextTerminal swingTerm) {
			supportsHandlers  = true;
			
			// Track when the user keys text
	        swingTerm.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void removeUpdate(DocumentEvent e) {userInput = true;}
	            @Override public void insertUpdate(DocumentEvent e) {userInput = true;}
	            @Override public void changedUpdate(DocumentEvent e) {userInput = true;}
	        });
		}
	}

	/**
	 * Gets user input in response to a prompt.
	 */
	@Override
	public String getInput(String prompt) {
		if (reader == null) {
			reader = textIO.newStringInputReader().withMinLength(0);
		}
		return reader.read(prompt);
	}

	@Override
	public void println(String text) {
		terminal.println(text);
	}

	@Override
	public void print(String text) {
		terminal.print(text);
	}

	/**
	 * Closes the channel to the user.
	 */
	@Override
	public void close() {
		textIO.dispose();
	}

	@Override
	public boolean registerHandler(String key, UserOutbound listener, boolean interuptExecution) {
		if (supportsHandlers) {
			String keyStroke = PRESSED_PREFIX + key.toUpperCase();
			SwingTextTerminal swingTerm = (SwingTextTerminal) terminal;
	
			if (interuptExecution) {
				// Register a handler
		        swingTerm.setUserInterruptKey(keyStroke);

				swingTerm.registerUserInterruptHandler(t ->
		            listener.keyStrokeApplied(key), false);	
			}
			else {
				// Register a normal handler
				swingTerm.registerHandler(keyStroke, t -> {
		            listener.keyStrokeApplied(key);
		            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
				});
			}
		}
		
		return supportsHandlers;
	}

	/**
	 * Gets any partial uncommitted input.
	 */
	@Override
	public String getPartialInput() {
		String result = null;
		if (supportsHandlers) {
			// User input since the replacement
			if (userInput || (previousPartialInput == null)) {
				SwingTextTerminal swingTerm = (SwingTextTerminal) terminal;
				result = swingTerm.getPartialInput();
				previousPartialInput = result;
			}
			else {
				result = previousPartialInput;
			}
		}
		
		return result;
	}

	@Override
	public void replaceUserInput(String replacement) {
		if (supportsHandlers) {
			SwingTextTerminal swingTerm = (SwingTextTerminal) terminal;
			swingTerm.replaceInput(replacement, false);
			userInput = false;
		}
	}
}
