/*
 * Mars Simulation Project
 * MeteorologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MeteorologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.science.ScienceType;

/**
 * A meta mission for the MeteorologyFieldStudy.
 */
public class MeteorologyFieldStudyMeta extends FieldStudyMeta {

    MeteorologyFieldStudyMeta() {
    	super(MissionType.METEOROLOGY, 
    		 Set.of(JobType.METEOROLOGIST, JobType.PHYSICIST),
    		 ScienceType.METEOROLOGY);
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new MeteorologyFieldStudy(person, needsReview);
    }
}
