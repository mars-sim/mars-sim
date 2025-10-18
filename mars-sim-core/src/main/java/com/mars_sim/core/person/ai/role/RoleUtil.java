/*
 * Mars Simulation Project
 * RoleUtil.java
 * @date 2023-11-15
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.job.util.JobSpec;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.training.TrainingType;
import com.mars_sim.core.structure.ChainOfCommand;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The RoleUtil class determines the roles of the settlers in a simulation.
 */
public class RoleUtil {


	private static Map<JobType, Map<RoleType, Double>> roleWeights
							= new EnumMap<>(JobType.class);

	private static List<RoleType> specalistRoles;

	private static List<RoleType> crewRoles;

	private static List<RoleType> chiefRoles;

	private static List<RoleType> councilRoles;
	
	private static List<RoleType> leadershipRoles;

	
	private RoleUtil() {
	}

	/**
	 * Initializes the role prospect array.
	 */
	public static void initialize() {
		if (roleWeights.isEmpty()) {
			for (JobSpec j : JobUtil.getJobs()) {
				JobType id = j.getType();
				roleWeights.put(id, j.getRoleProspects());
			}
		}

		// Cache the specialists
		specalistRoles = Arrays.stream(RoleType.values())
								.filter(RoleType::isSpecialist)
								.toList();
		
		// Cache the crew roles
		crewRoles = Arrays.stream(RoleType.values())
								.filter(RoleType::isCrew)
								.toList();
		
		// Cache the crew roles
		chiefRoles = Arrays.stream(RoleType.values())
								.filter(RoleType::isChief)
								.toList();	
		
		// Cache the council roles
		councilRoles = Arrays.stream(RoleType.values())
								.filter(RoleType::isCouncil)
								.toList();
		
		// Cache the leadership roles
		leadershipRoles = Stream.concat(chiefRoles.stream(), 
								councilRoles.stream())
                				.collect(Collectors.toList());
		
	}

	public static boolean isRoleWeightsInitialized() {
        return !roleWeights.isEmpty();
	}

	/**
	 * Returns a list of specialist roles.
	 * 
	 * @return
	 */
	public static List<RoleType> getSpecialists() {
		return specalistRoles;
	}

	/**
	 * Returns a list of crew roles.
	 * 
	 * @return
	 */
	public static List<RoleType> getCrewRoles() {
		return crewRoles;
	}
	
	/**
	 * Returns a list of council roles.
	 * 
	 * @return
	 */
	public static List<RoleType> getCouncil() {
		return councilRoles;
	}

	/**
	 * Returns a list of chief roles.
	 * 
	 * @return
	 */
	public static List<RoleType> getChiefs() {
		return chiefRoles;
	}

	/**
	 * Returns a list of leadership roles.
	 * 
	 * @return
	 */
	public static List<RoleType> getLeadership() {
		return leadershipRoles;
	}
	
	/**
	 * Finds the best role for a given person in a settlement.
	 *
	 * @param settlement
	 * @param p
	 * @return
	 */
	public static RoleType findBestRole(Person p) {
		RoleType selectedRole = null;
		double highestWeight = 0;

		ChainOfCommand chain = p.getSettlement().getChainOfCommand();
		
		int pop = p.getSettlement().getInitialPopulation();
		
		JobType job = p.getMind().getJobType();
		Map<RoleType, Double> weights = roleWeights.get(job);

		// Use a Tree map so entries sorting in increasing order.
		List<RoleType> roles = new ArrayList<>();
		int leastNum = 0;
		RoleType leastFilledRole = null;
		
		List<RoleType> types = null;
				
		if (pop <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
			types = crewRoles;
		}
		
		else {
			types = specalistRoles;
		}
		
		for (RoleType rt: types) {
			int num = chain.getNumFilled(rt);
			if (leastNum >= num) {
				leastNum = num;
				leastFilledRole = rt;
			}
			roles.add(rt);
		}

		// Move the least polluted role to the front
		if (leastFilledRole != null) {
			roles.remove(leastFilledRole);
			roles.add(0, leastFilledRole);
		}

		for (RoleType rt : roles) {
			boolean isRoleAvailable = chain.isRoleAvailable(rt);

			if (isRoleAvailable) {
				double jobScore = weights.get(rt);
				double trainingScore = getTrainingScore(p, rt);
				double totalScore = jobScore + trainingScore;

				if (highestWeight < totalScore) {
					highestWeight = totalScore;
					// Pick the role based on the highest weight
					selectedRole = rt;
				}
			}
		}

		// No role ????
		if (selectedRole == null) {
			int idx = RandomUtil.getRandomInt(roles.size() - 1);
			selectedRole = roles.get(idx);
		}
		return selectedRole;
	}

