/**
 * Mars Simulation Project
 * Medicbot.java
 * @version 3.07 2015-02-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;

/** 
 * The Medicbot class represents a job for an medical treatment expert.
 */
public class Medicbot
extends RobotJob
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Constructor. */
	public Medicbot() {
		// Use Job constructor
		super(Medicbot.class);

		jobTasks.add(PrescribeMedication.class);
		//jobTasks.add(TreatMedicalPatient.class);
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Robot robot) {

		double result = 0D;

		int kkill = robot.getBotMind().getSkillManager().getSkillLevel(SkillType.MEDICINE);
		result = kkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttribute.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = 0D;

		// Add total population / 10
		int population = settlement.getAllAssociatedPeople().size();
		result+= population / 10D;

		// Add (labspace * tech level) / 2 for all labs with medical specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
	

		// Add (tech level / 2) for all medical infirmaries.
		List<Building> medicalBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.MEDICAL_CARE);
		Iterator<Building> j = medicalBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			MedicalCare infirmary = (MedicalCare) building.getFunction(BuildingFunction.MEDICAL_CARE);
			result+= (double) infirmary.getTechLevel() ;
		}			

		return result;	
	}

}