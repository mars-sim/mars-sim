/*
 * Mars Simulation Project
 * MedicalStation.java
 * @date 2023-11-24
 * @author Scott Davis
 * Based on Barry Evan's SickBay class
 */
package com.mars_sim.core.person.health;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;

/**
 * This class represents a medical station. It provides a number of treatments
 * to persons, these are defined in the Medical.xml file.
 */
public class MedicalStation implements MedicalAid {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MedicalStation.class.getName());

	/** Treatment level of the facility. */
	private int level;
	
	/** The total number of sick beds. */
	private int totalNumMedicalBeds;

	/** The name of this medical station. */
	private String name;
	
	/** List of health problems currently being treated. */
	private List<HealthProblem> problemsBeingTreated;
	/** List of health problems awaiting treatment. */
	private List<HealthProblem> problemsAwaitingTreatment;
	
	
	/** List of people resting to recover a health problem. */
	private List<Person> restingRecoveryPeople;
	
	/** Treatments supported by the medical station. */
	private List<Treatment> supportedTreatments;
	
	/** The set of medical beds. */
	private Set<LocalPosition> medicalBedSet;
	
	private Map<Integer, LocalPosition> bedRegistry;
	
	/**
	 * Constructor.
	 * 
	 * @param level		The treatment level of the medical station.
	 * @param beds		# of sickbeds
	 */
	public MedicalStation(String name, int level, int beds) {
		this.name = name;
		this.level = level;

		this.totalNumMedicalBeds = beds;
		
		problemsBeingTreated = new CopyOnWriteArrayList<>();
		problemsAwaitingTreatment = new CopyOnWriteArrayList<>();
		restingRecoveryPeople = new CopyOnWriteArrayList<>();

		bedRegistry = new HashMap<>();
		
		// Get all supported treatments at this medical station
		supportedTreatments =  Simulation.instance().getMedicalManager().getSupportedTreatments(level);
	}

	
	/**
	 * Gets an empty, unoccupied bed.
	 * 
	 * @return
	 */
	private LocalPosition getEmptyBed() {
	
		Collection<LocalPosition> beds = bedRegistry.values();
		
		for (LocalPosition pos: medicalBedSet) {
			if (!beds.contains(pos))
				return pos;
		}

		return null;
	}
	

	/**
	 * Adds a patient to a medical bed.
	 * 
	 * @return
	 */
	public boolean addPatientToBed(Person person) {

		 Set<Integer> patients = bedRegistry.keySet();
		 
		 if (patients.contains(person.getIdentifier())) {
			 return true;
		 }
		 else {
			 LocalPosition aBed = getEmptyBed();
			 
			 if (aBed != null) {
				 bedRegistry.put(person.getIdentifier(), aBed);		 
				 // Release existing activity spot
				 person.leaveActivitySpot(false);
				 
				 return true;
			 }
		 }
		 
		return false;
	}
	
	/**
	 * Removes a patient from a medical bed.
	 * 
	 * @return
	 */
	public boolean removePatientFromBed(Person person) {

		 Set<Integer> patients = bedRegistry.keySet();
		 
		 if (patients.contains(person.getIdentifier())) {

			 bedRegistry.remove(person.getIdentifier());
			 
			 return true;
		 }
		 
		return false;
	}
	
	/**
	 * Are there any patients ?
	 * 
	 * @return 
	 */
	public boolean hasPatients() {
		if (!bedRegistry.isEmpty())
			return true;
		
		if (!problemsBeingTreated.isEmpty())
			return true;
		
		if (!problemsAwaitingTreatment.isEmpty())
			return true;
		
		if (!restingRecoveryPeople.isEmpty())
			return true;
		
		return false;
	}
	
	/**
	 * Sets the medical beds.
	 * 
	 * @param bedSet
	 */
	public void setMedicalBeds(Set<LocalPosition> bedSet) {
		this.medicalBedSet = bedSet;
	}
	
	/**
	 * Returns a set of medical beds.
	 * 
	 * @return
	 */
	public Set<LocalPosition> getMedicalBedSet() {
		return medicalBedSet;
	}
	
	@Override
	public List<HealthProblem> getProblemsAwaitingTreatment() {
		return new ArrayList<>(problemsAwaitingTreatment);
	}

	@Override
	public List<HealthProblem> getProblemsBeingTreated() {
		return new ArrayList<>(problemsBeingTreated);
	}

	@Override
	public List<Person> getRestingRecoveryPeople() {
		return new ArrayList<>(restingRecoveryPeople);
	}

	/**
	 * Gets the number of sick beds.
	 * 
	 * @return Sick bed count.
	 */
	public int getSickBedNum() {
		return totalNumMedicalBeds;
	}

	/**
	 * Gets the current number of people being treated here.
	 * 
	 * @return Patient count.
	 */
	public int getPatientNum() {
		return getPatients().size();
	}

