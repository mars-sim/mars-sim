/*
 * Mars Simulation Project
 * DeathInfo.java
 * @date 2026-07-29
 * @author Barry Evans
 */

package com.mars_sim.core.person.health;

import java.io.Serializable;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.Mind;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

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

	// Quotes from http://www.phrases.org.uk/quotes/last-words/suicide-notes.html
	// https://www.goodreads.com/quotes/tag/suicide-note
	private static final String[] LAST_WORDS = {"Take care of my family.",
						"One day, send my ashes back to Earth and give it to my family.",
						"Send my ashes to orbit around Mars one day.",
						"I have to move on. Farewell.",
						"I want to be buried outside the settlement.",
						"Take care my friend.",
						"I will be the patron saint for future Mars generations.",
						"I'm leaving this world. No more sorrow to bear. See ya."};

	// Data members

	/** Is the postmortem exam done ? */	
	private boolean examDone = false;	
	/** Amount of time performed so far in postmortem exam [in Millisols]. */	
	private double timeSpentExam;

	/** Estimated time the postmortem exam should take [in Millisols]. */	
	private double estTotExamTime;
	/** Percent of illness*/	
	private double healthCondition;	
	/** Cause of death. */	
	private String causeOfDeath;
	/** Place of death. */
	private String placeOfDeath = "";
	/** Name of the doctor who sign on to transfer the body to the medical. */	
	private String doctorRetrievingBody;
	/** Name of the doctor who sign the death certificate. */	
	private String doctorSigningCertificate;
	
	/** Name of mission at time of death. */
	private String mission;
	/** Name of mission phase at time of death. */
	private String missionPhase;	
	/** Name of task at time of death. */
	private String task;
	/** Phase of task at time of death. */
	private String taskPhase;
	/** Name of the most serious local emergency malfunction. */
	private String malfunction;
	/** The person's last word before departing. */
	private String lastWord = "None";
	
	/** Time of death. */
	private MarsTime timeOfDeath;
	/** Time of death. */
	private MarsTime timePostMortemExam;
	/** Medical problem contributing to the death. */
	private HealthProblem problem;
	/** Container unit at time of death. */
	private Entity containerUnit;
	/** Coordinate at time of death. */
	private Coordinates coordinates;
	/** The person. */
	private Person person;
	/** The robot. */
	private Robot robot;
	
	private Settlement settlement;
	
	/** Name of mission stage at time of death. */
	private Stage missionStage;
	/** The person's job at time of death. */
	private JobType job;
	/** Medical cause of death. */
	private ComplaintType illness;
	/** Person's role type. */
	private RoleType roleType;

	/**
	 * Constructor 1: creates an instance of a DeathInfo class for a person.
	 * 
	 * @param person the dead person
	 * @param problem
	 * @param cause
	 * @param lastWord
	 * @param martianTime
	 */
	public DeathInfo(Person person, HealthProblem problem, String cause, String lastWord,
							MarsTime martianTime) {
		this.person = person;
		this.problem = problem;
		
		containerUnit = person.getContainerUnit();
		
		String medicalCause = "";
		
		// Initialize data members
		if (lastWord == null) {
			int rand = RandomUtil.getRandomInt(LAST_WORDS.length);
			this.lastWord = LAST_WORDS[rand];
		}
		else {
			this.lastWord = lastWord;
		}
		
		timeOfDeath = martianTime;	

		if (problem == null) {
			
			// Double check if there are any medical complains
			var serious = person.getPhysicalCondition().getMostSerious();
			if (serious != null) {
				this.illness = serious.getType();
				healthCondition = 0;
				medicalCause = illness.getName();
			}
			else {
				medicalCause = "Non-Illness Related";
			}
		}
		else {
			this.illness = problem.getComplaint().getType();
			healthCondition = problem.getHealthRating();
			medicalCause = illness.getName();
		}

		// Set the cause of death
		if (cause.equals(PhysicalCondition.TBD)) {
			this.causeOfDeath = medicalCause;
		}
		else
			this.causeOfDeath = cause + "; " + medicalCause;
		
		settlement = person.getAssociatedSettlement();
		if (settlement == null)
			settlement = person.getBuriedSettlement();
		
		coordinates = person.getCoordinates();
		
		// Record the place of death
		if (person.isInVehicle()) {
			// such as died inside a vehicle
			placeOfDeath = person.getVehicle().getName();
		}

		else if (person.isOutside()) {
			placeOfDeath = coordinates.toString();
		}

		else {
			placeOfDeath = settlement.getName();
		}

		Mind mind = person.getMind();
		
		job = mind.getJobType();

		TaskManager taskMgr = mind.getTaskManager();

		task = taskMgr.getTaskName();
		if (task == null || task.equals(""))
			task = taskMgr.getLastTaskName();

		taskPhase = taskMgr.getTaskDescription(false);
		if (taskPhase.equals(""))
			taskPhase = taskMgr.getLastTaskDescription();
		
		if (mind.getMission() != null) {
			mission = mind.getMission().getFullMissionDesignation();
			
			missionPhase = mind.getMission().getPhaseDescription();
			
			missionStage = mind.getMission().getStage();
		}
	}

	/**
	 * Constructor 2: creates an instance of a DeathInfo class for a robot.
	 * 
	 * @param robot
	 * @param martianTime
	 */
	public DeathInfo(Robot robot, MarsTime martianTime) {
		// Initialize data members
		this.robot = robot;
		
		timeOfDeath = martianTime;

		TaskManager taskMgr = robot.getBotMind().getBotTaskManager();
		if (taskMgr.hasTask()) {

			if (task == null)
				task = taskMgr.getTaskName();

			if (taskPhase == null) {
				TaskPhase phase = taskMgr.getPhase();
				if (phase != null) {
					taskPhase = phase.getName();
				}
			}
		}

		MalfunctionManager malfunctionMgr = robot.getMalfunctionManager();
		if (malfunctionMgr.hasMalfunction()) {
			Malfunction m = malfunctionMgr.getMostSeriousMalfunction();
			malfunction = m.getName();
		}
	}

	/**
	 * Get the time of death.
	 * 
	 * @return formatted time.
	 */
	public MarsTime getTimeOfDeath() {
		return timeOfDeath;
	}
	
	/**
	 * Get the time of postmortem exam.
	 * 
	 * @return formatted time.
	 */
	public MarsTime getTimePostMortemExam() {
		return timePostMortemExam;
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
	public Entity getContainerUnit() {
		return containerUnit;
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
			return null;
	}

	/**
	 * Gets the location of death.
	 * 
	 * @return coordinates
	 */
	public Coordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * Gets the person's job at the time of death.
	 * 
	 * @return job
	 */
	public JobType getJob() {
		return job;
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
			return "?";
	}
	
	/**
	 * Gets the mission the person was doing at time of death.
	 * 
	 * @return mission designation
	 */
	public String getMission() {
		return mission;
	}

	/**
	 * Gets the mission phase the person was doing at time of death.
	 * 
	 * @return mission phase
	 */
	public String getMissionPhase() {
		return missionPhase;
	}

	/**
	 * Gets the mission stage the person was doing at time of death.
	 * 
	 * @return mission stage
	 */
	public Stage getMissionStage() {
		return missionStage;
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
			return "None";
	}

	/**
	 * Timestamps the exam when done.
	 * 
	 * @param value
	 */
	public void timestampExamDone(boolean value) {
		timePostMortemExam = Simulation.instance().getMasterClock().getMarsTime();
		examDone = value;
	}
	
	/**
	 * Is the exam done ?
	 * 
	 * @return
	 */
	public boolean getExamDone() {
		return examDone;
	}
	
	/**
	 * Gets the health problem.
	 * 
	 * @return
	 */
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
	 * Gets the name of the doctor retrieving the body.
	 * 
	 * @return
	 */
	public String getDoctorRetrievingBody() {
		return doctorRetrievingBody;
	}

	/**
	 * Sets the name of the doctor retrieving the body.
	 * 
	 * @param name
	 */
	public void setDoctorRetrievingBody(String name) {
		doctorRetrievingBody = name;
	}
	/**
	 * Gets the name of the doctor signing the death certificate.
	 * 
	 * @return
	 */
	public String getDoctorSigningCertificate() {
		return doctorSigningCertificate;
	}

	/**
	 * Sets the name of the doctor signing the death certificate.
	 * 
	 * @param name
	 */
	public void signOffDeathCertificate(String name) {
		doctorSigningCertificate = name;
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
	
	/**
	 * Gets the time spent in examination in millisols.
	 * 
	 * @return
	 */
	public double getTimeSpentExam() {
		return timeSpentExam;
	}

	/**
	 * Adds the time spent in examination in millisols.
	 * 
	 * @return
	 */
	public void addTimeSpentExam(double time) {
		timeSpentExam += time;
	}
	
	public double getEstTimeExam() {
		return estTotExamTime;
	}
	
	public void setEstTimeExam(double time) {
		estTotExamTime = time;
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
