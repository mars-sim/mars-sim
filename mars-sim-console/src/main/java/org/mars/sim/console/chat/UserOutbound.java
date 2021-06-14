/**
 * Mars Simulation Project
 * UserOutbound.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat;


public interface UserOutbound {

	/**
	 * A specialkey has been pressed by the user; this is treated as an out of bound communication.
	 * @param keyStroke Keystroke
	 */
	void keyStrokeApplied(String keyStroke);

}
