/**
 * Mars Simulation Project
 * BeeGrowing.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.ClockPulse;

public class BeeGrowing extends Function
implements Serializable {
	
	/** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	//private static final Logger logger = Logger.getLogger(BeeGrowing.class.getName());

    // The bigger the number, the more erratic (and the less frequent) the update
    private static final int TICKS_PER_UPDATE = 100; 
    
    private Farming farm;
    private BeeHive hive;

    public BeeGrowing(Farming farm) {
        // Use Function constructor.
        super(FunctionType.FARMING, farm.getBuilding());
		
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
