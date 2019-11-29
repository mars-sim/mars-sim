/**
 * Mars Simulation Project
 * RoleUtil.java
 * @version 3.1.0 2019-08-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.role;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;
import org.mars_sim.msp.core.person.TrainingUtils;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The RoleUtil class determines the roles of the settlers in a simulation.
 */
public class RoleUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
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
			
	private static Map<Integer, double[]> roleWeights = new HashMap<>();
	
	public static Map<Integer, double[]> getRoleWeights() {
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

		RoleType role1 = specialistRoles[RandomUtil.getRandomInt(6)];
//		RoleType role2 = roleTypes[RandomUtil.getRandomInt(6)];
		
		double highestWeight = 0;
//		double secondWeight = 0;
		
		Job job = p.getMind().getJob();
		int id = job.getJobID();
		double[] weights = roleWeights.get(id);
		

		for (int i=0; i<7; i++) {
			boolean isRoleAvailable = p.getSettlement().getChainOfCommand().isRoleAvailable(specialistRoles[i]);		
			
			if (isRoleAvailable) {
				
				double jobScore = weights[i];
				
				double trainingScore = getTrainingScore(p, specialistRoles[i], weights);
				
				double totalScore = jobScore + trainingScore;
				
				if (totalScore > highestWeight) {
					
					// Pick the role based on the highest weight
					role1 = specialistRoles[i];
					highestWeight = totalScore;
				}
			}	
		}
		
		return role1;
//		return new RoleType[] {role1, role2};
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
			double[] weights = roleWeights.get(id);
			
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
	public static double getRolePropectScore(Person person, RoleType role, double[] weights) {
		
		double jobScore = getJobScore(person, role, weights);
		
		double trainingScore = getTrainingScore(person, role, weights);

		return jobScore + trainingScore;
		
	}
	
	/**
	 * Gets the job score of a person on a particular role
	 * 
	 * @param person
	 * @param role
	 * @param weights
	 * @return the job score 
	 */
	public static double getJobScore(Person person, RoleType role, double[] weights) {
	
		// Note the specialist role currently begins at element 0 in RoleType class
		int roleEnum = role.ordinal();
		
		double jobScore = Math.round(weights[roleEnum] * 10.0)/10.0;
		
		return jobScore;
		
	}
	
	/**
	 * Gets the training score of a person on a particular role
	 * 
	 * @param person
	 * @param role
	 * @param weights
	 * @return the training score 
	 */
	public static double getTrainingScore(Person person, RoleType role, double[] weights) {
	
		// Note the specialist role currently begins at element 0 in RoleType class
		int roleEnum = role.ordinal();
		
		List<TrainingType> trainings = person.getTrainings();
		
		int trainingScore = 0;
		for (TrainingType tt : trainings) {
			trainingScore += TrainingUtils.getModifier(roleEnum, tt.ordinal());
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
	
	
}
