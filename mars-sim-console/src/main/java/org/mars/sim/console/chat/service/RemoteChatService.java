package org.mars.sim.console.chat.service;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.sshd.server.SshServer;
import org.mars_sim.msp.core.Simulation;

public class RemoteChatService {
	private final static Logger LOGGER = Logger.getLogger(RemoteChatService.class.getName());
	
	private int port;
	private SshServer sshd;
	private Credentials creds;
	
	public RemoteChatService(int port, Credentials creds) {
		this.port = port;
		this.creds = creds;
		buildServer();
	}
	
	private void buildServer() {
		this.sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setPasswordAuthenticator(creds);
		sshd.setShellFactory(new SSHConversationFactory());
		sshd.setKeyPairProvider(creds);
		
		LOGGER.info("Built server on port " + port);
	}

	public void start() throws IOException {
		LOGGER.info("Start SSH server on port " + port);
		sshd.start();
	}
	
	public void stop() throws IOException {
		sshd.stop();
	}
}
