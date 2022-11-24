/*
 * Mars Simulation Project
 * BuildingWindow.java
 * @date 2022-07-09
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelFoodProduction;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelPreparingDessert;


/**
 * The BuildingWindow is the window for displaying a piece of building.
 */
@SuppressWarnings("serial")
public class BuildingWindow extends UnitWindow {
	
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param building the building this window is for.
     */
    public BuildingWindow(MainDesktopPane desktop, Building building) {
        // Use UnitWindow constructor
        super(desktop, building, building.getSettlement().getName() + " - " + building.getNickName(), false);

        // Add tab panels
        addTabPanel(new BuildingPanelMaintenance(building, desktop));
        addTabPanel(new BuildingPanelMalfunctionable(building, desktop));
		addTabPanel(new BuildingPanelPower(building, desktop));
        
        for (Function f : building.getFunctions()) {
        	switch (f.getFunctionType()) {

			case ASTRONOMICAL_OBSERVATION:
				addTabPanel( new BuildingPanelAstronomicalObservation(building.getAstronomicalObservation(), desktop));
				break;
			case COMPUTATION:
				addTabPanel( new BuildingPanelComputation(building.getComputation(), desktop));
				break;
			case COOKING:
	        	addTabPanel( new BuildingPanelCooking(building.getCooking(), desktop));	
				break;
			case PREPARING_DESSERT:
				addTabPanel( new BuildingPanelPreparingDessert(building.getPreparingDessert(), desktop));
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
	       	case LIVING_ACCOMMODATIONS:
            	addTabPanel( new BuildingPanelLiving(building.getLivingAccommodations(), desktop));
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

        }
        
        // Sort tab panels
        sortTabPanels();
        
        // Add general tab panel as the first panel
        addFirstPanel(new BuildingPanelGeneral(building, desktop));
        
		// Add to tab panels with icons 
        addTabIconPanels();
    }

    /**
     * Updates this window.
     */
	@Override
    public void update() {
        super.update();
    }
	
    @Override
	public void stateChanged(ChangeEvent e) {
    	// nothing
	}
}
