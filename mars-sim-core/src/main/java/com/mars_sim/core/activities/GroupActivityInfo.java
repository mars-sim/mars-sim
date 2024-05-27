/*
 * Mars Simulation Project
 * GroupActivityInfo.java
 * @date 2023-03-17
 * @author Barry Evans
 */
package com.mars_sim.core.activities;

import java.io.Serializable;

import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.time.EventSchedule;

/**
 * Represents the definition of a group activity that involves many Persons in a single Settlement.
 * @param name The name of the group activity
 * @param waitDuration Time spent that Person can wait before the activity starts
 * @param activtyDuration Duratino of the actual activity
 * @param calendar Defines how the activity is scheduled
 * @param percentagePop What percentage of the settlement population can join
 * @param score The score of the activity
 * @param scope The scope of on/off duty worker
 * @param place The category of a meeting place to host the Activity
 * @param impact What impact does this activity have on the Worker attending
 * 
 */
public record GroupActivityInfo(String name, int waitDuration, int activityDuration,
                                EventSchedule calendar,
                                double percentagePop, int score, TaskScope scope,
                                BuildingCategory place, ExperienceImpact impact)
            implements Serializable {

    
    public static final ExperienceImpact DEFAULT_IMPACT = new ExperienceImpact(0.1D,
                NaturalAttributeType.CONVERSATION,false,  0D,
                SkillType.MANAGEMENT);

}
