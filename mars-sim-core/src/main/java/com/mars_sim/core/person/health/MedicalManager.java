/*
 * Mars Simulation Project
 * MedicalManager.java
 * @date 2023-07-21
 * @author Barry Evans
 */

package com.mars_sim.core.person.health;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.structure.Settlement;

/**
 * This class provides a Factory for the {@link Complaint} class. Some of the
 * Medical Complaints are pre-defined. Instances are accessed via a factory
 * method since the properties of the individual complaints are loaded from the
 * XML.
 */
public class MedicalManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Settlement's Postmortem Exam waiting list. */
	private Map<Integer, List<DeathInfo>> awaitingPostmortemExam;
	/** Settlement's Death Registry. */
	private Map<Integer, List<DeathInfo>> deathRegistry;

	private static MedicalConfig medicalConfig;

	/**
	 * Constructs a new {@link MedicalManager}. This also constructs all the
	 * pre-defined Complaints and the user-defined ones in the XML configuration
	 * file.
	 */
	public MedicalManager() {
		awaitingPostmortemExam = new ConcurrentHashMap<>();
		deathRegistry = new ConcurrentHashMap<>();		
	}


	/**
	 * Gets a list of all medical complaints.
	 * 
	 * @return list of complaints.
	 */
	public Collection<Complaint> getAllMedicalComplaints() {
		return medicalConfig.getComplaintList();
	}

	/**
	 * This is a finder method that returns a Medical Complaint matching the
	 * specified name.
	 * 
	 * @param name Name of the complaintType to retrieve.
	 * @return Matched complaint, if none is found then a null.
	 */
	public Complaint getComplaintByName(ComplaintType type) {
		return medicalConfig.getComplaintByName(type);
	}

	/**
	 * Gets the supported Treatments for a Medical Facility of a particular level.
	 * This will be a combination of all the Treatments of the specified level and
	 * all those lower.
	 * 
	 * @param level Level of Medical facility.
	 * @return List of Treatments
	 */
	public List<Treatment> getSupportedTreatments(int level) {
		return medicalConfig.getTreatmentsByLevel(level);
	}

	/**
	 * Returns the pre-defined Medical Complaint that signifies a dehydration
	 * complaint.
	 * 
	 * @return Medical complaint for shortage of water.
	 */
	public Complaint getDehydration() {
		return getComplaintByName(ComplaintType.DEHYDRATION);
	}

	/**
	 * Returns the pre-defined Medical Complaint that signifies a starvation
	 * complaint.
	 * 
	 * @return Medical complaint for shortage of food.
	 */
	public Complaint getStarvation() {
		return getComplaintByName(ComplaintType.STARVATION);
	}

	public void addDeathRegistry(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		awaitingPostmortemExam.get(id).remove(death);
		if (deathRegistry.containsKey(id)) {
			deathRegistry.get(id).add(death);
		} else {
			List<DeathInfo> list = new CopyOnWriteArrayList<>();
			list.add(death);
			deathRegistry.put(id, list);
		}
	}

	public List<DeathInfo> getDeathRegistry(Settlement s) {
		return deathRegistry.getOrDefault(s.getIdentifier(), null);
	}

	public void addPostmortemExam(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		if (awaitingPostmortemExam.containsKey(id)) {
			awaitingPostmortemExam.get(id).add(death);
		} else {
			List<DeathInfo> list = new CopyOnWriteArrayList<>();
			list.add(death);
			awaitingPostmortemExam.put(id, list);
		}
	}

	public List<DeathInfo> getPostmortemExam(Settlement s) {
		if (awaitingPostmortemExam.containsKey(s.getIdentifier())) {
			return awaitingPostmortemExam.get(s.getIdentifier());
		}
		
		return Collections.emptyList();
	}

	/**
	 * Initializes the Medical Complaints from the configuration.
	 * 
	 * @throws exception if not able to initialize complaints.
	 */
	public static void initializeInstances(MedicalConfig mc) {
		medicalConfig = mc;
	}

}
