/**
 * Mars Simulation Project
 * SSHConversationFactory.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.service;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;
import org.mars_sim.msp.core.Simulation;

class SSHConversationFactory implements ShellFactory {

	private ExecutorService executor;
	private RemoteChatService parent;
	
	public SSHConversationFactory(RemoteChatService remoteChatService) {
		this.executor = Executors.newCachedThreadPool();
		this.parent = remoteChatService;
	}
	
	@Override
	public Command createShell(ChannelSession channel) throws IOException {
		
		// Put a big idle timeout
		CoreModuleProperties.IDLE_TIMEOUT.set(channel.getServerSession(), Duration.ofMillis(60 * 60 * 1000L));
		
		return new SSHChannel(executor, parent, Simulation.instance());
	}

}
