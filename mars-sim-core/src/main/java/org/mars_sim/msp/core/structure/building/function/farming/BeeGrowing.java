/**
 * Mars Simulation Project
 * BeeGrowing.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.time.MarsClock;

public class BeeGrowing extends Function
implements Serializable {
	
	/** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	//private static Logger logger = Logger.getLogger(BeeGrowing.class.getName());

    private static final FunctionType FUNCTION = FunctionType.FARMING;

    private static int tally; 
    // The bigger the number, the more erratic (and the less frequent) the update
    private static final int TICKS_PER_UPDATE = 50; 
    
//    private Inventory inv;
    private Building building;
    private Farming farm;
    private BeeHive hive;

    public BeeGrowing(Farming farm) {
        // Use Function constructor.
        super(FUNCTION, farm.getBuilding());
		
        this.farm = farm;      
        this.building = farm.getBuilding();		
//        this.inv = building.getSettlementInventory();
//        this.settlement = building.getSettlement();
        		
        hive = new BeeHive(this, "Honey Bee");
        
	}

    public Farming getFarming() {
    	return farm;
    }

	@Override
	public double getMaintenanceTime() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void timePassing(double time) {
		MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
	    int millisols =  clock.getMillisolInt();
		//System.out.println("millisols : " + millisols);
		
		if (millisols % 100 == 1) {	
			//System.out.println("millisols : " + millisols);
			hive.timePassing(time);
			//tally = 0;
		}
		
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
