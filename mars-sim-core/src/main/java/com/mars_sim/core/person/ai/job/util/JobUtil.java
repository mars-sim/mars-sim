/*
 * Mars Simulation Project
 * JobUtil.java
 * @date 2025-10-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.job.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.Architect;
import com.mars_sim.core.person.ai.job.Areologist;
import com.mars_sim.core.person.ai.job.Astrobiologist;
import com.mars_sim.core.person.ai.job.Astronomer;
import com.mars_sim.core.person.ai.job.Botanist;
import com.mars_sim.core.person.ai.job.Chef;
import com.mars_sim.core.person.ai.job.Chemist;
import com.mars_sim.core.person.ai.job.ComputerScientist;
import com.mars_sim.core.person.ai.job.Doctor;
import com.mars_sim.core.person.ai.job.Engineer;
import com.mars_sim.core.person.ai.job.Mathematician;
import com.mars_sim.core.person.ai.job.Meteorologist;
import com.mars_sim.core.person.ai.job.Physicist;
import com.mars_sim.core.person.ai.job.Pilot;
import com.mars_sim.core.person.ai.job.Politician;
import com.mars_sim.core.person.ai.job.Psychologist;
import com.mars_sim.core.person.ai.job.Reporter;
import com.mars_sim.core.person.ai.job.Sociologist;
import com.mars_sim.core.person.ai.job.Technician;
import com.mars_sim.core.person.ai.job.Tourist;
import com.mars_sim.core.person.ai.job.Trader;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.robot.ai.job.Chefbot;
import com.mars_sim.core.robot.ai.job.Constructionbot;
import com.mars_sim.core.robot.ai.job.Deliverybot;
import com.mars_sim.core.robot.ai.job.Gardenbot;
import com.mars_sim.core.robot.ai.job.Makerbot;
import com.mars_sim.core.robot.ai.job.Medicbot;
import com.mars_sim.core.robot.ai.job.Repairbot;
import com.mars_sim.core.robot.ai.job.RobotJob;
import com.mars_sim.core.structure.ChainOfCommand;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The JobUtil class keeps track of the settler jobs in a simulation.
 */
public final class JobUtil {

	public static final String SETTLEMENT = "Settlement";
	public static final String MISSION_CONTROL = "Mission Control";
	public static final String USER = "User";

	// Data members
	/** List of the jobs in the simulation. */
	private static Map<JobType, JobSpec> jobSpecs;
	private static Map<RobotType, RobotJob> robotJobs;

	/**
	 * Private constructor for static utility class.
	 */
	private JobUtil() {
	}

	/**
	 * Initialize job list.
	 */
	private static synchronized void loadJobs() {
		if (jobSpecs != null) {
			// No need; a different Thread has already loaded
			return;
		}
		
		List<JobSpec> jobs = new ArrayList<>();
		jobs.add(new Architect());
		jobs.add(new Areologist());
		jobs.add(new Astronomer());
		jobs.add(new Astrobiologist());
		jobs.add(new Botanist());
		
		jobs.add(new Chef());
		jobs.add(new Chemist());
		jobs.add(new ComputerScientist());
		jobs.add(new Doctor());
		jobs.add(new Engineer());
		
		jobs.add(new Mathematician());
		jobs.add(new Meteorologist());
		jobs.add(new Physicist());
		jobs.add(new Pilot());
		jobs.add(new Politician());
		
		jobs.add(new Psychologist());
		jobs.add(new Reporter());
		jobs.add(new Sociologist());
		jobs.add(new Technician());
		jobs.add(new Tourist());
		jobs.add(new Trader());
		
		Map<JobType, JobSpec> newSpec = new EnumMap<>(JobType.class);
		for (JobSpec job : jobs) {
			newSpec.put(job.getType(), job);
		}
		jobSpecs = Collections.unmodifiableMap(newSpec);
	}

