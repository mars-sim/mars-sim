/*
 * Mars Simulation Project
 * PartGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;

/**
 * Generator for the Part entity.
 */
class PartGenerator extends TypeGenerator<Part> {

    static final String TYPE_NAME = "part";

    protected PartGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Part", "Parts used for repairs and processes",
        "parts");

        
        // Groups by part type
        setGrouper("Good Type", r-> r.getGoodType().getName());
    }

    /**
     * Add consumer and processes processes of this Part. This will also use the process-flow partial template
     * as well as the main part-detail template.
     * @param p The entity to display
     * @param scope Scope of the properties to use for the template
     */
    @Override
    protected void addEntityProperties(Part p, Map<String,Object> scope) {
        addProcessFlows(p.getName(), scope);
    }

    /**
     * Get a list of all Part entities.
     */
    @Override
    protected List<Part> getEntities() {
        return ItemResourceUtil.getItemResources().stream()
                    .sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
                    .toList();
    }

    @Override
    protected String getEntityName(Part v) {
        return v.getName();
    }
}
