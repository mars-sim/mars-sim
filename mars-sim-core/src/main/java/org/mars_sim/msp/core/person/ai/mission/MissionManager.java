/**
 * Mars Simulation Project
 * MissionManager.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMission;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMissionUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class keeps track of ongoing missions
 * in the simulation.<br>
 * <br>
 * The simulation has only one mission manager.
 */
public class MissionManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static transient Logger logger = Logger.getLogger(MissionManager.class.getName());

	// Data members
	private String phaseDescriptionCache;
	/** Current missions in the simulation. */
	private List<Mission> missions;
	/** Mission listeners. */
	private transient List<MissionManagerListener> listeners;

	// Cache variables.
	private transient Person personCache;
	private transient Robot robotCache;
	private transient MarsClock personTimeCache;
	private transient MarsClock robotTimeCache;
	private transient Map<MetaMission, Double> missionProbCache;
	private transient Map<MetaMission, Double> robotMissionProbCache;
	private transient double totalProbCache;
	//private transient MarsClock currentTime;// = Simulation.instance().getMasterClock().getMarsClock();
	
	/**
	 * Constructor.
	 */
	public MissionManager() {
		// Initialize cache values.
		personCache = null;
		robotCache = null;
		personTimeCache = null;
		robotTimeCache = null;
		totalProbCache = 0D;

		//if (Simulation.instance().getMasterClock() != null)
		//	currentTime = Simulation.instance().getMasterClock().getMarsClock();
		
		// Initialize data members
		missions = new ArrayList<Mission>(0);
		listeners = Collections.synchronizedList(new ArrayList<MissionManagerListener>(0));
		missionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getMetaMissions().size());
		robotMissionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getRobotMetaMissions().size());
	}


	/**
	 * Add a listener.
	 * @param newListener The listener to add.
	 */
	public void addListener(MissionManagerListener newListener) {

		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<MissionManagerListener>());
		}

		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		}
	}

	/**
	 * Remove a listener.
	 * @param oldListener the listener to remove.
	 */
	public void removeListener(MissionManagerListener oldListener) {

		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<MissionManagerListener>());
		}

		if (listeners.contains(oldListener)) {
			listeners.remove(oldListener);
		}
	}

	/**
	 * Gets the number of currently active missions.
	 * @return number of active missions
	 */
	public int getNumActiveMissions() {
		// Remove inactive missions.
		cleanMissions();
		return missions.size();
	}

	/**
	 * Gets a list of current missions.
	 * @return list of missions.
	 */
	public List<Mission> getMissions() {
		// Remove inactive missions.
		cleanMissions();
		if (missions != null)
			return new ArrayList<Mission>(missions);
		else
			return new ArrayList<Mission>();
	}

	/**
	 * Gets the mission a given person is a member of.
	 * If member isn't a part of any mission, return null.
	 * @param member the member.
	 * @return mission for that member
	 */
	public Mission getMission(MissionMember member) {
		Mission result = null;
		for (Mission tempMission : missions) {
		    if (tempMission.hasMember(member)) {
				result = tempMission;
			}
		}

		return result;
	}


	/*
	 * Prepares the task for recording in the task schedule
	 * @param newTask
	 */
	// 2015-10-22 Added recordMission()
	public void recordMission() {//Mission newMission) {
		// Records the new mission

		Mission newMission = null;
		if (personCache != null) {
			newMission = getMission(personCache);
		}
		else if (robotCache != null) {
			newMission = getMission(robotCache);
		}

		if (newMission != null) {
			String phaseDescription = newMission.getPhaseDescription();

			if (!phaseDescription.equals(phaseDescriptionCache)) {

				String desc = newMission.getDescription();
				String name = newMission.getName();

				if (personCache != null) {
					personCache.getTaskSchedule().recordTask(name, desc, phaseDescription);
				}
				else if (robotCache != null) {
					robotCache.getTaskSchedule().recordTask(name, desc, phaseDescription);
				}

				phaseDescriptionCache = phaseDescription;
			}
		}
	}

	/**
	 * Adds a new mission to the mission list.
	 * @param newMission new mission to be added
	 */
	public void addMission(Mission newMission) {
		if (newMission == null) {
			throw new IllegalArgumentException("newMission is null");
		}

		if (!missions.contains(newMission)) {
			missions.add(newMission);

			// Update listeners.
			if (listeners == null) {
				listeners = Collections.synchronizedList(new ArrayList<MissionManagerListener>());
			}

			synchronized(listeners) {
				Iterator<MissionManagerListener> i = listeners.iterator();
				while (i.hasNext()) {
					i.next().addMission(newMission);
				}
			}

			// 2015-10-22 Added recordMission()
			//recordMission(newMission);

			logger.finer("MissionManager: Added new mission - " + newMission.getName());
		}
	}

	/**
	 * Removes a mission from the mission list.
	 * @param the mission to be removed
	 */
	private void removeMission(Mission oldMission) {

		if (missions.contains(oldMission)) {
			missions.remove(oldMission);

			// Update listeners.
			if (listeners == null) {
				listeners = Collections.synchronizedList(new ArrayList<MissionManagerListener>());
			}

			synchronized(listeners) {
				Iterator<MissionManagerListener> i = listeners.iterator();
				while (i.hasNext()) {
					i.next().removeMission(oldMission);
				}
			}

			logger.finer("MissionManager: Removed old mission - " + oldMission.getName());
		}
	}

	/**
	 * Determines the total probability weight for available potential missions
	 * for a given person.
	 * @param person the given person
	 * @return total probability weight
	 */
	public double getTotalMissionProbability(Person person) {
		// If cache is not current, calculate the probabilities.
		if (!useCache(person)) {
			calculateProbability(person);
		}
		return totalProbCache;
	}

	public double getTotalMissionProbability(Robot robot) {
		// If cache is not current, calculate the probabilities.
		if (!useCache(robot)) {
			calculateProbability(robot);
		}
		return totalProbCache;
	}

	/**
	 * Gets a new mission for a person based on potential missions available.
	 * @param person person to find the mission for
	 * @return new mission
	 */
	public Mission getNewMission(Person person) {
		Mission result = null;
		// If cache is not current, calculate the probabilities.
		if (!useCache(person)) {
			calculateProbability(person);
		}

		// Get a random number from 0 to the total weight
		double totalProbability = getTotalMissionProbability(person);

		if (totalProbability == 0D) {
			throw new IllegalStateException(person +
					" has zero total mission probability weight.");
		}

		// Get a random number from 0 to the total probability weight.
		double r = RandomUtil.getRandomDouble(totalProbability);

		// Determine which mission is selected.
		MetaMission selectedMetaMission = null;
		Iterator<MetaMission> i = missionProbCache.keySet().iterator();
		while (i.hasNext() && (selectedMetaMission == null)) {
			MetaMission metaMission = i.next();
			double probWeight = missionProbCache.get(metaMission);
			if (r <= probWeight) {
				selectedMetaMission = metaMission;
			}
			else {
				r -= probWeight;
			}
		}

		if (selectedMetaMission == null) {
			throw new IllegalStateException(person + " could not determine a new mission.");
		}

		// Construct the mission
		result = selectedMetaMission.constructInstance(person);

		// Clear time cache.
		personTimeCache = null;

		return result;
	}

