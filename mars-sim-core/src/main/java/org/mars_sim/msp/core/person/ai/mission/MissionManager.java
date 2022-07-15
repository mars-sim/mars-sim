/*
 * Mars Simulation Project
 * MissionManager.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMission;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMissionUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class keeps track of ongoing missions in the simulation.<br>
 * <br>
 * The simulation has only one mission manager.
 */
public class MissionManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MissionManager.class.getName());

	private static final double PERCENT_PER_SCORE = 10D;
	/** static mission identifier */
	private int missionIdentifer;

	/** Mission listeners. */
	private transient List<MissionManagerListener> listeners;

	/** The currently on-going missions in the simulation. */
	private List<Mission> onGoingMissions;
	/** A history of mission plans by sol. */
	private SolMetricDataLogger<String> historicalMissions;

	private Map<String, Integer> settlementID;

	/** Prob boost for Mission Types */
	private transient Map<MissionType,Integer> missionBoost = new EnumMap<>(MissionType.class);

	/**
	 * Constructor.
	 */
	public MissionManager() {

		// Initialize data members
		missionIdentifer = 0;
		onGoingMissions = new CopyOnWriteArrayList<>();
		historicalMissions = new SolMetricDataLogger<>(30);
		settlementID = new HashMap<>();
		listeners = null;
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

	private int getSettlementID(String name) {
		synchronized (settlementID) {
			if (settlementID.containsKey(name)) {
				return settlementID.get(name);
			}
			else {
				int size = settlementID.size();
				settlementID.put(name, size);

				return size;
			}
		}
	}

	public String getMissionDesignationString(String settlementName) {
		return String.format("%2d-%d3", getSettlementID(settlementName), getNextIdentifier());
	}

	/**
	 * Add a listener.
	 *
	 * @param newListener The listener to add.
	 */
	public void addListener(MissionManagerListener newListener) {

		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();
		}

		synchronized (listeners) {
			if (!listeners.contains(newListener)) {
				listeners.add(newListener);
			}
		}
	}

	/**
	 * Remove a listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	public void removeListener(MissionManagerListener oldListener) {
		if (listeners != null) {
			synchronized (listeners) {
				if (listeners.contains(oldListener)) {
					listeners.remove(oldListener);
				}
			}
		}
	}

	/**
	 * Gets the number of currently active missions.
	 *
	 * @return number of active missions
	 */
	public int getNumActiveMissions() {
		return onGoingMissions.size();
	}

	/**
	 * Gets a list of current missions.
	 *
	 * @return list of missions.
	 */
	public List<Mission> getMissions() {
		if (onGoingMissions != null) {
			return Collections.unmodifiableList(onGoingMissions);
		}
		else
			return Collections.emptyList();
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

	/**
	 * Gets the mission a given person is a member of. If member isn't a part of any
	 * mission, return null.
	 *
	 * @param member the member.
	 * @return mission for that member
	 */
	public boolean hasMission(MissionMember member) {
		for (Mission tempMission : onGoingMissions) {
			if (tempMission.hasMember(member)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Adds a new mission to the mission list.
	 *
	 * @param newMission new mission to be added
	 */
	public void addMission(Mission newMission) {
		if (newMission == null) {
			throw new IllegalArgumentException("newMission is null");
		}

		synchronized (onGoingMissions) {
			if (!onGoingMissions.contains(newMission)) {
				onGoingMissions.add(newMission);

				// Update listeners.
				if (listeners != null) {
					synchronized (listeners) {
						for(MissionManagerListener l : listeners) {
							l.addMission(newMission);
						}
					}
				}
				logger.config("Added '" + newMission.getTypeID() + "' mission.");
			}
		}
	}

	/**
	 * Removes a mission from the mission list.
	 *
	 * @param the mission to be removed
	 */
	private void removeMission(Mission oldMission) {
		synchronized (onGoingMissions) {
			if (onGoingMissions.contains(oldMission)) {
				onGoingMissions.remove(oldMission);

				// Update listeners.
				if (listeners != null) {
					synchronized (listeners) {
						for(MissionManagerListener l : listeners) {
							l.removeMission(oldMission);
						}
					}
				}

				logger.config("Removing '" + oldMission.getTypeID() + "' mission.");
			}
		}
	}

	/**
	 * Gets a new mission for a person based on potential missions available.
	 *
	 * @param person person to find the mission for
	 * @return new mission
	 */
	public Mission getNewMission(Person person) {
		Mission result = null;

		// Probably must be calculated as a local otherwise method is not threadsafe using a shared cache
		Map<MetaMission, Double> missionProbCache = new HashMap<>();

		// Get a random number from 0 to the total weight
		double totalProbCache = 0D;

		ReportingAuthority sponsor = person.getAssociatedSettlement().getSponsor();

		// Determine probabilities.
		for (MetaMission metaMission : MetaMissionUtil.getMetaMissions()) {
			double baseProb = metaMission.getProbability(person);
			if (Double.isNaN(baseProb) || Double.isInfinite(baseProb)) {
					logger.severe(person, "Bad mission probability on " + metaMission.getName() + " probability: "
							+ baseProb);
			}
			else if (baseProb > 0D) {
				// Get any overriding ratio
				int boost = missionBoost.getOrDefault(metaMission.getType(), 0);
				double probability = baseProb + boost;

				double sponsorRatio = sponsor.getMissionRatio(metaMission.getType());
				probability *= sponsorRatio;

				logger.info(person, "Mission " + metaMission.getType() + " probability=" + probability
								+ " base prob=" + baseProb
								+ " boost=" + boost
								+ " sponsor=" + sponsorRatio);

				missionProbCache.put(metaMission, probability);
				totalProbCache += probability;
			}
		}

		if (totalProbCache == 0D) {
			logger.fine(person, "Has zero total mission probability weight. No mission selected.");

			return null;
		}

		// Get a random number from 0 to the total probability weight.
		double r = RandomUtil.getRandomDouble(totalProbCache);

		// Determine which mission is selected.
		MetaMission selectedMetaMission = null;
		for (Entry<MetaMission, Double> possible : missionProbCache.entrySet()) {
			double probWeight = possible.getValue();
			if (r <= probWeight) {
				selectedMetaMission = possible.getKey();
				break;
			} 

			r -= probWeight;
		}

		if (selectedMetaMission == null) {
			throw new IllegalStateException(person + " could not determine a new mission.");
		}

		// Construct the mission
		result = selectedMetaMission.constructInstance(person);

		return result;
	}


	/**
	 * Gets the number of particular missions that are active
	 *
	 * @param mType
	 * @param settlement
	 * @return number
	 */
	public int numParticularMissions(MissionType mType, Settlement settlement) {
		return (int) onGoingMissions.stream()
							.filter(( m -> !m.isDone()
									&& settlement.equals(m.getAssociatedSettlement())
									&& (m.getMissionType() == mType)))
							.count();
	}

	/**
	 * Gets all the active missions associated with a given settlement.
	 *
	 * @param settlement the settlement to find missions.
	 * @return list of missions associated with the settlement.
	 */
	public List<Mission> getMissionsForSettlement(Settlement settlement) {

		if (settlement == null) {
			throw new IllegalArgumentException("settlement is null");
		}

		if (onGoingMissions == null || onGoingMissions.isEmpty()) {
			return Collections.emptyList();
		}

		return onGoingMissions.stream()
				.filter(m -> (!m.isDone() && settlement.equals(m.getAssociatedSettlement())))
				.collect(Collectors.toList());
	}

	/**
	 * Gets the missions pending for approval in a given settlement.
	 *
	 * @param settlement
	 * @return list of pending missions associated with the settlement.
	 */
	public List<Mission> getPendingMissions(Settlement settlement) {

		if (settlement == null) {
			throw new IllegalArgumentException("settlement is null");
		}

		return onGoingMissions.stream()
							  .filter(m -> (!m.isDone()
									  && settlement.equals(m.getAssociatedSettlement())
									  && m.getPlan() != null
									  && m.getPlan().getStatus() == PlanType.PENDING))
							  .collect(Collectors.toList());
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
				if (mission instanceof VehicleMission
					&& ((VehicleMission) mission).getVehicle() == vehicle) {
					result = mission;
				} else if (mission.getMissionType() == MissionType.BUILDING_CONSTRUCTION) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (!construction.getConstructionVehicles().isEmpty()
						&& construction.getConstructionVehicles().contains(vehicle)) {
							result = mission;
					}
				} else if (mission.getMissionType() == MissionType.BUILDING_SALVAGE) {
					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
					if (!salvage.getConstructionVehicles().isEmpty()
						&& salvage.getConstructionVehicles().contains(vehicle)) {
							result = mission;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Updates mission based on passing time.
	 *
	 * @param pulse Simulation time has advanced
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// Remove inactive missions
		//TODO Create a history mission, e.g. keep aborted & completed seperate and purge

		return true;
	}

	/**
	 * Adds a mission plan
	 *
	 * @param plan {@link MissionPlanning}
	 */
	public void addMissionPlanning(MissionPlanning plan) {

		Mission mission = plan.getMission();
		Person p = mission.getStartingPerson();

		logger.info(p, "Put together a mission plan for " + plan.getMission().getTypeID() + ".");

		// Add this mission only after the mission plan has been submitted for review.
		addMission(mission);
	}

	/**
	 * Submit a request for approving a mission plan
	 *
	 * @param mission
	 */
	public void requestMissionApproving(MissionPlanning plan) {
		addMissionPlanning(plan);
	}

	/**
	 * Approves a mission plan
	 *
	 * @param missionPlan
	 * @param person
	 * @param d
	 * @param status
	 */
	public void approveMissionPlan(MissionPlanning missionPlan, Person person,
								   PlanType newStatus, double threshold) {

		if (missionPlan.getStatus() == PlanType.PENDING) {
			missionPlan.setPassingScore(threshold);

			if (newStatus == PlanType.APPROVED) {
				missionPlan.setStatus(PlanType.APPROVED);
				historicalMissions.increaseDataPoint(PlanType.APPROVED.name(), 1D);
			}
			else if (newStatus == PlanType.NOT_APPROVED) {
				missionPlan.setStatus(PlanType.NOT_APPROVED);
				historicalMissions.increaseDataPoint(PlanType.NOT_APPROVED.name(), 1D);
				removeMission(missionPlan.getMission());
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

		double percent = missionPlan.getPercentComplete();
		switch (role) {
			case COMMANDER:
					weight = 2.5; break;
			case SUB_COMMANDER:
			case CHIEF_OF_MISSION_PLANNING:
				weight = 2D; break;
			case CHIEF_OF_AGRICULTURE:
			case CHIEF_OF_COMPUTING:
			case CHIEF_OF_ENGINEERING:
			case CHIEF_OF_LOGISTICS_N_OPERATIONS:
			case CHIEF_OF_SAFETY_N_HEALTH:
			case CHIEF_OF_SCIENCE:
			case CHIEF_OF_SUPPLY_N_RESOURCES:
			case MISSION_SPECIALIST:
				weight = 1.5;  break;
			default:
				weight = 1; break;
		}

		double totalPercent = percent + weight * PERCENT_PER_SCORE;
		if (totalPercent > 100)
			totalPercent = 100;
		missionPlan.setPercentComplete(totalPercent);
		double score = missionPlan.getScore();
		missionPlan.setScore(score + weight * newScore);

		missionPlan.setReviewedBy(reviewer.getName());
	}

	public Map<Integer, Map<String, Double>> getHistoricalMissions() {
		return historicalMissions.getHistory();
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
	}

	/**
	 * Set up any Mission configurations
	 */
	public void initializeInstances(SimulationConfig simulationConfig) {
		if (missionBoost == null) {
			missionBoost = simulationConfig.getMissionBoosts();
		}
		else {
			missionBoost.putAll(simulationConfig.getMissionBoosts());
		}
	}
}
