/**
 * Mars Simulation Project
 * MissionManager.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMission;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMissionUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class keeps track of ongoing missions in the simulation.<br>
 * <br>
 * The simulation has only one mission manager.
 */
public class MissionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static transient Logger logger = Logger.getLogger(MissionManager.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static final int MAX_NUM_PLANS = 100;
	
	private static final double PERCENT_PER_SCORE = 10D;
	/** static mission identifier */
	private int missionIdentifer;

	/** Mission listeners. */
	private transient List<MissionManagerListener> listeners;

	// Cache variables.
	private transient double totalProbCache;
	
	/** The currently on-going missions in the simulation. */
	private List<Mission> onGoingMissions;
	/** A history of mission plans by sol. */
	private Map<Integer, List<MissionPlanning>> historicalMissions;
	
	// Transient members
	private transient MarsClock personTimeCache;
	private transient Map<MetaMission, Double> missionProbCache;
	private transient Map<MetaMission, Double> robotMissionProbCache;
	
	private static List<String> missionNames;
	private static Map<String, Integer> settlementID;
	
	// Note : MissionManager is instantiated before MarsClock
	// Need to call setMarsClock() right after MarsClock is initialized
	private static MarsClock marsClock;
	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	static {
		/**
		 * Creates an array of all missions
		 */
		missionNames = Arrays.asList(
					AreologyFieldStudy.DEFAULT_DESCRIPTION,
					BiologyFieldStudy.DEFAULT_DESCRIPTION,
					CollectIce.DEFAULT_DESCRIPTION,
					CollectRegolith.DEFAULT_DESCRIPTION,
					EmergencySupply.DEFAULT_DESCRIPTION,
					
					Exploration.DEFAULT_DESCRIPTION,
					MeteorologyFieldStudy.DEFAULT_DESCRIPTION,
					Mining.DEFAULT_DESCRIPTION,
					RescueSalvageVehicle.DEFAULT_DESCRIPTION,
					Trade.DEFAULT_DESCRIPTION,
					
					TravelToSettlement.DEFAULT_DESCRIPTION,
					
					BuildingConstructionMission.DEFAULT_DESCRIPTION, 
					BuildingSalvageMission.DEFAULT_DESCRIPTION
			);
		}
	
	
	/**
	 * Constructor.
	 */
	public MissionManager() {
		// Initialize cache values.
		personTimeCache = null;
		totalProbCache = 0D;
		
		// Initialize data members
		missionIdentifer = 0;
		onGoingMissions = new CopyOnWriteArrayList<>();
		historicalMissions = new HashMap<>();
		settlementID = new HashMap<>();
		listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<MissionManagerListener>(0));
		missionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getNumMetaMissions());
		robotMissionProbCache = new HashMap<MetaMission, Double>();
	}

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private synchronized int getNextIdentifier() {
		return missionIdentifer++;
	}
	
