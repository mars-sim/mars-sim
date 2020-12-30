package org.mars.sim.console.chat;

/**
 * A command that can be cancelled
 */
public interface CancellableCommand {

	/**
	 * Cancel the current command
	 */
	void cancel();
}
