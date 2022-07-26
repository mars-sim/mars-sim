/*
 * Mars Simulation Project
 * TaskSchedule.java
 * @date 2021-12-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

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

	// Data members
	private ShiftType currentShiftType;
	private ShiftType shiftTypeCache;

	private Person person;

	/** The degree of willingness (0 to 100) to take on a particular work shift. */
	private Map<ShiftType, Integer> shiftChoice;

	private static MarsClock marsClock;
	
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
	 * @return true or false
	 */
	public boolean isPersonAtStartOfWorkShift() {
		int now = marsClock.getMillisolInt();

		if (currentShiftType == ShiftType.ON_CALL) {
			return true; //isTimeAtStartOfAShift(missionWindow);
		}

		else if (currentShiftType == ShiftType.A) {
            return now >= A_START && (now <= A_START + 250);
		}

		else if (currentShiftType == ShiftType.B) {
            return now >= B_START && (now <= B_START + 250);
		}

		else if (currentShiftType == ShiftType.X) {
            return now >= X_START && (now <= X_START + 166);
		}

		else if (currentShiftType == ShiftType.Y) {
            return now >= Y_START && now <= Y_START + 166;
		}

		else if (currentShiftType == ShiftType.Z) {
            return now >= Z_START && now <= Z_START + 166;
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
		int now = marsClock.getMillisolInt();
		
		if ((now == 1000 || now >= A_START) && now <= A_START + missionWindow)
			return true;

		if (now >= B_START && now <= B_START + missionWindow)
			return true;

		if ((now == 1000 || now >= X_START) && now <= X_START + missionWindow)
			return true;

		if (now >= Y_START && now <= Y_START + missionWindow)
			return true;

        return now >= Z_START && now <= Z_START + missionWindow;
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
	public List<ShiftType> getPreferredShift() {
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

		return map.entrySet().stream().sorted((v1, v2) -> {
				return v2.getValue().compareTo(v1.getValue());
				})
				.map(e -> e.getKey())
				.collect(Collectors.toList());
	}

	/**
	 * Allocate a work shift for this person
	 */
	public void allocateAWorkShift() {
		person.getAssociatedSettlement().assignWorkShift(person, person.getAssociatedSettlement().getPopulationCapacity());
	}

	/**
	 * Initializes instances
	 *
	 * @param mc {@link MarsClock}
	 */
	public static void initializeInstances(MarsClock mc) {
		marsClock = mc;
	}

	public void destroy() {
		person = null;

		currentShiftType = null;
		shiftTypeCache = null;
		shiftChoice = null;
	}
}
