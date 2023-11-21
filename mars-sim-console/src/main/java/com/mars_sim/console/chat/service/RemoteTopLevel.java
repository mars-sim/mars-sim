/*
 * Mars Simulation Project
 * RemoteTopLevel.java
 * @date 2023-11-19
 * @author Barry Evans
 */

package com.mars_sim.console.chat.service;

import com.mars_sim.console.chat.simcommand.TopLevel;

/**
 * A Top level chat command for remote connections. This has the ability to disconnect the remote connection
 * and user management
 */
public class RemoteTopLevel extends TopLevel {

	private static final String INTRO = "Welcome %s to the mars-sim Chat AI.";

	public RemoteTopLevel(String username) {
		
		setIntroduction(String.format(INTRO, username));
		addSubCommand(ExitCommand.EXIT);
		addSubCommand(PasswordCommand.PASSWORD);
	}
}
