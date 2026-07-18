/**
 * Mars Simulation Project
 * LandmarkObjective.java
 * @date 2026-07-19
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.mission.MissionObjective;

/**
 * Objectives for visiting a Landmark. This is used to display the objectives in the Mission window.
 */
public class LandmarkObjective implements MissionObjective {

    private Landmark landmark;
    private int mSolOnSite;

    public LandmarkObjective(Landmark landmark, int mSolOnSite) {
        this.landmark = landmark;
        this.mSolOnSite = mSolOnSite;
    }

    public int getMSolOnSite() {
        return mSolOnSite;
    }

    @Override
    public String getName() {
        return "Visit Landmark: " + landmark.getName();
    }

    public Landmark getLandmark() {
        return landmark;
    }
}
