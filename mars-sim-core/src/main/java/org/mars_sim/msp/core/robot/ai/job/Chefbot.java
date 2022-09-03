/*
 * Mars Simulation Project
 * Chefbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Teach;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/** 
 * The Chefbot class represents a job for a chefbot.
 */
public class Chefbot
extends RobotJob
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** constructor. */
	public Chefbot() {
		// Use Job constructor
		super(Chefbot.class);

		// Add chef-related tasks.		
		jobTasks.add(CookMeal.class);
		jobTasks.add(PrepareDessert.class);
		jobTasks.add(ProduceFood.class);
		jobTasks.add(Sleep.class);
		jobTasks.add(Teach.class);
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the person to check.
	 * @return capability .
	 */	
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
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 15D;

		// Add all kitchen work space in settlement.
		List<Building> kitchenBuildings = settlement.getBuildingManager()
				.getBuildings(FunctionType.COOKING);
		Iterator<Building> i = kitchenBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Cooking kitchen = (Cooking) building.getFunction(FunctionType.COOKING); 
			result += (double) kitchen.getCookCapacity();
		}

		// Add all kitchen work space in settlement.
		List<Building> dessertBuildings = settlement.getBuildingManager()
				.getBuildings(FunctionType.PREPARING_DESSERT);
		Iterator<Building> ii = dessertBuildings.iterator();
		while (ii.hasNext()) {
			Building building = ii.next();
			PreparingDessert kitchen = (PreparingDessert) building.getFunction(FunctionType.PREPARING_DESSERT); 
			result += (double) kitchen.getCookCapacity();
		}
		
		int population = settlement.getIndoorPeopleCount();
		result += population / 12.5;

		return result;			
	}
}
