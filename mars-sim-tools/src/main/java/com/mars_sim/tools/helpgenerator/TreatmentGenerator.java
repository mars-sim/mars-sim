/*
 * Mars Simulation Project
 * ComplaintGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.health.Treatment;

public class TreatmentGenerator extends TypeGenerator<Treatment> {
    public static final String TYPE_NAME = "treatment";

    protected TreatmentGenerator(HelpGenerator parent) {
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

    
	/**
	 * Generate the file for the Treatment
	 * @param v Treatment being rendered.
     * @param output Destination of content
	 * @throws IOException
	 */
    @Override
	public void generateEntity(Treatment v, OutputStream output) throws IOException {
        var generator = getParent();

		// Individual  pages
	    var vScope = generator.createScopeMap("Treatment - " + v.getName());
		vScope.put(TYPE_NAME, v);

        // Generate the file
        generator.generateContent("treatment-detail", vScope, output);
	}
    

    @Override
    protected String getEntityName(Treatment v) {
        return v.getName();
    }
}
