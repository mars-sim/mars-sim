/*
 * Mars Simulation Project
 * RoleUtil.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;
import org.mars_sim.msp.core.person.TrainingUtils;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.ChainOfCommand;

/**
 * The RoleUtil class determines the roles of the settlers in a simulation.
 */
public class RoleUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(RoleUtil.class.getName());
	
	// Define the order of each specialist role in a role prospect array
	public static final  RoleType[] SPECIALISTS = new RoleType[] {
			RoleType.AGRICULTURE_SPECIALIST,
			RoleType.COMPUTING_SPECIALIST,
			RoleType.ENGINEERING_SPECIALIST,
			RoleType.LOGISTIC_SPECIALIST,
			RoleType.MISSION_SPECIALIST,
			RoleType.RESOURCE_SPECIALIST,
			RoleType.SAFETY_SPECIALIST,
			RoleType.SCIENCE_SPECIALIST
		};
			

	public static final RoleType[] CHIEFS = new RoleType[] { 
			RoleType.CHIEF_OF_AGRICULTURE,
			RoleType.CHIEF_OF_COMPUTING,
			RoleType.CHIEF_OF_ENGINEERING,
			RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS,
			RoleType.CHIEF_OF_MISSION_PLANNING,
			RoleType.CHIEF_OF_SAFETY_N_HEALTH,
			RoleType.CHIEF_OF_SCIENCE,
			RoleType.CHIEF_OF_SUPPLY_N_RESOURCES};
	
	public static final int NUM_CHIEF = CHIEFS.length;
	
	private static Map<JobType, Map<RoleType,Double>> roleWeights
							= new EnumMap<>(JobType.class);
	
	public static Map<JobType, Map<RoleType, Double>> getRoleWeights() {
		return roleWeights;
	}
	
	
	public RoleUtil() {
	}

	/**
	 * Initialize the role prospect array
	 */
	public static void initialize() {
		if (roleWeights.isEmpty()) {
			for (Job j : JobUtil.getJobs()) {
				JobType id = j.getType();
				roleWeights.put(id, j.getRoleProspects());
			}
		}
	}
	
	public static boolean isRoleWeightsInitialized() {
        return !roleWeights.isEmpty();
	}
	
	/**
	 * Find the best role for a given person in a settlement
	 * 
	 * @param settlement
	 * @param p
	 * @return
	 */
	public static RoleType findBestRole(Person p) {
		RoleType selectedRole = null; //specialistRoles[RandomUtil.getRandomInt(RoleType.SEVEN - 1)];
		double highestWeight = 0;
		
		ChainOfCommand chain = p.getSettlement().getChainOfCommand();
		JobType job = p.getMind().getJob();
		Map<RoleType, Double> weights = roleWeights.get(job);
		
		// Use a Tree map so entries sorting in increasing order.
		List<RoleType> roles = new ArrayList<>(Arrays.asList(SPECIALISTS));
		int leastNum = 0;
		RoleType leastFilledRole = null;
		for (RoleType rt: roles) {
			int num = chain.getNumFilled(rt);
			if (leastNum >= num) {
				leastNum = num;
				leastFilledRole = rt;
			}
		}
		// Remove that role
		roles.remove(leastFilledRole);
		// Add that role back to the first position
		roles.add(0, leastFilledRole);
				
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
		
		return selectedRole;
	}
	
	
	
	/**
	 * Find the person who is the best fit for a given role from a pool of candidates
	 * 
	 * @param role
	 * @param candidates
	 * @return
	 */
	public static Person findBestFit(RoleType role, List<Person> candidates) {
		Person bestPerson = null;
		double bestScore = 0;

		for (Person p : candidates) {
			
			JobType job = p.getMind().getJob();
			Map<RoleType, Double> weights = roleWeights.get(job);
			
			double score = getRolePropectScore(p, role, weights);
			
			if (score > bestScore) {
				bestScore = score;
				bestPerson = p;
			}
		}

		return bestPerson;
	}

	/**
	 * Gets the role prospect score of a person on a particular role
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
	 * Gets the training score of a person on a particular role
	 * 
	 * @param person
	 * @param role
	 * @return the training score 
	 */
	public static double getTrainingScore(Person person, RoleType role) {
		
		List<TrainingType> trainings = person.getTrainings();
		
		int trainingScore = 0;
		for (TrainingType tt : trainings) {
			trainingScore += TrainingUtils.getModifier(role, tt);
		}
		
		return trainingScore;
		
	}
	
	/**
	 * Checks if this is a leadership role
	 * 
	 * @param role
	 * @return yes if it is true
	 */
	public static boolean isLeadershipRole(RoleType role) {
		// Is this correct ? Are Specialists leadership ?
		for (RoleType r : SPECIALISTS) {
			if (r == role)
				return false;
		}
		return true;
	}
	
	/**
	 * Records the role change and fire the unit update
	 * 
	 * @param person
	 * @param roleType
	 */
	public static void recordNewRole(Person person, RoleType roleType) {
		// Save the new role in roleHistory
		person.getRole().addRoleHistory(roleType);
		// Fire the role event
		person.fireUnitUpdate(UnitEventType.ROLE_EVENT, roleType);
		
		//logger.info(person, "New Role " + roleType.getName());
	}
}
