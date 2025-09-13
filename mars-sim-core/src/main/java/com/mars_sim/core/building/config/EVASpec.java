/*
 * Mars Simulation Project
 * EVASpec.java
 * @date 2025-09-07
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import com.mars_sim.core.configuration.RelativePosition;

/**
 * This defines the specification of an EVA Function.
 * In addition to the normal FunctionSpec it holds the relative position of the 
 * internal & external airlocks
 */
public class EVASpec extends FunctionSpec {

    private RelativePosition airlockLoc;
	private RelativePosition interiorLoc;
	private RelativePosition exteriorLoc;

    public EVASpec(FunctionSpec base, RelativePosition airlockLoc, RelativePosition interiorLoc,
            RelativePosition exteriorLoc) {
        super(base);
        this.airlockLoc = airlockLoc;
        this.interiorLoc = interiorLoc;
        this.exteriorLoc = exteriorLoc;
    }

    public RelativePosition getAirlockLoc() {
        return airlockLoc;
    }

    public RelativePosition getInteriorLoc() {
        return interiorLoc;
    }

    public RelativePosition getExteriorLoc() {
        return exteriorLoc;
    }    
}
