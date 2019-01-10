/**
* Mars Simulation Project
 * Potting.java
 * @version 3.1.0 2016-06-27
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;

public class Potting extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	private static Logger logger = Logger.getLogger(Potting.class.getName());

    private static final FunctionType FUNCTION = FunctionType.FARMING;

//    private Inventory inv;
//    private Settlement settlement;
    private Building building;
	    
	public Potting(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        this.building = building;
//        this.inv = building.getSettlementInventory();
//        this.settlement = building.getSettlement();

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
 
	}


	@Override
	public double getMaintenanceTime() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void timePassing(double time) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getFullPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getPoweredDownPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}