	/**
	 * Finds the person who is the best fit for a given role from a pool of candidates.
	 *
	 * @param role
	 * @param candidates
	 * @return
	 */
	public static Person findBestFit(RoleType role, List<Person> candidates) {
		Person bestPerson = null;
		double bestScore = 0;

		for (Person p : candidates) {

			JobType job = p.getMind().getJobType();
			Map<RoleType, Double> weights = roleWeights.get(job);

			double score = getRolePropectScore(p, role, weights);

			if ((bestPerson == null) || (score > bestScore)) {
				bestScore = score;
				bestPerson = p;
			}
		}

		return bestPerson;
	}

	/**
	 * Gets the role prospect score of a person on a particular role.
	 *
	 * @param person
	 * @param role
	 * @return the role prospect score
	 */
	public static double getRolePropectScore(Person person, RoleType role, Map<RoleType, Double> weights) {

		double jobScore = Math.round(weights.get(role) * 10.0)/10.0;

		double trainingScore = getTrainingScore(person, role);

		return jobScore + trainingScore;
	}

	/**
	 * Gets the training score of a person on a particular role.
	 *
	 * @param person
	 * @param role
	 * @return the training score
	 */
	public static double getTrainingScore(Person person, RoleType role) {

		List<TrainingType> trainings = person.getTrainings();

		// Really should be passed in
		PersonConfig pc = SimulationConfig.instance().getPersonConfig();
		
		int trainingScore = 0;
		for (TrainingType tt : trainings) {
			trainingScore += pc.getTrainingModifier(role, tt);
		}

		return trainingScore;

	}

	/**
	 * Takes a Chief role type and return the associated specialty role.
	 * If the input role is not a Chief a null is returned.
	 * 
	 * @param roleType
	 * @return
	 */
	public static RoleType getChiefSpeciality(RoleType roleType) {
		RoleType candidateType = null;
		switch (roleType) {
	        case CHIEF_OF_AGRICULTURE:
	            candidateType = RoleType.AGRICULTURE_SPECIALIST;
	            break;

	        case CHIEF_OF_COMPUTING:
	        	candidateType = RoleType.COMPUTING_SPECIALIST;
	        	break;

	        case CHIEF_OF_ENGINEERING:
	        	candidateType = RoleType.ENGINEERING_SPECIALIST;
	        	break;

	        case CHIEF_OF_LOGISTIC_OPERATION:
	        	candidateType = RoleType.LOGISTIC_SPECIALIST;
	        	break;

	        case CHIEF_OF_MISSION_PLANNING:
	        	candidateType = RoleType.MISSION_SPECIALIST;
	        	break;

	        case CHIEF_OF_SAFETY_HEALTH_SECURITY:
	        	candidateType = RoleType.SAFETY_SPECIALIST;
	        	break;

	        case CHIEF_OF_SCIENCE:
	        	candidateType = RoleType.SCIENCE_SPECIALIST;
	        	break;

	        case CHIEF_OF_SUPPLY_RESOURCE:
	        	candidateType = RoleType.RESOURCE_SPECIALIST;
	        	break;

	        default:
	    }
		return candidateType;
	}

	/**
	 * Gets a list of role suitable for a settlement of a certain size
	 *
	 * @return
	 */
	public static List<RoleType> getRoles(int pop) {

		List<RoleType> roles = new ArrayList<>();

		if (pop <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
			roles.add(RoleType.COMMANDER);
			roles.addAll(crewRoles);
		}

		else if (pop <= ChainOfCommand.POPULATION_WITH_SUB_COMMANDER) {
			roles.add(RoleType.COMMANDER);
			roles.add(RoleType.SUB_COMMANDER);
			roles.addAll(specalistRoles);
		}

		else if (pop <= ChainOfCommand.POPULATION_WITH_CHIEFS) {
			for (RoleType r : RoleType.values()) {
				if (r != RoleType.PRESIDENT
						&& r != RoleType.MAYOR
						&& r != RoleType.ADMINISTRATOR)
					roles.add(r);
			}
		}

		else if (pop <= ChainOfCommand.POPULATION_WITH_ADMINISTRATOR) {
			for (RoleType r : RoleType.values()) {
				if (r != RoleType.PRESIDENT
						&& r != RoleType.MAYOR
						&& r != RoleType.DEPUTY_ADMINISTRATOR)
					roles.add(r);
			}
		}
		
		else if (pop <= ChainOfCommand.POPULATION_WITH_DEPUTY_ADMINISTRATOR) {
			for (RoleType r : RoleType.values()) {
				if (r != RoleType.PRESIDENT
						&& r != RoleType.MAYOR)
					roles.add(r);
			}
		}
		
		else if (pop <= ChainOfCommand.POPULATION_WITH_MAYOR) {
			for (RoleType r : RoleType.values()) {
				if (r != RoleType.PRESIDENT)
					roles.add(r);
			}
		}

		else {
			roles.addAll(Arrays.asList(RoleType.values()));
		}
		
		return roles;
	}
	
	public static Map<JobType, Map<RoleType, Double>> getRoleWeights() {
		return roleWeights;
	}

}
