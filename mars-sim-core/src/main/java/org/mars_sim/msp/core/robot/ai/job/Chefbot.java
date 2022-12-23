/*
 * Mars Simulation Project
 * Chefbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/** 
 * The Chefbot class represents a job for a chefbot.
 */
public class Chefbot extends RobotJob {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final double PREP_AREA_PER_CHEF = 8;
	private static final int CHEF_BOT_THRESHOLD = 10;

	/** constructor. */
	public Chefbot() {
		// Use Job constructor
		super();
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the person to check.
	 * @return capability .
	 */	
	@Override
	public double getCapability(Robot robot) {

		double result = 10D;

		int cookingSkill = robot.getSkillManager().getSkillLevel(SkillType.COOKING);
		result += cookingSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);	

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * For Chef it is base don the number of cooking oppotunities.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	@Override
	public double getOptimalCount(Settlement settlement) {
		int prepArea = 0;

		if (settlement.getAllAssociatedPeople().size() >= CHEF_BOT_THRESHOLD) {
			// Add all kitchen work space in settlement.
			for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.COOKING)) {
				Cooking kitchen = (Cooking) building.getFunction(FunctionType.COOKING); 
				prepArea += kitchen.getCookCapacity();
			}

			// Add all kitchen work space in settlement.
			for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.PREPARING_DESSERT)) {
				PreparingDessert kitchen = (PreparingDessert) building.getFunction(FunctionType.PREPARING_DESSERT); 
				prepArea += kitchen.getCookCapacity();
			}
		}
		return (prepArea/PREP_AREA_PER_CHEF);			
	}
}
