/*
 * Mars Simulation Project
 * CrewGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;

import com.mars_sim.core.person.Crew;
import com.mars_sim.core.person.CrewConfig;

public class CrewGenerator extends TypeGenerator<Crew> {
    public static final String TYPE_NAME = "crew";

    protected CrewGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Crew",
                "Predefined Crew for use in Scenarios",
                "crew_name");
        setChangeViaEditor(true);
    }

    /**
     * Get a list of all the predefined Crew configured.
     */
    protected List<Crew> getEntities() {
        var config = new CrewConfig(getConfig());
        return config.getKnownItems()
                            .stream()
                            .filter(Crew::isBundled)
		 					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
							.toList();
    }

    @Override
    protected String getEntityName(Crew v) {
        return v.getName();
    }
}
