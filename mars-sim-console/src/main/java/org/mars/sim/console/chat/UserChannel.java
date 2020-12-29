package org.mars.sim.console.chat;

public interface UserChannel {

	/**
	 * Get some text input from the user in response to a prompt
	 * @param prompt Prompt to display
	 * @return Entered text
	 */
	public String getInput(String prompt);
		
	/**
	 * Display text followed by a new line to the user
	 * @param text
	 */
	public void println(String text);
	
	/**
	 * Display text to the user but no new line
	 * @param text
	 */
	public void print(String text);

	/**
	 * Communications are completed.
	 */
	public void close();

	/**
	 * Add listener for the user pressing a special keystroke
	 * @param keyStroke Key to listen for
	 * @param listener Handler
	 * @return Was the handler registered
	 */
	boolean registerHandler(String keyStroke, UserOutbound listener);

	/**
	 * Get the input entered so far but not committed.
	 * @return Input so far
	 */
	public String getPartialInput();

	/**
	 * Replace any text the user has entered with a new message.
	 * @param replacement
	 */
	public void replaceUserInput(String replacement);
}
