package org.mars.sim.console.chat.service;

import org.mars.sim.console.chat.simcommand.TopLevel;

/**
 * A Top level chat command for remote connections. This has the ability to disconnect the remote connection
 * and user management
 */
public class RemoteTopLevel extends TopLevel {

	private static final String INTRO = "Welcome %s to the Mars Sim chat service.\nYou %s admin rights\n";

	public RemoteTopLevel(String username, String userHost, boolean isAdmin) {
		// Toplevel does not need a keyword or short command
		super(isAdmin);
		
		setIntroduction(String.format(INTRO, username, (isAdmin ? "have" : "do not have")));
		addSubCommand(ExitCommand.EXIT);
	}
}
