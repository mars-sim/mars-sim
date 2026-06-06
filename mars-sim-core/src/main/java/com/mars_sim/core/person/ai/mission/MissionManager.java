/*
 * Mars Simulation Project
 * MissionManager.java
 * @date 2025-10-11
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingLog;
import com.mars_sim.core.data.RatingScore;
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
	private static final SimLogger logger = SimLogger.getLogger(MissionManager.class.getName());
	
	/** The mission listeners. */
	private transient List<MissionManagerListener> listeners;

	/** The currently on-going missions in the simulation. */
	private List<Mission> onGoingMissions;

	/**
	 * Constructor.
	 */
	public MissionManager() {
		// Initialize data members
		onGoingMissions = new CopyOnWriteArrayList<>();
		listeners = null;
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
	public void addOngoingMission(Mission newMission) {
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
	public void removeOngoingMission(Mission oldMission) {
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

	private double calculateMissionProbabilities(Person person, List<MissionRating> missionProbCache,
												 ParameterManager paramMgr, Settlement startingSettlement) {
		double totalProbCache = 0D;

		for (MetaMission metaMission : MetaMissionUtil.getMetaMissions()) {
			if (canAcceptMission(metaMission, startingSettlement, paramMgr)) {
				RatingScore baseProb = metaMission.getProbability(person);
				totalProbCache += updateMissionRating(missionProbCache, metaMission, baseProb, paramMgr);
			}
		}
		return totalProbCache;
	}

	private boolean canAcceptMission(MetaMission metaMission,
									 Settlement settlement, ParameterManager paramMgr) {
		int maxMissions = paramMgr.getIntValue(MissionLimitParameters.INSTANCE.getKey(metaMission.getType()),
											Integer.MAX_VALUE);
		int activeMissions = numParticularMissions(metaMission.getType(), settlement);
		return activeMissions < maxMissions;
	}

	private double updateMissionRating(List<MissionRating> missionProbCache,
									   MetaMission metaMission, RatingScore baseProb, ParameterManager paramMgr) {
		double score = baseProb.getScore();
		if (score > 0) {
			double settlementRatio = paramMgr.getDoubleValue(
					MissionWeightParameters.INSTANCE.getKey(metaMission.getType()), 1D);
			baseProb.addModifier("settlement.ratio", settlementRatio);
		}

		missionProbCache.add(new MissionRating(metaMission, baseProb));
		
		return score;
	}


	/**
	 * Gets the number of particular missions that are active.
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
	}
}
