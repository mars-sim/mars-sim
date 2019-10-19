/**
 * Mars Simulation Project
 * JobUtil.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
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
public final class JobUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(JobUtil.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	public static final String SETTLEMENT = "Settlement";
	public static final String MISSION_CONTROL = "Mission Control";
	public static final String USER = "User";
	private static final String POLITICIAN = "Politician";

	// Data members
	/** List of the jobs in the simulation. */
	private static List<Job> jobs;
	private static List<RobotJob> robotJobs;
	private static List<String> jobList;

	/**
	 * Private constructor for static utility class.
	 */
	private JobUtil() {
	}

	/**
	 * Initialize job list.
	 */
	private static void loadJobs() {
		if (jobs == null) {
			jobs = new ArrayList<Job>();
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
			jobs.add(new Reporter());
			
			jobs.add(new Technician());
			jobs.add(new Trader());
		}
	}

	/**
	 * Initialize robotJobs list.
	 */
	private static void loadRobotJobs() {
		if (robotJobs == null) {
			robotJobs = new ArrayList<RobotJob>();
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
	 * Gets a list of available jobs in the simulation.
	 * 
	 * @return list of jobs.
	 */
	public static List<Job> getJobs() {
		if (jobs == null)
			loadJobs();
		return new ArrayList<Job>(jobs);
	}

	public static List<String> getJobList() {
		if (jobList == null) {
			loadJobs();
			jobList = new ArrayList<>();
			for (Job job : jobs) {
				String j = job.getName(GenderType.MALE);
				jobList.add(j);
			}
		}
		return jobList;
	}

	
	
	/**
	 * Gets the Job object.
	 * 
	 * @param id
	 * @return {@link Job}
	 */
	public static Job getJob(int id) {
		if (jobs == null)
			loadJobs();
		return jobs.get(id);
	}

	/**
	 * Gets a list of available jobs in the simulation.
	 * 
	 * @return list of jobs
	 */
	public static List<RobotJob> getRobotJobs() {
		if (robotJobs == null)
			loadRobotJobs();
		return new ArrayList<RobotJob>(robotJobs);
	}

	/**
	 * Gets a job from a job class name.
	 * 
	 * @param jobName the name of the job.
	 * @return job or null if job with name not found.
	 */
	public static Job getJob(String jobClassName) {
		if (jobs == null)
			loadJobs();
		for (Job job : jobs) {
			if (job.getClass().getSimpleName().compareToIgnoreCase(jobClassName) == 0) {
				return job;
			}
		}
		return null;
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
	public static double getRemainingSettlementNeed(Settlement settlement, Job job) {
		if (job == null)
			logger.warning("job is null !");
		double result = job.getSettlementNeed(settlement);

		// Check all people associated with the settlement.
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.getMind().getJob() == job)
				result -= job.getCapability(person);
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
	public static Job getNewJob(Person person) {
		// Find new job for person.
		double selectedJobProspect = Integer.MIN_VALUE;
		
		Job originalJob = person.getMind().getJob();
		Job selectedJob = originalJob;
		// Determine person's associated settlement.
		Settlement settlement = person.getAssociatedSettlement();
		
		int pop = settlement.getIndoorPeopleCount();
		if (pop == 0)
			// At the start of the game, pop = 0
			pop = settlement.getInitialPopulation();

		// Set limits on # of position available for a job, based on settlement's population
		// e.g. rather not having 3 botanists when the settlement has only 8 people
		
		while (selectedJob == originalJob) {
			double rand = RandomUtil.getRandomDouble(2.0);
			double t = pop/JobType.numJobTypes + rand;
			int maxPos = (int)(Math.ceil(t));
			
			if (settlement != null) {
				Iterator<Job> i = getJobs().iterator();
				while (i.hasNext()) {
					Job job = i.next();
					if (!job.equals(JobUtil.getJob(POLITICIAN))) {
//						logger.config("job : " + job + "  ID: " + job.getJobID());
						int numPositions = numJobs(job.getName(GenderType.MALE), settlement);
//						logger.config(job.getName(GenderType.MALE) +  ": " + numPositions + "  ");
						if (numPositions < maxPos) {
						// Exclude politician job which is reserved for Mayor only
							double jobProspect = getJobProspect(person, job, settlement, true);
							if (jobProspect > selectedJobProspect) {
								selectedJob = job;
								selectedJobProspect = jobProspect;
							}
						}
					}
				}
			} 
		}
//		logger.info(newJob.getName(GenderType.MALE) + " job prospects : " + newJobProspect);
//		logger.info(newJob + " : " + newJobProspect);
		return selectedJob;
	}

	public static Person findBestFit(Settlement settlement, Job job) {
		Person person = null;
		double bestScore = 0;

		List<Person> ppl = new ArrayList<>(settlement.getAllAssociatedPeople());
		for (Person p : ppl) {
			if (!p.getMind().getJob().getClass().equals(Engineer.class) //p.getJobName().toLowerCase().contains("engineer")
					&& !p.getMind().getJob().getClass().equals(Technician.class)) {//p.getJobName().toLowerCase().contains("technician")) {
				double score = Math.round(job.getCapability(p) * 100.0)/100.0;
	//			System.out.println("score : " + score);
				if (score > bestScore) {
					bestScore = score;
					person = p;
				}
			}
		}
		
//		person.setBestJobScore(bestScore);
//		System.out.println("person : " + person);
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
	public static double getJobProspect(Unit unit, Job job, Settlement settlement, boolean isHomeSettlement) {
		Person person = null;
		person = (Person) unit;

		double jobCapability = 0D;
		double remainingNeed = 0D;

		if (job != null)
			jobCapability = job.getCapability(person);

		remainingNeed = getRemainingSettlementNeed(settlement, job);

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
		Iterator<Job> i = getJobs().iterator();
		while (i.hasNext()) {
			Job job = i.next();
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
	public static int numJobs(String job, Settlement settlement) {
		int num = 0;
		for (Person p : settlement.getAllAssociatedPeople()) {
			if (p.getJobName().equalsIgnoreCase(job)) {
				num++;
			}
		}
		return num;
	}

	/**
	 * Returns a map of job with a list of person occupying that position
	 * 
	 * @param s
	 * @return
	 */
	public static Map<String, List<Person>> getJobMap(Settlement s)	{
		return 	s.getAllAssociatedPeople().stream().collect(Collectors.groupingBy(Person::getJobName));		
	}

}