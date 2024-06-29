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

import com.mars_sim.core.person.health.Complaint;

public class ComplaintGenerator extends TypeGenerator<Complaint> {
    public static final String TYPE_NAME = "complaint";

    protected ComplaintGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Complaint",
                "Health Complaints affecting Persons");
        
        // Groups according to Seriousness
        setGrouper("Seriousness", r-> getSeriousnessRange(r.getSeriousness()));
    }

    private static String getSeriousnessRange(int seriousness) {
        int baseLevel = ((seriousness-1)/10) * 10;
        return (baseLevel+1) + " to " + (baseLevel+10);
    }

    /**
     * Get a list of all the predefined Complaints.
     */
    @Override
    protected List<Complaint> getEntities() {
        return getParent().getConfig().getMedicalConfiguration().getComplaintList()
                    .stream()
		 			.sorted(Comparator.comparing(Complaint::getName))
					.toList();
    }

    
	/**
	 * Generate the file for the Complaint
	 * @param v Complaint being rendered.
     * @param output Destination of content
	 * @throws IOException
	 */
    @Override
	public void generateEntity(Complaint v, OutputStream output) throws IOException {
        var generator = getParent();

		// Individual  pages
	    var vScope = generator.createScopeMap("Complaint - " + v.getName());
		vScope.put(TYPE_NAME, v);

        // Generate the file
        generator.generateContent("complaint-detail", vScope, output);
	}
    

    @Override
    protected String getEntityName(Complaint v) {
        return v.getName();
    }
}
