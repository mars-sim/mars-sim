/**
 * Mars Simulation Project
 * DesktopLauncher.java
 * @version 3.1.2 2021-05-28
 * @author Manny Kung
 */
package com.mars_sim.libgdx;

import com.mars_sim.core.SimulationRuntime;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = SimulationRuntime.SHORT_TITLE;
        config.width = 990;
        config.height = 1000;
        new LwjglApplication(new MarsProjectLibGDX(), config);
	}
	
	// see a library of utility here at https://github.com/rafaskb/awesome-libgdx
}
