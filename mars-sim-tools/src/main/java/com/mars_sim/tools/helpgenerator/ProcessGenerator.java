/*
 * Mars Simulation Project
 * ProcessGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.process.ProcessInfo;

/**
 * Help file generator for manufacturing processes.
 */
class ProcessGenerator extends TypeGenerator<ProcessInfo> {

    public static final String TYPE_NAME = "process";

    ProcessGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Manufacturing Process",
                        "Manufacturing Processes that consume resources to create new resources.",
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
    protected void addEntityProperties(ProcessInfo p, Map<String,Object> scope) {
		addProcessInputOutput(scope, "Inputs", toQuantityItems(p.getInputList()),
									"Products", toQuantityItems(p.getOutputList()));

        scope.put("process-type", (p instanceof ManufactureProcessInfo ? "Manufacture" : "Salvage"));
    }

    /**
     * Get all the configured manufacturing processes.
     */
    @Override
    protected List<ProcessInfo> getEntities() {
		var manuConfig = getParent().getConfig().getManufactureConfiguration();
        return Stream.concat(manuConfig.getManufactureProcessList().stream(),
                                manuConfig.getSalvageInfoList().stream())
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    @Override
    protected String getEntityName(ProcessInfo v) {
        return v.getName();
    }
}
