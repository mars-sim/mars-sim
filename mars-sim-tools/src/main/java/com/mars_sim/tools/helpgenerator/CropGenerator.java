/*
 * Mars Simulation Project
 * CropGenerator.java
 * @date 2025-01-25
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;

import com.mars_sim.core.building.function.farming.CropSpec;

/**
 * The CropGenerator class is a type of generator for crops.
 */
public class CropGenerator extends TypeGenerator<CropSpec> {
    public static final String TYPE_NAME = "crop";

    protected CropGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Crop",
                "Crops are the plants that can be grown in the Greenhouse",
                "crops");

        // Groups by category
        setGrouperByKey("Category", r-> new GroupKey(r.getCropCategory().getName(),
                                                        r.getCropCategory().getDescription()));

    }

    /**
     * Get a list of all the predefined Crops configured.
     * @return sorted list of crops
     */
    protected List<CropSpec> getEntities() {
        return getConfig().getCropConfiguration().getCropTypes()
                            .stream()
                            .sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
                            .toList();
    }

    @Override
    protected String getEntityName(CropSpec v) {
        return v.getName();
    }
}