	/**
	 * Initialize robotJobs list.
	 */
	private static synchronized void loadRobotJobs() {
		if (robotJobs == null) {
			Map<RobotType,RobotJob> newJobs = new EnumMap<>(RobotType.class);
			newJobs.put(RobotType.CHEFBOT, new Chefbot());
			newJobs.put(RobotType.CONSTRUCTIONBOT, new Constructionbot());
			newJobs.put(RobotType.DELIVERYBOT, new Deliverybot());
			newJobs.put(RobotType.GARDENBOT, new Gardenbot());
			newJobs.put(RobotType.MAKERBOT, new Makerbot());
			newJobs.put(RobotType.MEDICBOT, new Medicbot());
			newJobs.put(RobotType.REPAIRBOT, new Repairbot());
			
			robotJobs = newJobs;
		}
	}

	/**
	 * Gets the JobSpec for a Job.
	 * 
	 * @param job
	 * @return
	 */
	public static JobSpec getJobSpec(JobType job) {
		if (jobSpecs == null)
			loadJobs();
		JobSpec result = jobSpecs.get(job);
		if (result == null) {
			throw new IllegalStateException("No JobSpec found for " + job);
		}
		return result;
	}

	/**
	 * Gets a list of available jobs in the simulation.
	 * 
	 * @return list of jobs.
	 */
	public static Collection<JobSpec> getJobs() {
		if (jobSpecs == null)
			loadJobs();
		return jobSpecs.values();
	}

	/**
	 * Gets the robot job class from its type
	 * 
	 * @param robotType
	 * @return
	 */
	public static RobotJob getRobotJob(RobotType robotType) {
		if (robotJobs == null)
			loadRobotJobs();
		return robotJobs.get(robotType);
	}

