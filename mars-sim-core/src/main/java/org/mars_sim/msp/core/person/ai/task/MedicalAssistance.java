/**
 * Mars Simulation Project
 * MedicalAssistance.java
 * @version 3.07 2015-02-27
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.person.medical.Treatment;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class represents a task that requires a person to provide medical
 * help to someone else.
 */
public class MedicalAssistance
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(MedicalAssistance.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.medicalAssistance"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.treatment")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;
	private static final double AVERAGE_MEDICAL_WASTE = .1;

	/** The medical station the person is at. */
	private MedicalAid medical;
	/** How long for treatment. */
	private double duration;
	/** Health problem to treat. */
	private HealthProblem problem;


	/**
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public MedicalAssistance(Person person) {
		super(NAME, person, true, true, STRESS_MODIFIER, true, 0D);

		// Get a local medical aid that needs work.
		List<MedicalAid> localAids = getNeedyMedicalAids(person);
		if (localAids.size() > 0) {
			int rand = RandomUtil.getRandomInt(localAids.size() - 1);
			medical = localAids.get(rand);

			// Get a curable medical problem waiting for treatment at the medical aid.
			problem = (HealthProblem) medical.getProblemsAwaitingTreatment().get(0);

			// Get the person's medical skill.
			int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

			// Treat medical problem.
			Treatment treatment = problem.getIllness().getRecoveryTreatment();
			setDescription(Msg.getString("Task.description.medicalAssistance.detail",
			        treatment.getName())); //$NON-NLS-1$
			setDuration(treatment.getAdjustedDuration(skill));
			setStressModifier(STRESS_MODIFIER * treatment.getSkill());

			// Start the treatment
			try {
				medical.startTreatment(problem, duration);
				logger.fine(person.getName() + " treating " + problem.getIllness().getType().toString());

				// Add person to medical care building if necessary.
				if (medical instanceof MedicalCare) {
					MedicalCare medicalCare = (MedicalCare) medical;
					//Building building = medicalCare.getBuilding();
					// Walk to medical care building.
					walkToActivitySpotInBuilding(medicalCare.getBuilding(), false);

					produceMedicalWaste();

				}
				else if (medical instanceof SickBay) {
				    Vehicle vehicle = ((SickBay) medical).getVehicle();
				    if (vehicle instanceof Rover) {

				        // Walk to rover sick bay activity spot.
				        walkToSickBayActivitySpotInRover((Rover) vehicle, false);

						produceMedicalWaste();
				    }
				}

				// Create starting task event if needed.
				if (getCreateEvents()) {
					TaskEvent startingEvent = new TaskEvent(person, this, EventType.TASK_START, "");
					Simulation.instance().getEventManager().registerNewEvent(startingEvent);
				}
			}
			catch (Exception e) {
				logger.severe("MedicalAssistance: " + e.getMessage());
				endTask();
			}
		}
		else {
			endTask();
		}

		// Initialize phase.
		addPhase(TREATMENT);
		setPhase(TREATMENT);
	}

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.MEDICAL_CARE;
    }

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (TREATMENT.equals(getPhase())) {
			return treatmentPhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the treatment phase of the task.
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left over after performing the phase.
	 */
	private double treatmentPhase(double time) {

		// If sickbay owner has malfunction, end task.
		if (getMalfunctionable(medical).getMalfunctionManager().hasMalfunction()) {
			endTask();
		}

		if (isDone()) {
			return time;
		}

		// Check for accident in infirmary.
		checkForAccident(time);

		if (getDuration() <= (getTimeCompleted() + time)) {
			problem.startRecovery();
			endTask();
		}

		// Add experience.
		addExperience(time);

		return 0D;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Medical" skill
		// (1 base experience point per 25 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 25D;
		int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
				NaturalAttribute.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(SkillType.MEDICINE, newPoints);
	}

	/**
	 * Gets the local medical aids that have patients waiting.
	 * @return List of medical aids
	 */
	public static List<MedicalAid> getNeedyMedicalAids(Person person) {
		List<MedicalAid> result = new ArrayList<MedicalAid>();

		LocationSituation location = person.getLocationSituation();
		if (location == LocationSituation.IN_SETTLEMENT) {
			try {
				Building building = getMedicalAidBuilding(person);
				if (building != null) {
					result.add((MedicalCare) building.getFunction(BuildingFunction.MEDICAL_CARE));
				}
			}
			catch (Exception e) {
				logger.severe("MedicalAssistance.getNeedyMedicalAids(): " + e.getMessage());
			}
		}
		else if (location == LocationSituation.IN_VEHICLE) {
			Vehicle vehicle = person.getVehicle();
			if (vehicle instanceof Medical) {
				MedicalAid aid = ((Medical) vehicle).getSickBay();
				if ((aid != null) && isNeedyMedicalAid(aid)) {
					result.add(aid);
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a medical aid needs work.
	 * @return true if medical aid has patients waiting and is not malfunctioning.
	 */
	private static boolean isNeedyMedicalAid(MedicalAid aid) {
		if (aid == null) {
			throw new IllegalArgumentException("aid is null");
		}

		boolean waitingProblems = (aid.getProblemsAwaitingTreatment().size() > 0);
		boolean malfunction = getMalfunctionable(aid).getMalfunctionManager().hasMalfunction();
		return waitingProblems && !malfunction;
	}

	/**
	 * Gets the malfunctionable associated with the medical aid.
	 * @param aid The medical aid
	 * @return the associated Malfunctionable
	 */
	private static Malfunctionable getMalfunctionable(MedicalAid aid) {
		Malfunctionable result = null;

		if (aid instanceof SickBay) {
			result = ((SickBay) aid).getVehicle();
		}
		else if (aid instanceof MedicalCare) {
			result = ((MedicalCare) aid).getBuilding();
		}
		else {
			result = (Malfunctionable) aid;
		}

		return result;
	}

	/**
	 * Check for accident in infirmary.
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		Malfunctionable entity = getMalfunctionable(medical);

		double chance = .001D;

		// Medical skill modification.
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
		if (skill <= 3) {
			chance *= (4 - skill);
		}
		else {
			chance /= (skill - 2);
		}

		// Modify based on the entity's wear condition.
		chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			// logger.info(person.getName() + " has accident during medical assistance.");
			entity.getMalfunctionManager().accident();
		}
	}

	@Override
	public void endTask() {
		super.endTask();

		// Stop treatment.
		try {
			medical.stopTreatment(problem);
		}
		catch (Exception e) {
			logger.severe("MedicalAssistance.endTask(): " + e.getMessage());
		}
	}

	/**
	 * Gets the medical aid the person is using for this task.
	 * @return medical aid or null.
	 */
	public MedicalAid getMedicalAid() {
		return medical;
	}

	/**
	 * Gets the least crowded medical care building with a patient that needs treatment.
	 * @param person the person looking for a medical care building.
	 * @return medical care building or null if none found.
	 */
	public static Building getMedicalAidBuilding(Person person) {
		Building result = null;

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			Settlement settlement = person.getSettlement();
			BuildingManager manager = settlement.getBuildingManager();
			List<Building> medicalBuildings = manager.getBuildings(BuildingFunction.MEDICAL_CARE);

			List<Building> needyMedicalBuildings = new ArrayList<Building>();
			Iterator<Building> i = medicalBuildings.iterator();
			while (i.hasNext()) {
				Building building = i.next();
				MedicalCare medical = (MedicalCare) building.getFunction(BuildingFunction.MEDICAL_CARE);
				if (isNeedyMedicalAid(medical)) {
					needyMedicalBuildings.add(building);
				}
			}

			List<Building> bestMedicalBuildings = BuildingManager.getNonMalfunctioningBuildings(needyMedicalBuildings);
			bestMedicalBuildings = BuildingManager.getLeastCrowdedBuildings(bestMedicalBuildings);

			if (bestMedicalBuildings.size() > 0) {
				Map<Building, Double> medBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, bestMedicalBuildings);
				result = RandomUtil.getWeightedRandomObject(medBuildingProbs);
			}
		}
		else {
			throw new IllegalStateException("MedicalAssistance.getMedicalAidBuilding(): Person is not in settlement.");
		}

		return result;
	}

	/**
	 * Checks to see if there is a doctor in the settlement or vehicle the person is in.
	 * @param person the person checking.
	 * @return true if a doctor nearby.
	 */
	public static boolean isThereADoctorInTheHouse(Person person) {
		boolean result = false;

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			Iterator<Person> i = person.getSettlement().getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if ((inhabitant != person) && (inhabitant.getMind().getJob())
						instanceof Doctor) {
					result = true;
				}
			}
		}
		else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
			if (person.getVehicle() instanceof Rover) {
				Rover rover = (Rover) person.getVehicle();
				Iterator<Person> i = rover.getCrew().iterator();
				while (i.hasNext()) {
					Person crewmember = i.next();
					if ((crewmember != person) && (crewmember.getMind().getJob()
							instanceof Doctor)) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.MEDICINE);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MEDICINE);
		return results;
	}


	public void produceMedicalWaste() {
        Unit containerUnit = person.getContainerUnit();
        if (containerUnit != null) {
            //Inventory inv = containerUnit.getInventory();
            Storage.storeAnResource(AVERAGE_MEDICAL_WASTE, MedicalCare.toxicWasteAR, containerUnit.getInventory());
            //System.out.println("MedicalAssistance.java : adding Toxic Waste : "+ AVERAGE_MEDICAL_WASTE);
	     }
	}

/*
	// 2015-02-06 Added storeAnResource()
	public boolean storeAnResource(double amount, String name, Inventory inv) {
		boolean result = false;
		try {
			AmountResource ar = AmountResource.findAmountResource(name);
			double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);

			if (remainingCapacity < amount) {
			    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
				amount = remainingCapacity;
				result = false;
			    //logger.info("addHarvest() : storage is full!");
			}
			else {
				inv.storeAmountResource(ar, amount, true);
				inv.addAmountSupplyAmount(ar, amount);
				result = true;
			}
		} catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
		}

		return result;
	}
*/
	
	@Override
	public void destroy() {
		super.destroy();

		medical = null;
		problem = null;
	}
}