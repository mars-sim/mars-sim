/**
* Mars Simulation Project
 * Potting.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

public class Potting extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
	    
	public Potting(Building building) {
        // Use Function constructor.
        super(FunctionType.FARMING, building);

	}

}
