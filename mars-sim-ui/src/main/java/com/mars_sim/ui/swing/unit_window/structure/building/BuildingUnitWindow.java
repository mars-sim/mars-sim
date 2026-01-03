/*
 * Mars Simulation Project
 * BuildingWindow.java
 * @date 2022-07-09
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.structure.building;

import java.util.Properties;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;
import com.mars_sim.ui.swing.unit_window.structure.building.food.BuildingPanelFoodProduction;


/**
 * The BuildingWindow is the window for displaying a piece of building.
 */
@SuppressWarnings("serial")
public class BuildingUnitWindow extends EntityContentPanel<Building> {
	
    /**
     * Constructor
     *
     * @param building the building
	 * @param context the UI context
	 * @param props initial properties
     */
    public BuildingUnitWindow(Building building, UIContext context, Properties props) {
        super(building, context);

        // Add tab panels
        addTabPanel(new BuildingPanelGeneral(building, context));
        addTabPanel(new MaintenanceTabPanel(building, context));
        addTabPanel(new MalfunctionTabPanel(building, context));
		addTabPanel(new BuildingPanelPowerGen(building, context));
        
        for (Function f : building.getFunctions()) {
        	switch (f.getFunctionType()) {

    			case ALGAE_FARMING:
    				addTabPanel( new BuildingPanelAlgae(building.getAlgae(), context));
    				break;
    			case ASTRONOMICAL_OBSERVATION:
    				addTabPanel( new BuildingPanelAstronomicalObservation(building.getAstronomicalObservation(), context));
    				break;
    			case COMPUTATION:
    				addTabPanel( new BuildingPanelComputation(building.getComputation(), context));
    				break;
    			case COOKING:
    	        	addTabPanel( new BuildingPanelCooking(building.getCooking(), context));	
    				break;
    			case EVA:
    				addTabPanel( new BuildingPanelEVA(building.getEVA(), context));
    				break;
    			case FARMING:
    	        	addTabPanel( new BuildingPanelFarming(building.getFarming(), context));
    				break;
    			case FISHERY:
    				addTabPanel( new BuildingPanelFishery(building.getFishery(), context));
    				break;
    			case FOOD_PRODUCTION:
    				addTabPanel( new BuildingPanelFoodProduction(building.getFoodProduction(), context));
    				break;
    			case VEHICLE_MAINTENANCE:
    				addTabPanel( new BuildingPanelVehicleMaintenance(building.getVehicleMaintenance(), context));
    				break;
    			case LIFE_SUPPORT:
    				addTabPanel( new BuildingPanelInhabitable(building.getLifeSupport(), context));
    				break;
    	     	case LIVING_ACCOMMODATION:
                	addTabPanel( new BuildingPanelAccommodation(building.getLivingAccommodation(), context));
                	break;
    			case MANUFACTURE:
    				addTabPanel( new BuildingPanelManufacture(building.getManufacture(), context));
    				break;
    			case MEDICAL_CARE:
    				addTabPanel( new BuildingPanelMedicalCare(building.getMedical(), context));
    				break;
    			case POWER_STORAGE:
    				addTabPanel( new BuildingPanelPowerStorage(building.getPowerStorage(), context));
    				break;
    			case RESEARCH:
    				addTabPanel( new BuildingPanelResearch(building.getResearch(), context));
    				break;
    			case RESOURCE_PROCESSING:
    				addTabPanel( new BuildingPanelResourceProcessing(building.getResourceProcessing(), context));
    				break;
    			case STORAGE:
    				addTabPanel( new BuildingPanelStorage(building.getStorage(), context));
    				break;
    			case THERMAL_GENERATION:
    				addTabPanel(new BuildingPanelThermal(building.getThermalGeneration(), context));
    				break;
    			case WASTE_PROCESSING:
    				addTabPanel( new BuildingPanelWasteProcessing(building.getWasteProcessing(), context));
    				break;
    	 		default:
    	 			break;
            }
        }
        
		applyProps(props);
    }
}
