/*
 * Mars Simulation Project
 * Botanist.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * The Botanist class represents a job for a botanist.
 */
class Botanist
extends Job {
	
	/**
	 * Constructor.
	 */
	public Botanist() {
		// Use Job constructor
		super(JobType.BOTANIST, Job.buildRoleMap(25.0, 5.0, 5.0, 5.0, 5.0, 20.0, 5.0, 35.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0;

		int botanySkill = person.getSkillManager().getSkillLevel(SkillType.BOTANY);
		result = botanySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result += result * ((averageAptitude - 100D) / 100D);
		
//		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
//		result+= result * ((averageAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) 
			result = result/2D;

//		System.out.println(person + " botanist : " + Math.round(result*100.0)/100.0);
		
		return result;
	}


	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = .1;

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level) / 2 for all labs with botany specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.BOTANY)) {
				result += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 12D;
			}
		}

		// Add (growing area in greenhouses) / 25
		List<Building> greenhouseBuildings = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		Iterator<Building> j = greenhouseBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			Farming farm = building.getFarming();
			result += (farm.getGrowingArea() / 100D);
		}

		// Multiply by food value at settlement.
		//Good foodGood = GoodsUtil.getResourceGood(AmountResource.findAmountResource(LifeSupport.FOOD));
		//double foodValue = settlement.getGoodsManager().getGoodValuePerItem(foodGood);
		//result *= foodValue;
		
		result = (result + population / 6D) / 2.0;
		
//		System.out.println(settlement + " Botany Need: " + result);

		return result;
	}
}
