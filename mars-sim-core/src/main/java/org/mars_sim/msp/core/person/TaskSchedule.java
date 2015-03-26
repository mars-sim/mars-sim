/**
 * Mars Simulation Project
 * TaskSchedule.java
 * @version 3.08 2015-03-26
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the task schedule of a person.
 */
public class TaskSchedule
implements Serializable {
//Cloneable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private int solCache;	
	private int startTime;
	private String actorName;
	private String taskName;
	private String doAction;	
	//private String startTime;
	
	private Map <Integer, List<DailyTask>> schedules;	  	
	private List<DailyTask> currentSchedule;	

	//private Person person;
	private MarsClock time;

	/**
	 * Constructor.
	 * @param actorName
	 * @param doAction
	 * @param taskName
	 * @param startTime
	 */
	public TaskSchedule(Person person) {	
		//this.person = person;
		actorName = person.getName();
		this.solCache = 0;
		this.schedules = new HashMap <Integer, List<DailyTask>>();
		this.currentSchedule = new ArrayList<DailyTask>();
	}
	
	public TaskSchedule(Robot robot) {	
		//this.robot = robot;
		actorName = robot.getName();
		this.solCache = 0;
		this.schedules = new HashMap <Integer, List<DailyTask>>();
		this.currentSchedule = new ArrayList<DailyTask>();
	}
	
	public void addTask(String taskName, String doAction) {
		this.taskName = taskName;
		this.doAction = doAction;
		this.time = Simulation.instance().getMasterClock().getMarsClock();
		this.startTime = (int) time.getMillisol();
		     
        // check for the passing of each day
        int solElapsed = MarsClock.getSolOfYear(time);         
        if ( solElapsed != solCache) {       	       	        	     
        	//System.out.println("solCache is " + solCache + "   solElapsed is " + solElapsed);
        	// save yesterday's schedule (except on the very first day when there's nothing to save from the prior day
        	if (solCache != 0) 
        		schedules.put(solCache, currentSchedule);   
        	// create a new schedule for the new day
        	List<DailyTask> newSchedule =  new ArrayList<DailyTask>();
    		this.currentSchedule = newSchedule;    		
        	solCache = solElapsed; 
        }
        //if (currentSchedule.isEmpty()) {
        DailyTask dailyTask = new DailyTask(startTime, taskName, doAction);			
        currentSchedule.add(dailyTask);  	

	}
	
	/**
	 * Gets all schedules of a person.
	 * @return schedules
	 */
	public Map <Integer, List<DailyTask>> getSchedules() {
		return schedules;
	}
	
	/**
	 * Gets the today's schedule.
	 * @return currentSchedule
	 */
	public List<DailyTask> getCurrentSchedule() {
		return currentSchedule;
	}
	
	/**
	 * Gets the current sol.
	 * @return solCache
	 */
	public int getSolCache() {
		return solCache;
	}
	
	/**
	 * Gets the task name.
	 * @return task name
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Gets the actor's name.
	 * @return actor's name
	 */
	public String getActorName() {
		return actorName;
	}

	/**
	 * Gets what the actor is doing
	 * @return what the actor is doin
	 */
	public String getDoAction() {
		return doAction;
	}
	
	/**
	 * Gets the start time of the task.
	 * @return start time
	 */
	public int getStartTime() {
		return startTime;
	}
	
	
	public class DailyTask implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private String taskName;

		private String doAction;
		
		private int startTime;

		public DailyTask(int startTime, String taskName, String doAction) {
			this.taskName = taskName;
			this.doAction = doAction;
			//time = Simulation.instance().getMasterClock().getMarsClock();	
			//this.startTime = time.getMillisolString();
			//this.startTime = (int) time.getMillisol();
			this.startTime = startTime;
		}
				
		public int getStartTime() {
			return startTime;
		}

		public String getTaskName() {
			return taskName;
		}
		
		public String getDoAction() {
			return doAction;
		}
		
	}
	
}