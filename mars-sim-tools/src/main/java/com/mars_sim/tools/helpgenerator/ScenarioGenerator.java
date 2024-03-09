/*
 * Mars Simulation Project
 * ScenarioGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.ScenarioConfig;

public class ScenarioGenerator extends TypeGenerator<Scenario> {
    public static final String TYPE_NAME = "scenario";

    protected ScenarioGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Scenarios",
                "Predefined Scenarios used to start the simulation");
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

    
	/**
	 * Generate the file for the Crew template.
	 * @param v Template being rendered.
     * @param output Destination of content
	 * @throws IOException
	 */
	public void generateEntity(Scenario v, OutputStream output) throws IOException {
        var generator = getParent();

		// Individual  pages
	    var vScope = generator.createScopeMap("Scenario - " + v.getName());
		vScope.put(TYPE_NAME, v);

        // Generate the file
        generator.generateContent("scenario-detail", vScope, output);
	}
    

    @Override
    protected String getEntityName(Scenario v) {
        return v.getName();
    }
}
