/**
 * Mars Simulation Project
 * TaskSchedule.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class represents the task schedule of a person.
 */
public class TaskSchedule implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(TaskSchedule.class.getName());

	/**
	 * Set the number of Sols to be logged (to limit the memory usage & saved file
	 * size)
	 */
	public static final int NUM_SOLS = 10;
	public static final int ON_CALL_START = 0;
	public static final int ON_CALL_END = 1000;
	
	public static final int A_START = 250;
	public static final int A_END = 750;
	public static final int B_START = 751;
	public static final int B_END = 249;
	
	public static final int X_START = 0;
	public static final int X_END = 333;
	public static final int Y_START = 334;
	public static final int Y_END = 666;
	public static final int Z_START = 667;
	public static final int Z_END = 1000;

	public static final int MISSION_WINDOW = 100;
	
	// Data members
	private int now = 0;
	
	private ShiftType currentShiftType;
	private ShiftType shiftTypeCache;

	private Person person;

	/** The degree of willingness (0 to 100) to take on a particular work shift. */
	private Map<ShiftType, Integer> shiftChoice;


	/**
	 * Constructor for TaskSchedule
	 * 
	 * @param person
	 */
	public TaskSchedule(Person person) {
		this.person = person;

		shiftChoice = new HashMap<>();
		shiftChoice.put(ShiftType.X, 15);
		shiftChoice.put(ShiftType.Y, 50);
		shiftChoice.put(ShiftType.Z, 35);
		shiftChoice.put(ShiftType.A, 75);
		shiftChoice.put(ShiftType.B, 25);
		shiftChoice.put(ShiftType.ON_CALL, 50);
		shiftChoice.put(ShiftType.OFF, 50);
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


	/**
	 * Unused
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
*/
	
//	/**
//	 * Asks if this task is EVA related
//	 * 
//	 * @param taskName
//	 * @return
//	 */
//	public boolean isEVATask(String taskName) {
//		String t = taskName.toLowerCase();
//		for (String s : TASKS) {
//			if (t.contains(s))
//				return true;
//		}
//		return false;
//	}
	
	/**
	 * Gets the EVA task time of a sol
	 * 
	 * @param sol
	 * @return
	 */
	/*
	 * Unused
	public double getEVATasksTime(int sol) {
		double time = 0;
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
*/
	
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
//		int startAirlockTime = -1;
//		List<OneActivity> list = allActivities.getSolData(sol);
//		for (OneActivity oneActivity : list) {
//			String tName = oneActivity.getTaskName();
//			if (startAirlockTime >= 0) {
//				// Count Airlocktime
//				time += (oneActivity.getStartTime() - startAirlockTime);
//				startAirlockTime = -1;
//			}
//			else if (isAirlockTask(tName)) {
//				startAirlockTime = oneActivity.getStartTime();
//			}				
//		}
//		// Anything left?
//		if (startAirlockTime >= 0) {
//			time += 100;
//		}
		
		return time;
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
			logger.warning(person, "setShiftType() : new shiftType is null");
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
		int millisols = now;

		if (currentShiftType == ShiftType.ON_CALL) {
			return isTimeAtStartOfAShift(missionWindow);
		}
		
		else if (currentShiftType == ShiftType.A) {
			if (millisols >= A_START && (millisols <= A_START + missionWindow))
				return true;
		}

		else if (currentShiftType == ShiftType.B) {
			if (millisols >= B_START && (millisols <= B_START + missionWindow))
				return true;
		}

		else if (currentShiftType == ShiftType.X) {
			if (millisols >= X_START && (millisols <= X_START + missionWindow))
				return true;
		}

		else if (currentShiftType == ShiftType.Y) {
			if (millisols >= Y_START && millisols <= Y_START + missionWindow)
				return true;
		}

		else if (currentShiftType == ShiftType.Z) {
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
		int millisols = now;

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
		    	st2 = st1;
		        i1 = score;
		        st1 = s;
		    }
		}
		
		return new ShiftType[] {st1, st2};
	}
	
	/**
	 * Allocate a work shift for this person
	 */
	public void allocateAWorkShift() {
		person.getAssociatedSettlement().assignWorkShift(person, person.getAssociatedSettlement().getPopulationCapacity());
	}
	

	
	public void destroy() {
		person = null;
		
		currentShiftType = null;
		shiftTypeCache = null;
		shiftChoice = null;
	}
}
