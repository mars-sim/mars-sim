/*
 * Mars Simulation Project
 * MissionControl.java
 * @date 2026-05-20
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingLog;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricGroup;
import com.mars_sim.core.mission.util.MissionRating;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.CacheCreator;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionLimitParameters;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * This control the Missions for a given Settlement. It tracks the scores of missions and calculates the minimum passing score for new missions based on the scores of previous missions.
 * It also handles the review process for mission plans and updates the mission status accordingly.
 * 
 * It is also a ScheduledEventHandler that triggers the refreshing of the statistics.
 */
public class MissionControl implements ScheduledEventHandler {

    /** Initial mission passing score. */
	private static final double INITIAL_MISSION_PASSING_SCORE = 50D;
	/** The Maximum mission score that can be recorded. */
	private static final double MAX_MISSION_SCORE = 1000D;

    // Status for a mission plan that is not approved
	private static final MissionStatus MISSION_PLAN_NOT_APPROVED = MissionStatus.createResourceStatus("Mission.status.notApproved");

    private static final SimLogger logger = SimLogger.getLogger(MissionControl.class.getName());

	private double minimumPassingScore = INITIAL_MISSION_PASSING_SCORE;
    private List<Double> missionScores = new ArrayList<>();
	private Set<Mission> allMissions = new CopyOnWriteArraySet<>();
	private Settlement owner;
	private int id = 1;
	
	private static final MetricCategory MISSION_CAT = new MetricCategory("Missions");

	// Event types when missions are added or removed from the control
	public static final String MISSION_ADD = "mission add";
	public static final String MISSION_REMOVED = "mission removed";

	private MetricGroup metrics = new MetricGroup(MISSION_CAT);

    public MissionControl(Settlement settlement) {
		this.owner = settlement;
        missionScores.add(minimumPassingScore);

		owner.getPreferences().putValue(MissionLimitParameters.MISSION_CHECK_SOL, true);

        // Get time of next sol and schedule refresh; compensates for time zone
        MarsTime startOfNextSol = getMarsTime();
		startOfNextSol = startOfNextSol.advanceToNextMSol(settlement.getTimeZone().getMSolOffset());
        settlement.getFutureManager().addEvent(startOfNextSol, this);
    }

	// The naming conventino to apply to a new Mission
	public record MissionNaming(String name, String callSign) {}

	/**
	 * Creates the mission unique names for a new mission under this control
	 * @param type The type of the mission
	 * @return Naming convention to apply
	 */
	public synchronized MissionNaming generateNames(MissionType type) {
		int missionSol = getMarsTime().getMissionSol();

		String sortieString = missionSol + "-" + id;

		StringBuilder buffer = new StringBuilder();
		buffer.append(type.getShortCode())
			  .append("-")
			  .append(owner.getSettlementCode())
			  .append("-")
			  .append(sortieString);
		id++;

		return new MissionNaming(type.getName() + " " + sortieString, buffer.toString());
	}

    /**
	 * Calculates the current minimum passing score.
	 *
	 * @return
	 */
	public double getMinimumPassingScore() {
		return minimumPassingScore;
	}

	/**
	 * Saves the mission score.
	 *
	 * @param score
	 */
	private void saveMissionScore(double score) {

		// Simplify how minimum score is calculated. Use of trending scores
		// seems to make it harder to get missions approved over time
		minimumPassingScore = missionScores.stream()
                    .mapToDouble(Double::doubleValue).average().orElse(INITIAL_MISSION_PASSING_SCORE);

		// Cap any very large score to protect the average
		double desiredMax = Math.clamp(minimumPassingScore * 1.5D, 0, MAX_MISSION_SCORE);
		missionScores.add(Math.min(score, desiredMax));

		if (missionScores.size() > 30)
			missionScores.remove(0);
	}

    /**
     * Description of event handler for mission control.
     */
    @Override
    public String getEventDescription() {
        return "Mission Control";
    }

    /**
     * New sol has arrived so refresh stats.
     * @return Time until the next evetn which will be 1 Sol
     */
    @Override
    public int execute(MarsTime currentTime) {
		// Decrease the Mission score.
		minimumPassingScore *= 1.05;
        return 1000;
    }

	/**
	 * Request a mission plan is reviewed
	 *
	 * @param plan {@link MissionPlanning}
	 */
	public void requestMissionReview(MissionPlanning plan) {

		Mission mission = plan.getMission();
		Person p = mission.getStartingPerson();

		logger.info(p, "Put together a mission plan for " + plan.getMission().getName() + ".");

		// Add this mission only after the mission plan has been submitted for review.
		addMission(mission);
	}


