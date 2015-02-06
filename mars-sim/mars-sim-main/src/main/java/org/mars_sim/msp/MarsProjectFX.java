/**
 * Mars Simulation Project 
 * MarsProjectFX.java
 * @version 3.08 2015-02-05
 * @author Manny Kung
 */
package org.mars_sim.msp;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * MarsProjectFX is the main class for MSP. It creates JavaFX/8 application thread.
 */
public class MarsProjectFX extends Application  {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsProjectFX.class.getName());

    static String[] args;
    
    /**
     * Constructor
     * @param args command line arguments.
     */ 

    public void start(Stage primaryStage) {

        logger.info("Starting Mars Simulation");

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
        
        
        setLogging();  
        setDirectory(); 
        new MarsProject(args);
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