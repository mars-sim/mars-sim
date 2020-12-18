package org.mars.sim.console.chat;


public interface UserOutbound {

	/**
	 * A specialkey has been pressed by the user; this is treated as an out of bound communication.
	 * @param keyStroke Keystroke
	 */
	void keyStrokeApplied(String keyStroke);

}
