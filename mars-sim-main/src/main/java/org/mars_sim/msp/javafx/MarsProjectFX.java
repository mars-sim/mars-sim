/**
 * Mars Simulation Project 
 * MarsProjectFX.java
 * @version 3.08 2015-02-05
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

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

import javax.swing.JOptionPane;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;

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

	// 2014-11-19 Added img and IMAGE_DIR for displaying MSP Logo Icon 
    private Image img;
    //private final static String IMAGE_DIR = "/images/";
    
    /**
     * Constructor
     * @param args command line arguments.
     */ 

	public void start(Stage primaryStage) {

        primaryStage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResource("/icons/LanderHab.png").toString()));
        
        logger.info("Starting " + Simulation.WINDOW_TITLE);
    
        setLogging();  
        setDirectory(); 
           	
        List<String> argList = Arrays.asList(args);
        useGUI = !argList.contains("-headless");
        generateHelp = argList.contains("-generateHelp");
        
        if (useGUI) {
    
            // Create a splash window
            //SplashWindow splashWindow = new SplashWindow();           
            //showSplashScreen(splashWindow);
            
	        // 2015-01-26 Added mwFX
	        MainMenu mmFX = new MainMenu(this, args, primaryStage, true);

	        // Dispose the splash window.
	        //splashWindow.remove();

	        // Initialize the simulation.
	        //initializeSimulation(args);        
	        // Start the simulation.
	        //startSimulation();
	        
	        // Open all initial windows.
	        //desktop.openInitialWindows();
	        
	    }
	    else {
	        // Initialize the simulation.
	        initializeSimulation(args);        
	        // Start the simulation.
	        startSimulation();
	    }
	    
	    // this will generate html files for in-game help based on config xml files
	    if (generateHelp) {
	    	HelpGenerator.generateHtmlHelpFiles();
	    }
	    
	}
    
    /*
    public void showSplashScreen(SplashWindow splashWindow) {

   		// 2014-11-19 Displayed MSP Logo Icon as SplashWindow is loaded
        String fullImageName = "LanderHab.png";
        String fileName = fullImageName.startsWith("/") ?
            	fullImageName :
            	IMAGE_DIR + fullImageName;
        URL resource = ImageLoader.class.getResource(fileName);
		Toolkit kit = Toolkit.getDefaultToolkit();
		img = kit.createImage(resource);
		splashWindow.getJFrame().setIconImage(img);			
        splashWindow.display();
        splashWindow.getJFrame().setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
    }
    */
    
    /**
     * Initialize the simulation.
     * @param args the command arguments.
     * @return true if new simulation (not loaded)
     */
    boolean initializeSimulation(String[] args) {
        boolean result = false;
        
        // Create a simulation
        List<String> argList = Arrays.asList(args);

        if (argList.contains("-new")) {
            // If new argument, create new simulation.
            handleNewSimulation(); // if this fails we always exit, continuing is useless
            result = true;

        } else if (argList.contains("-load")) {
            // If load argument, load simulation from file.
            try {
                handleLoadSimulation(argList);
            } catch (Exception e) {
                showError("Could not load the desired simulation, trying to create a new Simulation...", e);
                handleNewSimulation();
                result = true;
            }
        } else {
            try {
                handleLoadDefaultSimulation();
            } catch (Exception e) {
//                showError("Could not load the default simulation, trying to create a new Simulation...", e);
                handleNewSimulation();
                result = true;
            }
        }
        
        return result;
    }

    /**
     * Initialize the simulation.
     * @param args the command arguments.
     * @return true if new simulation (not loaded)
     */
    boolean initializeNewSimulation() {
        boolean result = false;
        
            handleNewSimulation(); // if this fails we always exit, continuing is useless
            result = true;
        
        return result;
    }
    
    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void exitWithError(String message, Exception e) {
        showError(message, e);
        System.exit(1);
    }

    /**
     * Show a modal error message dialog.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void showError(String message, Exception e) {
        if (e != null) {
            logger.log(Level.SEVERE, message, e);
        }
        else {
            logger.log(Level.SEVERE, message);
        }
        
        if (useGUI) {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the simulation from the default save file.
     * @throws Exception if error loading the default saved simulation.
     */
    private void handleLoadDefaultSimulation() throws Exception {
        try {
            // Load a the default simulation
            Simulation.instance().loadSimulation(null);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load default simulation", e);
            throw e;
        }
    }

    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    private void handleLoadSimulation(List<String> argList) throws Exception {
        try {
            int index = argList.indexOf("-load");
            // Get the next argument as the filename.
            File loadFile = new File(argList.get(index + 1));
            if (loadFile.exists() && loadFile.canRead()) {
                Simulation.instance().loadSimulation(loadFile);
            } else {
                exitWithError("Problem loading simulation. " + argList.get(index + 1) + 
                        " not found.", null); 
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem loading existing simulation", e);
            throw e;
        }
    }

    /**
     * Create a new simulation instance.
     */
    void handleNewSimulation() {
        try {
            SimulationConfig.loadConfig();
            if (useGUI) {
                SimulationConfigEditor editor = new SimulationConfigEditor(null, 
                        SimulationConfig.instance());

         		// 2014-11-19 Displayed MSP Logo Icon as editor is loaded
    			editor.setIconImage(img);
                editor.setVisible(true);
            }
            Simulation.createNewSimulation();
        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Could not create a new simulation, startup cannot continue", e);
        }
    }

    /**
     * Start the simulation instance.
     */
    public void startSimulation() {
        // Start the simulation.
        Simulation.instance().start();
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