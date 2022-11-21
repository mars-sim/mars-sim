/*
 * Mars Simulation Project
 * MarsProjectFXGL.java
 * @date 2022-11-21
 * @author Manny Kung
 */

package org.mars_sim.base;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.fxgl.MarsWorld;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.swing.MainWindow;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class MarsProjectFXGL extends GameApplication {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectFXGL.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";
    
	private MarsWorld first = new MarsWorld();

	/**
	 * Constructor
	 */
	public MarsProjectFXGL() {
//		logger.config("Starting " + Simulation.TITLE);
	}

	@Override
	protected void initSettings(GameSettings settings) {
		first.initSettings(settings);
	}

    @Override
    protected void initGameVars(Map<String, Object> vars) {
    	first.initGameVars(vars);
    }

	 @Override
	 protected void initGame() {
		 first.initGame();
	}

	 @Override
	 protected void initInput() {
		 first.initInput();
	 }

	 @Override
	 protected void initUI() {
		 first.initUI();
	 }

	 @Override
	 protected void initPhysics() {
		 first.initPhysics();
	 }

	 @Override
	 protected void onUpdate(double tpf) {
		 first.onUpdate(tpf);
	 }

	/**
	 * Parse the argument and start the simulation.
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {
//		logger.config("List of input args : " + Arrays.toString(args));
		SwingUtilities.invokeLater(MainWindow::startSplash);

		launch(args);
				
	    // Dispose the Splash Window
	    SwingUtilities.invokeLater(MainWindow::disposeSplash);
	}


	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * [landrus, 27.11.09]: Read the logging configuration from the classloader, so
		 * that this gets webstart compatible. Also create the logs dir in user.home
		 */
		new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();

		try {
			LogManager.getLogManager().readConfiguration(MarsProjectFXGL.class.getResourceAsStream(LOGGING_PROPERTIES));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Could read logging default config", e);
			}
		}

		// general text antialiasing
		System.setProperty("swing.aatext", "true");
		System.setProperty("awt.useSystemAAFontSettings", "lcd"); // for newer VMs
        // Use opengl
        // Question: How compatible are linux and macos with opengl ?
        // System.setProperty("sun.java2d.opengl", "true"); // not compatible with
        if (!MainWindow.OS.contains("linux")) {
            System.setProperty("sun.java2d.ddforcevram", "true");
        }
        
        logger.config("Starting " + Simulation.TITLE);
        
		// starting the simulation
		new MarsProjectFXGL().parseArgs(args);
	}

}
