/**
 * Mars Simulation Project
 * MarsProject.java
 * @version 2.70 2000-08-31
 * @author Scott Davis
 */

import java.io.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import java.awt.*;

/** MarsProject is the main class for the application. It creates both
 *  virtual Mars and the user interface.
 */
public class MarsProject {

    public MarsProject() {

	// prepare custom UI color theme
	MetalLookAndFeel.setCurrentTheme(new MarsTheme());
		
	// create a splash window
	SplashWindow splashWindow = new SplashWindow();
		
	// create virtual mars
	VirtualMars mars = new VirtualMars();
		
	// create main desktop window
	MainWindow window = new MainWindow(mars);
		
	// dispose splash window
	splashWindow.dispose();
	splashWindow = null;
    }

    /** Set error output to a text file (for debugging). */
    public void setDebugMode() {
	try {
	    FileOutputStream errFileStream = new FileOutputStream("err.txt");
	    System.setErr(new PrintStream(errFileStream));
	}
	catch(FileNotFoundException e) {
	    System.out.println("err.txt could not be opened");
	}
    }

    /** The starting method for the application */
    public static void main(String args[]) {
	MarsProject marsSim = new MarsProject();
    }
}
