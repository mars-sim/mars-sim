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

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
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
	private SolMetricDataLogger<String> historicalMissions;
	private Settlement owner;
	private int id = 1;

    public MissionControl(Settlement settlement) {
		this.owner = settlement;
        missionScores.add(minimumPassingScore);

		historicalMissions = new SolMetricDataLogger<>(10);

        // Get time of next sol and schedule refresh; compensates for time zone
        MarsTime startOfNextSol = Simulation.instance().getMasterClock().getMarsTime();
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
		int missionSol = Simulation.instance().getMasterClock().getMarsTime().getMissionSol();

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
		historicalMissions.increaseDataPoint(state.name(), 1D);

		// Updates the mission plan status
		mp.setStatus(state);
		if (state == PlanType.NOT_APPROVED) {
			// Failure needs a bit more work
			Mission m = mp.getMission();
			m.abortMission(MISSION_PLAN_NOT_APPROVED);

			var missionManager = Simulation.instance().getMissionManager();
			missionManager.removeMission(m);
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
		return historicalMissions.getHistory();
	}

}