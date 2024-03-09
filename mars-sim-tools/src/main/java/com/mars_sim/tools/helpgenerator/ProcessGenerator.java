/*
 * Mars Simulation Project
 * ProcessGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.mars_sim.core.manufacture.ManufactureProcessInfo;

/**
 * Help file generator for manufacturing processes.
 */
class ProcessGenerator extends TypeGenerator<ManufactureProcessInfo> {

    public static final String TYPE_NAME = "process";

    ProcessGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Manufacturing Processes",
                        "Manufacturing Processes that consume resoruce to create new resources.");

        // Groups according to first letter of name
        setGrouper("Name", r-> r.getName().substring(0, 1).toUpperCase());
    }

    /**
     * Generator an output for a specific process. This will also use the process-input partial template
     * as well as the main process-detail template.
     * @param p Process for generation
     * @param output Destination for content
     */
    @Override
    public void generateEntity(ManufactureProcessInfo p, OutputStream output) throws IOException {
        var generator = getParent();

		var pScope = generator.createScopeMap("Process " + p.getName());
		pScope.put(TYPE_NAME, p);
		addProcessInputOutput(pScope, "Inputs", toQuantityItems(p.getInputList()),
									"Products", toQuantityItems(p.getOutputList()));

        generator.generateContent("process-detail", pScope, output);
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
