/*
 * Mars Simulation Project
 * Job.java
 * @date 2022-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.job.util;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The Job class represents a person's job.
 */
public abstract class Job {

	private Map<RoleType, Double> jobProspects;
	private JobType jobType;

	protected static MissionManager missionManager;
	protected static UnitManager unitManager;
	
	/**
	 * Constructor.
	 * 
	 * @param jobProspects 
	 * @param name the name of the job.
	 */
	protected Job(JobType jobType, Map<RoleType, Double> jobProspects) {
		this.jobType = jobType;
		this.jobProspects = jobProspects;
	}

	public JobType getType() {
		return jobType;
	}
	
	/**
	 * Gets a person/robot's capability to perform this job.
	 * 
	 * @param person/robot the person/robot to check.
	 * @return capability (min 0.0).
	 */
	public abstract double getCapability(Person person);

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public abstract double getSettlementNeed(Settlement settlement);

	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param u {@link UnitManager}
	 * @param m {@link MissionManager}
	 */
	public static void initializeInstances(UnitManager u, MissionManager m) {
		unitManager = u;
		missionManager = m;
	}

	/**
	 * Builds a Map to cover specialist and crew roles.
	 * 
	 * @return
	 */
	protected static final Map<RoleType, Double> buildRoleMap(
		  double agr, double com, double eng, double mis, double log, double res, double saf, double sci) {
		
		Map<RoleType, Double> m = new EnumMap<>(RoleType.class);
		m.put(RoleType.AGRICULTURE_SPECIALIST, agr);
		m.put(RoleType.COMPUTING_SPECIALIST, com);
		m.put(RoleType.ENGINEERING_SPECIALIST, eng);
		m.put(RoleType.MISSION_SPECIALIST, mis);
		
		m.put(RoleType.LOGISTIC_SPECIALIST, log);
		m.put(RoleType.RESOURCE_SPECIALIST, res);
		m.put(RoleType.SAFETY_SPECIALIST, saf);
		m.put(RoleType.SCIENCE_SPECIALIST, sci);	

		m.put(RoleType.CREW_ENGINEER, eng);
		m.put(RoleType.CREW_SAFETY_OFFICER, saf);
		m.put(RoleType.CREW_OPERATION_OFFICER, log);
		m.put(RoleType.CREW_SCIENTIST, sci);
		
		return Collections.unmodifiableMap(m);
	}
	
	public Map<RoleType, Double> getRoleProspects() {
		return jobProspects;
	}

	/**
	 * Gets the science demand from buildings.
	 * 
	 * @param settlement
	 * @param type
	 * @param scale
	 * @return
	 */
	protected double getBuildingScienceDemand(Settlement settlement, ScienceType type, double scale) {
		double result = 0D;
		
		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.RESEARCH)) {
			Research lab = building.getResearch();
			if (lab.hasSpecialty(type)) {
				result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / scale);
			}
		}	
		return result;
	}

	/**
	 * Calculates the Science demand for Mission on the road. 
	 * 
	 * @param settlement
	 * @param type
	 * @param scale
	 * @return
	 */
	protected double getMissionScienceDemand(Settlement settlement, ScienceType type, double scale) {
		double result = 0D;
		Collection<Vehicle> parked = settlement.getParkedGaragedVehicles();
		
		for (Mission mission : missionManager.getMissionsForSettlement(settlement)) {
			if (mission instanceof RoverMission rm) {
				Rover rover = rm.getRover();
				if ((rover != null) && !parked.contains(rover)) {
					result += getRoverScienceScore(rover, type, scale);
				}
			}
		}
		
		return result;
	}

	/**
	 * Calculates the science demand for all the Parked vehicles at a settlement. 
	 * 
	 * @param settlement
	 * @param type
	 * @param scale
	 * @return
	 */
	protected double getParkedVehicleScienceDemand(Settlement settlement, ScienceType type, double scale) {
		double result = 0D;
		
		for (Vehicle vehicle : settlement.getParkedGaragedVehicles()) {
			if (vehicle instanceof Rover r) {
				result += getRoverScienceScore(r, type, scale);
			}
		}
		
		return result;
	}

	/**
	 * Extracts the science score from a rover.
	 * 
	 * @param rover
	 * @param type
	 * @param scale
	 * @return
	 */
	private static double getRoverScienceScore(Rover rover, ScienceType type, double scale) {
		if (rover.hasLab()) {
			Lab lab = rover.getLab();
			if (lab.hasSpecialty(type)) {
				return (lab.getLaboratorySize() * lab.getTechnologyLevel() / scale);
			}
		}
		return 0D;
	}
}
