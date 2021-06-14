/**
 * Mars Simulation Project
 * CancellableCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

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
