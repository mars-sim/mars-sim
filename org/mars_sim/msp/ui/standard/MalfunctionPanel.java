/**
 * Mars Simulation Project
 * MalfunctionPanel.java
 * @version 2.74 2002-05-14
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.malfunction.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * The MalfunctionPanel class displays info about a malfunction. 
 */
public class MalfunctionPanel extends JPanel {

    // Data members
    private Malfunction malfunction; // The malfunction.
    private JProgressBar emergencyBar; // Emergency repair progress.
    private JProgressBar normalBar; // Normal repair progress.
    private JProgressBar evaBar; // EVA repair progress.

    /** 
     * Constructs a MalfunctionPanel object.
     * @param prefixString the string to display before the malfunction name
     * @param malfunction the malfunction to display 
     */
    public MalfunctionPanel(String prefixString, Malfunction malfunction) {
        // Call JPanel constructor.
	super();

        // Initialize data members.
	this.malfunction = malfunction;
	
	// Set default font
	setFont(new Font("SansSerif", Font.BOLD, 12));

	// Set a border around the panel.
	setBorder(new CompoundBorder(new EtchedBorder(),
	        new EmptyBorder(5, 5, 5, 5)));
	
	// Set layout
	setLayout(new BorderLayout(0, 5));

	// Prepare name label.
	JLabel nameLabel = new JLabel(prefixString + malfunction.getName(), JLabel.LEFT);
	nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
	add(nameLabel, "North");

	// Prepare content pane.
	JPanel contentPane = new JPanel(new GridLayout(2, 3));
	add(contentPane, "Center");

	Font smallFont = new Font("SansSerif", Font.PLAIN, 8);

	// Prepare emergency label.
	JLabel emergencyLabel = new JLabel("Emergency", JLabel.CENTER);
	emergencyLabel.setFont(smallFont);
	contentPane.add(emergencyLabel);

	// Prepare normal label.
	JLabel normalLabel = new JLabel("Normal", JLabel.CENTER);
	normalLabel.setFont(smallFont);
	contentPane.add(normalLabel);

	// Prepare EVA label.
	JLabel evaLabel = new JLabel("EVA", JLabel.CENTER);
	evaLabel.setFont(smallFont);
	contentPane.add(evaLabel);

        Font progressFont = new Font("SansSerif", Font.PLAIN, 6);
	
	// Prepare emergency progress bar.
	emergencyBar = new JProgressBar();
	emergencyBar.setPreferredSize(new Dimension(20, 5));
	double emergencyWorkCompleted = malfunction.getCompletedEmergencyWorkTime();
	double emergencyWorkRequired = malfunction.getEmergencyWorkTime();
	int emergencyProgress = (int) (100D * emergencyWorkCompleted / emergencyWorkRequired);
	emergencyBar.setValue(emergencyProgress);
	if (emergencyWorkRequired == 0D) {
	    emergencyBar.setFont(progressFont);
	    emergencyBar.setString("n/a");
	    emergencyBar.setStringPainted(true);
	}
	contentPane.add(emergencyBar);

	// Prepare normal progress bar.
	normalBar = new JProgressBar();
	normalBar.setPreferredSize(new Dimension(20, 5));
	double normalWorkCompleted = malfunction.getCompletedWorkTime();
	double normalWorkRequired = malfunction.getWorkTime();
	int normalProgress = (int) (100D * normalWorkCompleted / normalWorkRequired);
	normalBar.setValue(normalProgress);
	if (normalWorkRequired == 0D) {
	    normalBar.setFont(progressFont);
	    normalBar.setString("n/a");
	    normalBar.setStringPainted(true);
	}
	contentPane.add(normalBar);

	// Prepare EVA progress bar.
	evaBar = new JProgressBar();
	evaBar.setPreferredSize(new Dimension(20, 5));
	double evaWorkCompleted = malfunction.getCompletedEVAWorkTime();
	double evaWorkRequired = malfunction.getEVAWorkTime();
	int evaProgress = (int) (100D * evaWorkCompleted / evaWorkRequired);
	evaBar.setValue(evaProgress);
	if (evaWorkRequired == 0D) {
	    evaBar.setFont(progressFont);
	    evaBar.setString("n/a");
	    evaBar.setStringPainted(true);
	}
	contentPane.add(evaBar);
    }

    /**
     * Updates the inventory panel's information.
     */
    public void updateInfo() {

        // Update emergency progress bar.
	double emergencyWorkCompleted = malfunction.getCompletedEmergencyWorkTime();
	double emergencyWorkRequired = malfunction.getEmergencyWorkTime();
	int emergencyProgress = (int) (100D * emergencyWorkCompleted / emergencyWorkRequired);
	emergencyBar.setValue(emergencyProgress);
	
	// Update normal progress bar.
	double normalWorkCompleted = malfunction.getCompletedWorkTime();
	double normalWorkRequired = malfunction.getWorkTime();
	int normalProgress = (int) (100D * normalWorkCompleted / normalWorkRequired);
	normalBar.setValue(normalProgress);

	// Update EVA progress bar.
	double evaWorkCompleted = malfunction.getCompletedEVAWorkTime();
	double evaWorkRequired = malfunction.getEVAWorkTime();
	int evaProgress = (int) (100D * evaWorkCompleted / evaWorkRequired);
	evaBar.setValue(evaProgress);
    }

    /**
     * Gets the malfunction.
     * @return malfunction
     */
    public Malfunction getMalfunction() {
        return malfunction;
    }
}
