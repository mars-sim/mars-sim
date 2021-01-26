package org.mars.sim.console.chat.service;

import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Simulation;

public class SSHConversation extends Conversation {

	private String username;
	private RemoteChatService parent;

	public SSHConversation(RemoteChatService parent, SSHChannel sshChannel, String username, boolean admin, Simulation sim) {
		super(sshChannel, new RemoteTopLevel(username, admin), admin, sim);
		this.username = username;
		this.parent = parent;
	}

	public RemoteChatService getService() {
		return parent;
	}
	
	public String getUsername() {
		return username;
	}

}
