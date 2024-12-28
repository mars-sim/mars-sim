/*
 * Mars Simulation Project
 * ComplaintGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.health.Complaint;

public class ComplaintGenerator extends TypeGenerator<Complaint> {
    public static final String TYPE_NAME = "complaint";

    protected ComplaintGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Complaint",
                "Health Complaints affecting Persons",
                "medical");
        
        // Groups according to Seriousness
        setGrouper("Seriousness", r-> getSeriousnessRange(r.getSeriousness()));
    }

    private static String getSeriousnessRange(int seriousness) {
        int baseLevel = ((seriousness-1)/10) * 10;
        return (baseLevel+1) + " to " + (baseLevel+10);
    }

    /**
     * Gets a list of all the predefined Complaints.
     */
    @Override
    protected List<Complaint> getEntities() {
        return getParent().getConfig().getMedicalConfiguration().getComplaintList()
                    .stream()
		 			.sorted(Comparator.comparing(Complaint::getName))
					.toList();
    }

    @Override
    protected String getEntityName(Complaint v) {
        return v.getName();
    }
}
