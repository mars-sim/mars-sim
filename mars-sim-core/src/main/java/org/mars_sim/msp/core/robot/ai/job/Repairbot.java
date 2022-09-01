/*
 * Mars Simulation Project
 * Repairbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.RepairInsideMalfunction;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Teach;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

public class Repairbot extends RobotJob implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public Repairbot() {
		// Use Job constructor
		super(Repairbot.class);

		// Add technician-related tasks.
		jobTasks.add(Maintenance.class);
		jobTasks.add(MaintainGroundVehicleGarage.class);
		// jobTasks.add(MaintenanceEVA.class); //check to what extend the walking bug is
		// affecting the outdoor portion of this task
		// jobTasks.add(RepairEVAMalfunction.class); //check to what extend the walking
		// bug is affecting the outdoor portion of this task
		jobTasks.add(RepairInsideMalfunction.class);
		jobTasks.add(Sleep.class);
		jobTasks.add(Teach.class);
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = 15D;

		// Add number of buildings in settlement.
		result += settlement.getBuildingManager().getNumBuildings() / 3D;

		// Add number of vehicles parked at settlement.
		result += settlement.getParkedVehicleNum() / 3D;

		return result;
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the person to check.
	 * @return capability.
	 */
	public double getCapability(Robot robot) {

		double result = 0D; // robot should be less capable than the person counterpart

		int mechanicSkill = robot.getSkillManager().getSkillLevel(SkillType.MECHANICS);
		result += mechanicSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// if (robot.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}
}
