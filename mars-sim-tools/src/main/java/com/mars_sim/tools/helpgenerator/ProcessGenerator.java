/*
 * Mars Simulation Project
 * ProcessGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.manufacture.ManufactureProcessInfo;

/**
 * Help file generator for manufacturing processes.
 */
class ProcessGenerator extends TypeGenerator<ManufactureProcessInfo> {

    public static final String TYPE_NAME = "process";

    ProcessGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Manufacturing Process",
                        "Manufacturing Processes that consume resoruce to create new resources.",
                        "manufacturing");

        // Groups according to first letter of name
        setGrouper("Name", r-> r.getName().substring(0, 1).toUpperCase());
    }

    /**
     * Add properties for the consuming and produced Resources. This will also use the process-input partial template
     * as well as the main process-detail template.
     * @param p Process for generation
     * @param output Destination for content
     */
    @Override
    protected void addEntityProperties(ManufactureProcessInfo p, Map<String,Object> scope) {
		addProcessInputOutput(scope, "Inputs", toQuantityItems(p.getInputList()),
									"Products", toQuantityItems(p.getOutputList()));
    }

    /**
     * Get all the configured manufacturing processes.
     */
    @Override
    protected List<ManufactureProcessInfo> getEntities() {
		return getParent().getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
    }

    @Override
    protected String getEntityName(ManufactureProcessInfo v) {
        return v.getName();
    }
}
