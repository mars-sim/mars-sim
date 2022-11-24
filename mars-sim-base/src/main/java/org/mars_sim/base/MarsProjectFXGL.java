/*
 * Mars Simulation Project
 * MarsProjectFXGL.java
 * @date 2022-11-21
 * @author Manny Kung
 */

package org.mars_sim.base;

import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getSaveLoadService;
import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;
import static com.almasb.fxgl.dsl.FXGL.getd;
import static com.almasb.fxgl.dsl.FXGL.getdp;
import static com.almasb.fxgl.dsl.FXGL.inc;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.run;
import static com.almasb.fxgl.dsl.FXGL.runOnce;
import static com.almasb.fxgl.dsl.FXGL.set;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.localization.Language;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;

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

	private static final String TIME = "time";
	
	private static final String LOGGING_PROPERTIES = "/logging.properties";
    
//	private static final String CH2_SAVE_FILE = SimulationFiles.getSaveDir() + File.separator + Simulation.CH2_SAVE_FILE;
	private static final String CH2_SAVE_FILE = Simulation.CH2_SAVE_FILE;
	        
	private MarsWorld first = new MarsWorld();

    private StackPane mainRoot;
	
    private int i = 0;
    
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
        vars.put(TIME, 0.0);
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
	     
	     run(() -> inc(TIME, 1.0), Duration.seconds(1.0));
	     
	     getGameScene().setBackgroundColor(new LinearGradient(
	                0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE,
	                new Stop(0.0, Color.ROSYBROWN),
	                new Stop(1.0, Color.SADDLEBROWN)
	        ));
	        
        List<Language> languages = new ArrayList<>();//FXGL.getSettings().getSupportedLanguages());
        languages.add(Language.ENGLISH);
//	        languages.add(new Language("KOREAN"));
//	        languages.add(new Language("CHINESE"));
        
        FXGL.getLocalizationService().addLanguageData(Language.ENGLISH, Map.of("some.key", "Welcome to Mars Simulation Project !"));
//	        FXGL.getLocalizationService().addLanguageData(new Language("KOREAN"), Map.of("some.key", "안녕 화성 !"));
//	        FXGL.getLocalizationService().addLanguageData(new Language("CHINESE"), Map.of("some.key", "你好，火星 !"));

        run(() -> {

            FXGL.getNotificationService().pushNotification(FXGL.localize("some.key"));
            FXGL.getSettings().getLanguage().setValue(languages.get(i));

            i++;
            if (i == languages.size())
                i = 0;

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
	    var uptimeText = getUIFactoryService().newText("Uptime: ", Color.BLACK, 18.0);
	    addUINode(uptimeText, 10, 20);
        var text = getUIFactoryService().newText("", Color.BLACK, 18.0);
        text.textProperty().bind(getdp(TIME).asString());
        addUINode(text, 100, 20);
	        
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
        getSaveLoadService().addHandler(new SaveLoadHandler() {
            @Override
            public void onSave(DataFile data) {
                // create a new bundle to store your data
                var bundle = new Bundle("gameData");

                // store some data
                double time = getd(TIME);
                bundle.put(TIME, time);

                // give the bundle to data file
                data.putBundle(bundle);
            }

            @Override
            public void onLoad(DataFile data) {
                // get your previously saved bundle
                var bundle = data.getBundle("gameData");

                // retrieve some data
                double time = bundle.get(TIME);

                // update your game with saved data
                set(TIME, time);
            }
        });
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
				logger.log(Level.WARNING, "Could read logging default config", e);
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
