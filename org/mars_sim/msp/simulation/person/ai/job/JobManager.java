/**
 * Mars Simulation Project
 * JobManager.java
 * @version 2.76 2004-06-10
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The JobManager class keeps track of the settler jobs in a simulation.
 */
public class JobManager implements Serializable {
	
	// Data members
	private List jobs; // List of the jobs in the simulation. 

	/**
	 * Constructor
	 */
	public JobManager() {
		
		// Add all jobs to list.
		jobs = new ArrayList();
		jobs.add(new Botanist());
		jobs.add(new Areologist());
		jobs.add(new Doctor());
		jobs.add(new Engineer());
		jobs.add(new Driver());
	}
	
	/**
	 * Gets a list of available jobs in the simulation.
	 * @return list of jobs
	 */
	public List getJobs() {
		return new ArrayList(jobs);
	}
	
	/**
	 * Gets the need for a job at a settlement minus the capability of the inhabitants
	 * performing that job there.
	 * @param settlement the settlement to check.
	 * @param job the job to check.
	 * @return settlement need minus total job capability of inhabitants with job.
	 */
	public double getRemainingSettlementNeed(Settlement settlement, Job job) {
		double result = job.getSettlementNeed(settlement);
		
		// Check all inhabitants of settlement.
		PersonIterator i = settlement.getInhabitants().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.getMind().getJob() == job) result-= job.getCapability(person);
		}
		
		// Check all missions for the settlement.
		List missions = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement);
		Iterator j = missions.iterator();
		while (j.hasNext()) {
			Mission mission = (Mission) j.next();
			PersonIterator k = mission.getPeople().iterator();
			while (k.hasNext()) {
				Person person = k.next();
				if (!person.getLocationSituation().equals(Person.INSETTLEMENT)) {
					if (person.getMind().getJob() == job) result-= job.getCapability(person);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets a new job for the person.
	 * Might be the person's current job.
	 * @param person the person to check.
	 * @param job the new job.
	 */
	public Job getNewJob(Person person) {
		
		Job originalJob = person.getMind().getJob();
		Job newJob = null;
		double newJobValue = Integer.MIN_VALUE;
		
		// Only change jobs when a person is at a settlement.
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			
			Iterator i = jobs.iterator();
			while (i.hasNext()) {
				Job job = (Job) i.next();
				double jobCapability = job.getCapability(person);
				double remainingNeed = getRemainingSettlementNeed(settlement, job);
				if (job == originalJob) remainingNeed+= jobCapability;
				double jobValue = (jobCapability + 1D) * remainingNeed;
				
				if (jobValue >= newJobValue) {
					newJob = job;
					newJobValue = jobValue;
				}
			}
			
			/*
			if ((newJob != null) && (newJob != originalJob)) {
				System.out.println(person.getName() + " changed jobs to " + newJob.getName());
			}
			*/
		}
		else newJob = originalJob;
		
		return newJob;
	}
}