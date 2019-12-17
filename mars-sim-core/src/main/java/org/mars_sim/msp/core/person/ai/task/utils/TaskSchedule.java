/**
 * Mars Simulation Project
 * TaskSchedule.java
 * @version 3.1.0 2017-08-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
//import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsClock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * This class represents the task schedule of a person.
 */
public class TaskSchedule implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TaskSchedule.class.getName());

	/**
	 * Set the number of Sols to be logged (to limit the memory usage & saved file
	 * size)
	 */
	public static final int NUM_SOLS = 100;
	public static final int ON_CALL_START = 0;
	public static final int ON_CALL_END = 999;
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

	public static final int MISSION_WINDOW = 100;
	
	// Data members
	private int solCache;
	private int startTime;
	private int id0Cache;
	private int id1Cache;
	private int id2Cache;
	private int id3Cache;
	
	private String actorName;
	private String taskName;
	private String doAction;
	private String phase;

	private ShiftType currentShiftType;
	private ShiftType shiftTypeCache;

	private Person person;
	private Robot robot;

	/* The degree of willingness (0 to 100) to take the work shift. */
	private Map<ShiftType, Integer> shiftChoice;

	// private Map <Integer, List<OneTask>> schedules;
	// private List<OneTask> todaySchedule;
	private transient Map<Integer, List<OneActivity>> allActivities;
