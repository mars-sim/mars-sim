/**
 * Mars Simulation Project
 * MaintenanceBuildingPanel.java
 * @version 2.75 2004-02-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

/**
 * The MaintenanceBuildingPanel class is a building function panel representing 
 * the maintenance state of a settlement building.
 */
public class MaintenanceBuildingPanel extends BuildingFunctionPanel {
    
    private Malfunctionable malfunctionable; // The malfunctionable building.
    private JLabel lastCompletedLabel; // The last completed label.
    private BoundedRangeModel progressBarModel; // The progress bar model.
    private int lastCompletedTime; // The time since last completed maintenance.
    
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
        setLayout(new GridLayout(3, 1, 0, 0));
        
        // Create maintenance label.
        JLabel maintenanceLabel = new JLabel("Maintenance", JLabel.CENTER);
        add(maintenanceLabel);
        
        // Create lastCompletedLabel.
        lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        lastCompletedLabel = new JLabel("Last Completed: " + lastCompletedTime + 
            " millisols", JLabel.CENTER);
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
            lastCompletedLabel.setText("Last Completed: " + lastCompletedTime + " millisols");
        }
        
        // Update progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);
    }
}
