/**
 * Mars Simulation Project
 * JobManager.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;

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
	 * Determines a new job for a person.
	 * (Note: more work here needed)
	 * @param person the person needing a job.
	 * @return new job
	 */
	public Job getNewJob(Person person) {
		return (Job) jobs.get(RandomUtil.getRandomInt(jobs.size() - 1));
	}
}