//	private Map<String, Integer> taskDescriptions;
//	private Map<String, Integer> taskNames;
//	private Map<String, Integer> missionNames;
//	private Map<String, Integer> taskPhases;
//	private Map<String, Integer> functions;

	private BiMap<Integer, String> taskDescriptions = HashBiMap.create();
	private BiMap<Integer, String> taskNames = HashBiMap.create();
	private BiMap<Integer, String> missionNames = HashBiMap.create();
	private BiMap<Integer, String> taskPhases = HashBiMap.create();
	
	private List<OneActivity> todayActivities;

	private static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();

	/**
	 * Constructor for TaskSchedule
	 * 
	 * @param person
	 */
	public TaskSchedule(Person person) {
		this.person = person;
		actorName = person.getName();
		this.solCache = 1;
		allActivities = new ConcurrentHashMap<>();
		todayActivities = new CopyOnWriteArrayList<OneActivity>();
		// this.schedules = new ConcurrentHashMap <>();
		// this.todaySchedule = new CopyOnWriteArrayList<OneTask>();
		
//		taskDescriptions = new ConcurrentHashMap<String, Integer>();
//		taskNames = new ConcurrentHashMap<String, Integer>();
//		missionNames = new ConcurrentHashMap<String, Integer>();
//		taskPhases = new ConcurrentHashMap<String, Integer>();
//		functions = new ConcurrentHashMap<String, Integer>();

		shiftChoice = new HashMap<>();
		shiftChoice.put(ShiftType.X, 51);
		shiftChoice.put(ShiftType.Y, 51);
		shiftChoice.put(ShiftType.Z, 51);
		shiftChoice.put(ShiftType.A, 51);
		shiftChoice.put(ShiftType.B, 51);
		shiftChoice.put(ShiftType.ON_CALL, 50);
		shiftChoice.put(ShiftType.OFF, 50);

//		if (Simulation.instance().getMasterClock() != null)
//			marsClock = Simulation.instance().getMasterClock().getMarsClock();
	}

	/**
	 * Constructor for TaskSchedule
	 * 
	 * @param robot
	 */
	public TaskSchedule(Robot robot) {
		this.robot = robot;
		actorName = robot.getName();
		this.solCache = 1;
		allActivities = new ConcurrentHashMap<>();
		todayActivities = new CopyOnWriteArrayList<OneActivity>();
		// this.schedules = new ConcurrentHashMap <>();
		// this.todaySchedule = new CopyOnWriteArrayList<OneTask>();
//		taskDescriptions = new ConcurrentHashMap<String, Integer>();
//		taskNames = new ConcurrentHashMap<String, Integer>();
//		missionNames = new ConcurrentHashMap<String, Integer>();
//		taskPhases = new ConcurrentHashMap<String, Integer>();
//		functions = new ConcurrentHashMap<String, Integer>();

//		marsClock = Simulation.instance().getMasterClock().getMarsClock();
	}

	/**
	 * Records a task onto the schedule
	 * 
	 * @param taskName
	 * @param description
	 */
	public void recordTask(String task, String description, String phase, String mission) {
		
		startTime = marsClock.getMillisolInt();
		int solElapsed = marsClock.getMissionSol();
		
		// For a new day
		if (solCache != solElapsed) {

			if (allActivities == null)
				allActivities = new ConcurrentHashMap<>();
			
			// Removed the sol log from LAST_SOL ago
			if (solElapsed > NUM_SOLS) {
				int diff = solElapsed - NUM_SOLS;
				allActivities.remove(diff);
				if (allActivities.containsKey(diff - 1))
					allActivities.remove(diff - 1);
			}

			// Save yesterday's schedule (except on the very first day when there's nothing
			// to save from the prior day
			allActivities.put(solCache, todayActivities);
			// Update solCache
			solCache = solElapsed;
			// Create a new schedule for this brand new day
			todayActivities = new CopyOnWriteArrayList<OneActivity>();
			
			if (solElapsed > 1)
				// Add recordYestersolTask()
				recordYestersolLastTask();

		}

		// Add maps
		int id0 = getID(taskNames, task);
		int id1 = getID(taskDescriptions, description);
		int id2 = getID(taskPhases, phase);
		int id3 = getID(missionNames, mission);
		// int id3 = getID(functions, functionType.toString());

		if (id0Cache != id0
				|| id1Cache != id1
				|| id2Cache != id2
				|| id3Cache != id3) {
			
			todayActivities.add(new OneActivity(startTime, id0, id1, id2, id3));
			id0Cache = id0;
			id1Cache = id1;
			id2Cache = id2;
			id3Cache = id3;
		}
	}

	/**
	 * Gets the ID of a BiMap
	 * 
	 * @param map
	 * @param value
	 * @return
	 */
	public int getID(BiMap<Integer, String> map, String value) {
		if (map.containsValue(value)) {
			return map.inverse().get(value);
		} else {
			int size = map.size();
			map.put(size + 1, value);
			return size + 1;
		}
	}

	/**
	 * Gets the string of a map
	 * 
	 * @param map
	 * @param id
	 * @return
	 */
	public String getString(Map<Integer, String> map, Integer id) {
		return map.get(id);
	}

	public String convertTaskName(Integer id) {
		return getString(taskNames, id);
	}

	public String convertMissionName(Integer id) {
		return getString(missionNames, id);
	}

	public String convertTaskDescription(Integer id) {
		return getString(taskDescriptions, id);
	}

	public String convertTaskPhase(Integer id) {
		return getString(taskPhases, id);
	}

	public Map<ShiftType, Integer> getShiftChoice() {
		return shiftChoice;
	}
	
	/**
	 * Normalize the score of the shift choice toward the center score of 50.
	 */
	public void normalizeShiftChoice() {
		for (ShiftType key : shiftChoice.keySet()) {
			int score = shiftChoice.get(key);
			if (score < 50) {
				shiftChoice.put(key, score + 1);
			} else
				shiftChoice.put(key, score - 1);
		}
	}

	/**
	 * Increments the score of the shift choice .
	 */
	public void incrementShiftChoice() {
		for (ShiftType key : shiftChoice.keySet()) {
			int score = shiftChoice.get(key);
			if (score < 100) {
				shiftChoice.put(key, score + 1);
			}
		}
	}

	/**
	 * Adjusts the work shift choice
	 * 
	 * @param hours array
	 */
	public void adjustShiftChoice(int[] hours) {

		ShiftType st0 = determineShiftType(hours[0]);
		ShiftType st1 = determineShiftType(hours[1]);

		for (ShiftType key : shiftChoice.keySet()) {

			if (key == st0 || key == st1) {
				// increment the score
				int score = shiftChoice.get(key);
				if (score < 100) {
					shiftChoice.put(key, score - 1);
				}
			}

			else {
				// decrement the score
				int score = shiftChoice.get(key);
				if (score > 0) {
					shiftChoice.put(key, score + 1);
				}
			}
		}
	}

	/**
	 * Determines the work shift type
	 * 
	 * @param hour
	 * @return {@link ShiftType}
	 */
	public ShiftType determineShiftType(int hour) {
		ShiftType st = null;
		int numShift = person.getAssociatedSettlement().getNumShift();

		if (numShift == 2) {
			if (hour <= A_END)
				st = ShiftType.A;
			else
				st = ShiftType.B;
		}

		else if (numShift == 3) {
			if (hour <= X_END)
				st = ShiftType.X;
			else if (hour <= Y_END)
				st = ShiftType.Y;
			else
				st = ShiftType.Z;
		}

		return st;
	}

