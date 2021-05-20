/**
 * Mars Simulation Project
 * RoleUtil.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;
import org.mars_sim.msp.core.person.TrainingUtils;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.ChainOfCommand;

/**
 * The RoleUtil class determines the roles of the settlers in a simulation.
 */
public class RoleUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static Logger logger = Logger.getLogger(RoleUtil.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	// Define the order of each specialist role in a role prospect array
	public static RoleType[] specialistRoles = new RoleType[] {
			RoleType.AGRICULTURE_SPECIALIST,
			RoleType.ENGINEERING_SPECIALIST,
			RoleType.LOGISTIC_SPECIALIST,
			RoleType.MISSION_SPECIALIST,
			RoleType.RESOURCE_SPECIALIST,	
			RoleType.SAFETY_SPECIALIST,
			RoleType.SCIENCE_SPECIALIST	
		};
			
	private static Map<Integer, Map<RoleType,Double>> roleWeights = new HashMap<>();
	
	public static Map<Integer, Map<RoleType, Double>> getRoleWeights() {
		return roleWeights;
	}
	
	
	public RoleUtil() {
	}

	/**
	 * Initialize the role prospect array
	 */
	public static void initialize() {
		if (roleWeights.isEmpty()) {
			List<Job> jobList = JobUtil.getJobs();
			for (Job j : jobList) {
				int id = j.getJobID();
				roleWeights.put(id, j.getRoleProspects());
			}
		}
	}
	
	public static boolean isRoleWeightsInitialized() {
		if (roleWeights.isEmpty())
			return false;
		else
			return true;
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
		Job job = p.getMind().getJob();
		int id = job.getJobID();
		Map<RoleType, Double> weights = roleWeights.get(id);
		
		List<RoleType> roles = new ArrayList<>(RoleType.getSpecialistRoles());
		RoleType leastFilledRole = null;
		int leastNum = 0;
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
				
		for (RoleType rt : RoleType.getSpecialistRoles()) {
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
			
			Job job = p.getMind().getJob();
			int id = job.getJobID();
			Map<RoleType, Double> weights = roleWeights.get(id);
			
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
		for (RoleType r : RoleType.getSpecialistRoles()) {
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
		
		String s = String.format("[%s] %25s (Role) -> %s",
				person.getLocationTag().getLocale(), 
				person.getName(), 
				roleType.getName());
		
		LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, s);
	}
	
}
