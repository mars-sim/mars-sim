/**
 * Mars Simulation Project
 * DeathInfo.java
 * @version 3.1.0 2017-02-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents the status of a Person when death occurs. It records
 * the Complaint that caused the death to occur, the time of death and the
 * Location.<br/>
 * The Location is recorded as a dead body may be moved from the place of death.
 * This class is immutable since once Death occurs it is final.
 */
public class DeathInfo implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DeathInfo.class.getName());

	// Data members
	/** Has the body been retrieved for exam */	
	private boolean bodyRetrieved = false;	
	/** Is the postmortem exam done ? */	
	private boolean examDone = false;	
	/** Mission sol */	
	private int missionSol;
	/** Amount of time performed so far in postmortem exam [in Millisols]. */	
	private double timeExam;
	/** Estimated time the postmortem exam should take [in Millisols]. */	
	private double estTimeExam;
	/** Percent of illness*/	
	private double healthCondition;	
	/** Cause of death. */	
	private String causeOfDeath;
	/** Time of death. */
	private String timeOfDeath;
	/** Time of death. */
	private String earthTimeOfDeath;
	/** Place of death. */
	private String placeOfDeath;
	/** Name of the doctor who did the postmortem. */	
	private String doctorName = "(Postmortem Exam not done yet)";
	/** Name of mission at time of death. */
	private String mission;
	/** Phase of mission at time of death. */
	private String missionPhase;
	/** Name of task at time of death. */
	private String task;
	/** Phase of task at time of death. */
	private String taskPhase;
	/** Name of sub task at time of death. */
	private String subTask;
	/** Phase of sub task at time of death. */
	private String subTaskPhase;
	/** Name of the most serious local emergency malfunction. */
	private String malfunction;
	/** The person's last word before departing. */
	private String lastWord = "None";

	/** the robot's job at time of being decomissioned. */
	private RobotJob robotJob;
	/** Medical problem contributing to the death. */
	private HealthProblem problem;
	/** Container unit at time of death. */
	private Unit containerUnit;
	private int containerID;
	/** Coordinate at time of death. */
	private Coordinates locationOfDeath;
	/** The person's job at time of death. */
	private Job job;
	/** The person. */
	private Person person;
	/** The robot. */
	private Robot robot;
	
	/** Medical cause of death. */
	private ComplaintType illness;
	/** Person's Gender. */
	private GenderType gender;
	/** Bot's RoboType. */
	private RobotType robotType;
	/** Person's role type. */
	private RoleType roleType;
	

	/**
	 * The construct creates an instance of a DeathInfo class.
	 * 
	 * @param person the dead person
	 */
	public DeathInfo(Person person, HealthProblem problem, String cause, String lastWord) {
		this.person = person;
		this.problem = problem;
		this.causeOfDeath = cause;
//		this.lastWord = lastWord;
		this.gender = person.getGender();

		// Initialize data members
		if (lastWord.equals("")) {
			int rand = RandomUtil.getRandomInt(7);
			// Quotes from http://www.phrases.org.uk/quotes/last-words/suicide-notes.html
			// https://www.goodreads.com/quotes/tag/suicide-note
			if (rand == 0)
				this.lastWord = "Take care of my family.";
			else if (rand == 1)
				this.lastWord = "One day, send my ashes back to Earth and give it to my family.";
			else if (rand == 2)
				this.lastWord = "Send my ashes to orbit around Mars one day.";
			else if (rand == 3)
				this.lastWord = "I have to move on. Farewell.";
			else if (rand == 4)
				this.lastWord = "I want to be buried outside the settlement.";
			else if (rand == 5)
				this.lastWord = "Take care my friend.";
			else if (rand == 6)
				this.lastWord = "I will be the patron saint for the future Mars generations.";
			else 
				this.lastWord = "I'm leaving this world. No more sorrow to bear. See ya.";
		}
		
		MasterClock masterClock = Simulation.instance().getMasterClock();
		
		timeOfDeath = masterClock.getMarsClock().getDateTimeStamp();
				
		missionSol = masterClock.getMarsClock().getMissionSol();
		
		earthTimeOfDeath = masterClock.getEarthClock().getTimeStampF1();
				
		if (problem == null) {
			Complaint serious = person.getPhysicalCondition().getMostSerious();
			if (serious != null) {
				this.illness = serious.getType();
				healthCondition = 0;
				cause = "Non-Illness Related";
			}
		} else {
			this.illness = problem.getIllness().getType();
			healthCondition = problem.getHealthRating();
		}

		if (person.isInVehicle()) {
			// such as died inside a vehicle
			containerUnit = person.getContainerUnit();
			placeOfDeath = person.getVehicle().getName();
		}

		else if (person.isOutside()) {
			placeOfDeath = person.getCoordinates().toString();// "An known location on Mars";
		}

		else if (person.isInSettlement()) {
			placeOfDeath = person.getSettlement().getName();
			// It's eligible for retrieval
			bodyRetrieved = true;
		}

		else if (person.isBuried()) {
			placeOfDeath = person.getBuriedSettlement().getName();
		}

		else {
			placeOfDeath = "Unspecified Location";
		}

		locationOfDeath = person.getCoordinates();

		logger.log(Level.WARNING, person + " passed away in " + placeOfDeath);

		Mind mind = person.getMind();
		
		job = mind.getJob();

		TaskManager taskMgr = mind.getTaskManager();

		task = taskMgr.getTaskName();
		if (task == null || task.equals(""))
			task = taskMgr.getLastTaskName();

		taskPhase = taskMgr.getTaskDescription(false);
		if (taskPhase.equals(""))
			taskPhase = taskMgr.getLastTaskDescription();

		subTask = taskMgr.getSubTaskName();
//		if (subTask == null || subTask.equals(""))
//			subTask = taskMgr.getLastTaskName();

		taskPhase = taskMgr.getSubTaskDescription();
//		if (taskPhase.equals(""))
//			taskPhase = taskMgr.getLastTaskDescription();
		
		Mission mm = mind.getMission();
		if (mm != null) {
			mission = mm.getDescription();
			missionPhase = mm.getPhaseDescription();
		}

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
		Malfunction mostSerious = null;
		int severity = 0;
		while (i.hasNext()) {
			Malfunctionable entity = i.next();
			MalfunctionManager malfunctionMgr = entity.getMalfunctionManager();
			if (malfunctionMgr.hasEmergencyMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousEmergencyMalfunction();
				if (m != null && m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}

			else if (malfunctionMgr.hasEVAMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousEVAMalfunction();
				if (m != null && m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}

			else if (malfunctionMgr.hasGeneralMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousGeneralMalfunction();
				if (m != null && m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}

			else if (malfunctionMgr.hasMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousMalfunction();
				if (m != null && m.getSeverity() > severity) { // why java.lang.NullPointerException ?
					mostSerious = m;
					severity = m.getSeverity();
				}
			}
		}

		if (mostSerious != null)
			malfunction = mostSerious.getName();

	}

	public DeathInfo(Robot robot) {
		// Initialize data members
		this.robot = robot;
		
		timeOfDeath = Simulation.instance().getMasterClock().getMarsClock().getDateTimeStamp();

		BotMind botMind = robot.getBotMind();

		robotJob = botMind.getRobotJob();

		BotTaskManager taskMgr = botMind.getBotTaskManager();
		if (taskMgr.hasTask()) {

			if (task == null)
				task = taskMgr.getTaskName();

			if (taskPhase == null) {
				TaskPhase phase = taskMgr.getPhase();
				if (phase != null) {
					taskPhase = phase.getName();
				}
				// else {
				// taskPhase = "";
				// }
			}
		}

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
		Malfunction mostSerious = null;
		int severity = 0;
		while (i.hasNext()) {
			Malfunctionable entity = i.next();
			MalfunctionManager malfunctionMgr = entity.getMalfunctionManager();
			if (malfunctionMgr.hasEmergencyMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousEmergencyMalfunction();
				if (m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}

			else if (malfunctionMgr.hasEVAMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousEVAMalfunction();
				if (m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}

			else if (malfunctionMgr.hasGeneralMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousGeneralMalfunction();
				if (m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}

			else if (malfunctionMgr.hasMalfunction()) {
				Malfunction m = malfunctionMgr.getMostSeriousMalfunction();
				if (m.getSeverity() > severity) {
					mostSerious = m;
					severity = m.getSeverity();
				}
			}
		}

		if (mostSerious != null)
			malfunction = mostSerious.getName();

		this.robotType = robot.getRobotType();

	}

	/**
	 * Get the time of death.
	 * 
	 * @return formatted time.
	 */
	public String getTimeOfDeath() {
		if (timeOfDeath != null)
			return timeOfDeath;
		else
			return "";
	}

	/**
	 * Get the earth time of death.
	 * 
	 * @return formatted time.
	 */
	public String getEarthTimeOfDeath() {
		if (earthTimeOfDeath != null)
			return earthTimeOfDeath;
		else
			return "";
	}
	
	/**
	 * Gets the mission sol on the day of passing
	 * 
	 * @return
	 */
	public int getMissionSol() {
		return missionSol;
	}
	
	/**
	 * Gets the place the death happened. Either the name of the unit the person was
	 * in, or 'outside' if the person died on an EVA.
	 * 
	 * @return place of death.
	 */
	public String getPlaceOfDeath() {
		if (placeOfDeath != null)
			return placeOfDeath;
		else
			return "";
	}

	/**
	 * Gets the container unit at the time of death. Returns null if none.
	 * 
	 * @return container unit
	 */
	public Unit getContainerUnit() {
		return containerUnit;
	}

	public void backupContainerUnit(Unit c) {
		containerUnit = c;
	}
	
	public void backupContainerID(int c) {
		containerID = c;
	}
	
	/**
	 * Get the type of the illness that caused the death.
	 * 
	 * @return type of the illness.
	 */
	public ComplaintType getIllness() {
		if (illness != null)
			return illness;
		else
			return null;// "";
	}

	/**
	 * Gets the location of death.
	 * 
	 * @return coordinates
	 */
	public Coordinates getLocationOfDeath() {
		return locationOfDeath;
	}

	/**
	 * Gets the person's job at the time of death.
	 * 
	 * @return job
	 */
	public String getJob() {
		if (job != null)
			return job.getName(gender);
		else
			return "   --";
	}

	public String getRobotJob() {
		if (robotJob != null)
			return RobotJob.getName(robotType);
		else
			return "   --";
	}

	/**
	 * Gets the mission the person was on at time of death.
	 * 
	 * @return mission name
	 */
	public String getMission() {
		if (mission != null)
			return mission;
		else
			return "   --";
	}

	/**
	 * Gets the mission phase at time of death.
	 * 
	 * @return mission phase
	 */
	public String getMissionPhase() {
		if (missionPhase != null)
			return missionPhase;
		else
			return "   --";
	}

	/**
	 * Gets the task the person was doing at time of death.
	 * 
	 * @return task name
	 */
	public String getTask() {
		if (task != null)
			return task;
		else
			return "   --";
	}

	/**
	 * Gets the sub task the person was doing at time of death.
	 * 
	 * @return sub task name
	 */
	public String getSubTask() {
		if (subTask != null)
			return subTask;
		else
			return "   --";
	}
	
	/**
	 * Gets the task phase at time of death.
	 * 
	 * @return task phase
	 */
	public String getTaskPhase() {
		if (taskPhase != null)
			return taskPhase;
		else
			return "   --";
	}

	/**
	 * Gets the sub task phase at time of death.
	 * 
	 * @return sub task phase
	 */
	public String getSubTaskPhase() {
		if (subTaskPhase != null)
			return subTaskPhase;
		else
			return "   --";
	}
	
	/**
	 * Gets the most serious emergency malfunction local to the person at time of
	 * death.
	 * 
	 * @return malfunction name
	 */
	public String getMalfunction() {
		if (malfunction != null)
			return malfunction;
		else
			return "   --";
	}

	public void setBodyRetrieved(boolean b) {
		bodyRetrieved = b;
	}

	public boolean getBodyRetrieved() {
		return bodyRetrieved;
	}

	public void setExamDone(boolean value) {
		examDone = value;
	}
	
	public boolean getExamDone() {
		return examDone;
	}
	
	public HealthProblem getProblem() {
		return problem;
	}
	
	/**
	 * Gets the cause of death.
	 * 
	 * @return
	 */
	public String getCause() {
		return causeOfDeath;
	}
	
	/**
	 * Gets the cause of death.
	 * 
	 * @return
	 */
	public void setCause(String cause) {
		causeOfDeath = cause;
	}
	
	/**
	 * Gets the doctor's name.
	 * 
	 * @return
	 */
	public String getDoctor() {
		return doctorName;
	}
	
	/**
	 * Gets the person. 
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * Gets the robot. 
	 */
	public Robot getRobot() {
		return robot;
	}
	
	public double getTimeExam() {
		return timeExam;
	}

	public void addTimeExam(double time) {
		timeExam += time;
	}
	
	public double getEstTimeExam() {
		return estTimeExam;
	}
	
	public void setEstTimeExam(double time) {
		estTimeExam = time;
	}
	
	public double getHealth() {
		return healthCondition;
	}
	
	public void setLastWord(String s) {
		lastWord = s;
	}

	public String getLastWord() {
		return lastWord;
	}
	
	public RoleType getRoleType() {
		return roleType;
	}
	
	public void setRoleType(RoleType type) {
		roleType = type;
	}
}