	/**
	 * Gets the need for a job at a settlement minus the capability of the
	 * inhabitants performing that job there.
	 * 
	 * @param settlement the settlement to check.
	 * @param job        the job to check.
	 * @return settlement need minus total job capability of inhabitants with job.
	 */
	public static double getRemainingSettlementNeed(Settlement settlement, JobType job) {
		JobSpec jobSpec = getJobSpec(job);
		double need = jobSpec.getSettlementNeed(settlement);
		double capability = 0;
		int num = JobUtil.numJobs(job, settlement);
		
		// Check all people associated with the settlement.
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.getMind().getJobType() == job) {
				capability += jobSpec.getCapability(person);
			}
		}

		double result = need - num - capability;

		if (result < 0)
			result = 0;
		
		return result;
	}
	
	/**
	 * Gets a new job for the person. Might be the person's current job.
	 * 
	 * @param person the person to check.
	 * @return the new job.
	 */
	public static JobType getNewJob(Person person) {
		// Find new job for person.
		double selectedJobProspect = Integer.MIN_VALUE;
		
		JobType originalJob = person.getMind().getJobType();
		JobType selectedJob = originalJob;
		// Determine person's associated settlement.
		Settlement settlement = person.getAssociatedSettlement();
		
		int pop = settlement.getIndoorPeopleCount();
		if (pop == 0)
			// At the start of the game, pop = 0
			pop = settlement.getInitialPopulation();

		// Set limits on # of position available for a job, based on settlement's population
		// e.g. rather not having 3 botanists when the settlement has only 8 people
		int numberOfJobs = JobType.values().length;
		while (selectedJob == originalJob) {
			Iterator<JobSpec> i = getJobs().iterator();
			while (i.hasNext()) {
				JobSpec job = i.next();
				if (job.getType() != JobType.POLITICIAN) {
					// Exclude politician job which is reserved for Mayor only
					double rand = RandomUtil.getRandomDouble(0.8);
					double t = 1.0 * pop / numberOfJobs  + rand;
					int maxPos = (int)(Math.ceil(t));
					int numPositions = numJobs(job.getType(), settlement);
					if (numPositions < maxPos) {
						double jobProspect = getJobProspect(person, job.getType(), settlement, true);
						if (jobProspect > selectedJobProspect) {
							selectedJob = job.getType();
							selectedJobProspect = jobProspect;
						}
					}
				}
			} 
		}

		return selectedJob;
	}

	/**
	 * Finds the best person who fit this job position.
	 * 
	 * @param settlement
	 * @param job
	 * @return
	 */
	public static Person findBestFit(Settlement settlement, JobType job) {
		Person person = null;
		double bestScore = 0;
		JobSpec jobSpec = getJobSpec(job);
		List<Person> ppl = new ArrayList<>(settlement.getAllAssociatedPeople());
		for (Person p : ppl) {
			double score = Math.round(jobSpec.getCapability(p) * 100.0)/100.0;
			if (score > bestScore) {
				bestScore = score;
				person = p;
			}
		}

		return person;
	}
	

	/**
	 * Gets the job prospect value for a person and a particular job at a settlement.
	 * 
	 * @param person           the person to check for
	 * @param job              the job to check for
	 * @param settlement       the settlement to do the job in.
	 * @param isHomeSettlement is this the person's home settlement?
	 * @return job prospect value (0.0 min)
	 */
	public static double getJobProspect(Person person, JobType job, Settlement settlement, boolean isHomeSettlement) {
		JobSpec jobSpec = jobSpecs.get(job);
		double jobCapability = jobSpec.getCapability(person);
		
		double remainingNeed = getRemainingSettlementNeed(settlement, job);

		if ((job == person.getMind().getJobType()) && isHomeSettlement)
			remainingNeed += jobCapability;

		return (jobCapability + 1D) * remainingNeed;
	}

	/**
	 * Gets the best job prospect value for a person at a settlement.
	 * 
	 * @param person           the person to check for
	 * @param settlement       the settlement to do the job in
	 * @param isHomeSettlement is this the person's home settlement?
	 * @return best job prospect value
	 */
	public static double getBestJobProspect(Person person, Settlement settlement, boolean isHomeSettlement) {
		double bestProspect = Double.MIN_VALUE;
		for (JobType job : JobType.values()) {
			double prospect = getJobProspect(person, job, settlement, isHomeSettlement);
			if (prospect > bestProspect)
				bestProspect = prospect;
		}
		return bestProspect;
	}

	
	/**
	 * Counts the number of people having a particular job
	 * 
	 * @param job string
	 * @param settlement
	 * @return number
	 */
	public static int numJobs(JobType job, Settlement settlement) {
		return (int) settlement.getAllAssociatedPeople().stream()
					.filter(p -> p.getMind().getJobType() == job)
					.count();
	}

	/**
	 * Assigns the best candidate to a job position.
	 * 
	 * @param settlement
	 * @param job
	 */
	private static void assignBestCandidate(Settlement settlement, JobType job) {
		Person p0 = findBestFit(settlement, job);
		// Designate a specific job to a person
		if (p0 != null) {
			p0.getMind().assignJob(job, true, JobUtil.SETTLEMENT, AssignmentType.APPROVED, JobUtil.SETTLEMENT);
		}
	}

	/**
	 * Tunes up the settlement with unique job position.
	 */
	public static void tuneJobDeficit(Settlement settlement) {
		int numEngs = numJobs(JobType.ENGINEER, settlement);
		int numTechs = numJobs(JobType.TECHNICIAN, settlement);

		if ((numEngs == 0) || (numTechs == 0)) {
			Person bestEng = findBestFit(settlement, JobType.ENGINEER);
			Person bestTech = findBestFit(settlement, JobType.TECHNICIAN);

			// Make sure the best person is not the only one in the other Job
			if ((bestEng != null) && bestEng.equals(bestTech)) {
				// Can only do one so job
				if (numEngs == 0) {
					// Keep the bestEng find
					bestTech = null;
				}
				else {
					// Keep best tech Loose the eng
					bestEng = null;
				}
			}
			if ((numEngs == 0) && (bestEng != null)) {
				bestEng.getMind().assignJob(JobType.ENGINEER, true,
						JobUtil.SETTLEMENT,
						AssignmentType.APPROVED,
						JobUtil.SETTLEMENT);
			}
			if ((numTechs == 0) && (bestTech != null)) {
				bestTech.getMind().assignJob(JobType.TECHNICIAN, true,
						JobUtil.SETTLEMENT,
						AssignmentType.APPROVED,
						JobUtil.SETTLEMENT);
			}
		}


		if (settlement.getNumCitizens() > ChainOfCommand.POPULATION_WITH_CHIEFS) {
			int numWeatherman = numJobs(JobType.METEOROLOGIST, settlement);
			if (numWeatherman == 0) {
				assignBestCandidate(settlement, JobType.METEOROLOGIST);
			}
		}
	}
}