    /**
     * Review of a misson plan is completed.
     * @param mp Plan completed.
     */
    public void reviewCompleted(MissionPlanning mp) {
		if (mp.getStatus() != PlanType.PENDING) {
			logger.warning(mp.getMission(), "Attempted to review a mission plan that is not pending review.");
			return;
		}

        double score = mp.getScore();
		double minScore = mp.getPassingScore();   // Passing score is set when the review is started
		PlanType state = (score > minScore ? PlanType.APPROVED : PlanType.NOT_APPROVED);
		metrics.recordValue(state.getName(), 1D, owner);

		// Updates the mission plan status
		mp.setStatus(state);
		if (state == PlanType.NOT_APPROVED) {
			// Failure needs a bit more work
			Mission m = mp.getMission();
			m.abortMission(MISSION_PLAN_NOT_APPROVED);

			removeMission(m);
		}
					
		logger.info(mp.getMission(), "Review completed, outcome is " + state.getName() + ", score: " 
						+ Math.round(score*10.0)/10.0 
						+ " [Min: " + Math.round(minScore*10.0)/10.0 + "].");
								
		saveMissionScore(score);
    }

	
	/**
	 * Gets the historical mission maps.
	 * 
	 * @return
	 */
	public Map<Integer, Map<String, Double>> getHistoricalMissions() {
		return metrics.getSolBreakdown();
	}

	
	/**
	 * Adds a new mission as the ongoing list under control of this control.
	 * @param mission The mission to be added to the ongoing list.
	 */
	public void addMission(Mission mission) {
		allMissions.add(mission);
		metrics.recordValue("Started", 1D, owner);

		owner.fireUnitUpdate(MISSION_ADD, mission);
	}

	/**
	 * Removes a mission from the ongoing list under control of this control.
	 * @param mission Mission to remove
	 */
	public void removeMission(Mission mission) {
		if (allMissions.remove(mission)) {
			owner.fireUnitUpdate(MISSION_REMOVED, mission);
		}
	}

	/**
	 * Gets all the missions associated with this control.
	 * @return set of missions associated with this control.
	 */
	public Set<Mission> getAllMissions() {
		return allMissions;
	}

	/**
	 * Gets all the active missions associated with this control.
	 * @return set of active missions associated with this control.
	 */
	public Set<Mission> getActiveMissions() {
		return allMissions.stream()
				.filter(m -> !m.isDone())
				.collect(Collectors.toSet());
	}

	
	/**
	 * Gets a new mission for a person based on potential missions available.
	 *
	 * @param person person to find the mission for
	 * @return new mission
	 */
	public Mission getNewMission(Person person) {
		var potentialMissions = calculateMissionProbabilities(person);

		// Any to choose from?
		if (potentialMissions.isEmpty()) {
			person.getMind().getTaskManager().setMissionRatings(potentialMissions, null);
			return null;
		}

		// Build a cache of the suitable ones
		CacheCreator<MissionRating> missionProbCache = new CacheCreator<>("Mission", null);
		missionProbCache.add(potentialMissions);

		// Choose one based on the scores and return it
		var selectedMission = missionProbCache.getRandomSelection();
		if (selectedMission == null) {
			logger.severe(person, 20_000L, "selectedMission is null. Could not determine a new mission.");
			return null;
		}

		RatingLog.logSelectedRating("missionstart", person.getName(), selectedMission, potentialMissions);

		// Construct and return the mission
		var builder = new MissionBuilder(selectedMission.getMeta(), person);
		Mission mission = builder.buildMission(true);
		person.getMind().getTaskManager().setMissionRatings(potentialMissions, selectedMission);

		return mission;
	}

	/**
	 * Get the MetaMissions that this control could currently support a Misison.
	 * This check the sol threshold and the maximum number of missions of that type allowed by the settlement preferences.
	 * @return MetaMissions that can be started.
	 */
	public List<MetaMission> getPossibleMissions() {
		ParameterManager paramMgr = owner.getPreferences();
		var checkSol = paramMgr.getBooleanValue(MissionLimitParameters.MISSION_CHECK_SOL, true);

		var activeMissionsByType = getActiveMissions().stream()
				.collect(Collectors.groupingBy(Mission::getMissionType, Collectors.counting()));
		
		return MetaMissionRegistry.getAutomaticMetaMissions().stream()
				.filter(m -> canAcceptMission(m, paramMgr, checkSol, activeMissionsByType))
				.toList();
	}

