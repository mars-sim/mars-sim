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
	public static RoleType[] roleTypes = new RoleType[] {
			RoleType.AGRICULTURE_SPECIALIST,
			RoleType.ENGINEERING_SPECIALIST,
			RoleType.LOGISTIC_SPECIALIST,
			RoleType.MISSION_SPECIALIST,
			RoleType.RESOURCE_SPECIALIST,	
			RoleType.SAFETY_SPECIALIST,
			RoleType.SCIENCE_SPECIALIST	
		};
			
	private static Map<Integer, double[]> roleWeights = new HashMap<>();
	
	public RoleUtil() {
	}

	/**
	 * Initialize the role prospect array
	 */
	public static void initialize() {

		List<Job> jobList = JobUtil.getJobs();
		for (Job j : jobList) {
			int id = j.getJobID();
			roleWeights.put(id, j.getRoleProspects());
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
		
		RoleType role1 = roleTypes[RandomUtil.getRandomInt(6)];
//		RoleType role2 = roleTypes[RandomUtil.getRandomInt(6)];
		
		double highestWeight = 0;
//		double secondWeight = 0;
		
		Job job = p.getMind().getJob();
		int id = job.getJobID();
		double[] weights = roleWeights.get(id);
		
		List<TrainingType> trainings = p.getTrainings();
		
		for (int i=0; i<7; i++) {
			boolean isRoleAvailable = p.getSettlement().getChainOfCommand().isRoleAvailable(roleTypes[i]);		
			// If the RoleUtil has not been initialized, then ignore checking 
			// for the role availability, 
			if (isRoleWeightsInitialized())
				initialize();
			
			double jobScore = weights[i];
			
			int trainingScore = 0;
			for (TrainingType tt : trainings) {
				trainingScore += TrainingUtils.getModifier(roleTypes[i].ordinal(), tt.ordinal());
			}
			
			double totalScore = jobScore + trainingScore;
			
			if (isRoleAvailable && (totalScore > highestWeight)) {
				
				// Pick the role based on the highest weight
				role1 = roleTypes[i];
				highestWeight = totalScore;
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

//		List<Person> ppl = new ArrayList<>(settlement.getAllAssociatedPeople());
		for (Person p : candidates) {
			
			Job job = p.getMind().getJob();
			int id = job.getJobID();
			double[] weights = roleWeights.get(id);
			
			// Note the specialist role begins at element 11 in RoleType class
			int roleEnum = role.ordinal() - 11;
			
			double jobScore = Math.round(weights[roleEnum] * 10.0)/10.0;
			
			List<TrainingType> trainings = p.getTrainings();
			
			int trainingScore = 0;
			for (TrainingType tt : trainings) {
				trainingScore += TrainingUtils.getModifier(roleEnum, tt.ordinal());
			}
			
			double score = jobScore + trainingScore * 2;
			
			if (score > bestScore) {
				bestScore = score;
				bestPerson = p;
			}
		}

		return bestPerson;
	}
	
}
