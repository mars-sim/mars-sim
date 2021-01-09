package org.mars.sim.console.chat.service;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;
import org.mars_sim.msp.core.Simulation;

class SSHConversationFactory implements ShellFactory {

	private ExecutorService executor;
	private Simulation sim;
	
	public SSHConversationFactory(Simulation sim) {
		executor = Executors.newCachedThreadPool();
		this.sim = sim;
	}
	
	@Override
	public Command createShell(ChannelSession channel) throws IOException {
		return new SSHChannel(executor, sim);
	}

}
