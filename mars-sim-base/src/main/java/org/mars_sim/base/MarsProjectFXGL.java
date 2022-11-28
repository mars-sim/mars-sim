/*
 * Mars Simulation Project
 * MarsProjectFXGL.java
 * @date 2022-11-25
 * @author Manny Kung
 */

package org.mars_sim.base;

import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getSaveLoadService;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.run;
import static com.almasb.fxgl.dsl.FXGL.runOnce;

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
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.localization.Language;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

public class MarsProjectFXGL extends GameApplication {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectFXGL.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";

	private static final String CH2_SAVE_FILE = Simulation.CH2_SAVE_FILE;
	        
	private MarsWorld first = new MarsWorld();

    private StackPane mainRoot;

	/**
	 * Constructor
	 */
	public MarsProjectFXGL() {
//		logger.info("CH2_SAVE_FILE: " + CH2_SAVE_FILE);
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
	     mainRoot = new StackPane();
	     mainRoot.setPrefSize(getAppWidth(), getAppHeight());
	     addUINode(mainRoot);
	     
	     runOnce(() -> {
//	         spawn("splashtext", 300, 500);
	     }, Duration.seconds(0));
	          
	     getGameScene().setBackgroundColor(new LinearGradient(
	                0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE,
	                new Stop(0.0, Color.ROSYBROWN),
	                new Stop(1.0, Color.SADDLEBROWN)
	        ));

        FXGL.getLocalizationService().addLanguageData(Language.ENGLISH, Map.of("some.key", "Welcome to Mars Simulation Project !"));

        run(() -> {

            FXGL.getNotificationService().pushNotification(FXGL.localize("some.key"));
            FXGL.getSettings().getLanguage().setValue(Language.ENGLISH);

        }, Duration.seconds(60));
	        
		 first.initGame();
	}

	 @Override
	 protected void initInput() {
		 first.initInput();
		 
		 onKeyDown(KeyCode.V, "Save", () -> {
             getSaveLoadService().saveAndWriteTask(CH2_SAVE_FILE).run();
             FXGL.getNotificationService().pushNotification("Current Simulation saved");
             logger.log(Level.WARNING, "Current Simulation saved.");
         });

         onKeyDown(KeyCode.L, "Load", () -> {
             getSaveLoadService().readAndLoadTask(CH2_SAVE_FILE).run();
             String s = "Previous simulation loaded";
             FXGL.getDialogService().showMessageBox(s, () -> {
                 // code to run after dialog is dismissed
             });
             FXGL.getNotificationService().pushNotification(s);
             logger.log(Level.WARNING, s);
         });
	 }
	 
	 @Override
	 protected void initUI() {
		first.initUI();
		
		// Dispose the Splash Window
	    SwingUtilities.invokeLater(MainWindow::disposeSplash);
	 }

	 @Override
	 protected void initPhysics() {
		 first.initPhysics();
	 }

	 @Override
	 protected void onUpdate(double tpf) {
		 first.onUpdate(tpf);
	 }

    @Override
    protected void onPreInit() {
        first.onPreInit();
    }

	/**
	 * Parse the argument and start the simulation.
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {
//		logger.config("List of input args : " + Arrays.toString(args));
		launch(args);
	}


	/**
	 * The starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
	    
	    SwingUtilities.invokeLater(MainWindow::startSplash);

		 // Create the logs dir in user.home
		new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();

		try {
		    // Read the logging configuration from the classloader
			LogManager.getLogManager().readConfiguration(MarsProjectFXGL.class.getResourceAsStream(LOGGING_PROPERTIES));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Could read logging default config", e1);
			}
		}

		// Text antialiasing
		System.setProperty("swing.aatext", "true");
		System.setProperty("awt.useSystemAAFontSettings", "lcd"); // for newer VMs
		
        // Question: How compatible are linux and macos with opengl ?
        // System.setProperty("sun.java2d.opengl", "true");
		
        if (!MainWindow.OS.contains("linux")) {
            System.setProperty("sun.java2d.ddforcevram", "true");
        }
        
        logger.config("Starting " + Simulation.TITLE);
        
		// starting the simulation
		new MarsProjectFXGL().parseArgs(args);
	}

}
