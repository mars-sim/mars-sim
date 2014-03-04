/**
 * Mars Simulation Project
 * Botanist.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.ResearchScientificStudy;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.core.structure.building.function.Research;

/** 
 * The Botanist class represents a job for a botanist.
 */
public class Botanist
extends Job
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static Logger logger = Logger.getLogger(Botanist.class.getName());

	/**
	 * Constructor
	 */
	public Botanist() {
		// Use Job constructor
		super("Botanist");
		
		// Add botany-related tasks.
        jobTasks.add(AssistScientificStudyResearcher.class);
        jobTasks.add(CompileScientificStudyResults.class);
        jobTasks.add(InviteStudyCollaborator.class);
        jobTasks.add(PeerReviewStudyPaper.class);
        jobTasks.add(PerformLaboratoryExperiment.class);
        jobTasks.add(PerformLaboratoryResearch.class);
        jobTasks.add(ProposeScientificStudy.class);
        jobTasks.add(ResearchScientificStudy.class);
        jobTasks.add(RespondToStudyInvitation.class);
        jobTasks.add(TendGreenhouse.class);
        jobTasks.add(DigLocalIce.class);
		
		// Add botanist-related missions.
        jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);	
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
        jobMissionJoins.add(BuildingSalvageMission.class);
        jobMissionStarts.add(EmergencySupplyMission.class);
        jobMissionJoins.add(EmergencySupplyMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int botanySkill = person.getMind().getSkillManager().getSkillLevel(SkillType.BOTANY);
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
		
		// Add (labspace * tech level) / 2 for all labs with botany specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
		    Building building = i.next();
		    Research lab = (Research) building.getFunction(Research.NAME);
		    if (lab.hasSpeciality(ScienceType.BOTANY)) 
		        result += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D;
		}

		// Add (growing area in greenhouses) / 25
		List<Building> greenhouseBuildings = settlement.getBuildingManager().getBuildings(Farming.NAME);
		Iterator<Building> j = greenhouseBuildings.iterator();
		while (j.hasNext()) {
		    Building building = j.next();
		    Farming farm = (Farming) building.getFunction(Farming.NAME);
		    result += (farm.getGrowingArea() / 25D);
		}

		// Multiply by food value at settlement.
		//Good foodGood = GoodsUtil.getResourceGood(AmountResource.findAmountResource("food"));
		//double foodValue = settlement.getGoodsManager().getGoodValuePerItem(foodGood);
		//result *= foodValue;
		
		return result;	
	}	
}