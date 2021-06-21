/**
 * Mars Simulation Project
 * RemoteChatService.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.logging.Logger;

import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

/**
 * This provides a remote chat service using a SSH protocol. 
 * Connected SSH channel uses Conversation class to interact with the Simulation.
 *
 */
public class RemoteChatService {
	private static final Logger LOGGER = Logger.getLogger(RemoteChatService.class.getName());

	private static final String SERVICE_KEY_FILE = "service-key";

	private static final long DEFAULT_HEARTBEAT = 60000; // Heartbeat every minute
	
	private int port;
	private SshServer sshd;
	private Credentials creds;
	
	/**
	 * Build a remote chat service that will present an SSH service at the specified port.
	 * @param port Port number
	 * @param dataDir Directory that holds any persistent files such as host key.
	 * @param creds Credentials of allowed users.
	 */
	public RemoteChatService(int port, File dataDir, Credentials creds) {
		this.port = port;
		this.creds = creds;
		buildServer(dataDir);
	}

	/**
	 * Build the server ready to use.
	 * @param dataDir Direcotry to host host key
	 */
	private void buildServer(File dataDir) {
		this.sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session)
					throws PasswordChangeRequiredException, AsyncAuthException {
				return creds.authenticate(username, password);
			}
		});
		sshd.setSessionHeartbeat(HeartbeatType.IGNORE, Duration.ofMillis(DEFAULT_HEARTBEAT));
		sshd.setShellFactory(new SSHConversationFactory(this));
		Path keyPath = Paths.get(dataDir.getAbsolutePath() + File.separator + SERVICE_KEY_FILE);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyPath));
		
		LOGGER.info("Built server on port " + port);
	}

	public void start() throws IOException {
		LOGGER.info("Start SSH server on port " + port);
		sshd.start();
	}
	
	public void stop() throws IOException {
		sshd.stop();
	}

	Credentials getCredentials() {
		return creds;
	}
}
