/*
 * Mars Simulation Project
 * ResourceProcessGenerator.java
 * @date 2025-03-01
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resourceprocess.ResourceProcessSpec;

/**
 * Help file generator for resource processes.
 */
class ResourceProcessGenerator extends TypeGenerator<ResourceProcessSpec> {

    public static final String TYPE_NAME = "resprocess";

    ResourceProcessGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Resource Process",
                        "Automate processes that consume resources to create new resources.",
                        "resource-process");
    }

    /**
     * Add properties for the consuming and produced Resources. This will also use the process-input partial template
     * as well as the main process-detail template.
     * @param p Process for generation
     * @param output Destination for content
     */
    @Override
    protected void addEntityProperties(ResourceProcessSpec p, Map<String,Object> scope) {
        scope.put("inputs", toItems(p, false));
        scope.put("outputs", toItems(p, true));
    }

    private record ResProcItem(String name, double rate, boolean flag, double min) {}

    private List<ResProcItem> toItems(ResourceProcessSpec p, boolean output) {
        var resources = output ? p.getOutputResources() : p.getInputResources();
        List<ResProcItem> results = new ArrayList<>(resources.size());

        for(var r : resources) {
            String name = ResourceUtil.findAmountResourceName(r);
            double rate = output ? p.getBaseOutputRate(r) : p.getBaseInputRate(r);
            double min = (output ? 0D : p.getMinimumInputs().getOrDefault(r, 0D));
            boolean flag = output ? p.isWasteOutputResource(r) : p.isAmbientInputResource(r);
            results.add(new ResProcItem(name, rate * 1000D, flag, min));
        }

        return results;
    }

    /**
     * Get all the configured resource processes.
     */
    @Override
    protected List<ResourceProcessSpec> getEntities() {
		var config = getParent().getConfig().getResourceProcessConfiguration();
        return config.getProcessSpecs().stream()
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    @Override
    protected String getEntityName(ResourceProcessSpec v) {
        return v.getName();
    }
}
