/*
 * Mars Simulation Project
 * BuildingGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;

import com.mars_sim.core.structure.building.BuildingSpec;

/**
 * Generators help files for a BuildingSpec showing the details of the Functions allocated

 */
public class BuildingGenerator extends TypeGenerator<BuildingSpec>{
    public static final String TYPE_NAME = "building";

    protected BuildingGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Building Spec",
                "Building Specifications available for bases");

        // Groups by category
        setGrouper("Category", r-> r.getCategory().getName());
    }
    
    /**
     * Gets a list of all the building specifications configured.
     */
    protected List<BuildingSpec> getEntities() {
        return getParent().getConfig().getBuildingConfiguration().getBuildingTypes()
                            .stream()
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    /**
     * Get the name from a BuildingSpec
     * @param v Building spec
     */
    @Override
    protected String getEntityName(BuildingSpec v) {
        return v.getName();
    }
}
