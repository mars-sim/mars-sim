/**
 * Mars Simulation Project
 * MalfunctionPanel.java
 * @version 2.75 2003-06-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/** 
 * The MalfunctionPanel class displays info about a malfunction. 
 */
public class MalfunctionPanel extends JPanel {

    // Data members
    private Malfunction malfunction; // The malfunction.
    private JLabel nameLabel; // The name label.
    private BoundedRangeModel repairBarModel; // The repair bar model.

    /** 
     * Constructs a MalfunctionPanel object.
     *
     * @param malfunction the malfunction to display 
     */
    public MalfunctionPanel(Malfunction malfunction) {
   
        // Call JPanel constructor.
        super();

        // Initialize data members.
        this.malfunction = malfunction;
    
        // Set layout
        setLayout(new GridLayout(2, 1, 0, 0));

        // Set border
        setBorder(new MarsPanelBorder());
        
        // Prepare name label.
        nameLabel = new JLabel(malfunction.getName(), JLabel.CENTER);
        if (malfunction.getCompletedEmergencyWorkTime() < malfunction.getEmergencyWorkTime()) {
            nameLabel.setText(malfunction.getName() + " - Emergency");
            nameLabel.setForeground(Color.red);
        }
        add(nameLabel);

        // Prepare repair pane.
        JPanel repairPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(repairPane);
    
        // Prepare repair progress bar.
        JProgressBar repairBar = new JProgressBar();
        repairBarModel = repairBar.getModel();
        repairBar.setStringPainted(true);
        repairPane.add(repairBar);
        
        // Set initial value for repair progress bar.
        double totalRequiredWork = malfunction.getEmergencyWorkTime() + malfunction.getWorkTime() 
            + malfunction.getEVAWorkTime();
        double totalCompletedWork = malfunction.getCompletedEmergencyWorkTime() + 
            malfunction.getCompletedWorkTime() + malfunction.getCompletedEVAWorkTime();
        int percentComplete = 0;
        if (totalRequiredWork > 0D) percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
        repairBarModel.setValue(percentComplete);
    }

    /**
     * Updates the panel's information.
     */
    public void update() {
        
        // Update name label.
        if (malfunction.getCompletedEmergencyWorkTime() < malfunction.getEmergencyWorkTime()) {
            nameLabel.setText(malfunction.getName() + " - Emergency");
            nameLabel.setForeground(Color.red);
        }
        else {
            nameLabel.setText(malfunction.getName());
            nameLabel.setForeground(Color.black);
        }
        
        // Update progress bar.
        double totalRequiredWork = malfunction.getEmergencyWorkTime() + malfunction.getWorkTime() 
            + malfunction.getEVAWorkTime();
        double totalCompletedWork = malfunction.getCompletedEmergencyWorkTime() + 
            malfunction.getCompletedWorkTime() + malfunction.getCompletedEVAWorkTime();
        int percentComplete = 0;
        if (totalRequiredWork > 0D) percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
        repairBarModel.setValue(percentComplete);
    }

    /**
     * Gets the malfunction.
     *
     * @return malfunction
     */
    public Malfunction getMalfunction() {
        return malfunction;
    }
}
