/**
 * Mars Simulation Project
 * $Id: NewDialog.java,v 1.6 2004-04-13 01:48:18 scud1 Exp $
 * @version 2.75 2004-04-11
 * @author Jani Patokallio
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** 
 * The NewDialog is an information window that is called from the
 * "New The Mars Simulation Project" item in the MainWindowMenu.
 */
public class NewDialog extends JFrame {

    /** 
     * Constructs an NewDialog object 
     * @param mainWindow the main window
     */
    public NewDialog(MainWindow mainWindow) {

        // Use JDialog constructor
        super();

        // Create the main panel
        JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(10, 20, 10, 20));
        setContentPane(mainPane);

		JLabel instructionLabel = new JLabel("Starting new simulation...", JLabel.CENTER);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(instructionLabel, BorderLayout.CENTER);

        // Set the size of the window
        setSize(200, 100);

        // Center the window on the parent window.
        Point parentLocation = mainWindow.getLocation();
        int Xloc = (int) parentLocation.getX() + ((mainWindow.getWidth() - 200) / 2);
        int Yloc = (int) parentLocation.getY() + ((mainWindow.getHeight() - 100) / 2);
        setLocation(Xloc, Yloc);

        // Prevent the window from being resized by the user.
        // setResizable(false);

        // Show the window
        setVisible(true);
    }
}