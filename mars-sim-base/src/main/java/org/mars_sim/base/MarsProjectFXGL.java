/*
 * Mars Simulation Project
 * MarsProjectFXGL.java
 * @date 2021-09-26
 * @author Manny Kung
 */

package org.mars_sim.base;

import java.util.logging.Logger;

//import org.mars_sim.msp.core.Simulation;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MarsProjectFXGL extends GameApplication {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectFXGL.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";	
	
	/**
	 * Constructor 
	 */
	public MarsProjectFXGL() {
//		logger.config("Starting " + Simulation.title);
	};
	
//	/**
//	 * Parse the argument and start the simulation.
//	 * @param args
//	 */
//	public void parseArgs(String[] args) {
//		logger.config("List of input args : " + Arrays.toString(args));
//	}

	
	@Override
	protected void initSettings(GameSettings settings) {
		settings.setWidth(1366);// 1024);
		settings.setHeight(768);
//		 settings.setStageStyle(StageStyle.UNDECORATED);
		settings.setTitle("Mars Simulation Project");
		settings.setVersion("v3.3.1");
		settings.setProfilingEnabled(false); // turn off fps
		settings.setCloseConfirmation(false); // turn off exit dialog
		settings.setIntroEnabled(false); // turn off intro
//		settings.setMenuEnabled(false); // turn off menus
		settings.setCloseConfirmation(true);	
	}

	  @Override
	    protected void initGame() {
	        FXGL.entityBuilder()
	                .at(150, 150)
	                .view(new Rectangle(40, 40, Color.BLUE))
	                .buildAndAttach();
	    }
	  
	public static void main(String[] args) {
//		MarsProjectFXGL.args = args;
		launch(args);
	}
	
}
