/**
 * Mars Simulation Project
 * BuildingPanel.java
 * @version 3.07 2014-11-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Cooking;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.MakingSoy;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.structure.building.function.ThermalStorage;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;

/**
 * The BuildingPanel class is a panel representing a settlement building.
 */
public class BuildingPanel
extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The name of the panel. */
	private String panelName;
	/** The building this panel is for. */
	private Building building;
	/** The function panels. */
	private List<BuildingFunctionPanel> functionPanels;

	//private String buildingType;
	

	/**
	 * Constructor
	 *
	 * @param panelName the name of the panel.
	 * @param building the building this panel is for.
	 * @param desktop the main desktop.
	 * @throws MalformedURLException 
	 */
	public BuildingPanel(String panelName, Building building, MainDesktopPane desktop) {
		super();

        // Initialize data members
        this.panelName = panelName;
        this.building = building;
        //buildingType = building.getBuildingType();

        this.functionPanels = new ArrayList<BuildingFunctionPanel>();
        
        // Set layout
        setLayout(new BorderLayout(0, 0));
        
        // Prepare function scroll panel.
        JScrollPane functionScrollPanel = new JScrollPane();
        //functionScrollPanel.setPreferredSize(new Dimension(200, 220));
        functionScrollPanel.setPreferredSize(new Dimension(270, 270));
        add(functionScrollPanel, BorderLayout.CENTER);
        
        // Prepare function list panel.
        JPanel functionListPanel = new JPanel();
        functionListPanel.setLayout(new BoxLayout(functionListPanel, BoxLayout.Y_AXIS));
        functionScrollPanel.setViewportView(functionListPanel);
        
        // 2014-11-04 Added SVG Image loading for the building
  	    Dimension expectedDimension = new Dimension(100, 100);
	        //GraphicsNode node = SVGMapUtil.getSVGGraphicsNode("building", buildingType); 
	    Settlement settlement = building.getBuildingManager().getSettlement();
	        // Conclusion: this panel is called only once per opening the unit window session.
	    SettlementMapPanel svgPanel = new SettlementMapPanel(settlement, building);
        
	    svgPanel.setPreferredSize(expectedDimension);
	    svgPanel.setMaximumSize(expectedDimension);
	    svgPanel.setMinimumSize(expectedDimension);
	        
	    Box box = new Box(BoxLayout.Y_AXIS);
	    box.add(Box.createVerticalGlue());
	    box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
	        // 2014-11-05 Added setBorder()
	    box.setBorder(BorderFactory.createLineBorder(Color.black));
	    box.add(svgPanel);
	    box.add(Box.createVerticalGlue());

	        functionListPanel.add(box);
	        
        // Prepare inhabitable panel if building has lifeSupport.
        if (building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
//        	try {
        		LifeSupport lifeSupport = (LifeSupport) building.getFunction(BuildingFunction.LIFE_SUPPORT);
            	BuildingFunctionPanel inhabitablePanel = new BuildingPanelInhabitable(lifeSupport, desktop);
            	functionPanels.add(inhabitablePanel);
            	functionListPanel.add(inhabitablePanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare manufacture panel if building has manufacturing.
        if (building.hasFunction(BuildingFunction.MANUFACTURE)) {
//        	try {
        		Manufacture workshop = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
        		BuildingFunctionPanel manufacturePanel = new BuildingPanelManufacture(workshop, desktop);
        		functionPanels.add(manufacturePanel);
        		functionListPanel.add(manufacturePanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare farming panel if building has farming.
        if (building.hasFunction(BuildingFunction.FARMING)) {
//        	try {
        		Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
            	BuildingFunctionPanel farmingPanel = new BuildingPanelFarming(farm, desktop);
            	functionPanels.add(farmingPanel);
            	functionListPanel.add(farmingPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
		// Prepare cooking panel if building has cooking.
		if (building.hasFunction(BuildingFunction.COOKING)) {
//			try {
				Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
				BuildingFunctionPanel cookingPanel = new BuildingPanelCooking(kitchen, desktop);
				functionPanels.add(cookingPanel);
				functionListPanel.add(cookingPanel);
//			}
//			catch (BuildingException e) {}
		}
        
		//2014-11-11 Added soy making function
		// Prepare cooking panel if building has soy making function.
		if (building.hasFunction(BuildingFunction.MAKINGSOY)) {
//			try {
				MakingSoy kitchen = (MakingSoy) building.getFunction(BuildingFunction.MAKINGSOY);
				BuildingFunctionPanel soyMakingPanel = new BuildingPanelMakingSoy(kitchen, desktop);
				functionPanels.add(soyMakingPanel);
				functionListPanel.add(soyMakingPanel);
//			}
//			catch (BuildingException e) {}
		}		
        // Prepare medical care panel if building has medical care.
        if (building.hasFunction(BuildingFunction.MEDICAL_CARE)) {
//        	try {
        		MedicalCare med = (MedicalCare) building.getFunction(BuildingFunction.MEDICAL_CARE);
            	BuildingFunctionPanel medicalCarePanel = new BuildingPanelMedicalCare(med, desktop);
            	functionPanels.add(medicalCarePanel);
            	functionListPanel.add(medicalCarePanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
		// Prepare vehicle maintenance panel if building has vehicle maintenance.
		if (building.hasFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE)) {
//			try {
				VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
				BuildingFunctionPanel vehicleMaintenancePanel = new BuildingPanelVehicleMaintenance(garage, desktop);
				functionPanels.add(vehicleMaintenancePanel);
				functionListPanel.add(vehicleMaintenancePanel);
//			}
//			catch (BuildingException e) {}
		}
        
        // Prepare research panel if building has research.
        if (building.hasFunction(BuildingFunction.RESEARCH)) {
//        	try {
        		Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
            	BuildingFunctionPanel researchPanel = new BuildingPanelResearch(lab, desktop);
            	functionPanels.add(researchPanel);
            	functionListPanel.add(researchPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        
        // Prepare Observation panel if building has Observatory.
        if (building.hasFunction(BuildingFunction.ASTRONOMICAL_OBSERVATIONS)) {
//        	try {
        		AstronomicalObservation observation = (AstronomicalObservation) building.getFunction(BuildingFunction.ASTRONOMICAL_OBSERVATIONS);
            	BuildingFunctionPanel observationPanel = new BuildingPanelAstronomicalObservation(observation, desktop);
            	functionPanels.add(observationPanel);
            	functionListPanel.add(observationPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare power panel.
        BuildingFunctionPanel powerPanel = new BuildingPanelPower(building, desktop);
        functionPanels.add(powerPanel);
        functionListPanel.add(powerPanel);
        
        // Prepare power storage panel if building has power storage.
        if (building.hasFunction(BuildingFunction.POWER_STORAGE)) {
//            try {
                PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
                BuildingFunctionPanel powerStoragePanel = new BuildingPanelPowerStorage(storage, desktop);
                functionPanels.add(powerStoragePanel);
                functionListPanel.add(powerStoragePanel);
//            }
//            catch (BuildingException e) {}
        }
        
        //2014-10-27 mkung: Modified Heating Panel
        if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
//          try {
		        BuildingFunctionPanel heatPanel = new BuildingPanelThermal(building, desktop);
		        functionPanels.add(heatPanel);
		        functionListPanel.add(heatPanel);
//        }
//      catch (BuildingException e) {}
  }
        //2014-10-17 mkung: Added Heating Storage 
        // Prepare heating storage panel if building has heating storage.
        if (building.hasFunction(BuildingFunction.THERMAL_STORAGE)) {
//            try {
                ThermalStorage storage = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
                BuildingFunctionPanel heatStoragePanel = new BuildingPanelThermalStorage(storage, desktop);
                functionPanels.add(heatStoragePanel);
                functionListPanel.add(heatStoragePanel);
//            }
//            catch (BuildingException e) {}
        }
        
        // Prepare resource processing panel if building has resource processes.
        if (building.hasFunction(BuildingFunction.RESOURCE_PROCESSING)) {
//        	try {
        		ResourceProcessing processor = (ResourceProcessing) building.getFunction(BuildingFunction.RESOURCE_PROCESSING);
            	BuildingFunctionPanel resourceProcessingPanel = new BuildingPanelResourceProcessing(processor, desktop);
            	functionPanels.add(resourceProcessingPanel);
            	functionListPanel.add(resourceProcessingPanel);
//        	}
//        	catch (BuildingException e) {}
        }
        
        // Prepare storage process panel if building has storage function.
        if (building.hasFunction(BuildingFunction.STORAGE)) {
//            try {
                Storage storage = (Storage) building.getFunction(BuildingFunction.STORAGE);
                BuildingFunctionPanel storagePanel = new BuildingPanelStorage(storage, desktop);
                functionPanels.add(storagePanel);
                functionListPanel.add(storagePanel);
//            }
//            catch (BuildingException e) {}
        }
        
        // Prepare malfunctionable panel.
        BuildingFunctionPanel malfunctionPanel = 
            new BuildingPanelMalfunctionable(building, desktop);
        functionPanels.add(malfunctionPanel);
        functionListPanel.add(malfunctionPanel);
        
        // Prepare maintenance panel.
        BuildingFunctionPanel maintenancePanel = 
            new BuildingPanelMaintenance(building, desktop);
        functionPanels.add(maintenancePanel);
        functionListPanel.add(maintenancePanel);
    }


	  
    /**
     * Gets the panel's name.
     * @return panel name
     */
    public String getPanelName() {
        return panelName;
    }
    
    /**
     * Gets the panel's building.
     * @return building
     */
    public Building getBuilding() {
        return building;
    }
    
    /**
     * Update this panel.
     */
    public void update() {
        // Update each building function panel.
        Iterator<BuildingFunctionPanel> i = functionPanels.iterator();
        while (i.hasNext()) i.next().update();
    }
}