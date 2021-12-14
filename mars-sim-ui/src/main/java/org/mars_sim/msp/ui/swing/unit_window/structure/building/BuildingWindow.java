/**
 * Mars Simulation Project
 * BuildingWindow.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.TabPanelActivity;
import org.mars_sim.msp.ui.swing.unit_window.person.TabPanelAttribute;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelFoodProduction;


/**
 * The BuildingWindow is the window for displaying a piece of building.
 */
@SuppressWarnings("serial")
public class BuildingWindow extends UnitWindow {

	// Data members
	/** The cache for the currently selected TabPanel. */
	private TabPanel oldTab;

    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param building the building this window is for.
     */
    public BuildingWindow(MainDesktopPane desktop, Building building) {
        // Use UnitWindow constructor
        super(desktop, building, false);

        // Add tab panels
        addTopPanel(new BuildingPanelGeneral(building, desktop));
        addTabPanel(new BuildingPanelMaintenance(building, desktop));
        addTabPanel(new BuildingPanelMalfunctionable(building, desktop));
		addTabPanel(new BuildingPanelPower(building, desktop));
        
        for (Function f : building.getFunctions()) {
        	TabPanel newTab = null;
        	switch (f.getFunctionType()) {
        	case LIVING_ACCOMMODATIONS:
            	newTab = new BuildingPanelLiving(building.getLivingAccommodations(), desktop);
            	break;
			case ASTRONOMICAL_OBSERVATION:
				newTab = new BuildingPanelAstronomicalObservation(building.getAstronomicalObservation(), desktop);
				break;
			case COMPUTATION:
				newTab = new BuildingPanelComputation(building.getComputation(), desktop);
				break;
			case COOKING:
	        	newTab = new BuildingPanelCooking(building.getCooking(), desktop);	
				break;
			case EVA:
				newTab = new BuildingPanelEVA(building.getEVA(), desktop);
				break;
			case FARMING:
	        	newTab = new BuildingPanelFarming(building.getFarming(), desktop);
				break;
			case FISHERY:
				newTab = new BuildingPanelFishery(building.getFishery(), desktop);
				break;
			case FOOD_PRODUCTION:
				newTab = new BuildingPanelFoodProduction(building.getFoodProduction(), desktop);
				break;
			case GROUND_VEHICLE_MAINTENANCE:
				newTab = new BuildingPanelVehicleMaintenance(building.getVehicleMaintenance(), desktop);
				break;
			case LIFE_SUPPORT:
				newTab = new BuildingPanelInhabitable(building.getLifeSupport(), desktop);
				break;
			case MANUFACTURE:
				newTab = new BuildingPanelManufacture(building.getManufacture(), desktop);
				break;
			case MEDICAL_CARE:
				newTab = new BuildingPanelMedicalCare(building.getMedical(), desktop);
				break;
			case POWER_STORAGE:
				newTab = new BuildingPanelPowerStorage(building.getPowerStorage(), desktop);
				break;
			case RESEARCH:
				newTab = new BuildingPanelResearch(building.getResearch(), desktop);
				break;
			case RESOURCE_PROCESSING:
				newTab = new BuildingPanelResourceProcessing(building.getResourceProcessing(), desktop);
				break;
			case STORAGE:
				newTab = new BuildingPanelStorage(building.getStorage(), desktop);
				break;
			case THERMAL_GENERATION:
				newTab = new BuildingPanelThermal(building.getThermalGeneration(), desktop);
				break;
			default:
				break;
        	}
        
        	if (newTab != null) {
        		addTabPanel(newTab);
        	}
        }
         
    	sortTabPanels();
    }

    @Override
	public void stateChanged(ChangeEvent e) {
		// SwingUtilities.updateComponentTreeUI(this);
		TabPanel newTab = getSelected();

		if (newTab != oldTab) {

			if (newTab instanceof TabPanelActivity) {
//				if (tabPanelActivity.isUIDone());
//				 	tabPanelActivity.initializeUI();
			} else if (newTab instanceof TabPanelAttribute) {
				
			}
		}
	}
}
