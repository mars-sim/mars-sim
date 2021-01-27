package org.mars.sim.console.chat.service;

import org.mars.sim.console.chat.simcommand.TopLevel;

/**
 * A Top level chat command for remote connections. This has the ability to disconnect the remote connection
 * and user management
 */
public class RemoteTopLevel extends TopLevel {

	private static final String INTRO = "Welcome %s to the Mars Sim chat service.";

	public RemoteTopLevel(String username) {
		
		setIntroduction(String.format(INTRO, username));
		addSubCommand(ExitCommand.EXIT);
		addSubCommand(PasswordCommand.PASSWORD);
	}
}
