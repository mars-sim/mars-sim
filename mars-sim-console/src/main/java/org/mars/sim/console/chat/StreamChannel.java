/**
 * Mars Simulation Project
 * StreamChannel.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Provides a UserChannel implementation using standard java Streams.
 */
public class StreamChannel implements UserChannel {
	
	private Scanner in;
	private PrintStream out;

	public StreamChannel(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.out = out;
	}
	
	@Override
	public String getInput(String prompt) {
		out.print(prompt);
    	return in.nextLine().trim();
	}

	@Override
	public void println(String text) {
		out.println(text);
	}

	@Override
	public void print(String text) {
    	out.print(text);
    	out.flush();
	}

	@Override
	public void close() {
		// Nothing to do here
	}

	@Override
	public boolean registerHandler(String keyStroke, UserOutbound listener, boolean interuptExecution) {
		// System console does not support outbound keystrokes
		return false;
	}

	@Override
	public String getPartialInput() {
		// No partial input at the moment
		return null;
	}

	@Override
	public void replaceUserInput(String replacement) {
		// Not supported
		throw new UnsupportedOperationException();
	}
}
