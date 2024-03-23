/*
 * Mars Simulation Project
 * GroupActivityInfo.java
 * @date 2023-03-17
 * @author Barry Evans
 */
package com.mars_sim.core.activities;

import java.io.Serializable;

import com.mars_sim.core.structure.building.BuildingCategory;

/**
 * Respresents the definition of a group activity that involves many Persons in a single Settlement.
 */
public record GroupActivityInfo(String name, int scheduledStart, int waitDuration,
                                int activityDuration, int solFrequency,
                                double percentagePop, int score, BuildingCategory place)
            implements Serializable {}
