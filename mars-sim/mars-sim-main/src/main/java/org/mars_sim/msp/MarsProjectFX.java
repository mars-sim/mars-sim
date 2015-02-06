/**
 * Mars Simulation Project 
 * MarsProjectFX.java
 * @version 3.08 2015-02-05
 * @author Manny Kung
 */
package org.mars_sim.msp;

import javafx.application.Application;

import javafx.stage.Stage;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mars_sim.msp.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.javafx.MainWindowFX;
import org.mars_sim.msp.ui.swing.SplashWindow;

/**
 * MarsProjectFX is the main class for MSP. It creates JavaFX/8 application thread.
 */
public class MarsProjectFX extends Application  {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsProjectFX.class.getName());

    static String[] args;
    
    /** true if displaying graphic user interface. */
    private boolean useGUI = true;

    /** true if help documents should be generated from config xml files. */
    private boolean generateHelp = false;

    /**
     * Constructor
     * @param args command line arguments.
     */ 

    public void start(Stage primaryStage) {

        logger.info("Starting Mars Simulation");

        /*
        Button btn = new Button();
        btn.setText("Say 'Hello Mars-simmers!'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
     
        @Override
        public void handle(ActionEvent event) {
             System.out.println("Hello Mars-simmers!");
                }
        });
            
        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Mars Simulation Project FX");
        primaryStage.setScene(scene);
        primaryStage.show();
        */
        setLogging();  
        setDirectory(); 
        
        MarsProject mp = new MarsProject(true);
         	
        List<String> argList = Arrays.asList(args);
        useGUI = !argList.contains("-headless");
        generateHelp = argList.contains("-generateHelp");
        
        if (useGUI) {
    
            // Create a splash window
            SplashWindow splashWindow = new SplashWindow();           
            mp.showSplashScreen(splashWindow);
            
            boolean newSim = mp.initializeSimulation(args);

	        // 2015-01-26 Added mwFX
	        MainWindowFX mwFX = new MainWindowFX(primaryStage, newSim);
	        
	        // Start simulation
	        mp.startSimulation();
	    
	        // Dispose the splash window.
	        splashWindow.remove();
	    }
	    else {
	        // Initialize the simulation.
	        mp.initializeSimulation(args);        
	        // Start the simulation.
	        mp.startSimulation();
	    }
	    
	    // this will generate html files for in-game help based on config xml files
	    if (generateHelp) {
	    	HelpGenerator.generateHtmlHelpFiles();
	    }
	    
	}
    

    
    public void setDirectory() {
        new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();
    }
    
    
    public void setLogging() {

        try {
            LogManager.getLogManager().readConfiguration(MarsProjectFX.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load logging properties", e);
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Could read logging default config", e);
            }
        }
    }
    
    public static void main(String[] args) {    	
    	MarsProjectFX.args = args;
        launch(args);
    }
}