//	public static int getSettlementID(String name) {
//		return getSettlementID(name);
//	}
	
	public static int getSettlementID(String name) {
		if (settlementID.containsKey(name)) {
			return settlementID.get(name);			
		}
		else {
			int size = settlementID.size();
			settlementID.put(name, size);
			
			return size;
		}
	}
	
	public String getMissionDesignationString(String settlementName) {
		return padZeros(getSettlementID(settlementName)+"", 2) + "-" + padZeros(getNextIdentifier()+"", 3);
	}
	
	public static String padZeros(String s, int numDigital) {
		String value = "";
		int size = s.length();
		int numZeros = numDigital-size;
		if (numZeros > 0) {
			for (int i=0; i< numDigital-size; i++)
				value += "0";
			value += s;
		}
		
		else 
			value = "" + s;
		
		return value;
	}
	
	
	/**
	 * Add a listener.
	 * 
	 * @param newListener The listener to add.
	 */
	public void addListener(MissionManagerListener newListener) {

		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<MissionManagerListener>());
		}

		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		}
	}

	/**
	 * Remove a listener.
	 * 
	 * @param oldListener the listener to remove.
	 */
	public void removeListener(MissionManagerListener oldListener) {

		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<MissionManagerListener>());
		}

		if (listeners.contains(oldListener)) {
			listeners.remove(oldListener);
		}
	}

	/**
	 * Gets the number of currently active missions.
	 * 
	 * @return number of active missions
	 */
	public int getNumActiveMissions() {
		// Remove inactive missions.
		//cleanMissions();
		return onGoingMissions.size();
	}

	/**
	 * Gets a list of current missions.
	 * 
	 * @return list of missions.
	 */
	public List<Mission> getMissions() {
//		// Remove inactive missions.
//		//cleanMissions();
		if (onGoingMissions != null) {
			if (GameManager.mode == GameMode.COMMAND) {
				List<Mission> missions = new ArrayList<Mission>();
				if (unitManager == null)
					unitManager = Simulation.instance().getUnitManager();
				Iterator<Mission> i = onGoingMissions.iterator();
				while (i.hasNext()) {
					Mission m = i.next(); 
					if (m.getAssociatedSettlement().equals(unitManager.getCommanderSettlement()))
						missions.add(m);
				}
				
				return missions;
			}
			else {
				return new ArrayList<Mission>(onGoingMissions);
			}

		}			
//			return missions;
		else
			return new ArrayList<Mission>();
	}

	/**
	 * Gets the mission a given person is a member of. If member isn't a part of any
	 * mission, return null.
	 * 
	 * @param member the member.
	 * @return mission for that member
	 */
	public Mission getMission(MissionMember member) {
		Mission result = null;
		for (Mission tempMission : onGoingMissions) {
			if (tempMission.hasMember(member)) {
				result = tempMission;
			}
		}

		return result;
	}

