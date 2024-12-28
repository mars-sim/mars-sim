/*
 * Mars Simulation Project
 * ScenarioGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;

import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.ScenarioConfig;

public class ScenarioGenerator extends TypeGenerator<Scenario> {
    public static final String TYPE_NAME = "scenario";

    protected ScenarioGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Scenario",
                "Predefined Scenarios used to start the simulation",
                "scenario_name");
        setChangeViaEditor(true);
    }

    /**
     * Get a list of all the predefined Crew configured.
     */
    protected List<Scenario> getEntities() {
        var config = new ScenarioConfig();
        return config.getKnownItems()
                            .stream()
                            .filter(Scenario::isBundled)
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    @Override
    protected String getEntityName(Scenario v) {
        return v.getName();
    }
}
