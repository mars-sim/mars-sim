/**
 * Mars Simulation Project
 * BeeGrowing.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function.farming;

import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.time.ClockPulse;

public class BeeGrowing extends Function {
	
	/** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	//private static final Logger logger = Logger.getLogger(BeeGrowing.class.getName());

    // The bigger the number, the more erratic (and the less frequent) the update
    private static final int TICKS_PER_UPDATE = 100; 
    
    private Farming farm;
    private BeeHive hive;

    public BeeGrowing(Farming farm, FunctionSpec spec) {
        // Use Function constructor.
        super(FunctionType.FARMING, spec, farm.getBuilding());
		
        this.farm = farm;      
        		
        hive = new BeeHive(this, "Honey Bee");
        
	}

    public Farming getFarming() {
    	return farm;
    }

	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
		    int millisols =  pulse.getMarsTime().getMillisolInt();
			if (millisols % TICKS_PER_UPDATE == 1) {	
				hive.timePassing(pulse);
			}
		}
		return valid;
	}
}
