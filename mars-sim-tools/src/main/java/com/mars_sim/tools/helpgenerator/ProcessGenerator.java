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
import com.mars_sim.core.manufacture.WorkshopProcessInfo;

/**
 * Help file generator for manufacturing processes.
 */
class ProcessGenerator extends TypeGenerator<WorkshopProcessInfo> {

    public static final String TYPE_NAME = "process";
    private static final GroupKey NO_TOOLING = new GroupKey("None", "No extra tooling required");

    ProcessGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Manufacturing Process",
                        "Manufacturing Processes that consume resources to create new resources.",
                        "manufacturing");

        // Groups according to the type of tooling
        setGrouperByKey("Tooling", r-> {var t = r.getTooling();
                                            if (t == null) return NO_TOOLING;
                                            return new GroupKey(t.name(), t.description());
                                        });
    }

    /**
     * Add properties for the consuming and produced Resources. This will also use the process-input partial template
     * as well as the main process-detail template.
     * @param p Process for generation
     * @param output Destination for content
     */
    @Override
    protected void addEntityProperties(WorkshopProcessInfo p, Map<String,Object> scope) {
		addProcessInputOutput(scope, "Inputs", toQuantityItems(p.getInputList()),
									"Products", toQuantityItems(p.getOutputList()));

        scope.put("process-type", (p instanceof ManufactureProcessInfo ? "Manufacture" : "Salvage"));
    }

    /**
     * Get all the configured manufacturing processes.
     */
    @Override
    protected List<WorkshopProcessInfo> getEntities() {
		var manuConfig = getConfig().getManufactureConfiguration();
        return Stream.concat(manuConfig.getManufactureProcessList().stream(),
                                manuConfig.getSalvageInfoList().stream())
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    @Override
    protected String getEntityName(WorkshopProcessInfo v) {
        return v.getName();
    }
}
