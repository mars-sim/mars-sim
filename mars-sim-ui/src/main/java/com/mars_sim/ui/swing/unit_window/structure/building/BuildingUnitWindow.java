/*
 * Mars Simulation Project
 * BuildingWindow.java
 * @date 2022-07-09
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.structure.building;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
import com.mars_sim.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;
import com.mars_sim.ui.swing.unit_window.structure.building.food.BuildingPanelFoodProduction;


/**
 * The BuildingWindow is the window for displaying a piece of building.
 */
@SuppressWarnings("serial")
public class BuildingUnitWindow extends UnitWindow {
	
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param building the building this window is for.
     */
    public BuildingUnitWindow(MainDesktopPane desktop, Building building) {
        // Use UnitWindow constructor
        super(desktop, building, building.getSettlement().getName() + " - " + building.getName(), false);

        // Add tab panels
        addTabPanel(new MaintenanceTabPanel(building, desktop));
        addTabPanel(new MalfunctionTabPanel(building, desktop));
		addTabPanel(new BuildingPanelPowerGen(building, desktop));
        
        for (Function f : building.getFunctions()) {
        	
        	try {
        		switch (f.getFunctionType()) {

    			case ALGAE_FARMING:
    				addTabPanel( new BuildingPanelAlgae(building.getAlgae(), desktop));
    				break;
    			case ASTRONOMICAL_OBSERVATION:
    				addTabPanel( new BuildingPanelAstronomicalObservation(building.getAstronomicalObservation(), desktop));
    				break;
    			case COMPUTATION:
    				addTabPanel( new BuildingPanelComputation(building.getComputation(), desktop));
    				break;
    			case COOKING:
    	        	addTabPanel( new BuildingPanelCooking(building.getCooking(), desktop));	
    				break;
    			case EVA:
    				addTabPanel( new BuildingPanelEVA(building.getEVA(), desktop));
    				break;
    			case FARMING:
    	        	addTabPanel( new BuildingPanelFarming(building.getFarming(), desktop));
    				break;
    			case FISHERY:
    				addTabPanel( new BuildingPanelFishery(building.getFishery(), desktop));
    				break;
    			case FOOD_PRODUCTION:
    				addTabPanel( new BuildingPanelFoodProduction(building.getFoodProduction(), desktop));
    				break;
    			case VEHICLE_MAINTENANCE:
    				addTabPanel( new BuildingPanelVehicleMaintenance(building.getVehicleMaintenance(), desktop));
    				break;
    			case LIFE_SUPPORT:
    				addTabPanel( new BuildingPanelInhabitable(building.getLifeSupport(), desktop));
    				break;
    	       	case LIVING_ACCOMMODATION:
                	addTabPanel( new BuildingPanelAccommodation(building.getLivingAccommodation(), desktop));
                	break;
    			case MANUFACTURE:
    				addTabPanel( new BuildingPanelManufacture(building.getManufacture(), desktop));
    				break;
    			case MEDICAL_CARE:
    				addTabPanel( new BuildingPanelMedicalCare(building.getMedical(), desktop));
    				break;
    			case POWER_STORAGE:
    				addTabPanel( new BuildingPanelPowerStorage(building.getPowerStorage(), desktop));
    				break;
    			case RESEARCH:
    				addTabPanel( new BuildingPanelResearch(building.getResearch(), desktop));
    				break;
    			case RESOURCE_PROCESSING:
    				addTabPanel( new BuildingPanelResourceProcessing(building.getResourceProcessing(), desktop));
    				break;
    			case STORAGE:
    				addTabPanel( new BuildingPanelStorage(building.getStorage(), desktop));
    				break;
    			case THERMAL_GENERATION:
    				addTabPanel(new BuildingPanelThermal(building.getThermalGeneration(), desktop));
    				break;
    			case WASTE_PROCESSING:
    				addTabPanel( new BuildingPanelWasteProcessing(building.getWasteProcessing(), desktop));
    				break;
    			default:
    				break;
            	}
        	} catch (Exception e) {
				throw new IllegalStateException("building function exception " + e);
        	}
        }
        
        // Sort tab panels
        sortTabPanels();
        
        // Add general tab panel as the first panel
        addFirstPanel(new BuildingPanelGeneral(building, desktop));
        
		// Add to tab panels with icons 
        addTabIconPanels();
    }
}
