/*
 * Mars Simulation Project
 * BuildingGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.building.BuildingSpec;

/**
 * Generators help files for a BuildingSpec showing the details of the Functions allocated

 */
public class BuildingGenerator extends TypeGenerator<BuildingSpec>{
    public static final String TYPE_NAME = "building";

    protected BuildingGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Building Spec",
                "Building Specifications available for bases",
                "buildings");

        // Groups by category
        setGrouper("Category", r-> r.getCategory().getName());
    }

    /**
     * Add link to associated construction stage
     * @param r Construction stage for generation
     * @param output Destination of the content
     */
    @Override
    protected void addEntityProperties(BuildingSpec r, Map<String,Object> scope) {
        var stage = getConfig().getConstructionConfiguration()
                        .getConstructionStageInfoByName(r.getName());
        if (stage != null) {
            scope.put("construction", stage);
        }
    }

    /**
     * Gets a list of all the building specifications configured.
     */
    protected List<BuildingSpec> getEntities() {
        return getConfig().getBuildingConfiguration().getBuildingSpecs()
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
