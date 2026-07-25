/**
 * Mars Simulation Project
 * LandmarkObjective.java
 * @date 2026-07-19
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * Objectives for visiting a Landmark. This is used to display the objectives in the Mission window.
 */
public class LandmarkObjective implements MissionObjective {

    private Landmark landmark;
    private int mSolAtSite;
    private int mSolViewing;
    private Map<String, Double> evaTimes = new HashMap<>();

    /**
     * Objective to visit a landmark. The objective is complete when the worker has spent the required time at the landmark.
     * @param landmark Landmark being visited
     * @param mSolAtSite Total time spent at the landmark
     * @param mSolViewing Time spent viewing the landmark per Person
     */
    public LandmarkObjective(Landmark landmark, int mSolAtSite, int mSolViewing) {
        this.landmark = landmark;
        this.mSolAtSite = mSolAtSite;
        this.mSolViewing = mSolViewing;
    }

    public int getMSolAtSite() {
        return mSolAtSite;
    }

    public int getMSolViewing() {
        return mSolViewing;
    }

    @Override
    public String getName() {
        return "Visit Landmark: " + landmark.getName();
    }

    public Landmark getLandmark() {
        return landmark;
    }
    
    /**
     * Has this worker done engough time visiting the landmark to complete the objective?
     * @param worker Worker
     * @return true if the worker has completed the objective, false otherwise
     */
    public boolean isEVADone(Worker worker) {
        return evaTimes.getOrDefault(worker.getName(), 0.0) >= mSolAtSite;
    }

    /**
     * Records that a worker has completed an EVA to visit the landmark.
     * @param worker The Worker that has completed the EVA
     * @param timeCompleted The time the EVA was completed
     */
    public void recordEVA(Worker worker, double timeCompleted) {
        evaTimes.merge(worker.getName(), timeCompleted, (a,b) -> a + b);
    }

    public Map<String, Double> getEVATimes() {
        return evaTimes;
    }
}
