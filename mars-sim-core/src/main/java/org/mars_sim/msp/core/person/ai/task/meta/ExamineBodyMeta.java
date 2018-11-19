/**
 * Mars Simulation Project
 * ExamineBodyMeta.java
 * @version 3.1.0 2018-11-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.task.ExamineBody;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalAid;
import org.mars_sim.msp.core.person.health.Treatment;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ExamineBody task.
 */
public class ExamineBodyMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.examineBody"); //$NON-NLS-1$

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new ExamineBody(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		if (person.isInSettlement()) {
			int num = Simulation.instance().getMedicalManager().getPostmortemExams(person.getSettlement()).size();
			// Get the local medical aids to use.
			if (hasNeedyMedicalAids(person)
				&& num > 0) {
				result += 500D + 300 * num;
			}

			// Effort-driven task modifier.
			result *= person.getPerformanceRating();

			// Job modifier.
			if (JobManager.numJobs(Doctor.class, person.getSettlement()) > 0) {
				Job job = person.getMind().getJob();
				if (job != null) {
					result *= job.getStartTaskProbabilityModifier(ExamineBody.class);
				}
			}
			else {
				double skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
				if (skill == 0)
					skill = .5;
				result *= skill;
			}

			result = result + result * person.getPreference().getPreferenceScore(this) / 5D;

			if (result < 0)
				result = 0;

		}
//		System.out.println(person + " : " + Math.round(result*10.0)/10.0);
		return result;
	}

	/**
	 * Checks if there are local medical aids that have people waiting for
	 * treatment.
	 * 
	 * @param person the person.
	 * @return true if needy medical aids.
	 */
	private boolean hasNeedyMedicalAids(Person person) {

		boolean result = false;

		if (person.isInSettlement()) {
			result = hasNeedyMedicalAidsAtSettlement(person, person.getSettlement());
		} else if (person.isInVehicle()) {
			result = hasNeedyMedicalAidsInVehicle(person, person.getVehicle());
		}

		return result;
	}

	/**
	 * Checks if there are medical aids at a settlement that have people waiting for
	 * treatment.
	 * 
	 * @param person     the person.
	 * @param settlement the settlement.
	 * @return true if needy medical aids.
	 */
	private boolean hasNeedyMedicalAidsAtSettlement(Person person, Settlement settlement) {

		boolean result = false;

		// Check all medical care buildings.
		Iterator<Building> i = person.getSettlement().getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE)
				.iterator();
		while (i.hasNext() && !result) {
			Building building = i.next();
			// Check if building currently has a malfunction.
			boolean malfunction = building.getMalfunctionManager().hasMalfunction();

			if (!malfunction) {
				// Check if there are any sick beds at building.
				MedicalCare medicalCare = building.getMedical();
				if (medicalCare.hasEmptyBeds()) {
					return result;	
				}
			}
		}

		return result;
	}

	/**
	 * Checks if there are medical aids in a vehicle that have people waiting for
	 * treatment.
	 * 
	 * @param person  the person.
	 * @param vehicle the vehicle.
	 * @return true if needy medical aids.
	 */
	private boolean hasNeedyMedicalAidsInVehicle(Person person, Vehicle vehicle) {

		boolean result = false;

		if (person.getVehicle() instanceof Rover) {
			Rover rover = (Rover) person.getVehicle();
			if (rover.hasSickBay()) {
				SickBay sickBay = rover.getSickBay();
				if (sickBay.hasEmptyBeds()) {
					return true;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a medical aid has waiting people with health problems that the
	 * person can treat.
	 * 
	 * @param person the person.
	 * @param aid    the medical aid.
	 * @return true if treatable health problems.
	 */
	private boolean hasTreatableHealthProblems(Person person, MedicalAid aid) {

		boolean result = false;

		// Get the person's medical skill.
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

		// Check if there are any treatable health problems awaiting treatment.
		Iterator<HealthProblem> j = aid.getProblemsAwaitingTreatment().iterator();
		while (j.hasNext() && !result) {
			HealthProblem problem = j.next();
			Treatment treatment = problem.getIllness().getRecoveryTreatment();
			if (treatment != null) {
				int requiredSkill = treatment.getSkill();
				if (skill >= requiredSkill) {
					result = true;
				}
			}
		}

		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}