//	/**
//	 * Checks if there are any empty beds for new patients.
//	 * 
//	 * @return true or false
//	 */
//	public boolean hasEmptyBeds() {
//        return getPatientNum() < getSickBedNum();
//	}
//	
	/**
	 * Gets the patients at this medical station.
	 * 
	 * @return Collection of People.
	 */
	public Collection<Person> getPatients() {
		Collection<Person> result = new ConcurrentLinkedQueue<>();

		// Add patients being treated for health problems.
		Iterator<HealthProblem> i = problemsBeingTreated.iterator();
		while (i.hasNext()) {
			Person patient = i.next().getSufferer();
			if (!result.contains(patient)) {
				result.add(patient);
			}
		}

		// Add patients that are resting to recover from health problems.
		Iterator<Person> j = restingRecoveryPeople.iterator();
		while (j.hasNext()) {
			Person patient = j.next();
			if (!result.contains(patient)) {
				result.add(patient);
			}
		}

		return result;
	}

	@Override
	public List<Treatment> getSupportedTreatments() {
		return new CopyOnWriteArrayList<>(supportedTreatments);
	}

	@Override
	public boolean canTreatProblem(HealthProblem problem) {
		if (problem == null)
			return false;
		else {
			boolean degrading = problem.getState() == HealthProblemState.DEGRADING;

			// Check if treatment is supported in this medical station.
			Treatment requiredTreatment = problem.getComplaint().getRecoveryTreatment();
			boolean supported = supportedTreatments.contains(requiredTreatment);

			// Check if problem is already being treated.
			boolean treating = problemsBeingTreated.contains(problem);

			// Check if problem is waiting to be treated.
			boolean waiting = problemsAwaitingTreatment.contains(problem);

			return supported && degrading && !treating && !waiting;
		}
	}

	@Override
	public void requestTreatment(HealthProblem problem) {

		if (problem == null) {
			throw new IllegalArgumentException("Requested Health problem cannot be null");
		}

		// Check if treatment is supported here.
		if (canTreatProblem(problem)) {
			// Add the problem to the waiting queue.
			problemsAwaitingTreatment.add(problem);
			
			if (problem.getSufferer().isInSettlement())
				// Add the patient to a bed
				addPatientToBed(problem.getSufferer());
			
		} else {
			logger.info("[" + name + "] " + problem.getComplaint() + " cannot be treated in medical station.");
		}
	}

	@Override
	public void cancelRequestTreatment(HealthProblem problem) {

		if (problem == null) {
			throw new IllegalArgumentException("Health problem is null");
		}

		// Check if problem is being treated here.
		if (problemsAwaitingTreatment.contains(problem)) {
			// Remove the problem 
			problemsAwaitingTreatment.remove(problem);
			if (problem.getSufferer().isInSettlement())
				// Remove the patient from a bed
				removePatientFromBed(problem.getSufferer());
			
		} else {
			logger.severe("[" + name + "] " + "Health problem " + problem.getComplaint()
					+ " request cannot be canceled as it is not awaiting response.");
		}
	}

	@Override
	public void startTreatment(HealthProblem problem, double treatmentDuration) {

		if (problem == null) {
			throw new IllegalArgumentException("Health problem is null");
		}

		if (problemsAwaitingTreatment.contains(problem)) {
			problem.startTreatment(treatmentDuration, this);
			problemsBeingTreated.add(problem);
			problemsAwaitingTreatment.remove(problem);
		} else {
			logger.warning("[" + name + "] " + problem.getComplaint()
					+ " cannot be treated in medical station is not equipped to handle.");
		}
	}

	@Override
	public void stopTreatment(HealthProblem problem) {

		if (problemsBeingTreated.contains(problem)) {
			
			problem.stopTreatment();
			
			problemsBeingTreated.remove(problem);
			
			if (problem.getSufferer().isInSettlement())
				// Remove the patient from a bed
				removePatientFromBed(problem.getSufferer());
			
			var state = problem.getState();
			boolean dead = problem.getSufferer().getPhysicalCondition().isDead();
			
			if ((state != HealthProblemState.CURED) && !dead && (state != HealthProblemState.RECOVERING)) {
				problemsAwaitingTreatment.add(problem);
			}
		} else {
			logger.severe("[" + name + "] " + "Health problem " + problem.getComplaint() + " not currently being treated.");
		}
	}

	@Override
	public void startRestingRecovery(Person person) {

		if (!restingRecoveryPeople.contains(person)) {
			
			restingRecoveryPeople.add(person);
			
			if (person.isInSettlement())
				// Add a patient to a bed
				addPatientToBed(person);
			
		} else {
			logger.severe(person + " already resting at medical station.");
		}
	}

	@Override
	public void stopRestingRecovery(Person person) {

		if (restingRecoveryPeople.contains(person)) {
			
			restingRecoveryPeople.remove(person);
			
			if (person.isInSettlement())
				// Add a patient to a bed
				removePatientFromBed(person);
		} else {
			logger.severe(person + " isn't resting at medical station.");
		}
	}

	/**
	 * Gets the treatment level of the medical station.
	 * 
	 * @return treatment level
	 */
	public int getTreatmentLevel() {
		return level;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		problemsBeingTreated.clear();
		problemsBeingTreated = null;
		problemsAwaitingTreatment.clear();
		problemsAwaitingTreatment = null;
		supportedTreatments.clear();
		supportedTreatments = null;
	}
}
