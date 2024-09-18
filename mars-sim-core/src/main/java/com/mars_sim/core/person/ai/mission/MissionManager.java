/*
 * Mars Simulation Project
 * MissionManager.java
 * @date 2023-08-17
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingLog;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.util.MissionRating;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.CacheCreator;
import com.mars_sim.core.person.ai.mission.meta.MetaMission;
import com.mars_sim.core.person.ai.mission.meta.MetaMissionUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * This class keeps track of ongoing missions in the simulation.
 * The simulation has only one mission manager.
 */
public class MissionManager implements Serializable {

	/** default serial identifier. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MissionManager.class.getName());

	/** The mission identifier. */
	private int identifier;
	/** The sol cache. */	
	private int solCache;
	
	/** The mission listeners. */
	private transient List<MissionManagerListener> listeners;

	/** The currently on-going missions in the simulation. */
	private List<Mission> onGoingMissions;
	/** A history of mission plans by sol. */
	private SolMetricDataLogger<String> historicalMissions;

	/**
	 * Constructor.
	 */
	public MissionManager() {
		// Initialize data members
		identifier = 1;
		solCache = 1;
		onGoingMissions = new CopyOnWriteArrayList<>();
		historicalMissions = new SolMetricDataLogger<>(30);
		listeners = null;
	}

	/**
	 * Gets the mission string. Must be synchronised to prevent duplicate identifiers 
	 * being assigned via different threads.
	 *
	 * @return
	 */
	synchronized String getMissionString() {
		int missionSol = Simulation.instance().getMasterClock().getMarsTime().getMissionSol();
		int id = 1;
		if (solCache != missionSol) {
			solCache = missionSol;
			identifier = 1;
		}
		else
			id = identifier++;
		return missionSol + "-" + String.format("%03d", id);
	}

	/**
	 * Gets the identifier.
	 *
	 * @return
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Adds a listener.
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
	 * Removes a listener.
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

				logger.config("Removing '" + oldMission.getName() + "' mission.");
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
		MarsTime marsTime = Simulation.instance().getMasterClock().getMarsTime();
		CacheCreator<MissionRating> missionProbCache = new CacheCreator<>("Mission", marsTime);
		List<MissionRating> missionCache = new ArrayList<>();
		Settlement startingSettlement = person.getAssociatedSettlement();
		ParameterManager paramMgr = startingSettlement.getPreferences();

		double calculateTotalProbCache = calculateMissionProbabilities(
				person, missionCache, paramMgr, startingSettlement);

		if (calculateTotalProbCache <= 0D) {
			person.getMind().getTaskManager().setMissionRatings(missionCache, null);
			return null;
		}

		var selectedMission = missionProbCache.getRandomSelection();

		if (selectedMission == null) {
			throw new IllegalStateException(person + " could not determine a new mission.");
		}

		RatingLog.logSelectedRating("missionstart", person.getName(), selectedMission, missionCache);

		// Construct and return the mission
		Mission mission = selectedMission.getMeta().constructInstance(person, true);
		person.getMind().getTaskManager().setMissionRatings(missionCache, selectedMission);

		return mission;
	}

	private double calculateMissionProbabilities(Person person, List<MissionRating> missionProbCache,
												 ParameterManager paramMgr, Settlement startingSettlement) {
		double totalProbCache = 0D;

		for (MetaMission metaMission : MetaMissionUtil.getMetaMissions()) {
			if (canAcceptMission(metaMission, startingSettlement, paramMgr)) {
				RatingScore baseProb = metaMission.getProbability(person);
				totalProbCache += updateMissionRating(person, missionProbCache, metaMission, baseProb, paramMgr);
			}
		}
		return totalProbCache;
	}

	private boolean canAcceptMission(MetaMission metaMission,
									 Settlement settlement, ParameterManager paramMgr) {
		int maxMissions = paramMgr.getIntValue(MissionLimitParameters.INSTANCE,
				metaMission.getType().name(), Integer.MAX_VALUE);
		int activeMissions = numParticularMissions(metaMission.getType(), settlement);
		return activeMissions < maxMissions;
	}

	private double updateMissionRating(Person person, List<MissionRating> missionProbCache,
									   MetaMission metaMission, RatingScore baseProb, ParameterManager paramMgr) {
		double score = baseProb.getScore();
		if (score > 0) {
			double settlementRatio = paramMgr.getDoubleValue(
					MissionWeightParameters.INSTANCE, metaMission.getType().name(), 1D);
			baseProb.addModifier("settlement.ratio", settlementRatio);

			logger.info(person, metaMission.getType().getName() + " " + baseProb.getOutput());
			missionProbCache.add(new MissionRating(metaMission, baseProb));
		}
		return score;
	}


	/**
	 * Gets the number of particular missions that are active
	 *
	 * @param mType
	 * @param settlement
	 * @return number
	 */
	private int numParticularMissions(MissionType mType, Settlement settlement) {
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
	 * Adds a mission plan.
	 *
	 * @param plan {@link MissionPlanning}
	 */
	public void addMissionPlanning(MissionPlanning plan) {

		Mission mission = plan.getMission();
		Person p = mission.getStartingPerson();

		logger.info(p, "Put together a mission plan for " + plan.getMission().getName() + ".");

		// Add this mission only after the mission plan has been submitted for review.
		addMission(mission);
	}

	/**
	 * Submits a request for approving a mission plan.
	 *
	 * @param mission
	 */
	public void requestMissionApproving(MissionPlanning plan) {
		addMissionPlanning(plan);
	}

	/**
	 * Approves a mission plan.
	 *
	 * @param missionPlan
	 * @param d
	 * @param status
	 */
	public void approveMissionPlan(MissionPlanning missionPlan, 
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

				Mission m = missionPlan.getMission();
				m.abortMission("Rejected");
				removeMission(m);
			}
		}
	}

	/**
	 * Gets the historical mission maps.
	 * 
	 * @return
	 */
	public Map<Integer, Map<String, Double>> getHistoricalMissions() {
		return historicalMissions.getHistory();
	}

	/**
	 * Prepares object for garbage collection.
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
		if (historicalMissions != null) {
			historicalMissions = null;
		}
	}
}
