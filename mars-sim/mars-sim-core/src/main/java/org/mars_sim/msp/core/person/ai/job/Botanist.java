/**
 * Mars Simulation Project
 * Botanist.java
 * @version 2.86 2009-05-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/** 
 * The Botanist class represents a job for a botanist.
 */
public class Botanist extends Job implements Serializable {
    
    	private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.job.Botanist";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	/**
	 * Constructor
	 */
	public Botanist() {
		// Use Job constructor
		super("Botanist");
		
		// Add botany-related tasks.
		jobTasks.add(TendGreenhouse.class);
		
		// Add botanist-related missions.
        jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);	
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int botanySkill = person.getMind().getSkillManager().getSkillLevel(Skill.BOTANY);
		result = botanySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result+= result * ((averageAptitude - 50D) / 100D);
		
		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
		
		return result;
	}
	
	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;
		
		// Add (labspace * tech level) / 2 for all labs with botany specialities.
		List laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
		Iterator i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			try {
				Research lab = (Research) building.getFunction(Research.NAME);
				if (lab.hasSpeciality(Skill.BOTANY)) 
					result += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D;
			}
			catch (BuildingException e) {
			    logger.log(Level.SEVERE,"Botanist.getSettlementNeed()", e);
			}
		}
		
		// Add (growing area in greenhouses) / 100
		List greenhouseBuildings = settlement.getBuildingManager().getBuildings(Farming.NAME);
		Iterator j = greenhouseBuildings.iterator();
		while (j.hasNext()) {
			Building building = (Building) j.next();
			try {
				Farming farm = (Farming) building.getFunction(Farming.NAME);
				result += (farm.getGrowingArea() / 100D);
			}
			catch (BuildingException e) {
			    logger.log(Level.SEVERE,"Botanist.getSettlementNeed()", e);
			}
		}
		
		// Multiply by food value at settlement.
		try {
			Good foodGood = GoodsUtil.getResourceGood(AmountResource.findAmountResource("food"));
			double foodValue = settlement.getGoodsManager().getGoodValuePerItem(foodGood);
			result *= foodValue;
		}
		catch (Exception e) {
		    logger.log(Level.SEVERE, "Botanist.getSettlementNeed()", e);
		}
		
		return result;	
	}	
}