/*
 * Mars Simulation Project
 * MissionManager.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
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
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class keeps track of ongoing missions in the simulation.<br>
 * <br>
 * The simulation has only one mission manager.
 */
public class MissionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MissionManager.class.getName());

	/** static mission identifier */
	private int missionIdentifer;

	/** Mission listeners. */
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
		missionIdentifer = 1;
		onGoingMissions = new CopyOnWriteArrayList<>();
		historicalMissions = new SolMetricDataLogger<>(30);
		listeners = null;
	}

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 *
	 * @return
	 */
	synchronized int getNextIdentifier() {
		return missionIdentifer++;
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
//				logger.config("Added '" + newMission.getName() + "' mission.");
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
		Mission result = null;

		// Probably must be calculated as a local otherwise method is not threadsafe using a shared cache
		Map<MetaMission, Double> missionProbCache = new HashMap<>();

		// Get a random number from 0 to the total weight
		double totalProbCache = 0D;

		Settlement startingSettlement = person.getAssociatedSettlement();

		// Determine probabilities.
		for (MetaMission metaMission : MetaMissionUtil.getMetaMissions()) {
			if (startingSettlement.isMissionEnable(metaMission.getType())) {
				double baseProb = metaMission.getProbability(person);
				if (Double.isNaN(baseProb) || Double.isInfinite(baseProb)) {
						logger.severe(person, "Bad mission probability on " + metaMission.getName() 
						+ " probability: "	+ Math.round(baseProb * 100.0)/100.0);
				}
				else if (baseProb > 0D) {
					// Get any overriding ratio
					double probability = baseProb;
					double settlementRatio = startingSettlement.getPreferenceModifier(
										new PreferenceKey(PreferenceKey.Type.MISSION,
														metaMission.getType().name()));
					probability *= settlementRatio;

					logger.info(person, "Mission '" + metaMission.getType().getName() 
							+ "' probability: " + Math.round(probability * 100.0)/100.0
									+ " base prob: " + Math.round(baseProb * 100.0)/100.0
									+ " sponsor: " + settlementRatio);
					if (probability > 0) {
						missionProbCache.put(metaMission, probability);
						totalProbCache += probability;
					}
				}
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

		// Construct the mission and needs a review
		result = selectedMetaMission.constructInstance(person, true);

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

	public Map<Integer, Map<String, Double>> getHistoricalMissions() {
		return historicalMissions.getHistory();
	}

	/**
	 * Sets up any Mission configurations.
	 */
	public void initializeInstances(SimulationConfig simulationConfig) {
		EVAOperation.setMinSunlight(simulationConfig.getMinEVALight());
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