//	private Optional<String> getKey(ConcurrentHashMap<String, Integer> map, Integer value){
//	    return map.entrySet().stream().filter(e -> e.getValue().equals(value)).map(e -> e.getKey()).findFirst();
//	}

//	/*
//	 * Performs the actions per frame
//	 * 
//	 * @param time amount of time passing (in millisols).
//	 */
//    public void timePassing(double time) {
//    }

	/*
	 * Records the first task of the sol on today's schedule as the last task from
	 * yestersol
	 */
	public void recordYestersolLastTask() {
		if (solCache > 1) {
			if (allActivities == null)
				allActivities = new ConcurrentHashMap<>();
			// Load the last task from yestersol's schedule
			List<OneActivity> yesterSolschedule = allActivities.get(solCache - 1);

			if (yesterSolschedule != null) {
				int size = yesterSolschedule.size();
				if (size != 0) {
					OneActivity lastTask = yesterSolschedule.get(yesterSolschedule.size() - 1);
					// Carry over and save the last yestersol task as the first task on today's
					// schedule
					// Set the last task from yesterday to 000 millisol
					todayActivities.add(new OneActivity(0, lastTask.getTaskName(), lastTask.getDescription(),
							lastTask.getPhase(), lastTask.getMission()));// , lastTask.getFunction()));
				}
			}
		}
	}

	/**
	 * Gets all activities of all days a person.
	 * 
	 * @return all activity schedules
	 */
	public Map<Integer, List<OneActivity>> getAllActivities() {
		if (allActivities == null)
			allActivities = new ConcurrentHashMap<>();
		return allActivities;
	}
	
	public double getTaskTime(int sol, String name) {
		double time = 0;
		if (allActivities == null)
			allActivities = new ConcurrentHashMap<>();
		if (allActivities.containsKey(sol)) {
			List<OneActivity> list = allActivities.get(sol);
			int size = list.size();
			for (int i=0; i < size; i++) {
				OneActivity o0 = list.get(i);
				String tName = convertMissionName(o0.getTaskName());
				if (tName.equals(name)) {
//					System.out.println("tName : " + tName);
					int endTime = 1000;
					if (i+1 < size) {
						endTime = list.get(i + 1).getStartTime();
					}
					
					time += endTime - o0.getStartTime();
//					System.out.println("time : " + time);
				}
			}
		}
		
		return time;
	}

	/**
	 * Asks if it is a task name
	 * 
	 * @param taskName
	 * @return
	 */
	public boolean isEVATask(String taskName) {
		String t = taskName.toLowerCase();
		return (t.contains("eva")
				|| t.contains("dig")
				|| t.contains("exploresite")
				|| t.contains("salvagebuilding")
				|| t.contains("walkoutside")
				|| t.contains("minesite")
				|| t.contains("collectmined")
				|| t.contains("fieldwork")
				|| t.contains("collectresources")
				);
	}
	
	
	/**
	 * Gets the EVA task time of a sol
	 * 
	 * @param sol
	 * @return
	 */
	public double getEVATasksTime(int sol) {
		double time = 0;
		if (allActivities == null)
			allActivities = new ConcurrentHashMap<>();
		if (allActivities.containsKey(sol)) {
			List<OneActivity> list = allActivities.get(sol);
			int size = list.size();
			for (int i=0; i < size; i++) {
				OneActivity o0 = list.get(i);
				String tName = convertTaskName(o0.getTaskName());
				if (isEVATask(tName)) {
//					System.out.println("tName : " + tName);
					int endTime = 1000;
					if (i+1 < size) {
						endTime = list.get(i + 1).getStartTime();
					}
					
					time += endTime - o0.getStartTime();
//					System.out.println("time : " + time);
				}
			}
		}
		
		return time;
	}

	/**
	 * Checks if it is an airlock task
	 * 
	 * @param taskName
	 * @return
	 */
	public boolean isAirlockTask(String taskName) {
		return (taskName.toLowerCase().contains("airlock"));
	}
	
	/**
	 * Gets the airlock task time of a sol
	 * 
	 * @param sol
	 * @return
	 */
	public double getAirlockTasksTime(int sol) {
		double time = 0;
		if (allActivities == null)
			allActivities = new ConcurrentHashMap<>();
		if (allActivities.containsKey(sol)) {
			List<OneActivity> list = allActivities.get(sol);
			int size = list.size();
			for (int i=0; i < size; i++) {
				OneActivity o0 = list.get(i);
				String tName = convertTaskName(o0.getTaskName());
				if (isAirlockTask(tName)) {
					int endTime = 0;
					if (i+1 < size) {
						OneActivity o1 = list.get(i + 1);
						endTime = o1.getStartTime();
					}
					else {
						endTime = 1000;
					}
					
					time += endTime - o0.getStartTime();
				}
			}
		}
		
		return time;
	}
	
	/**
	 * Gets the today's activities.
	 * 
	 * @return a list of today's activities
	 */
	public List<OneActivity> getTodayActivities() {
		return todayActivities;
	}

	/**
	 * Gets the current sol.
	 * 
	 * @return solCache
	 */
	public int getSolCache() {
		return solCache;
	}

	/**
	 * Gets the time the shift starts
	 * 
	 * @return time in millisols
	 */
	public int getShiftStart() {
		int start = -1;
		if (currentShiftType.equals(ShiftType.A))
			start = A_START;
		else if (currentShiftType.equals(ShiftType.B))
			start = B_START;
		else if (currentShiftType.equals(ShiftType.X))
			start = X_START;
		else if (currentShiftType.equals(ShiftType.Y))
			start = Y_START;
		else if (currentShiftType.equals(ShiftType.Z))
			start = Z_START;
		else if (currentShiftType.equals(ShiftType.ON_CALL))
			start = ON_CALL_START;
		return start;
	}

	/**
	 * Gets the time the shift end
	 * 
	 * @return time in millisols
	 */
	public int getShiftEnd() {
		int end = -1;
		if (currentShiftType.equals(ShiftType.A))
			end = A_END;
		else if (currentShiftType.equals(ShiftType.B))
			end = B_END;
		else if (currentShiftType.equals(ShiftType.X))
			end = X_END;
		else if (currentShiftType.equals(ShiftType.Y))
			end = Y_END;
		else if (currentShiftType.equals(ShiftType.Z))
			end = Z_END;
		else if (currentShiftType.equals(ShiftType.ON_CALL))
			end = ON_CALL_END;
		return end;
	}

	/***
	 * Gets the shift type
	 * 
	 * @return shift type
	 */
	public ShiftType getShiftType() {
		return currentShiftType;
	}

	/*
	 * Sets up the shift type
	 * 
	 * @param shiftType
	 */
	public void setShiftType(ShiftType newShift) {

		if (newShift != null) {

			// Decrement the desire to tak on-call work shift
			if (shiftTypeCache == ShiftType.ON_CALL) {
				int score = shiftChoice.get(ShiftType.ON_CALL);
				if (score > 1) {
					shiftChoice.put(ShiftType.ON_CALL, score - 1);
				}
			}

			// Back up the previous shift type
			shiftTypeCache = currentShiftType;

			// Update the new shift type
			currentShiftType = newShift;

			if (person != null) {

				Settlement s = null;

				if (person.isBuried()) {
					s = person.getBuriedSettlement();

					if (shiftTypeCache != null)
						s.decrementAShift(shiftTypeCache);

					s.incrementAShift(newShift);
				} 
				
				else if (person.isDeclaredDead()) {
					s = person.getAssociatedSettlement();

					if (shiftTypeCache != null)
						s.decrementAShift(shiftTypeCache);

					s.incrementAShift(newShift);
				}
				
				else {
					s = person.getAssociatedSettlement();

					if (shiftTypeCache != null)
						s.decrementAShift(shiftTypeCache);

					s.incrementAShift(newShift);

//					if (marsClock == null)
//						marsClock = Simulation.instance().getMasterClock().getMarsClock();

					int now = marsClock.getMillisolInt();
					boolean isOnShiftNow = isShiftHour(now);
					boolean isOnCall = getShiftType() == ShiftType.ON_CALL;

					// if a person is NOT on-call && is on shift right now
					if (!isOnCall && isOnShiftNow) {
						// suppress sleep habit right now
						person.updateSleepCycle(now, false);
					}
				}
			}

			// Call CircadianClock immediately to adjust the sleep hour according

		}

		else
			logger.warning("TaskSchedule: setShiftType() : " + person + "'s new shiftType is null");
	}

	/*
	 * Checks if a person is on shift
	 * 
	 * @param time in millisols
	 * 
	 * @return true or false
	 */
	public boolean isShiftHour(int millisols) {
		boolean result = false;

		if (currentShiftType == ShiftType.A) {
			if (millisols == 1000 || (millisols >= A_START && millisols <= A_END))
				result = true;
		}

		else if (currentShiftType == ShiftType.B) {
			if (millisols >= B_START && millisols <= B_END)
				result = true;
		}

		else if (currentShiftType == ShiftType.X) {
			if (millisols == 1000 || (millisols >= X_START && millisols <= X_END))
				result = true;
		}

		else if (currentShiftType == ShiftType.Y) {
			if (millisols >= Y_START && millisols <= Y_END)
				result = true;
		}

		else if (currentShiftType == ShiftType.Z) {
			if (millisols >= Z_START && millisols <= Z_END)
				result = true;
		}

		else if (currentShiftType == ShiftType.ON_CALL) {
			result = true;
		}

		return result;
	}

	/**
	 * Checks if a person is at the beginning (within the mission window) of his work shift
	 * 
	 * @param missionWindow in millisols
	 * @return true or false
	 */
	public boolean isPersonAtStartOfWorkShift(int missionWindow) {
		int millisols = startTime;

		if (currentShiftType == ShiftType.ON_CALL) {
			return isTimeAtStartOfAShift(missionWindow);
		}
		
		if (currentShiftType == ShiftType.A) {
			if (millisols == 1000 || (millisols >= A_START && millisols <= A_START + missionWindow))
				return true;
		}

		if (currentShiftType == ShiftType.B) {
			if (millisols >= B_START && millisols <= B_START + missionWindow)
				return true;
		}

		if (currentShiftType == ShiftType.X) {
			if (millisols == 1000 || (millisols >= X_START && millisols <= X_START + missionWindow))
				return true;
		}

		if (currentShiftType == ShiftType.Y) {
			if (millisols >= Y_START && millisols <= Y_START + missionWindow)
				return true;
		}

		if (currentShiftType == ShiftType.Z) {
			if (millisols >= Z_START && millisols <= Z_START + missionWindow)
				return true;
		}

		return false;
	}
	
	/**
	 * Checks if the time now is at the beginning (within mission window) of a work shift
	 * 
	 * @param missionWindow in millisols
	 * @return true or false
	 */
	public boolean isTimeAtStartOfAShift(int missionWindow) {
		int millisols = startTime;

		if ((millisols == 1000 || millisols >= A_START) && millisols <= A_START + missionWindow)
			return true;

		if (millisols >= B_START && millisols <= B_START + missionWindow)
			return true;

		if ((millisols == 1000 || millisols >= X_START) && millisols <= X_START + missionWindow)
			return true;

		if (millisols >= Y_START && millisols <= Y_START + missionWindow)
			return true;

		if (millisols >= Z_START && millisols <= Z_START + missionWindow)
			return true;

		return false;
	}
	
	/**
	 * Gets the score of a work shift
	 * 
	 * @param st
	 * @return score
	 */
	public int getWorkShiftScore(ShiftType st) {
		return shiftChoice.get(st);
	}

	/**
	 * Gets the preferred work shift 
	 * 
	 * @return
	 */
	public ShiftType[] getPreferredShift() {
		int i1 = 0;
		ShiftType st1 = null;
		int i2 = 0;
		ShiftType st2 = null;
		
		Map<ShiftType, Integer> map = new HashMap<>(shiftChoice);
		
		int numShift = person.getAssociatedSettlement().getNumShift();
		
		if (numShift == 3) {
			map.remove(ShiftType.A);
			map.remove(ShiftType.B);
			map.remove(ShiftType.OFF);
		}
		else {
			map.remove(ShiftType.X);
			map.remove(ShiftType.Y);
			map.remove(ShiftType.Z);
			map.remove(ShiftType.OFF);
		}
	
//		if (currentShiftType == ShiftType.X
//		 || currentShiftType == ShiftType.Y
//		 || currentShiftType == ShiftType.Z) {
//			map.remove(ShiftType.A);
//			map.remove(ShiftType.B);
//			map.remove(ShiftType.OFF);
//		}
//		else if (currentShiftType == ShiftType.ON_CALL) {	
//		}

		for (ShiftType s : map.keySet()) {
			int score = map.get(s);
		    if (i1 < score) {
		    	i2 = i1;
		    	st2 = st1;
		        i1 = score;
		        st1 = s;
		    }
		}
		
		return new ShiftType[] {st1, st2};
	}
	
	/*
	 * This class represents a record of a given activity (task or mission)
	 * undertaken by a person
	 */
	public class OneActivity implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private int taskName;
		private int missionName;
		private int description;
		private int phase;
		private int startTime;
		// private int function;

		public OneActivity(int startTime, int taskName, int description, int phase, int missionName) {
			this.taskName = taskName;
			this.missionName = missionName;
			this.description = description;
			this.startTime = startTime;
			this.phase = phase;
			// this.function = function;
		}

		/**
		 * Gets the start time of the task.
		 * 
		 * @return start time
		 */
		public int getStartTime() {
			return startTime;
		}

		/**
		 * Gets the task name.
		 * 
		 * @return task name id
		 */
		public int getTaskName() {
			return taskName;
		}

		/**
		 * Gets the description what the actor is doing.
		 * 
		 * @return description id
		 */
		public int getDescription() {
			return description;
		}

		/**
		 * Gets the task phase.
		 * 
		 * @return task phase id
		 */
		public int getPhase() {
			return phase;
		}

		public int getMission() {
			return missionName;
		}
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}
	
	public void destroy() {
		person = null;
		marsClock = null;
		robot = null;
		// todaySchedule = null;
		// schedules = null;
		allActivities = null;
		todayActivities = null;
		currentShiftType = null;
		shiftTypeCache = null;
	}
}