	/**
	 * Calculate the scores for all the potential missions for a person and return the list of scored missions.
	 * @param leader Leader wanting to start a new Mission
	 * @return List of scored potential missions for the person. The list may be empty if no suitable missions are found.
	 */
	private List<MissionRating> calculateMissionProbabilities(Person leader) {
		ParameterManager paramMgr = owner.getPreferences();

		return getPossibleMissions().stream()
				.map(m -> scoreMission(m, leader, paramMgr))
				.filter(r -> r != null)
				.toList();
	}

	/**
	 * Checks if a mission can be accepted based on the maximum number of active missions of that type allowed
	 * by the settlement preferences. Also it check the sol threshold.
	 * @param metaMission The mission type to check
	 * @param paramMgr The parameter manager containing the settlement preferences
	 * @param checkSol Whether to check the sol threshold for the mission
	 * @param activeMissionsByType A map of active missions grouped by their type
	 * @return true if the mission can be accepted, false otherwise
	 */
	private boolean canAcceptMission(MetaMission metaMission, ParameterManager paramMgr,
					boolean checkSol, Map<MissionType, Long> activeMissionsByType) {
		if (checkSol && getMarsTime().getMissionSol() < metaMission.getSolThreshold()) {
			return false;
		}
		
		int maxMissions = paramMgr.getIntValue(MissionLimitParameters.INSTANCE.getKey(metaMission.getType()),
											Integer.MAX_VALUE);
		int activeMissions = activeMissionsByType.getOrDefault(metaMission.getType(), 0L).intValue();
		return activeMissions < maxMissions;
	}

	private MarsTime getMarsTime() {
		return Simulation.instance().getMasterClock().getMarsTime();
	}

	/**
	 * Score the valid meta mission to be started by a leader.
	 * The score is based on the base probability of the mission and modified by the settlement ratio for that type of mission.
	 * @return MissionRating for the mission which includes the meta mission and the final score. Null if not suitable.
	 */
	private MissionRating scoreMission(MetaMission metaMission, Person person, ParameterManager paramMgr) {
		var score = metaMission.getProbability(person);
		if (score.getScore() <= 0) {
			return null;
		}

		// Valid Mission so get full score
		double settlementRatio = paramMgr.getDoubleValue(
					MissionWeightParameters.INSTANCE.getKey(metaMission.getType()), 1D);
		score.addModifier("settlement.ratio", settlementRatio);

		return new MissionRating(metaMission, score);
	}

	/**
	 * A mission has been finished and the control needs to be notified so it can update the statistics.
	 * @param successful true if the mission was successful, false otherwise
	 */
	public void finishMission(boolean successful) {
		metrics.recordValue(successful ? "Successful" : "Aborted", 1D, owner);
	}

	/**
	 * Enables or disable a mission type.
	 *  
	 * @param mission
	 * @param disable
	 */
	public void setMissionDisable(MissionType mission, boolean disable) {
		if (disable) {
			owner.getPreferences().putValue(MissionLimitParameters.INSTANCE.getKey(mission), 0);
		}
		else {
			var metaMission = MetaMissionRegistry.getMetaMission(mission);
			var maxMissions = Math.max(metaMission.getMaxMissions(owner.getNumCitizens()), 1);
			owner.getPreferences().putValue(MissionLimitParameters.INSTANCE.getKey(mission), maxMissions);
		}
	}
	
	/**
	 * Checks if the mission is enabled.
	 *
	 * @param mission the type of the mission calling this method
	 * @return probability value
	 */
	public boolean isMissionEnable(MissionType mission) {
		return owner.getPreferences().getIntValue(MissionLimitParameters.INSTANCE.getKey(mission), 0) > 0;
	}

	/**
	 * Population has changed in the settlement, so the mission control may need to update max. missions.
	 */
    public void populationChanged() {
		var numCitizens = owner.getNumCitizens();
		var preferences = owner.getPreferences();

		for(var metaMission : MetaMissionRegistry.getMetaMissions()) {
			MissionType type = metaMission.getType();
			var maxMissions = metaMission.getMaxMissions(numCitizens);

			if (maxMissions > 0) {
				preferences.putValue(MissionLimitParameters.INSTANCE.getKey(type), maxMissions);
			}
		}

		// Set total mission limit
		int optimalMissions = Math.max(1, (numCitizens/5));
		preferences.putValue(MissionLimitParameters.TOTAL_MISSIONS, optimalMissions);
    }
}