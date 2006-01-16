/**
 * Mars Simulation Project
 * MarsProject.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */
package org.mars_sim.msp;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.ui.standard.MainWindow;
import org.mars_sim.msp.ui.standard.SplashWindow;

/** 
 * MarsProject is the main class for the application. It creates both
 * Mars and the user interface.
 */
public class MarsProject {

    /** 
     * Constructor
     * @param args command line arguments. 
     */
    public MarsProject(String args[]) {

        // Create a splash window
        SplashWindow splashWindow = new SplashWindow();
        
        // Create a simulation
        List argList = Arrays.asList(args);
        
        // If debug argument, put in debug mode.
        if (argList.contains("-debug")) setDebugMode();
        
        if (argList.contains("-new")) {
        	// If new argument, create new simulation.
        	try {
        		Simulation.createNewSimulation();
        	}
        	catch (Exception e) {
        		System.err.println("Problem creating new simulation " + e);
        		System.exit(0);
        	}
        }
        else if (argList.contains("-load")) {
        	// If load argument, load simulation from file.
        	try {
        		int index = argList.indexOf("-load");
        		// Get the next argument as the filename.
        		File loadFile = new File((String) argList.get(index + 1));
        		if (loadFile.exists()) Simulation.instance().loadSimulation(loadFile);
        		else {
        			System.err.println("Problem loading simulation.");
                    System.err.println(argList.get(index + 1) + " not found.");
                    System.exit(0);
        		}
        	}
        	catch (Exception e) {
        		System.err.println("Problem loading existing simulation " + e);
        		System.exit(0);
        	}
        }

        // Load a the default simulation
        if (argList.size() == 0) {
            try {
            	Simulation.instance().loadSimulation(null);
            }
            catch (Exception e) {
                System.err.println("Problem loading default simulation " + e);
                
                try {
                	// If error reading default saved file, create new simulation.
                	System.err.println("Creating new simulation");
					Simulation.createNewSimulation();
                }
				catch (Exception e2) {
					System.err.println("Problem creating new simulation " + e2);
					System.exit(0);
				}
            }
        }
        
        // Start the simulation.
        Simulation.instance().start();

        // Create the main desktop window.
        new MainWindow();
       
        // Dispose the splash window.
        splashWindow.dispose();
    }

    /** 
     * Set error output to a text file (for debugging) 
     */
    private void setDebugMode() {
        try {
            FileOutputStream errFileStream = new FileOutputStream("err.log");
            System.setErr(new PrintStream(errFileStream));
            System.err.println("Testing");
        } 
        catch (FileNotFoundException e) {
            System.err.println("err.log could not be opened");
        }
    }

    /** 
     * The starting method for the application
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new MarsProject(args);
    }
}