//	/*
//	 * Prepares the task for recording in the task schedule
//	 * @param newTask
//	 */
//	public void recordMission(MissionMember member) {
//		Mission newMission = null;
//		Person p = null;
//		Robot r = null;
//		
//		if (member instanceof Person) {
//			p = (Person) member;
//			newMission = getMission(p);
//		}
//		else {
//			r = (Robot) member;
//			newMission = getMission(r);
//		}
///*			
//
//		if (personCache != null) {
//			newMission = getMission(personCache);
//		}
//		else if (robotCache != null) {
//			newMission = getMission(robotCache);
//		}
//*/
//		if (newMission != null) {
//			String phaseDescription = newMission.getPhaseDescription();
//
//			if (!phaseDescription.equals(phaseDescriptionCache)) {
//
//				String desc = newMission.getDescription();
//				String name = newMission.getName();
//				//FunctionType type = FunctionType.UNKNOWN;//newMission.getFunction();
//				
//				if (p != null) {
//					p.getTaskSchedule().recordTask(name, desc, phaseDescription);//, type);
//				}
//				else if (r != null) {
//					r.getTaskSchedule().recordTask(name, desc, phaseDescription);//, type);
//				}
//
//				phaseDescriptionCache = phaseDescription;
//			}
//		}
//	}

	/**
	 * Adds a new mission to the mission list.
	 * 
	 * @param newMission new mission to be added
	 */
	public void addMission(Mission newMission) {
		if (newMission == null) {
			throw new IllegalArgumentException("newMission is null");
		}

		if (!onGoingMissions.contains(newMission)) {
			onGoingMissions.add(newMission);

			// Update listeners.
			if (listeners == null) {
				listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<MissionManagerListener>());
			}

			synchronized (listeners) {
				Iterator<MissionManagerListener> i = listeners.iterator();
				while (i.hasNext()) {
					i.next().addMission(newMission);
				}
			}

			// recordMission(newMission);

			logger.fine("Added a new '" + newMission.getName() + "' mission.");
		}
	}

	/**
	 * Removes a mission from the mission list.
	 * 
	 * @param the mission to be removed
	 */
	public void removeMission(Mission oldMission) {

		if (onGoingMissions.contains(oldMission)) {
			onGoingMissions.remove(oldMission);

			oldMission.fireMissionUpdate(MissionEventType.END_MISSION_EVENT);
					
			// Update listeners.
			if (listeners != null) {
//				listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<MissionManagerListener>());
				synchronized (listeners) {
					Iterator<MissionManagerListener> i = listeners.iterator();
					while (i.hasNext()) {
						i.next().removeMission(oldMission);
					}
				}
			}

			logger.fine("Removing an old '" + oldMission.getName() + "' mission.");
		}
	}

	/**
	 * Determines the total probability weight for available potential missions for
	 * a given person.
	 * 
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

//	public double getTotalMissionProbability(Robot robot) {
//		// If cache is not current, calculate the probabilities.
//		if (!useCache(robot)) {
//			calculateProbability(robot);
//		}
//		return totalProbCache;
//	}

	/**
	 * Gets a new mission for a person based on potential missions available.
	 * 
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
			//throw new IllegalStateException(person + " has zero total mission probability weight.");
			logger.log(Level.FINEST, person + " has zero total mission probability weight. No mission selected.");
			// Clear time cache.
			personTimeCache = null;
			
			return null;
		}

		// Get a random number from 0 to the total probability weight.
		double r = RandomUtil.getRandomDouble(totalProbability);

		// Determine which mission is selected.
		MetaMission selectedMetaMission = null;
		Iterator<MetaMission> i = missionProbCache.keySet().iterator();
		while (i.hasNext() && (selectedMetaMission == null)) {
			MetaMission metaMission = i.next();
			double probWeight = missionProbCache.get(metaMission);
			if (r <= probWeight && probWeight != 0) {
				selectedMetaMission = metaMission;
			} else {
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

//	public Mission getNewMission(Robot robot) {
//		Mission result = null;
//		// If cache is not current, calculate the probabilities.
//		if (!useCache(robot)) {
//			calculateProbability(robot);
//		}
//
//		// Get a random number from 0 to the total weight
//		double totalProbability = getTotalMissionProbability(robot);
//
//		if (totalProbability == 0D) {
//			throw new IllegalStateException(robot +
//					" has zero total mission probability weight.");
//		}
//
//		// Get a random number from 0 to the total probability weight.
//		double r = RandomUtil.getRandomDouble(totalProbability);
//
//		// Determine which mission is selected.
//		MetaMission selectedMetaMission = null;
//		for (MetaMission mm : robotMissionProbCache.keySet()) {
//			double probWeight = robotMissionProbCache.get(mm);
//			if (r <= probWeight) {
//				selectedMetaMission = mm;
//			}
//			else {
//				r -= probWeight;
//			}
//		}
//
//		if (selectedMetaMission == null) {
//			throw new IllegalStateException(robot + " could not determine a new mission.");
//		}
//
//		// Construct the mission
//		result = selectedMetaMission.constructInstance(robot);
//
//		// Clear time cache.
//		robotTimeCache = null;
//
//		return result;
//	}

	/**
	 * Gets the number of particular missions that are active
	 * 
	 * @param mission
	 * @param settlement
	 * @return number
	 */
	public int numParticularMissions(String mName, Settlement settlement) {
		int num = 0;
		List<Mission> m1 = onGoingMissions;
		if (!m1.isEmpty()) {		
			Iterator<Mission> i = m1.iterator();
			while (i.hasNext()) {
				Mission m = i.next();
				if (!m.isDone() && mName.equalsIgnoreCase(m.getName())
						&& settlement == m.getAssociatedSettlement()) {
					num++;
				}
			}
		}
		return num;
	}
	
	/**
	 * Gets all the active missions associated with a given settlement.
	 * 
	 * @param settlement the settlement to find missions.
	 * @return list of missions associated with the settlement.
	 */
	public List<Mission> getMissionsForSettlement(Settlement settlement) {

		if (settlement == null) {
			// System.out.println("settlement is null");
			throw new IllegalArgumentException("settlement is null");
		}

		List<Mission> m0 = new ArrayList<Mission>();
		List<Mission> m1 = getMissions();
		if (!m1.isEmpty()) {		
			Iterator<Mission> i = m1.iterator();
			while (i.hasNext()) {
				Mission m = i.next();
				if (!m.isDone() 
						&& settlement == m.getAssociatedSettlement()) {
					m0.add(m);
				}
			}
		}

		return m0;
	}

	/**
	 * Gets the missions pending for approval in a given settlement.
	 * 
	 * @param settlement
	 * @return list of pending missions associated with the settlement.
	 */
	public List<Mission> getPendingMissions(Settlement settlement) {

		if (settlement == null) {
			// System.out.println("settlement is null");
			throw new IllegalArgumentException("settlement is null");
		}
		
		List<Mission> m0 = new ArrayList<Mission>();
		List<Mission> m1 = onGoingMissions;
		if (!m1.isEmpty()) {		
			Iterator<Mission> i = m1.iterator();
			while (i.hasNext()) {
				Mission m = i.next();
				if (!m.isDone() 
						&& settlement.getName().equalsIgnoreCase(m.getAssociatedSettlement().getName())
//						&& !m.isApproved()
						&& m.getPlan() != null
						&& m.getPlan().getStatus() == PlanType.PENDING) {
					m0.add(m);
				}
			}
		}

		return m0;
	}
	
	/**
	 * Gets a mission that the given vehicle is a part of.
	 * 
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
						if (((Mining) mission).getLightUtilityVehicle() == vehicle) {
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
				} else if (mission instanceof BuildingConstructionMission) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (construction.getConstructionVehicles() != null) {
						if (construction.getConstructionVehicles().contains(vehicle)) {
							result = mission;
							// logger.info(vehicle.getName() + " has been reserved for the building
							// construction mission.");
						}
					}
					// else {
					// result = null;
					// }
				} else if (mission instanceof BuildingSalvageMission) {
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
		if (onGoingMissions != null && !onGoingMissions.isEmpty()) { 
			// Check if onGoingMissions is null for passing maven test
			while (index < onGoingMissions.size()) {
				Mission m = onGoingMissions.get(index);
				List<MissionStatus> mss = m.getMissionStatus();
				if (mss != null && !mss.isEmpty()) {
					for (MissionStatus ms: mss) {
	//					MissionStatus reason = m.getReason().toLowerCase();
						if (// ms == null
		//						|| m.isDone() 
		//						|| !m.isApproved() // initially it's not approved until it passes the approval phase
								m.getPlan() == null
								|| m.getPhase() == null
								|| ms == MissionStatus.DESTINATION_IS_NULL								
								|| ms == MissionStatus.USER_ABORTED_MISSION
								|| ms == MissionStatus.NO_RESERVABLE_VEHICLES
								|| ms == MissionStatus.LUV_ATTACHMENT_PARTS_NOT_LOADABLE				
								|| ms == MissionStatus.LUV_NOT_AVAILABLE
								|| ms == MissionStatus.LUV_NOT_RETRIEVED
								|| ms == MissionStatus.NO_AVAILABLE_VEHICLES
								|| ms == MissionStatus.NO_EXPLORATION_SITES
								|| ms == MissionStatus.EVA_SUIT_CANNOT_BE_LOADED
								|| ms == MissionStatus.MINING_SITE_NOT_BE_DETERMINED
								|| ms == MissionStatus.NEW_CONSTRUCTION_STAGE_NOT_DETERMINED
								|| ms == MissionStatus.NO_TRADING_SETTLEMENT							
//								|| ms.getName().toLowerCase().contains("no ")
//								|| ms.getName().toLowerCase().contains("not ")
								|| (m.getPlan() != null && m.getPlan().getStatus() == PlanType.NOT_APPROVED)
								) {
							removeMission(m);
						} 
					}
				}
				
				index++;
			}
		}
//		if (missions != null) {
//			int size = missions.size();
//			for (int i=0; i<size; i++) {
//				Mission m = missions.get(i);
//				if (m == null || m.isDone() 
//						|| (m.getPlan() != null && m.getPlan().getStatus() == PlanType.NOT_APPROVED)) {
//					removeMission(m);
//				}
//			}
//		}
	}

	/**
	 * Calculates and caches the probabilities.
	 * 
	 * @param person the person to check for.
	 */
	private void calculateProbability(Person person) {
		if (missionProbCache == null) {
			missionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getNumMetaMissions());
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
			} else {
				missionProbCache.put(metaMission, 0D);
				logger.severe(person.getName() + " had bad mission probability on " + metaMission.getName() + " probability: "
						+ probability);
			}
