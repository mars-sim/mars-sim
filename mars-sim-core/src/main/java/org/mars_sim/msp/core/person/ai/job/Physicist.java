/**
 * Mars Simulation Project
 * Physicist.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Physicist class represents a job for a physicist.
 */
class Physicist extends Job {
	
	/** Constructor. */
	public Physicist() {
		// Use Job constructor
		super(JobType.PHYSICIST, Job.buildRoleMap(5.0, 15.0, 10.0, 10.0, 15.0, 15.0, 30.0));


		// Add physicist-related missions.
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int physicsSkill = person.getSkillManager().getSkillLevel(SkillType.PHYSICS);
		result = physicsSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) 
			result = 0D;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2D) for all labs with physics specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.PHYSICS)) {
				result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 12D);
			}
		}

		Iterator<Mission> k = missionManager.getMissionsForSettlement(settlement).iterator();
		while (k.hasNext()) {
			Mission mission = k.next();
			if (mission instanceof RoverMission) {
				Rover rover = ((RoverMission) mission).getRover();
				if ((rover != null) && !settlement.getParkedVehicles().contains(rover)) {
					if (rover.hasLab()) {
						Lab lab = rover.getLab();
						if (lab.hasSpecialty(ScienceType.PHYSICS)) {
							result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 12D);
						}
					}
				}
			}
		}
		
		result = (result + population / 16D) / 2.0;
		
//		System.out.println(settlement + " Physicist need: " + result);
		
		return result;
	}
}
