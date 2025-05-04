/*
 * Mars Simulation Project
 * WorkshopProcessInfo.java
 * @date 2025-05-04
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;

/**
 * Definiton of a process in a Workshop that needs tooling
 */
public abstract class WorkshopProcessInfo extends ProcessInfo {
    private Tooling tooling;

    WorkshopProcessInfo(String name, String description, int techLevelRequired, int skillLevelRequired,
			double workTimeRequired, double processTimeRequired, double powerRequired, Tooling tool,
			List<ProcessItem> inputList, List<ProcessItem> outputList) {
		super(name, description, techLevelRequired, skillLevelRequired, workTimeRequired, processTimeRequired,
				powerRequired, inputList, outputList);
        this.tooling = tool;
    }

    /**
	 * What tool is used for this process?
	 * @return
	 */
    public Tooling getTooling() {
        return tooling;
    }

    /**
     * Is this process supported by the given tools?
     * @param tools
     * @return
     */
    public boolean isSupported(Set<Tooling> tools) {
        if (tooling != null) {
            return tools.contains(tooling);
        }
        return true;
    }
}
