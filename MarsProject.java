/**
 * Mars Simulation Project
 * MarsProject.java
 * @version 2.73 2001-12-04
 * @author Scott Davis
 */

import org.mars_sim.msp.simulation.VirtualMars;
import org.mars_sim.msp.ui.standard.*;
import java.io.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import java.awt.*;

/** MarsProject is the main class for the application. It creates both
 *  virtual Mars and the user interface.
 */
public class MarsProject {

    /** Constructs a MarsProject object */
    public MarsProject(String args[]) {

        // create a splash window
        SplashWindow splashWindow = new SplashWindow();
        
        // create virtual mars
        boolean usage = false;
        VirtualMars mars = null;
        if (args.length == 1) {
            if (args[0].equals("-new")) {
                mars = new VirtualMars();
            }
            else {
                usage = true;
            }
        }

        else if (args.length == 2) {
            // Load a previous simulation
            if (args[0].equals("-load")) {
                File loadFile = new File(args[1]);
                try {
                    mars = VirtualMars.load(loadFile);
                }
                catch (Exception e) {
                    System.err.println("Problem loading existing simulation " +
                                        e);
                    return;
                }
            }
            else {
                usage = true;
            }
        }

        // Load a the default simulation
        else if (args.length == 0) {
            try {
                mars = VirtualMars.load(null);
            }
            catch (Exception e) {
                System.err.println("Problem loading default simulation " + e);
                return;
            }

            // If no default, then create a new one
            if (mars == null) {
                mars = new VirtualMars();
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
        mars.start();

        // create main desktop window
        MainWindow window = new MainWindow(mars);
       
        // dispose splash window
        splashWindow.dispose();
    }

    /** Set error output to a text file (for debugging) */
    private void setDebugMode() {
        try {
            FileOutputStream errFileStream = new FileOutputStream("err.txt");
            System.setErr(new PrintStream(errFileStream));
        } catch (FileNotFoundException e) {
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