/*	
	public Mission getNewMission(Robot robot) {
		Mission result = null;
		// If cache is not current, calculate the probabilities.
		if (!useCache(robot)) {
			calculateProbability(robot);
		}

		// Get a random number from 0 to the total weight
		double totalProbability = getTotalMissionProbability(robot);

		if (totalProbability == 0D) {
			throw new IllegalStateException(robot +
					" has zero total mission probability weight.");
		}

		// Get a random number from 0 to the total probability weight.
		double r = RandomUtil.getRandomDouble(totalProbability);

		// Determine which mission is selected.
		MetaMission selectedMetaMission = null;
		for (MetaMission mm : robotMissionProbCache.keySet()) {
			double probWeight = robotMissionProbCache.get(mm);
			if (r <= probWeight) {
				selectedMetaMission = mm;
			}
			else {
				r -= probWeight;
			}
		}

		if (selectedMetaMission == null) {
			throw new IllegalStateException(robot + " could not determine a new mission.");
		}

		// Construct the mission
		result = selectedMetaMission.constructInstance(robot);

		// Clear time cache.
		robotTimeCache = null;

		return result;
	}
*/
	/**
	 * Gets all the active missions associated with a given settlement.
	 * @param settlement the settlement to find missions.
	 * @return list of missions associated with the settlement.
	 */
	public List<Mission> getMissionsForSettlement(Settlement settlement) {

		if (settlement == null) {
			//System.out.println("settlement is null");
			throw new IllegalArgumentException("settlement is null");
		}

		List<Mission> settlementMissions = new ArrayList<Mission>();
		if (!getMissions().isEmpty()) {
			Iterator<Mission> i = getMissions().iterator();
			while (i.hasNext()) {
				Mission mission = i.next();
				if (!mission.isDone() && (settlement == mission.getAssociatedSettlement())) {
					settlementMissions.add(mission);
				}
			}
		}
		
		return settlementMissions;
	}

	/**
	 * Gets a mission that the given vehicle is a part of.
	 * @param vehicle the vehicle to check for.
	 * @return mission or null if none.
	 */
	public Mission getMissionForVehicle(Vehicle vehicle) {

		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}

		Mission result = null;

		Iterator<Mission> i = getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (!mission.isDone()) {
				if (mission instanceof VehicleMission) {
					if (((VehicleMission) mission).getVehicle() == vehicle) {
						result = mission;
					}

					if (mission instanceof Mining) {
						if (((Mining)mission).getLightUtilityVehicle() == vehicle) {
							result = mission;
						}
					}

					if (mission instanceof Trade) {
						Rover towingRover = (Rover) ((Trade) mission).getVehicle();
						if (towingRover != null) {
							if (towingRover.getTowedVehicle() == vehicle) {
								result = mission;
							}
						}
					}
				}
				else if (mission instanceof BuildingConstructionMission) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (construction.getConstructionVehicles() != null) {
						if (construction.getConstructionVehicles().contains(vehicle)) {
							result = mission;
						}
					}
					//else {
						// 2015-12-28
					//	result = null;
					//}
				}
				else if (mission instanceof BuildingSalvageMission) {
					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
					if (salvage.getConstructionVehicles().contains(vehicle)) {
						result = mission;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Remove missions that are already completed.
	 */
	private void cleanMissions() {

		int index = 0;
		if (missions != null) { // for passing maven test
			while (index < missions.size()) {
				Mission tempMission = missions.get(index);
				if ((tempMission == null) || tempMission.isDone()) {
					removeMission(tempMission);
				}
				else {
					index++;
				}
			}
		}
	}

	/**
	 * Calculates and caches the probabilities.
	 * @param person the person to check for.
	 */
	private void calculateProbability(Person person) {
		if (missionProbCache == null) {
			missionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getMetaMissions().size());
		}

		// Clear total probabilities.
		totalProbCache = 0D;

		// Determine probabilities.
		Iterator<MetaMission> i = MetaMissionUtil.getMetaMissions().iterator();
		while (i.hasNext()) {
			MetaMission metaMission = i.next();
			double probability = metaMission.getProbability(person);
			if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				missionProbCache.put(metaMission, probability);
				totalProbCache += probability;
			}
			else {
				missionProbCache.put(metaMission, 0D);
				logger.severe(person.getName() + " bad mission probability: " +  metaMission.getName() +
						" probability: " + probability);
			}
		}

		// Set the time cache to the current time.
		personTimeCache = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		//if (currentTime == null && Simulation.instance().getMasterClock() != null)
		//	currentTime = Simulation.instance().getMasterClock().getMarsClock();
		
		//personTimeCache = (MarsClock) currentTime.clone();

		personCache = person;
	}

	private void calculateProbability(Robot robot) {
		if (robotMissionProbCache == null) {
			robotMissionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getRobotMetaMissions().size());
		}

		// Clear total probabilities.
		totalProbCache = 0D;

		// Determine probabilities.
		Iterator<MetaMission> i = MetaMissionUtil.getRobotMetaMissions().iterator();
		while (i.hasNext()) {
			MetaMission metaMission = i.next();
			double probability = metaMission.getProbability(robot);
			if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				robotMissionProbCache.put(metaMission, probability);
				totalProbCache += probability;
			}
			else {
				robotMissionProbCache.put(metaMission, 0D);
				logger.severe(robot.getName() + " bad mission probability: " +  metaMission.getName() +
						" probability: " + probability);
			}
		}

		// Set the time cache to the current time.
		robotTimeCache = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		//robotTimeCache = (MarsClock) currentTime.clone();

		robotCache = robot;
	}

	/**
	 * Checks if task probability cache should be used.
	 * @param the person to check for.
	 * @return true if cache should be used.
	 */
	private boolean useCache(Person person) {
		//if (currentTime == null)
		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		return currentTime.equals(personTimeCache) && (person == personCache);
	}

	private boolean useCache(Robot robot) {
		//if (currentTime == null)
		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		return currentTime.equals(robotTimeCache) && (robot == robotCache);
	}
	/**
	 * Updates mission based on passing time.
	 * @param time amount of time passing (millisols)
	 */
	public void timePassing(double time) {
		Iterator<Mission> i = missions.iterator();
		while (i.hasNext()) {
			i.next().timePassing(time);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		if (missions != null) {
			missions.clear();
			missions = null;
		}
		if (listeners != null) {
			listeners.clear();
			listeners = null;
		}
		personCache = null;
		personTimeCache = null;
		robotTimeCache = null;
		if (missionProbCache != null) {
			missionProbCache.clear();
			missionProbCache = null;
		}
		if (robotMissionProbCache != null) {
			robotMissionProbCache.clear();
			robotMissionProbCache = null;
		}

	}
}