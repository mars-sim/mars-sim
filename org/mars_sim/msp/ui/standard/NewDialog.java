/**
 * Mars Simulation Project
 * $Id: NewDialog.java,v 1.4 2002-03-18 07:26:16 scud1 Exp $
 * @version 2.74
 * @author Jani Patokallio
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.SimulationProperties;

import java.awt.*;
import java.awt.event.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/** The NewDialog is an information window that is called from the
 *  "New The Mars Simulation Project" item in the MainWindowMenu.
 *  It provides information about the project, credit to contributors and the GPL license.
 */
public class NewDialog extends JDialog implements ActionListener {

    // Data members
    private JButton okButton, cancelButton;
    private JSlider peopleSlider, vehicleSlider, settlementSlider;
    private SimulationProperties p;
    private int result = JOptionPane.CANCEL_OPTION;

    /** Constructs an NewDialog object 
     *  @param mainWindow the main window
     */
    public NewDialog(SimulationProperties p, MainWindow mainWindow) {

        // Use JDialog constructor
        super(mainWindow, "Start New Simulation", true);
	this.p = p;

        // Create the main panel
        JPanel mainPane = new JPanel();
	mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.setBorder(new EmptyBorder(10, 20, 10, 20));
        setContentPane(mainPane);

	JLabel instructionLabel =
	    new JLabel("Configure the parameters of the new simulation:",
		       JLabel.CENTER);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	mainPane.add(instructionLabel);
	mainPane.add(Box.createRigidArea(new Dimension(0,15)));

	peopleSlider =
	    addSlider(mainPane, "People", 0, 150, p.getInitPeople(), 10, 5);
	settlementSlider =
	    addSlider(mainPane, "Settlements", 1, 15, p.getInitSettlements(),
		      1, 1);
	vehicleSlider =
	    addSlider(mainPane, "Vehicles", 0, 30, p.getInitVehicles(), 5, 1);

        // Create button panel
        JPanel buttonPane = new JPanel(new GridLayout(0,2));
        buttonPane.setBorder(new EmptyBorder(10, 10, 10, 10));
	mainPane.add(Box.createVerticalGlue());
        mainPane.add(buttonPane);

        // Create close button
        okButton = new JButton("Start!");
        okButton.addActionListener(this);
        buttonPane.add(okButton);

        // Create cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton);

        // Set the size of the window
        setSize(400, 400);

        // Center the window on the parent window.
        Point parentLocation = mainWindow.getLocation();
        int Xloc = (int) parentLocation.getX() + ((mainWindow.getWidth() - 350) / 2);
        int Yloc = (int) parentLocation.getY() + ((mainWindow.getHeight() - 400) / 2);
        setLocation(Xloc, Yloc);

        // Prevent the window from being resized by the user.
        setResizable(false);

        // Show the window
        setVisible(true);
    }

    private JSlider addSlider(JPanel mainPane, String name,
		      int min, int max, int start, int majorTickSpacing, int minorTickSpacing) {
	JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, start);
	slider.setMajorTickSpacing(majorTickSpacing);
	slider.setMinorTickSpacing(minorTickSpacing);
	slider.setSnapToTicks(true);
	slider.setPaintTicks(true);
	slider.setPaintLabels(true);
	slider.setName(name);
	slider.setBorder
	    (BorderFactory.createTitledBorder(name));
	mainPane.add(slider);
	return slider;
    }

    // Implementing ActionListener method
    public void actionPerformed(ActionEvent event) {
	if(event.getSource() == okButton) {
	    p.setInitPeople((int) peopleSlider.getValue());
	    p.setInitVehicles((int) vehicleSlider.getValue());
	    p.setInitSettlements((int) settlementSlider.getValue());
	    result = JOptionPane.OK_OPTION;
	}
        dispose();
    }

    public int getResult() {
	return result;
    }
}
