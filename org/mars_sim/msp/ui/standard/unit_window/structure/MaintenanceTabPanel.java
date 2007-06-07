/**
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @version 2.81 2007-06-06
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

public class MaintenanceTabPanel extends TabPanel {

	private Settlement settlement;
	private List buildingsList;
	private JScrollPane maintenanceScrollPanel;
	private JPanel maintenanceListPanel;
	private JScrollPane malfunctionsScrollPanel;
	private JPanel malfunctionsListPanel;
	
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public MaintenanceTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Maint", null, "Maintenance", unit, desktop);
        
        settlement = (Settlement) unit;
        
        // Create topPanel.
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        centerContentPanel.add(topPanel);
        
        // Create maintenance panel.
        JPanel maintenancePanel = new JPanel(new BorderLayout());
        topPanel.add(maintenancePanel);
        
        // Create maintenance label.
        JLabel maintenanceLabel = new JLabel("Building Maintenance", JLabel.CENTER);
        maintenancePanel.add(maintenanceLabel, BorderLayout.NORTH);
        
		// Create scroll panel for maintenance list panel.
		maintenanceScrollPanel = new JScrollPane();
		maintenanceScrollPanel.setPreferredSize(new Dimension(200, 75));
		maintenancePanel.add(maintenanceScrollPanel, BorderLayout.CENTER);  
		
        // Prepare maintenance list panel.
        maintenanceListPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        maintenanceListPanel.setBorder(new MarsPanelBorder());
        maintenanceScrollPanel.setViewportView(maintenanceListPanel);
        populateMaintenanceList();
        
        // Create malfunctions panel.
        JPanel malfunctionsPanel = new JPanel(new BorderLayout());
        topPanel.add(malfunctionsPanel);
        
        // Create malfunctions label.
        JLabel malfunctionsLabel = new JLabel("Building Malfunctions", JLabel.CENTER);
        malfunctionsPanel.add(malfunctionsLabel, BorderLayout.NORTH);
        
		// Create scroll panel for malfunctions list panel.
        malfunctionsScrollPanel = new JScrollPane();
		malfunctionsScrollPanel.setPreferredSize(new Dimension(200, 75));
        malfunctionsPanel.add(malfunctionsScrollPanel, BorderLayout.CENTER);  
		
        // Prepare malfunctions list panel.
        malfunctionsListPanel = new JPanel(new GridLayout(0, 1, 5, 2));
        malfunctionsListPanel.setBorder(new MarsPanelBorder());
        malfunctionsScrollPanel.setViewportView(malfunctionsListPanel);
        // populateProcessList();
    }
    
    private void populateMaintenanceList() {
    	// Clear the list.
    	maintenanceListPanel.removeAll();
    	
    	buildingsList = settlement.getBuildingManager().getBuildings();
    	Iterator i = buildingsList.iterator();
    	while (i.hasNext()) {
    		Building building = (Building) i.next();
    		JPanel panel = new BuildingMaintenancePanel(building);
    		maintenanceListPanel.add(panel);
    	}
    }
	
	@Override
	public void update() {
		
		// Check if building list has changed.
		Settlement settlement = (Settlement) unit;
		List tempBuildings = settlement.getBuildingManager().getBuildings();
		if (!tempBuildings.equals(buildingsList)) {
			// Populate maintenance list.
			buildingsList = tempBuildings;
			populateMaintenanceList();
			maintenanceScrollPanel.validate();
		}
		else {
			// Update all building maintenance panels.
			Component[] components = maintenanceListPanel.getComponents();
			for (int x = 0; x < components.length; x++) {
				BuildingMaintenancePanel panel = (BuildingMaintenancePanel) components[x];
				panel.update();
			}
		}
	}
	
	private class BuildingMaintenancePanel extends JPanel {
		
		private MalfunctionManager manager;
		private int lastCompletedCache;
		private BoundedRangeModel progressBarModel;
		private JLabel lastLabel;
		
		BuildingMaintenancePanel(Building building) {
			// User JPanel constructor.
			super();
			
			manager = building.getMalfunctionManager();
			
			setLayout(new BorderLayout(0, 0));
			setBorder(new MarsPanelBorder());
			
			JLabel buildingLabel = new JLabel(building.getName(), JLabel.LEFT);
			add(buildingLabel, BorderLayout.NORTH);
			
			JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
			add(mainPanel, BorderLayout.CENTER);
			
			lastCompletedCache = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
			lastLabel = new JLabel("Last Completed: " + lastCompletedCache + " sols", JLabel.LEFT);
			mainPanel.add(lastLabel, BorderLayout.WEST);
			
			JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			mainPanel.add(progressBarPanel, BorderLayout.CENTER);
			
	        // Prepare progress bar.
	        JProgressBar progressBar = new JProgressBar();
	        progressBarModel = progressBar.getModel();
	        progressBar.setStringPainted(true);
	        progressBar.setPreferredSize(new Dimension(80, 15));
	        progressBarPanel.add(progressBar);
	        
	        // Set initial value for progress bar.
	        double completed = manager.getMaintenanceWorkTimeCompleted();
	        double total = manager.getMaintenanceWorkTime();
	        int percentDone = (int) (100D * (completed / total));
	        progressBarModel.setValue(percentDone);
		}
		
		/**
		 * Update this panel.
		 */
		private void update() {
			// Update progress bar.
			double completed = manager.getMaintenanceWorkTimeCompleted();
	        double total = manager.getMaintenanceWorkTime();
	        int percentDone = (int) (100D * (completed / total));
	        progressBarModel.setValue(percentDone);
	        
	        // Update last completed.
	        int lastCompleted = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
	        if (lastCompleted != lastCompletedCache) {
	        	lastCompletedCache = lastCompleted;
	        	lastLabel.setText("Last Completed: " + lastCompletedCache + " sols");
	        }
		}
	}
}