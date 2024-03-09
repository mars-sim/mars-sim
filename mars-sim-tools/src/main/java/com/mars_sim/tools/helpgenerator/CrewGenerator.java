/*
 * Mars Simulation Project
 * CrewGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.mars_sim.core.person.Crew;
import com.mars_sim.core.person.CrewConfig;

public class CrewGenerator extends TypeGenerator<Crew> {
    public static final String TYPE_NAME = "crew";

    protected CrewGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Crew",
                "Predefined Crew for use in Scenarios");
    }

    /**
     * Get a list of all the predefined Crew configured.
     */
    protected List<Crew> getEntities() {
        var config = new CrewConfig();
        return config.getKnownItems()
                            .stream()
                            .filter(Crew::isBundled)
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    
	/**
	 * Generate the file for the Crew template.
	 * @param v Template being rendered.
     * @param output Destination of content
	 * @throws IOException
	 */
	public void generateEntity(Crew v, OutputStream output) throws IOException {
        var generator = getParent();

		// Individual  pages
	    var vScope = generator.createScopeMap("Crew - " + v.getName());
		vScope.put(TYPE_NAME, v);

        // Generate the file
        generator.generateContent("crew-detail", vScope, output);
	}
    

    @Override
    protected String getEntityName(Crew v) {
        return v.getName();
    }
}
