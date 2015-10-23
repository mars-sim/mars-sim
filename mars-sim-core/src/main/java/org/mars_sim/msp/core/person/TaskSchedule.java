/**
 * Mars Simulation Project
 * TaskSchedule.java
 * @version 3.08 2015-06-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents the task schedule of a person.
 */
public class TaskSchedule implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final int A_START = 0;
	public static final int A_END = 499;
	public static final int B_START = 500;
	public static final int B_END = 999;
	public static final int X_START = 0;
	public static final int X_END = 333;
	public static final int Y_START = 334;
	public static final int Y_END = 665;
	public static final int Z_START = 666;
	public static final int Z_END = 999;
	
	// Data members
	private int solCache;
	private int startTime;
	private String actorName;
	private String taskName;
	private String doAction;
	private String phase;
	private String shiftType;

	private Map <Integer, List<OneTask>> schedules;
	private List<OneTask> todaySchedule;

	private MarsClock clock;
	private Person person;
	private Robot robot;

	/**
	 * Constructor.
	 * @param person
	 */
	public TaskSchedule(Person person) {
		this.person = person;
		actorName = person.getName();
		this.solCache = 1;
		this.schedules = new ConcurrentHashMap <>();
		this.todaySchedule = new CopyOnWriteArrayList<OneTask>();
	
		clock = Simulation.instance().getMasterClock().getMarsClock();
	}

	public TaskSchedule(Robot robot) {
		this.robot = robot;
		actorName = robot.getName();
		this.solCache = 1;
		this.schedules = new ConcurrentHashMap <>();
		this.todaySchedule = new CopyOnWriteArrayList<OneTask>();
		
		clock = Simulation.instance().getMasterClock().getMarsClock();
	}

	/**
	 * Records a task onto the schedule
	 * @param taskName
	 * @param description
	 */
	public void recordTask(String taskName, String description, String phase) {
		this.taskName = taskName;
		this.doAction = description;
		this.phase = phase;
	
		int startTime = (int) clock.getMillisol();
		int solElapsed = clock.getTotalSol();
		if (solElapsed != solCache) {
        	//System.out.println("solCache is " + solCache + "   solElapsed is " + solElapsed);		
        	// save yesterday's schedule (except on the very first day when there's nothing to save from the prior day
        	schedules.put(solCache, todaySchedule);
        	// create a new schedule for the new day
    		todaySchedule = new CopyOnWriteArrayList<OneTask>();

        	solCache = solElapsed;    	
    		// 2015-10-21 Added recordYestersolTask()
        	recordYestersolLastTask();
		}

		// add this task
		todaySchedule.add(new OneTask(startTime, taskName, description, phase));

	}

	/*
     * Performs the actions per frame
     * @param time amount of time passing (in millisols).
     */
	// 2015-06-29 Added timePassing()
    public void timePassing(double time) {
    }

    /*
     *  Records the first task of the sol on today's schedule as the last task from yestersol
     */
    // 2015-10-21 Added recordYestersolLastTask()
    public void recordYestersolLastTask() {
 /*   	
		Task currentTask = null;
		
		if (person != null) {
			currentTask = person.getMind().getTaskManager().getTask();
		}
		else if (robot != null) {			
			currentTask = robot.getBotMind().getTaskManager().getTask();	
		}	
		
		if (currentTask != null) {
			//String doAction = currentTask.getDescription();
			//String name = currentTask.getName();				
			//recordTask(name, doAction, 1);
		}
*/
    	if (solCache > 1) {
    		// Load the last task from yestersol's schedule
    		List<OneTask> yesterSolschedule = schedules.get(solCache-1);
    		OneTask lastTask = yesterSolschedule.get(yesterSolschedule.size()-1); 
    		//String name = lastTask.getTaskName();
    		//String des = lastTask.getDoAction();	
    		// Save this yestersol task as the first task on today's schedule
    		todaySchedule.add(new OneTask(0, lastTask.getTaskName(), lastTask.getDescription(), lastTask.getPhase()));
    	}
    }
    
	/**
	 * Gets all schedules of a person.
	 * @return schedules
	 */
	public Map <Integer, List<OneTask>> getSchedules() {
		return schedules;
	}

	/**
	 * Gets the today's schedule.
	 * @return todaySchedule
	 */
	public List<OneTask> getTodaySchedule() {
		return todaySchedule;
	}

	/**
	 * Gets the current sol.
	 * @return solCache
	 */
	public int getSolCache() {
		return solCache;
	}

	public int getShiftStart() {
		int start = -1;
		if (shiftType.equals("A"))
			start = A_START;
		else if (shiftType.equals("B"))
			start = B_START;
		if (shiftType.equals("X"))
			start = X_START;
		else if (shiftType.equals("Y"))
			start = Y_START;
		else if (shiftType.equals("Z"))
			start = Z_START;
		return start;
	}

	public int getShiftEnd() {
		int start = -1;
		if (shiftType.equals("A"))
			start = A_END;
		else if (shiftType.equals("B"))
			start = B_END;
		if (shiftType.equals("X"))
			start = X_END;
		else if (shiftType.equals("Y"))
			start = Y_END;
		else if (shiftType.equals("Z"))
			start = Z_END;
		return start;
	}
	
	public String getShiftType() {
		return shiftType;
	}


	public void setShiftType(String shiftType){
		this.shiftType = shiftType;
	}

	public boolean isShiftHour(int millisols){
		boolean result = false;
		if (shiftType.equals("A")) {
			if (millisols == 1000 || (millisols >= A_START && millisols <= A_END))
				result = true;
		}

		else if (shiftType.equals("B")) {
			if (millisols >= B_START && millisols <= B_END)
				result = true;
		}

		if (shiftType.equals("X")) {
			if (millisols == 1000 || (millisols >= X_START && millisols <= X_END))
				result = true;
		}

		else if (shiftType.equals("Y")) {
			if (millisols >= Y_START && millisols <= Y_END)
				result = true;
		}

		else if (shiftType.equals("Z")) {
			if (millisols >= Z_START && millisols <= Z_END)
				result = true;
		}

		return result;
	}

	 public class OneTask implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private String taskName;
		private String description;
		private String phase;
		private int startTime;

		public OneTask(int startTime, String taskName, String description, String phase) {
			this.taskName = taskName;
			this.description = description;
			this.startTime = startTime;
			this.phase = phase;
		}

		/**
		 * Gets the start time of the task.
		 * @return start time
		 */
		public int getStartTime() {
			return startTime;
		}

		/**
		 * Gets the task name.
		 * @return task name
		 */
		public String getTaskName() {
			return taskName;
		}

		/**
		 * Gets the description what the actor is doing
		 * @return what the actor is doing
		 */
		public String getDescription() {
			return description;
		}


		/**
		 * Gets the task phase.
		 * @return task phase
		 */
		public String getPhase() {
			return phase;
		}
	}

/*
	public class Shift implements Serializable {


		private static final long serialVersionUID = 1L;

		// Data members
		private String shiftName;
		private int startTime;
		private int endTime;

		public Shift(String shiftName, int startTime, int endTime) {
			this.shiftName = shiftName;
			this.startTime = startTime;
			this.endTime = endTime;
		}
*
		**
		 * Gets the shift name
		 * @return shift name
		 *
		public String getShiftName() {
			return shiftName;
		}

		**
		 * Gets the start time of the task
		 * @return start time
		 *
		public int getStartTime() {
			return startTime;
		}

		**
		 * Gets the end time of the task
		 * @return end time
		 *
		public int getEndTime() {
			return endTime;
		}
	}
*/
}