//			if (probability > 0)
//				System.out.println(person + " " + metaMission.getName() + " is " + probability);
		}

		// Set the time cache to the current time.
		personTimeCache = (MarsClock) marsClock.clone();
	}

//	/**
//	 * Calculates and caches the probabilities.
//	 * 
//	 * @param robot the robot to check for.
//	 */
//	private void calculateProbability(Robot robot) {
//		if (robotMissionProbCache == null) {
//			robotMissionProbCache = new HashMap<MetaMission, Double>(MetaMissionUtil.getRobotMetaMissions().size());
//		}
//
//		// Clear total probabilities.
//		totalProbCache = 0D;
//
//		// Determine probabilities.
//		Iterator<MetaMission> i = MetaMissionUtil.getRobotMetaMissions().iterator();
//		while (i.hasNext()) {
//			MetaMission metaMission = i.next();
//			double probability = metaMission.getProbability(robot);
//			if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
//				robotMissionProbCache.put(metaMission, probability);
//				totalProbCache += probability;
//			} else {
//				robotMissionProbCache.put(metaMission, 0D);
//				logger.severe(robot.getName() + " bad mission probability: " + metaMission.getName() + " probability: "
//						+ probability);
//			}
//		}
//
//		// Set the time cache to the current time.
//		robotTimeCache = (MarsClock) marsClock.clone();
//
//	}

	/**
	 * Checks if task probability cache should be used.
	 * 
	 * @param the person to check for.
	 * @return true if cache should be used.
	 */
	private boolean useCache(Person person) {
		return marsClock.equals(personTimeCache);
	}

