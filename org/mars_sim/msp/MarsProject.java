/**
 * Mars Simulation Project
 * MarsProject.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */
package org.mars_sim.msp;

import java.io.*;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.ui.standard.MainWindow;
import org.mars_sim.msp.ui.standard.SplashWindow;

/** 
 * MarsProject is the main class for the application. It creates both
 * Mars and the user interface.
 */
public class MarsProject {

    /** Constructs a MarsProject object */
    public MarsProject(String args[]) {

        // create a splash window
        SplashWindow splashWindow = new SplashWindow();
        
        // create Simulation
        boolean usage = false;
        if (args.length == 1) {
            if (args[0].equals("-new")) {
            	try {
            		Simulation.createNewSimulation();
            	}
            	catch (Exception e) {
            		System.err.println("Problem creating new simulation " + e);
            		System.exit(0);
            	}
            } 
            else usage = true;
        }

        else if (args.length == 2) {
            // Load a previous simulation
            if (args[0].equals("-load")) {
                File loadFile = new File(args[1]);
                if (loadFile.exists()) {
                    try {
                    	Simulation.instance().loadSimulation(loadFile);
                    }
                    catch (Exception e) {
                        System.err.println("Problem loading existing simulation " + e);
                        System.exit(0);
                    }
                }
                else {
                    System.err.println("Problem loading simulation.");
                    System.err.println(args[1] + " not found.");
                    System.exit(0);
                } 
            }
            else {
                usage = true;
            }
        }

        // Load a the default simulation
        else if (args.length == 0) {
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
        else {
            usage = true;
        }
        
        // Is the command line correct
        if (usage) {
            System.err.println("MarsProject [-new | -load file]");
            System.exit(0);
        }
        Simulation.instance().start();

        // create main desktop window
        MainWindow window = new MainWindow();
       
        // dispose splash window
        splashWindow.dispose();
    }

    /** Set error output to a text file (for debugging) */
    private void setDebugMode() {
        try {
            FileOutputStream errFileStream = new FileOutputStream("err.txt");
            System.setErr(new PrintStream(errFileStream));
        } 
        catch (FileNotFoundException e) {
            System.out.println("err.txt could not be opened");
        }
    }

    /** The starting method for the application
     *  @param args the command line arguments
     */
    public static void main(String args[]) {
        MarsProject marsSim = new MarsProject(args);
    }
}