package org.mars.sim.console.chat.service;

import java.util.Set;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.Simulation;

public class SSHConversation extends Conversation {

	private String username;
	private RemoteChatService parent;

	public SSHConversation(RemoteChatService parent, SSHChannel sshChannel, String username,
						   Set<ConversationRole> roles, Simulation sim) {
		super(sshChannel, new RemoteTopLevel(username), roles, sim);
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
