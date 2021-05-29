/**
 * Mars Simulation Project
 * DesktopLauncher.java
 * @version 3.1.2 2021-05-28
 * @author Manny Kung
 */
package org.mars_sim.libgdx;

import org.mars_sim.msp.core.Simulation;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = Simulation.title;
        config.width = 990;
        config.height = 1000;
        new LwjglApplication(new MarsProjectLibGDX(), config);
//		new LwjglApplication(new MySplash(), config);
//		new LwjglApplication(new Drop(), config);
//		new LwjglApplication(new DropExtended(), config);
	}
	
	// see a library of utility here at https://github.com/rafaskb/awesome-libgdx
}
