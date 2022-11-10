/*
 * Mars Simulation Project
 * ExamineBodyMeta.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ExamineBody;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Meta task for the ExamineBody task.
 */
public class ExamineBodyMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.examineBody"); //$NON-NLS-1$

	private static MedicalManager medicalManager = Simulation.instance().getMedicalManager();

    public ExamineBodyMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);

		setTrait(TaskTrait.MEDICAL);
		setPreferredJob(JobType.MEDICS);
	}

	@Override
	public Task constructInstance(Person person) {
		return new ExamineBody(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0D;

		if (person.isInSettlement()) {

	        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
	        	return 0;
	        }

			int num = medicalManager.getPostmortemExams(person.getSettlement()).size();

			// Get the local medical aids to use.
			if (num > 0 && hasNeedyMedicalAids(person)) {
				result = 500.0 + 300 * num;
			}

			// Effort-driven task modifier.
			result *= getPersonModifier(person);

			double skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

			if (skill == 0)
				skill = .5;
			result *= skill;


			if (result < 0)
				result = 0;

		}

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
			result = hasNeedyMedicalAidsAtSettlement(person);
		} else if (person.isInVehicle()) {
			result = hasNeedyMedicalAidsInVehicle(person);
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
	private boolean hasNeedyMedicalAidsAtSettlement(Person person) {

		// Check all medical care buildings.
		Iterator<Building> i = person.getSettlement().getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE)
				.iterator();
		while (i.hasNext()) {
			// Check if there are any sick beds at building.
			MedicalCare medicalCare = i.next().getMedical();
			if (medicalCare.hasEmptyBeds()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if there are medical aids in a vehicle that have people waiting for
	 * treatment.
	 *
	 * @param person  the person.
	 * @param vehicle the vehicle.
	 * @return true if needy medical aids.
	 */
	private boolean hasNeedyMedicalAidsInVehicle(Person person) {
		if (VehicleType.isRover(person.getVehicle().getVehicleType())) {
			Rover rover = (Rover) person.getVehicle();
			if (rover.hasSickBay()) {
				SickBay sickBay = rover.getSickBay();
                return sickBay.hasEmptyBeds();
			}
		}

		return false;
	}
}
