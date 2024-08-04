/*
 * Mars Simulation Project
 * MissionJob.java
 * @date 2024-08-04
 * @author Manny Kung
 */
package com.mars_sim.core.mission.util;

import java.io.Serializable;

import com.mars_sim.core.data.Rating;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.robot.Robot;

/**
 * This represents a potential mission that can be executed. Each mission has a score for the benefit of
 * doing the mission.
 */
public interface MissionJob extends Rating, Serializable {

    /**
     * Creates the mission for a person.
     */
	Mission createMission(Person person);

    /**
     * Creates the mission for a robot.
     * 
     * @param robot 
     */
	Mission createMission(Robot robot);

}
