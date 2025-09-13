/*
 * Mars Simulation Project
 * MedicalCareSpec.java
 * @date 2025-09-07
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.Set;

/**
 * Defines the specification of a MedicalCare function. This includes the relative position of the beds
 */
public class MedicalCareSpec extends FunctionSpec {

    private Set<NamedPosition> beds;

    public MedicalCareSpec(FunctionSpec base, Set<NamedPosition> beds) {
        super(base);
        this.beds = beds;
    }

    /**
     * What beds are defined in the Medical Care function
     * @return
     */
    public Set<NamedPosition> getBeds() {
        return beds;
    }
    
}
