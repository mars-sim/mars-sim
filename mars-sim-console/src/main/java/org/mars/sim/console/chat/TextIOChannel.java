package org.mars.sim.console.chat;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.StringInputReader;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;

public class TextIOChannel implements UserChannel {

	private TextIO textIO;
	private TextTerminal<?> terminal;
	private StringInputReader reader;
	private boolean supportsHandlers = false;
    private boolean userInput = false;
	private String previousPartialInput;

	public TextIOChannel(TextIO term) {
		this.textIO = term;
		this.terminal = textIO.getTextTerminal();
		
		if (terminal instanceof SwingTextTerminal) {
			supportsHandlers  = true;
			SwingTextTerminal swingTerm = (SwingTextTerminal) terminal;
			
			// Track when the user keys text
	        swingTerm.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void removeUpdate(DocumentEvent e) {userInput = true;}
	            @Override public void insertUpdate(DocumentEvent e) {userInput = true;}
	            @Override public void changedUpdate(DocumentEvent e) {userInput = true;}
	        });
		}
	}

	/**
	 * Get user input in response to a prompt
	 */
	@Override
	public String getInput(String prompt) {
		if (reader == null) {
			reader = textIO.newStringInputReader();
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
	 * Close the channel to the user.
	 */
	@Override
	public void close() {
		textIO.dispose();
	}

	
	/**
	 * Add listener for the user pressing a special keystroke
	 * @param keyStroke Key to listen for
	 * @param listener Handler
	 * @return Was the handler registered
	 */
	@Override
	public boolean registerHandler(String keyStroke, UserOutbound listener, boolean interuptExecution) {
		if (supportsHandlers) {
			SwingTextTerminal swingTerm = (SwingTextTerminal) terminal;
	
			if (interuptExecution) {
				// Register a handler
		        swingTerm.setUserInterruptKey(keyStroke);

				swingTerm.registerUserInterruptHandler(t -> {
		            listener.keyStrokeApplied(keyStroke);
				}, false);	
			}
			else {
				// Register a normal handler
				swingTerm.registerHandler(keyStroke, t -> {
		            listener.keyStrokeApplied(keyStroke);
		            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
				});
			}
		}
		
		return supportsHandlers;
	}

	/**
	 * Get any partial uncommited input.
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
