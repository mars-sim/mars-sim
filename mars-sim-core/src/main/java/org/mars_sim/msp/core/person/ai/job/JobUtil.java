/**
 * Mars Simulation Project
 * JobUtil.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Chefbot;
import org.mars_sim.msp.core.robot.ai.job.Constructionbot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;
import org.mars_sim.msp.core.robot.ai.job.Gardenbot;
import org.mars_sim.msp.core.robot.ai.job.Makerbot;
import org.mars_sim.msp.core.robot.ai.job.Medicbot;
import org.mars_sim.msp.core.robot.ai.job.Repairbot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The JobUtil class keeps track of the settler jobs in a simulation.
 */
public final class JobUtil {

	public static final String SETTLEMENT = "Settlement";
	public static final String MISSION_CONTROL = "Mission Control";
	public static final String USER = "User";

	// Data members
	/** List of the jobs in the simulation. */
	private static Map<JobType,Job> jobSpecs;
	private static List<RobotJob> robotJobs;

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
		
		List<Job> jobs = new ArrayList<>();
		jobs.add(new Architect());
		jobs.add(new Areologist());
		jobs.add(new Astronomer());
		jobs.add(new Biologist());
		jobs.add(new Botanist());
		
		jobs.add(new Chef());
		jobs.add(new Chemist());
		jobs.add(new Doctor());
		jobs.add(new Engineer());
		jobs.add(new Mathematician());
		
		jobs.add(new Meteorologist());
		jobs.add(new Physicist());
		jobs.add(new Pilot());
		jobs.add(new Politician());
		jobs.add(new Psychologist());
		
		jobs.add(new Reporter());
		jobs.add(new Technician());
		jobs.add(new Trader());
		
		jobSpecs = new EnumMap<>(JobType.class);
		for (Job job : jobs) {
			jobSpecs.put(job.getType(), job);
		}
	}

	/**
	 * Initialize robotJobs list.
	 */
	private static void loadRobotJobs() {
		if (robotJobs == null) {
			robotJobs = new CopyOnWriteArrayList<RobotJob>();
			robotJobs.add(new Chefbot());
			robotJobs.add(new Constructionbot());
			robotJobs.add(new Deliverybot());
			robotJobs.add(new Gardenbot());
			robotJobs.add(new Makerbot());
			robotJobs.add(new Medicbot());
			robotJobs.add(new Repairbot());
		}
	}

	/**
	 * Get the JobSpec for a Job.
	 * @param job
	 * @return
	 */
	public static Job getJobSpec(JobType job) {
		if (jobSpecs == null)
			loadJobs();
		Job result = jobSpecs.get(job);
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
	public static Collection<Job> getJobs() {
		if (jobSpecs == null)
			loadJobs();
		return Collections.unmodifiableCollection(jobSpecs.values());
	}

	/**
	 * Gets a list of available jobs in the simulation.
	 * 
	 * @return list of jobs
	 */
	public static List<RobotJob> getRobotJobs() {
		if (robotJobs == null)
			loadRobotJobs();
		return new CopyOnWriteArrayList<RobotJob>(robotJobs);
	}

	public static RobotJob getRobotJob(String jobClassName) {
		if (robotJobs == null)
			loadRobotJobs();
		for (RobotJob robotJob : robotJobs) {
			if (robotJob.getClass().getSimpleName().compareToIgnoreCase(jobClassName) == 0) {
				return robotJob;
			}
		}
		return null;
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
		Job jobSpec = getJobSpec(job);
		double result = jobSpec.getSettlementNeed(settlement);

		// Check all people associated with the settlement.
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.getMind().getJob() == job) {
				result -= jobSpec.getCapability(person);
			}
		}

		result = result / 2D;

		if (result < 0)
			result = 0;
		
		return result;
	}

	// TODO: determine the need for this method since it promotes robotJob switching
	// For robots
	public static double getRemainingSettlementNeed(Settlement settlement, RobotJob robotJob) {
		double result = robotJob.getSettlementNeed(settlement);

		// Check all Robots associated with the settlement.
		Iterator<Robot> j = settlement.getAllAssociatedRobots().iterator();
		while (j.hasNext()) {
			Robot robot = j.next();
			if (robot.getBotMind().getRobotJob() == robotJob)
				result -= robotJob.getCapability(robot);
		}

		result = result / 2D;

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
		
		JobType originalJob = person.getMind().getJob();
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
			Iterator<Job> i = getJobs().iterator();
			while (i.hasNext()) {
				Job job = i.next();
				if (job.getType() != JobType.POLITICIAN) {
					double rand = RandomUtil.getRandomDouble(0.8);
					double t = 1.0 * pop / numberOfJobs  + rand;
					int maxPos = (int)(Math.ceil(t));
//						logger.config("job : " + job + "  ID: " + job.getJobID());
					int numPositions = numJobs(job.getType(), settlement);
//						logger.config(job.getName(GenderType.MALE) +  ": " + numPositions + "  ");
					if (numPositions < maxPos) {
					// Exclude politician job which is reserved for Mayor only
						double jobProspect = getJobProspect(person, job.getType(), settlement, true);
						if (jobProspect > selectedJobProspect) {
							selectedJob = job.getType();
							selectedJobProspect = jobProspect;
						}
					}
				}
			} 
		}
//		logger.info(newJob.getName(GenderType.MALE) + " job prospects : " + newJobProspect);
//		logger.info(newJob + " : " + newJobProspect);
		return selectedJob;
	}

	public static Person findBestFit(Settlement settlement, JobType job) {
		Person person = null;
		double bestScore = 0;
		Job jobSpec = getJobSpec(job);
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
	 * Get the job prospect value for a person and a particular job at a settlement.
	 * 
	 * @param person           the person to check for
	 * @param job              the job to check for
	 * @param settlement       the settlement to do the job in.
	 * @param isHomeSettlement is this the person's home settlement?
	 * @return job prospect value (0.0 min)
	 */
	public static double getJobProspect(Unit unit, JobType job, Settlement settlement, boolean isHomeSettlement) {
		Person person = null;
		person = (Person) unit;


		Job jobSpec = jobSpecs.get(job);
		double jobCapability = jobSpec.getCapability(person);
		
		double remainingNeed = getRemainingSettlementNeed(settlement, job);

		if ((job == person.getMind().getJob()) && isHomeSettlement)
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
		int num = 0;
		for (Person p : settlement.getAllAssociatedPeople()) {
			if (p.getMind().getJob() == job) {
				num++;
			}
		}
		return num;
	}

	/**
	 * Facade method to remove the need to lookup a JobSpec to get the 
	 * start Mission probability.
	 * @param job
	 * @param missionClass
	 * @return
	 */	
	public static double getStartMissionProbabilityModifier(JobType job, Class<? extends Mission> missionClass) {
		Job j = getJobSpec(job);
		return j.getStartMissionProbabilityModifier(missionClass);
	}
}
