/**
 * Mars Simulation Project
 * MalfunctionPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Map;

/** 
 * The MalfunctionPanel class displays info about a malfunction. 
 */
public class MalfunctionPanel extends JPanel {

    // Data members
    private Malfunction malfunction; // The malfunction.
    private JLabel nameLabel; // The name label.
    private BoundedRangeModel repairBarModel; // The repair bar model.
    private JLabel partsLabel; // The repair parts label.
    
    /** 
     * Constructs a MalfunctionPanel object with a name prefex..
     * @param malfunction the malfunction to display 
     */
    public MalfunctionPanel(Malfunction malfunction) {
   
        // Call JPanel constructor.
        super();

        // Initialize data members.
        this.malfunction = malfunction;
    
        // Set layout
        setLayout(new GridLayout(3, 1, 0, 0));

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
        
        // Prepare repair parts label.
        partsLabel = new JLabel(getPartsString(), JLabel.CENTER);
        partsLabel.setPreferredSize(new Dimension(-1, -1));
        add(partsLabel);
        
        // Add tooltip.
        setToolTipText(getToolTipString());
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
        
        // Update parts label.
        partsLabel.setText(getPartsString());
    }
    
    /**
     * Gets the parts string.
     * @return string.
     */
    private String getPartsString() {
        StringBuilder buf = new StringBuilder("Parts: ");
    	
    	Map<Part, Integer> parts = malfunction.getRepairParts();
    	if (parts.size() > 0) {
    		Iterator<Part> i = parts.keySet().iterator();
    		while (i.hasNext()) {
    			Part part = i.next();
    			int number = parts.get(part);
                buf.append(number).append(" ").append(part.getName());
    			if (i.hasNext()) buf.append(", ");
    		}
    	}
    	else buf.append("none");
    	
    	return buf.toString();
    }

    /**
     * Gets the malfunction.
     *
     * @return malfunction
     */
    public Malfunction getMalfunction() {
        return malfunction;
    }
    
    /**
     * Creates multi-line tool tip text.
     */
    private String getToolTipString() {
        StringBuilder result = new StringBuilder("<html>");
        result.append(malfunction.getName()).append("<br>");
        result.append("General repair time: ").append((int) malfunction.getWorkTime()).append(" milliols<br>");
        result.append("EVA repair time: ").append((int) malfunction.getEVAWorkTime()).append(" milliols<br>");
        result.append("Emergency repair time: ").append((int) malfunction.getEmergencyWorkTime()).append(" milliols<br>");
        result.append("Repair ").append(getPartsString().toLowerCase());
    	result.append("</html>");
    	
    	return result.toString();
    }
}