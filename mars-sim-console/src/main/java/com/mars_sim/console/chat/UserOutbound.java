/*
 * Mars Simulation Project
 * UserOutbound.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat;


public interface UserOutbound {

	/**
	 * A special key has been pressed by the user; this is treated as an out of bound communication.
	 * 
	 * @param keyStroke Keystroke
	 */
	void keyStrokeApplied(String keyStroke);

}
