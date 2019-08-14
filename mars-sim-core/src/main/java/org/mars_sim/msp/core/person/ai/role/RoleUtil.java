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
		
		RoleType roleType = roleTypes[RandomUtil.getRandomInt(6)];
		double highestWeight = 0;
		Job job = p.getMind().getJob();
		int id = job.getJobID();
		double[] weights = roleWeights.get(id);
		
		for (int i=0; i<7; i++) {
			boolean isRoleAvailable = p.getSettlement().getChainOfCommand().isRoleAvailable(roleTypes[i]);		
			// If the RoleManager has not been initialized, then ignore checking 
			// for the role availability, and pick the role based on the highest weight
			if (isRoleWeightsInitialized())
				initialize();
			
			if (weights[i] > highestWeight 
					&& isRoleAvailable) {
				roleType = roleTypes[i];
				highestWeight = weights[i];
			}
		}
		
		// Finalize setting a person's new role
		p.getRole().setNewRoleType(roleType);
		
		return roleType;
	}
	
	
	
//	/**
//	 * Find the person who is the best fit for a given role from a pool of candidates
//	 * 
//	 * @param role
//	 * @param candidates
//	 * @return
//	 */
//	public static Person findBestFit(RoleType role, List<Person> candidates) {
//		Person person = null;
//		double bestScore = 0;
//
////		List<Person> ppl = new ArrayList<>(settlement.getAllAssociatedPeople());
//		for (Person p : candidates) {
//			double score = Math.round(p.getARoleProspectScore(role) * 100.0)/100.0;
//			if (score > bestScore) {
//				bestScore = score;
//				person = p;
//			}
//		}
//
////		System.out.println(person + " : " + bestScore);
//		return person;
//	}
	
}
