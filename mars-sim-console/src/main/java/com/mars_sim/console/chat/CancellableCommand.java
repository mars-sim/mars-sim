/*
 * Mars Simulation Project
 * CancellableCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat;

/**
 * A command that can be cancelled.
 */
public interface CancellableCommand {

	/**
	 * Cancels the current command.
	 */
	void cancel();
}
