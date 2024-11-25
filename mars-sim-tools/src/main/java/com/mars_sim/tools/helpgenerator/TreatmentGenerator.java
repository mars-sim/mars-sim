/*
 * Mars Simulation Project
 * ComplaintGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.health.Treatment;

public class TreatmentGenerator extends TypeGenerator<Treatment> {
    public static final String TYPE_NAME = "treatment";

    protected TreatmentGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Treatment",
                "Treatments applied to cure Complaints");

        // Groups by part type
        setGrouper("Medical Tech Level", r-> Integer.toString(r.getFacilityLevel()));
    }

    /**
     * Get a list of all the predefined Treatments.
     */
    @Override
    protected List<Treatment> getEntities() {
        return getParent().getConfig().getMedicalConfiguration().getTreatmentsByLevel(20)
                    .stream()
		 			.sorted(Comparator.comparing(Treatment::getName))
					.toList();
    }

    @Override
    protected String getEntityName(Treatment v) {
        return v.getName();
    }
}
