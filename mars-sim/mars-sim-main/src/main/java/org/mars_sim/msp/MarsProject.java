/**
 * Mars Simulation Project 
 * MarsProject.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.SplashWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * MarsProject is the main class for the application. It creates both Mars and
 * the user interface.
 */
public class MarsProject {

    private static String CLASS_NAME = "org.mars_sim.msp.MarsProject";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor
     *
     * @param args command line arguments.
     */
    public MarsProject(String args[]) {

        logger.info("Starting Mars Simulation");

        // Create a splash window
        SplashWindow splashWindow = new SplashWindow();
        splashWindow.show();

        initializeSimulation(args, splashWindow);

        // Create the main desktop window.
        MainWindow w = new MainWindow();
        w.show();
     
        // Start simulation
        startSimulation();
        
        // Dispose the splash window.
        splashWindow.hide();
    }

    private void initializeSimulation(String[] args, SplashWindow splashWindow) {
        // Create a simulation
        List<String> argList = Arrays.asList(args);

        if (argList.contains("-new")) {
            // If new argument, create new simulation.
            handleNewSimulation(); // if this fails we always exit, continuing is useless


        } else if (argList.contains("-load")) {
            // If load argument, load simulation from file.
            try {
                handleLoadSimulation(argList);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load the desired simulation, trying to create new...", e);
                showError(splashWindow.getJFrame(), "Could not load the desired simulation, trying to create a new Simulation...");
                handleNewSimulation();
            }
        } else {
            try {
                handleLoadDefaultSimulation();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load the default simulation, trying to create new...", e);
                showError(splashWindow.getJFrame(), "Could not load the default simulation, trying to create a new Simulation...");
                handleNewSimulation();
            }
        }
    }


    private void exitWithError(Component dialogParent, String message) {
        showError(dialogParent, message);
        System.exit(1);
    }

    private void showError(Component dialogParent, String message) {
        JOptionPane.showMessageDialog(dialogParent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void handleLoadDefaultSimulation() throws Exception {
        try {
            // Load a the default simulation
            Simulation.instance().loadSimulation(null);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load default simulation", e);
            throw e;
        }
    }

    private void handleLoadSimulation(List<String> argList) throws Exception {
        try {
            int index = argList.indexOf("-load");
            // Get the next argument as the filename.
            File loadFile = new File(argList.get(index + 1));
            if (loadFile.exists() && loadFile.canRead()) {
                Simulation.instance().loadSimulation(loadFile);
            } else {
                logger.log(Level.SEVERE, "Problem loading simulation");
                logger.log(Level.SEVERE, argList.get(index + 1) + " not found.");
                System.exit(0);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem loading existing simulation", e);
            throw e;
        }
    }

    private void handleNewSimulation() {
        try {
            SimulationConfig.loadConfig();
            Simulation.createNewSimulation();
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Could not create a new simulation", e);
            exitWithError(null, "Could not create a new simulation, startup cannot continue");
        }
    }

    public void startSimulation() {
        // Start the simulation.
        Simulation.instance().start();
    }

    /**
     * The starting method for the application
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* [landrus, 27.11.09]: Read the logging configuration from the classloader, so that this gets
           * webstart compatible. Also create the logs dir in user.home */
        new File(System.getProperty("user.home"), "mars-sim" + File.separator + "logs").mkdirs();

        try {
            LogManager.getLogManager().readConfiguration(MarsProject.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load logging properties", e);
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Could read logging default config", e);
            }
        }
        
        // starting the simulation
        System.setProperty("swing.aatext", "true"); // general text antialiasing

        new MarsProject(args);
    }
}