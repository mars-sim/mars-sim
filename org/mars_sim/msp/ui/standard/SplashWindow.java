/**
 * Mars Simulation Project
 * SplashWindow.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import javax.swing.*;

/** The SplashWindow class is a splash screen shown when the project
 *  is loading. It's started as a new thread. It fails silently if the
 *  splash image file is not found.
 */
public class SplashWindow extends JWindow implements Runnable {

    // Constant data member
    private static final String splashFile = "images/SplashImage.jpg";

    /** Constructs a new SplashWindow object */
    public SplashWindow() {
        run();
    }

    /** Start thread */
    public void start() {
        Thread kicker = new Thread(this);
        kicker.start();
    }

    /** Load and display image window */
    public void run() {

        // Don't display until window is created.
        setVisible(false);

        // Set the background to black.
        setBackground(Color.black);

        // Create ImageIcon from SplashImage.jpg.
        ImageIcon splashIcon = new ImageIcon(splashFile);

        // Put image on label and add it to the splash window.
        JLabel splashLabel = new JLabel(splashIcon);
        getContentPane().add(splashLabel);

        // Pack the splash window to it's minimum size with the image.
        pack();

        // Sets root pane to double buffered.
        getRootPane().setDoubleBuffered(true);

        // Center the splash window on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(((screenSize.width - windowSize.width) / 2),
                ((screenSize.height - windowSize.height) / 2));

        // Display the splash window.
        setVisible(true);
    }

    /** for component testing 
     *  @param argv command line arguments
     */
    public static void main(String argv[]) {
        SplashWindow s = new SplashWindow();
    }
}
