/*
 * Mars Simulation Project
 * PartGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;

/**
 * Generator for the Part entity.
 */
class PartGenerator extends TypeGenerator<Part> {

    static final String TYPE_NAME = "part";

    protected PartGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Parts", "Parts used for repairs and processes");

        
        // Groups by part type
        setGrouper("Good Type", r-> r.getGoodType().getName());
    }

    /**
     * Generator an output for a specific Part. This will also use the process-flow partial template
     * as well as the main part-detail template.
     * @param p Part for generation
     * @param output Destination of content
     */
    @Override
    public void generateEntity(Part p, OutputStream output) throws IOException {
        var generator = getParent();

        var pScope = generator.createScopeMap("Part - " +p.getName());
        pScope.put("part", p);
        addProcessFlows(p.getName(), pScope);

        generator.generateContent("part-detail", pScope, output);

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
