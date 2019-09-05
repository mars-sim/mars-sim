/**
 * Mars Simulation Project
 * MedicalStation.java
 * @version 3.1.0 2017-09-01
 * @author Scott Davis
 * Based on Barry Evan's SickBay class
 */
package org.mars_sim.msp.core.person.health;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class represents a medical station. It provides a number of treatments
 * to persons, these are defined in the Medical.xml file.
 */
public class MedicalStation implements MedicalAid, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(MedicalStation.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Treatment level of the facility. */
	private int level;
	/** Number of sick beds. */
	private int sickBeds;
	/** List of health problems currently being treated. */
	private List<HealthProblem> problemsBeingTreated;
	/** List of health problems awaiting treatment. */
	private List<HealthProblem> problemsAwaitingTreatment;
	/** List of people resting to recover a health problem. */
	private List<Person> restingRecoveryPeople;
	/** Treatments supported by the medical station. */
	private List<Treatment> supportedTreatments;

//	private MedicalCare medicalCare;

	private Vehicle vehicle;

	private Building building;

	private static MedicalManager medManager;
	
//    private Map<Person, Point2D> bedMap = new HashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param level    The treatment level of the medical station.
	 * @param sickBeds Number of sickbeds.
	 */
	public MedicalStation(int level, int sickBeds) {
//		this.medicalCare = medicalCare;
		this.level = level;
		this.sickBeds = sickBeds;
		problemsBeingTreated = new ArrayList<HealthProblem>();
		problemsAwaitingTreatment = new ArrayList<HealthProblem>();
		restingRecoveryPeople = new ArrayList<Person>();

		// Get all supported treatments.
		if (medManager == null) {
//			System.out.println("medManager is null");
			medManager = Simulation.instance().getMedicalManager();
		}
//		else {
//			System.out.println("medManager is NOT null");
//		}
		supportedTreatments = medManager.getSupportedTreatments(level);
	}

	public void setVehicle(Vehicle v) {
		vehicle = v;
	}

	public void setBuilding(Building b) {
		building = b;
	}

	@Override
	public List<HealthProblem> getProblemsAwaitingTreatment() {
		return new ArrayList<HealthProblem>(problemsAwaitingTreatment);
	}

	@Override
	public List<HealthProblem> getProblemsBeingTreated() {
		return new ArrayList<HealthProblem>(problemsBeingTreated);
	}

	@Override
	public List<Person> getRestingRecoveryPeople() {
		return new ArrayList<Person>(restingRecoveryPeople);
	}

	/**
	 * Gets the number of sick beds.
	 * 
	 * @return Sick bed count.
	 */
	public int getSickBedNum() {
		return sickBeds;
	}

	/**
	 * Gets the current number of people being treated here.
	 * 
	 * @return Patient count.
	 */
	public int getPatientNum() {
		return getPatients().size();
	}

	/**
	 * Checks if there are any empty beds for new patients
	 * 
	 * @return true or false
	 */
	public boolean hasEmptyBeds() {
		if (getPatientNum() < getSickBedNum())
			return true;
		else
			return false;
	}
	
	/**
	 * Gets the patients at this medical station.
	 * 
	 * @return Collection of People.
	 */
	public Collection<Person> getPatients() {
		Collection<Person> result = new ConcurrentLinkedQueue<Person>();

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
		return new ArrayList<Treatment>(supportedTreatments);
	}

	@Override
	public boolean canTreatProblem(HealthProblem problem) {
		if (problem == null)
			return false;
		else {
			boolean degrading = problem.isDegrading();

			// Check if treatment is supported in this medical station.
			Treatment requiredTreatment = problem.getIllness().getRecoveryTreatment();
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
			throw new IllegalArgumentException("Health problem is null");
		}

		// Check if treatment is supported here.
		if (canTreatProblem(problem)) {

			// Add the problem to the waiting queue.
			problemsAwaitingTreatment.add(problem);
		} else {
			String loc0 = null;
			String loc1 = null;
			if (building != null) {
				loc0 = building.getNickName();
				loc1 = building.getLocationTag().getSettlementName();
			} else if (vehicle != null) {
				loc0 = vehicle.getName();
				loc1 = loc0;
			}

			LogConsolidated.log(logger, Level.INFO, 2000, sourceName,
					"[" + loc0 + "] " + problem.getIllness() + " cannot be treated in " + loc1 + "'s medical station.",
					null);
		}
	}

	@Override
	public void cancelRequestTreatment(HealthProblem problem) {

		if (problem == null) {
			throw new IllegalArgumentException("Health problem is null");
		}

		// Check if problem is being treated here.
		if (problemsAwaitingTreatment.contains(problem)) {
			problemsAwaitingTreatment.remove(problem);
		} else {
			logger.severe("Health problem " + problem.getIllness()
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
			String loc0 = null;
			String loc1 = null;
			if (building != null) {
				loc0 = building.getNickName();
				loc1 = building.getLocationTag().getSettlementName();
			} else if (vehicle != null) {
				loc0 = vehicle.getName();
				loc1 = loc0;
			}

			LogConsolidated.log(logger, Level.INFO, 2000, sourceName, "[" + loc0 + "] " + problem.getIllness()
					+ " cannot be treated in " + loc1 + "'s medical station is not equipped to handle.", null);
		}
	}

	@Override
	public void stopTreatment(HealthProblem problem) {

		if (problemsBeingTreated.contains(problem)) {
			problem.stopTreatment();
			problemsBeingTreated.remove(problem);
			boolean cured = problem.isCured();
			boolean dead = problem.getSufferer().getPhysicalCondition().isDead();
			boolean recovering = problem.getRecovering();
			if (!cured && !dead && !recovering) {
				problemsAwaitingTreatment.add(problem);
			}
		} else {
			logger.severe("Health problem " + problem.getIllness() + " not currently being treated.");
		}
	}

	@Override
	public void startRestingRecovery(Person person) {

		if (!restingRecoveryPeople.contains(person)) {
			restingRecoveryPeople.add(person);
		} else {
			logger.severe(person + " already resting at medical station.");
		}
	}

	@Override
	public void stopRestingRecovery(Person person) {

		if (restingRecoveryPeople.contains(person)) {
			restingRecoveryPeople.remove(person);
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

//	public MedicalCare getMedicalCare() {
//		return medicalCare;
//	}

	/**
	 * Prepare object for garbage collection.
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