/*
 * Mars Simulation Project
 * ComplaintReference.java
 * @date 2024-07-28
 * @author Barry Evans
 */
package com.mars_sim.core.person.health;

import java.io.Serializable;

/**
 * This centralises the process of having a reference to a Complaint but storing the serialised
 * representation as a complaint name String.
 */
class ComplaintReference implements Serializable {
	
    private static final long serialVersionUID = 1L;

	private static MedicalManager medicalManager;

    private String name;
    private transient Complaint complaint;

    ComplaintReference(Complaint complaint) {
        this.name = complaint.getID();
        this.complaint = complaint;
    }

    /**
	 * Returns the complaint. This implements a lazy loading pattern.
	 *
	 * @return Complaint.
	 */
	public Complaint getComplaint() {
		if (complaint == null) {
			complaint = medicalManager.getComplaintByID(name);
		}
		return complaint;
	}

    /**
     * Returns the complaint identifier name.
     * @return complaint type name
     */
    public String getID() {
        return name;
    }

    /**
	 * Initializes instances after loading from a saved sim
	 * 
	 * @param m {@link medicalManager}
	 */
	static void initializeInstances(MedicalManager m) {
		medicalManager = m;
	}
}
