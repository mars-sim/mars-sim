/**
 * Mars Simulation Project
 * MaintenanceBuildingPanel.java
 * @version 2.84 2008-05-30
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The MaintenanceBuildingPanel class is a building function panel representing 
 * the maintenance state of a settlement building.
 */
public class MaintenanceBuildingPanel extends BuildingFunctionPanel {
    
    private Malfunctionable malfunctionable; // The malfunctionable building.
    private JLabel lastCompletedLabel; // The last completed label.
    private BoundedRangeModel progressBarModel; // The progress bar model.
    private int lastCompletedTime; // The time since last completed maintenance.
    private JLabel partsLabel; // Label for parts.
    
    /**
     * Constructor
     *
     * @param malfunctionable the malfunctionable building the panel is for.
     * @param desktop The main desktop.
     */
    public MaintenanceBuildingPanel(Malfunctionable malfunctionable, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super((Building) malfunctionable, desktop);
        
        // Initialize data members.
        this.malfunctionable = malfunctionable;
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();
        
        // Set the layout
        setLayout(new GridLayout(4, 1, 0, 0));
        
        // Create maintenance label.
        JLabel maintenanceLabel = new JLabel("Maintenance", JLabel.CENTER);
        add(maintenanceLabel);
        
        // Create lastCompletedLabel.
        lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        lastCompletedLabel = new JLabel("Last Completed: " + lastCompletedTime + 
            " sols", JLabel.CENTER);
        add(lastCompletedLabel);
        
        // Create maintenance progress bar panel.
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(progressPanel);
    
        // Prepare progress bar.
        JProgressBar progressBar = new JProgressBar();
        progressBarModel = progressBar.getModel();
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        
        // Set initial value for progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);
        
        // Prepare maintenance parts label.
        partsLabel = new JLabel(getPartsString(), JLabel.CENTER);
        partsLabel.setPreferredSize(new Dimension(-1, -1));
        add(partsLabel);
        
        // Add tooltip.
        setToolTipText(getToolTipString());
    }
    
    /**
     * Update this panel
     */
    public void update() {
    
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();
    
        // Update last completed label.
        int lastComplete = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        if (lastComplete != lastCompletedTime) {
            lastCompletedTime = lastComplete;
            lastCompletedLabel.setText("Last Completed: " + lastCompletedTime + " sols");
        }
        
        // Update progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);
        
        // Update parts label.
        partsLabel.setText(getPartsString());
    }
    
    /**
     * Gets the parts string.
     * @return string.
     */
    private String getPartsString() {
    	StringBuffer buf = new StringBuffer("Parts: ");
    	
    	Map<Part, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
    	if (parts.size() > 0) {
    		Iterator<Part> i = parts.keySet().iterator();
    		while (i.hasNext()) {
    			Part part = i.next();
    			int number = parts.get(part);
    			buf.append(number + " " + part.getName());
    			if (i.hasNext()) buf.append(", ");
    		}
    	}
    	else buf.append("none");
    	
    	return buf.toString();
    }
    
    /**
     * Creates multi-line tool tip text.
     */
    private String getToolTipString() {
    	MalfunctionManager manager = malfunctionable.getMalfunctionManager();
    	StringBuffer result = new StringBuffer("<html>");
    	int maintSols = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
    	result.append("Last completed maintenance: " + maintSols + " sols<br>");
    	result.append("Repair " + getPartsString().toLowerCase());
    	result.append("</html>");
    	
    	return result.toString();
    }
}