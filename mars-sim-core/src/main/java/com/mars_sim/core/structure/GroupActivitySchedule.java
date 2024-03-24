/*
 * Mars Simulation Project
 * GroupActivitySchedule.java
 * @date 2024-03-24
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.activities.GroupActivityInfo;

/**
 * Defines a scheduled for group activities
 * @param name Name of the schedule
 * @param minPop Optional minimum population
 * @param specials The type based special GroupActivities
 * @param meetings Regular repeating meetings
 */
public record GroupActivitySchedule(String name, int minPop, Map<GroupActivityType,GroupActivityInfo> specials, 
                List<GroupActivityInfo> meetings) {}
