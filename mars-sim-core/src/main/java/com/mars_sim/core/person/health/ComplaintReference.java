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
 * representation as a ComplaintType.
 * 
 * This will be remvoved when issue #1341 is implemented
 * 
 */
class ComplaintReference implements Serializable {
	
    private static final long serialVersionUID = 1L;

	private static MedicalManager medicalManager;

    private ComplaintType type;
    private transient Complaint complaint;

    ComplaintReference(ComplaintType type) {
        this.type = type;
        complaint = medicalManager.getComplaintByName(type);
    }

    /**
	 * Returns the complaint. THis implements a lazy loadng pattern.
	 *
	 * @return Complaint.
	 */
	public Complaint getComplaint() {
		if (complaint == null) {
			complaint = medicalManager.getComplaintByName(type);
		}
		return complaint;
	}

    public ComplaintType getType() {
        return type;
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