//	/**
//	 * Checks if task probability cache should be used.
//	 * 
//	 * @param the robot to check for.
//	 * @return true if cache should be used.
//	 */
//	private boolean useCache(Robot robot) {
//		// if (currentTime == null)
//		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
//		return currentTime.equals(robotTimeCache);// && (robot == robotCache);
//	}

	/**
	 * Updates mission based on passing time.
	 * 
	 * @param time amount of time passing (millisols)
	 */
	public void timePassing(double time) {
		// Remove inactive missions
		cleanMissions();
		
//		Iterator<Mission> i = missions.iterator();
//		while (i.hasNext()) {
//			i.next().timePassing(time);
//		}
	}

	/**
	 * Adds a mission plan
	 * 
	 * @param plan {@link MissionPlanning}
	 */
	public void addMissionPlanning(MissionPlanning plan) {
		if (marsClock == null)
			logger.info("marsClock is null");
		int mSol = marsClock.getMissionSol();
		
		logger.info("On sol " + mSol + ", " + plan.getMission().getStartingMember() + " put together a mission plan.");
		
		if (historicalMissions.containsKey(mSol)) {
			List<MissionPlanning> plans = historicalMissions.get(mSol);
			plans.add(plan);
		}
		else {
			List<MissionPlanning> plans = new ArrayList<>();
			plans.add(plan);
			historicalMissions.put(mSol, plans);
			
			// Keep only the last x # of sols of mission plans
			if (mSol > MAX_NUM_PLANS && historicalMissions.size() > MAX_NUM_PLANS) {
				historicalMissions.get(mSol-MAX_NUM_PLANS);
			}
		}
		
//		logger.info("Done addMissionPlanning()");
	}
	
	
	/**
	 * Submit a request for approving a mission plan
	 * 
	 * @param mission
	 */
	public void requestMissionApproving(MissionPlanning plan) {
//		logger.info(plan.getMission().getStartingMember() + " was supposed to call requestMissionApproval()");
		addMissionPlanning(plan);
	}
	
	/**
	 * Approves a mission plan
	 * 
	 * @param missionPlan
	 * @param person
	 * @param status
	 */
	public void approveMissionPlan(MissionPlanning missionPlan, Person person, PlanType newStatus) {
//		logger.info(person + " was at approveMissionPlan()");
		for (int mSol : historicalMissions.keySet()) {
			List<MissionPlanning> plans = historicalMissions.get(mSol);
			for (MissionPlanning mp : plans) {
				if (mp == missionPlan) {
					mp.setApproved(person);
//					mp.setStatus(status);
					if (mp.getStatus() == PlanType.PENDING) {
						if (newStatus == PlanType.APPROVED) {
							mp.setStatus(PlanType.APPROVED);
							mp.getMission().setApproval(true);
//							mp.getMission().setPhase();
						}
						else if (newStatus == PlanType.NOT_APPROVED) {
							mp.setStatus(PlanType.NOT_APPROVED);
							mp.getMission().setApproval(false);
//							mp.getMission().setPhase();
							// Do NOT remove this on-going mission from the current mission list
//							removeMission(mp.getMission());
						}
//						mp.getMission().fireMissionUpdate(MissionEventType.PHASE_EVENT, mp.getMission().getPhaseDescription());
					}
					return;
				}
			}
		}
	}

	/**
	 * Score a mission plan
	 * 
	 * @param missionPlan
	 * @param person
	 * @param status
	 */
	public void scoreMissionPlan(MissionPlanning missionPlan, double newScore, Person reviewer) {
		double weight = 1D;
		RoleType role = reviewer.getRole().getType();
		for (int mSol : historicalMissions.keySet()) {
			List<MissionPlanning> plans = historicalMissions.get(mSol);
			for (MissionPlanning mp : plans) {
				if (mp == missionPlan) {// && mp.getStatus() == PlanType.PENDING) {
					double percent = mp.getPercentComplete();
					
					if (role == RoleType.COMMANDER)
						weight = 2.5;
					else if (role == RoleType.SUB_COMMANDER
							|| role == RoleType.CHIEF_OF_MISSION_PLANNING)
						weight = 2D;
					else if (role == RoleType.CHIEF_OF_AGRICULTURE
							|| role == RoleType.CHIEF_OF_ENGINEERING
							|| role == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
							|| role == RoleType.CHIEF_OF_SAFETY_N_HEALTH
							|| role == RoleType.CHIEF_OF_SCIENCE
							|| role == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES
							|| role == RoleType.MISSION_SPECIALIST)
						weight = 1.5;
					else
						weight = 1;
					double totalPercent = percent + weight * PERCENT_PER_SCORE;
					if (totalPercent > 100)
						totalPercent = 100;
					mp.setPercentComplete(totalPercent);
					double score = mp.getScore();
					mp.setScore(score + weight * newScore);
					logger.info(mp.getMission().getStartingMember() + "'s " + mp.getMission().getDescription() 
							+ " mission plan - current score : " + Math.round(mp.getScore()*10.0)/10.0 
							+ " (" + mp.getPercentComplete() + "% review completed)");
					mp.setReviewedBy(reviewer.getName());
					mp.getMission().setPhaseDescription(mp.getMission().getPhaseDescription());
//					mp.getMission().fireMissionUpdate(MissionEventType.PHASE_DESCRIPTION_EVENT, mp.getMission().getPhaseDescription());
					break;
				}
			}
		}
	}

	public static int matchMissionID(String name) {
		int id = -1;
		if (missionNames.contains(name)) {
			id = missionNames.indexOf(name);
		}
		return id;
	}
	
	public Map<Integer, List<MissionPlanning>> getHistoricalMissions() {
		return historicalMissions;
	}
	
	/**
	 * Gets a list of all mission names
	 */
	public static List<String> getMissionNames() {
		return missionNames;
	}
	
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		if (onGoingMissions != null) {
			onGoingMissions.clear();
			onGoingMissions = null;
		}
		if (listeners != null) {
			listeners.clear();
			listeners = null;
		}

		marsClock = null;
		// personCache = null;
		personTimeCache = null;
//		robotTimeCache = null;
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