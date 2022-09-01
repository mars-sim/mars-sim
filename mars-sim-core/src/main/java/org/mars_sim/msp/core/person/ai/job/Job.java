/*
 * Mars Simulation Project
 * Job.java
 * @date 2022-09-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Job class represents a person's job.
 */
public abstract class Job {

	private static final String JOB_STR = "job.";
	private static final String MALE_STR = "male.";
	private static final String FEMALE_STR = "female.";
	private static final String UNKNOWN = "unknown.";


	private Map<RoleType, Double> jobProspects;
	private JobType jobType;

	protected static MissionManager missionManager = Simulation.instance().getMissionManager();
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();
	
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

	/**
	 * Gets the job's internationalized name for display in user interface. This
	 * uses directly the name of the class that extends {@link Job}, so take care
	 * not to rename those, or if you do then remember to change the keys in
	 * <code>messages.properties</code> accordingly.
	 * 
	 * @param gender {@link GenderType}
	 * @return name
	 */
	public String getName(GenderType gender) {
		StringBuilder key = new StringBuilder().append(JOB_STR); // $NON-NLS-1$
		switch (gender) {
		case MALE:
			key.append(MALE_STR);
			break; // $NON-NLS-1$
		case FEMALE:
			key.append(FEMALE_STR);
			break; // $NON-NLS-1$
		default:
			key.append(UNKNOWN);
			break; // $NON-NLS-1$
		}
		key.append(jobType.getName());
		return Msg.getString(key.toString().replace(" ", "")); // $NON-NLS-1$
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
	 * Builds a Map to cover the Specialist RoleTypes.
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

		return Collections.unmodifiableMap(m);
	}
	
	public Map<RoleType,Double> getRoleProspects() {
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
		
		for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH)) {
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
		Collection<Vehicle> parked = settlement.getParkedVehicles();
		
		for (Mission mission : missionManager.getMissionsForSettlement(settlement)) {
			if (mission instanceof RoverMission) {
				Rover rover = ((RoverMission) mission).getRover();
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
		
		for (Vehicle vehicle : settlement.getParkedVehicles()) {
			if (vehicle instanceof Rover) {
				result += getRoverScienceScore((Rover) vehicle, type, scale);
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
