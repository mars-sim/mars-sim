/**
 * Mars Simulation Project
 * BuildingPanel.java
 * @version 2.77 2004-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

/**
 * The BuildingPanel class is a panel representing a settlement building.
 */
public class BuildingPanel extends JPanel {
    
    private String panelName; // The name of the panel.
    private Building building; // The building this panel is for.
    private java.util.List functionPanels; // The function panels
    
    /**
     * Constructor
     *
     * @param panelName the name of the panel.
     * @param building the building this panel is for.
     * @param desktop the main desktop.
     */
    public BuildingPanel(String panelName, Building building, MainDesktopPane desktop) {
        super();
        
        // Initialize data members
        this.panelName = panelName;
        this.building = building;
        this.functionPanels = new ArrayList();
        
        // Set layout
        setLayout(new BorderLayout(0, 0));
        
        // Prepare function scroll panel.
        JScrollPane functionScrollPanel = new JScrollPane();
        functionScrollPanel.setPreferredSize(new Dimension(200, 220));
        add(functionScrollPanel, BorderLayout.NORTH);
        
        // Prepare function list panel.
        JPanel functionListPanel = new JPanel();
        functionListPanel.setLayout(new BoxLayout(functionListPanel, BoxLayout.Y_AXIS));
        functionScrollPanel.setViewportView(functionListPanel);
        
        // Prepare inhabitable panel if building has lifesupport.
        if (building.hasFunction(LifeSupport.NAME)) {
        	try {
        		LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
            	BuildingFunctionPanel inhabitablePanel = new InhabitableBuildingPanel(lifeSupport, desktop);
            	functionPanels.add(inhabitablePanel);
            	functionListPanel.add(inhabitablePanel);
        	}
        	catch (BuildingException e) {}
        }
        
        // Prepare farming panel if building has farming.
        if (building.hasFunction(Farming.NAME)) {
        	try {
        		Farming farm = (Farming) building.getFunction(Farming.NAME);
            	BuildingFunctionPanel farmingPanel = new FarmingBuildingPanel(farm, desktop);
            	functionPanels.add(farmingPanel);
            	functionListPanel.add(farmingPanel);
        	}
        	catch (BuildingException e) {}
        }
        
		// Prepare cooking panel if building has cooking.
		if (building.hasFunction(Cooking.NAME)) {
			try {
				Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME);
				BuildingFunctionPanel cookingPanel = new CookingBuildingPanel(kitchen, desktop);
				functionPanels.add(cookingPanel);
				functionListPanel.add(cookingPanel);
			}
			catch (BuildingException e) {}
		}
        
        // Prepare medical care panel if building has medical care.
        if (building.hasFunction(MedicalCare.NAME)) {
        	try {
        		MedicalCare med = (MedicalCare) building.getFunction(MedicalCare.NAME);
            	BuildingFunctionPanel medicalCarePanel = new MedicalCareBuildingPanel(med, desktop);
            	functionPanels.add(medicalCarePanel);
            	functionListPanel.add(medicalCarePanel);
        	}
        	catch (BuildingException e) {}
        }
        
		// Prepare vehicle maintenance panel if building has vehicle maintenance.
		if (building.hasFunction(GroundVehicleMaintenance.NAME)) {
			try {
				VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
				BuildingFunctionPanel vehicleMaintenancePanel = new VehicleMaintenanceBuildingPanel(garage, desktop);
				functionPanels.add(vehicleMaintenancePanel);
				functionListPanel.add(vehicleMaintenancePanel);
			}
			catch (BuildingException e) {}
		}
        
        // Prepare research panel if building has research.
        if (building.hasFunction(Research.NAME)) {
        	try {
        		Research lab = (Research) building.getFunction(Research.NAME);
            	BuildingFunctionPanel researchPanel = new ResearchBuildingPanel(lab, desktop);
            	functionPanels.add(researchPanel);
            	functionListPanel.add(researchPanel);
        	}
        	catch (BuildingException e) {}
        }
        
        // Prepare power panel.
        BuildingFunctionPanel powerPanel = new PowerBuildingPanel(building, desktop);
        functionPanels.add(powerPanel);
        functionListPanel.add(powerPanel);
        
        // Prepare resource processing panel if building has resource processes.
        if (building.hasFunction(ResourceProcessing.NAME)) {
        	try {
        		ResourceProcessing processor = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
            	BuildingFunctionPanel resourceProcessingPanel = new ResourceProcessingBuildingPanel(processor, desktop);
            	functionPanels.add(resourceProcessingPanel);
            	functionListPanel.add(resourceProcessingPanel);
        	}
        	catch (BuildingException e) {}
        }
        
        // Prepare malfunctionable panel.
        BuildingFunctionPanel malfunctionPanel = 
            new MalfunctionableBuildingPanel((Malfunctionable) building, desktop);
        functionPanels.add(malfunctionPanel);
        functionListPanel.add(malfunctionPanel);
        
        // Prepare maintenance panel.
        BuildingFunctionPanel maintenancePanel = 
            new MaintenanceBuildingPanel((Malfunctionable) building, desktop);
        functionPanels.add(maintenancePanel);
        functionListPanel.add(maintenancePanel);
    }
    
    /**
     * Gets the panel's name.
     *
     * @return panel name
     */
    public String getPanelName() {
        return panelName;
    }
    
    /**
     * Gets the panel's building.
     *
     * @return building
     */
    public Building getBuilding() {
        return building;
    }
    
    /**
     * Update this panel
     */
    public void update() {
    
        // Update each building function panel.
        Iterator i = functionPanels.iterator();
        while (i.hasNext()) {
            BuildingFunctionPanel panel = (BuildingFunctionPanel) i.next();
            panel.update();
        }
    }
}
        
    
