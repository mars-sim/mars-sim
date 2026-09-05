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

import com.mars_sim.core.SimulationConfig;
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

	// Constants for complaint identifiers that are directly referenced in code.
	public static final String RADIATION_SICKNESS = "RADIATION_SICKNESS";
	public static final String DECOMPRESSION = "DECOMPRESSION";
	public static final String DEHYDRATION = "DEHYDRATION";
	public static final String STARVATION = "STARVATION";
	public static final String PANIC_ATTACK = "PANIC_ATTACK";
	public static final String DEPRESSION = "DEPRESSION";
	public static final String SUFFOCATION = "SUFFOCATION";
	public static final String FREEZING = "FREEZING";
	public static final String HEAT_STROKE = "HEAT_STROKE";

	/** Settlement's Postmortem Exam waiting list. */
	private Map<Integer, List<DeathInfo>> awaitingPostmortemExam;
	/** Settlement's Death Registry. */
	private Map<Integer, List<DeathInfo>> deathRegistry;

	private static MedicalConfig medicalConfig = SimulationConfig.instance().getMedicalConfiguration();

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
	 * specified identifier name.
	 * 
	 * @param name identifier (UPPERCASE_UNDERSCORE format) of the complaint to retrieve.
	 * @return Matched complaint, if none is found then a null.
	 */
	public Complaint getComplaintByID(String name) {
		return medicalConfig.getComplaintByID(name);
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
	 * Adds a death info object to the death registry in a settlement.
	 * @param s
	 * @param death
	 */
	public void addDeathRegistry(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		if (awaitingPostmortemExam.containsKey(id)) {
			awaitingPostmortemExam.get(id).remove(death);

			if (deathRegistry.containsKey(id)) {
				deathRegistry.get(id).add(death);
			} else {
				List<DeathInfo> list = new CopyOnWriteArrayList<>();
				list.add(death);
				deathRegistry.put(id, list);
			}
		}
	}

	/**
	 * Returns the death registry list.
	 * 
	 * @param s
	 * @return
	 */
	public List<DeathInfo> getDeathRegistry(Settlement s) {
		return deathRegistry.getOrDefault(s.getIdentifier(), null);
	}

	/**
	 * Adds a death info instance to the post mortem exam for a settlement.
	 * 
	 * @param s
	 * @param death
	 */
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

	/**
	 * Gets a list of death info instances.
	 * 
	 * @param s
	 * @return
	 */
	public List<DeathInfo> getPostmortemExam(Settlement s) {
		if (awaitingPostmortemExam.containsKey(s.getIdentifier())) {
			return awaitingPostmortemExam.get(s.getIdentifier());
		}
		
		return Collections.emptyList();
	}
}
