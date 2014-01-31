/**
 * Mars Simulation Project
 * BuildingPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.*;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The BuildingPanel class is a panel representing a settlement building.
 */
public class BuildingPanel extends JPanel {
    
    private String panelName; // The name of the panel.
    private Building building; // The building this panel is for.
    private List<BuildingFunctionPanel> functionPanels; // The function panels
    
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
        this.functionPanels = new ArrayList<BuildingFunctionPanel>();
        
        // Set layout
        setLayout(new BorderLayout(0, 0));
        
        // Prepare function scroll panel.
        JScrollPane functionScrollPanel = new JScrollPane();
        functionScrollPanel.setPreferredSize(new Dimension(200, 220));
        add(functionScrollPanel, BorderLayout.CENTER);
        
        // Prepare function list panel.
        JPanel functionListPanel = new JPanel();
        functionListPanel.setLayout(new BoxLayout(functionListPanel, BoxLayout.Y_AXIS));
        functionScrollPanel.setViewportView(functionListPanel);
        
        // Prepare inhabitable panel if building has lifesupport.
        if (building.hasFunction(LifeSupport.NAME)) {
//        	try {
        		LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
            	BuildingFunctionPanel inhabitablePanel = new InhabitableBuildingPanel(lifeSupport, desktop);
            	functionPanels.add(inhabitablePanel);
            	functionListPanel.add(inhabitablePanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare manufacture panel if building has manufacturing.
        if (building.hasFunction(Manufacture.NAME)) {
//        	try {
        		Manufacture workshop = (Manufacture) building.getFunction(Manufacture.NAME);
        		BuildingFunctionPanel manufacturePanel = new ManufactureBuildingPanel(workshop, desktop);
        		functionPanels.add(manufacturePanel);
        		functionListPanel.add(manufacturePanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare farming panel if building has farming.
        if (building.hasFunction(Farming.NAME)) {
//        	try {
        		Farming farm = (Farming) building.getFunction(Farming.NAME);
            	BuildingFunctionPanel farmingPanel = new FarmingBuildingPanel(farm, desktop);
            	functionPanels.add(farmingPanel);
            	functionListPanel.add(farmingPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
		// Prepare cooking panel if building has cooking.
		if (building.hasFunction(Cooking.NAME)) {
//			try {
				Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME);
				BuildingFunctionPanel cookingPanel = new CookingBuildingPanel(kitchen, desktop);
				functionPanels.add(cookingPanel);
				functionListPanel.add(cookingPanel);
//			}
//			catch (BuildingException e) {}
		}
        
        // Prepare medical care panel if building has medical care.
        if (building.hasFunction(MedicalCare.NAME)) {
//        	try {
        		MedicalCare med = (MedicalCare) building.getFunction(MedicalCare.NAME);
            	BuildingFunctionPanel medicalCarePanel = new MedicalCareBuildingPanel(med, desktop);
            	functionPanels.add(medicalCarePanel);
            	functionListPanel.add(medicalCarePanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
		// Prepare vehicle maintenance panel if building has vehicle maintenance.
		if (building.hasFunction(GroundVehicleMaintenance.NAME)) {
//			try {
				VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
				BuildingFunctionPanel vehicleMaintenancePanel = new VehicleMaintenanceBuildingPanel(garage, desktop);
				functionPanels.add(vehicleMaintenancePanel);
				functionListPanel.add(vehicleMaintenancePanel);
//			}
//			catch (BuildingException e) {}
		}
        
        // Prepare research panel if building has research.
        if (building.hasFunction(Research.NAME)) {
//        	try {
        		Research lab = (Research) building.getFunction(Research.NAME);
            	BuildingFunctionPanel researchPanel = new ResearchBuildingPanel(lab, desktop);
            	functionPanels.add(researchPanel);
            	functionListPanel.add(researchPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        
        // Prepare Observation panel if building has Observatory.
        if (building.hasFunction(AstronomicalObservation.NAME)) {
//        	try {
        		AstronomicalObservation observation = (AstronomicalObservation) building.getFunction(AstronomicalObservation.NAME);
            	BuildingFunctionPanel observationPanel = new AstronomicalObservationBuildingPanel(observation, desktop);
            	functionPanels.add(observationPanel);
            	functionListPanel.add(observationPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare power panel.
        BuildingFunctionPanel powerPanel = new PowerBuildingPanel(building, desktop);
        functionPanels.add(powerPanel);
        functionListPanel.add(powerPanel);
        
        // Prepare power storage panel if building has power storage.
        if (building.hasFunction(PowerStorage.NAME)) {
//            try {
                PowerStorage storage = (PowerStorage) building.getFunction(PowerStorage.NAME);
                BuildingFunctionPanel powerStoragePanel = new PowerStorageBuildingPanel(storage, desktop);
                functionPanels.add(powerStoragePanel);
                functionListPanel.add(powerStoragePanel);
//            }
//            catch (BuildingException e) {}
        }
        
        // Prepare resource processing panel if building has resource processes.
        if (building.hasFunction(ResourceProcessing.NAME)) {
//        	try {
        		ResourceProcessing processor = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
            	BuildingFunctionPanel resourceProcessingPanel = new ResourceProcessingBuildingPanel(processor, desktop);
            	functionPanels.add(resourceProcessingPanel);
            	functionListPanel.add(resourceProcessingPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare storage process panel if building has storage function.
        if (building.hasFunction(Storage.NAME)) {
//            try {
                Storage storage = (Storage) building.getFunction(Storage.NAME);
                BuildingFunctionPanel storagePanel = new StorageBuildingPanel(storage, desktop);
                functionPanels.add(storagePanel);
                functionListPanel.add(storagePanel);
//            }
//            catch (BuildingException e) {}
        }
        
        // Prepare malfunctionable panel.
        BuildingFunctionPanel malfunctionPanel = 
            new MalfunctionableBuildingPanel(building, desktop);
        functionPanels.add(malfunctionPanel);
        functionListPanel.add(malfunctionPanel);
        
        // Prepare maintenance panel.
        BuildingFunctionPanel maintenancePanel = 
            new MaintenanceBuildingPanel(building, desktop);
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
        Iterator<BuildingFunctionPanel> i = functionPanels.iterator();
        while (i.hasNext()) i.next().update();
    }
}