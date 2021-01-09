package org.mars.sim.console.chat.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

public class Credentials implements PasswordAuthenticator, KeyPairProvider {

	private Map<String, String> users;
	private KeyPair keyPair;
	
	public Credentials() {
		users = new HashMap<>();
		users.put("admin", "hello");
		
		generateKeyPair();
	}
	
	
	@Override
	public boolean authenticate(String username, String password, ServerSession session)
			throws PasswordChangeRequiredException {
		String found = users.get(username);
		
		return (found != null) && found.equals(password);
	}


	@Override
	public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
		List<KeyPair> keys = new ArrayList<>();
		keys.add(keyPair);
		return keys;
	}

	private void generateKeyPair() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(512);
			keyPair = keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
