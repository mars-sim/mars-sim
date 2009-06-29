/**
 * Mars Simulation Project
 * JobManager.java
 * @version 2.87 209-06-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The JobManager class keeps track of the settler jobs in a simulation.
 */
public final class JobManager implements Serializable {
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.person.ai.job.JobManager";
	
    	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Data members
	private static List<Job> jobs; // List of the jobs in the simulation. 

	/**
	 * Private constructor for static utility class.
	 */
	private JobManager() {}
	
	/**
	 * Initialize job list.
	 */
	private static void loadJobs() {
		jobs = new ArrayList<Job>();
		jobs.add(new Botanist());
		jobs.add(new Areologist());
		jobs.add(new Doctor());
		jobs.add(new Engineer());
		jobs.add(new Driver());
		jobs.add(new Chef());
		jobs.add(new Trader());
		jobs.add(new Technician());
        jobs.add(new Architect());
        jobs.add(new Biologist());
        jobs.add(new Astronomer());
        jobs.add(new Chemist());
        jobs.add(new Physicist());
        jobs.add(new Mathematician());
        jobs.add(new Meteorologist());
	}
	
	/**
	 * Gets a list of available jobs in the simulation.
	 * @return list of jobs
	 */
	public static List<Job> getJobs() {
		if (jobs == null) loadJobs();
		return new ArrayList<Job>(jobs);
	}
	
	/**
	 * Gets a job from a job name.
	 * @param jobName the name of the job.
	 * @return job or null if job with name not found.
	 */
	public static Job getJob(String jobName) {
		Job result = null;
		Iterator<Job> i = getJobs().iterator();
		while (i.hasNext()) {
			Job job = i.next();
			if (job.getName().equalsIgnoreCase(jobName)) result = job;
		}
		return result;
	}
	
	/**
	 * Gets the need for a job at a settlement minus the capability of the inhabitants
	 * performing that job there.
	 * @param settlement the settlement to check.
	 * @param job the job to check.
	 * @return settlement need minus total job capability of inhabitants with job.
	 */
	public static double getRemainingSettlementNeed(Settlement settlement, Job job) {
		double result = job.getSettlementNeed(settlement);
		
		// Check all people associated with the settlement.
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.getMind().getJob() == job) result-= job.getCapability(person);
		}
		
		return result;
	}
	
	/**
	 * Gets a new job for the person.
	 * Might be the person's current job.
	 * @param person the person to check.
	 * @return the new job.
	 */
	public static Job getNewJob(Person person) {
		
		Job originalJob = person.getMind().getJob();
		
		// Determine person's associated settlement.
		Settlement settlement = null;
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) 
			settlement = person.getSettlement();
		else if (person.getMind().hasActiveMission()) 
			settlement = person.getMind().getMission().getAssociatedSettlement();
			
		// Find new job for person.
		Job newJob = null;
		double newJobProspect = Integer.MIN_VALUE;					
		if (settlement != null) {
			Iterator<Job> i = getJobs().iterator();
			while (i.hasNext()) {
				Job job = i.next();
				double jobProspect = getJobProspect(person, job, settlement, true);
				if (jobProspect >= newJobProspect) {
					newJob = job;
					newJobProspect = jobProspect;
				}
			}
			
			if(logger.isLoggable(Level.FINEST)) {
			    if ((newJob != null) && (newJob != originalJob)) 
			 	logger.finest(person.getName() + " changed jobs to " + newJob.getName());
			 else logger.finest(person.getName() + " keeping old job of " + originalJob.getName());
			    
			}
			
		}
		else newJob = originalJob;
		
		return newJob;
	}
	
	/**
	 * Get the job prospect value for a person and a particular job at a settlement.
	 * @param person the person to check for
	 * @param job the job to check for
	 * @param settlement the settlement to do the job in.
	 * @param isHomeSettlement is this the person's home settlement?
	 * @return job prospect value (0.0 min)
	 */
	public static double getJobProspect(Person person, Job job, Settlement settlement, boolean isHomeSettlement) {
		double jobCapability = 0D;
		if (job != null) jobCapability = job.getCapability(person);
		double remainingNeed = getRemainingSettlementNeed(settlement, job);
		if ((job == person.getMind().getJob()) && isHomeSettlement) remainingNeed+= jobCapability;
		return (jobCapability + 1D) * remainingNeed;
	}
	
	/**
	 * Gets the best job prospect value for a person at a settlement.
	 * @param person the person to check for
	 * @param settlement the settlement to do the job in
	 * @param isHomeSettlement is this the person's home settlement?
	 * @return best job prospect value
	 */
	public static double getBestJobProspect(Person person, Settlement settlement, boolean isHomeSettlement) {
		double bestProspect = Double.MIN_VALUE;
		Iterator<Job> i = getJobs().iterator();
		while (i.hasNext()) {
			Job job = i.next();
			double prospect = getJobProspect(person, job, settlement, isHomeSettlement);
			if (prospect > bestProspect) bestProspect = prospect;
		}
		return bestProspect;
	}
}