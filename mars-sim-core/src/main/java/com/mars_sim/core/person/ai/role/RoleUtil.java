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
								.filter(rt -> rt.getLevel() == RoleLevel.SPECIALIST)
								.toList();
		
		// Cache the crew roles
		crewRoles = Arrays.stream(RoleType.values())
								.filter(rt -> rt.getLevel() == RoleLevel.CREW)
								.toList();
		
		// Cache the crew roles
		chiefRoles = Arrays.stream(RoleType.values())
								.filter(RoleType::isChief)
								.toList();	
		
		// Cache the council roles
		leadershipRoles = Arrays.stream(RoleType.values())
								.filter(RoleType::isLeadership)
								.toList();
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
		
		
		JobType job = p.getMind().getJobType();
		Map<RoleType, Double> weights = roleWeights.get(job);

		// Use a Tree map so entries sorting in increasing order.
		List<RoleType> roles = new ArrayList<>();
		int leastNum = 0;
		RoleType leastFilledRole = null;
		
		List<RoleType> types = chain.getGovernance().getAssignableRoles();		
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

	public static Map<JobType, Map<RoleType, Double>> getRoleWeights() {
		return roleWeights;
	}

}
