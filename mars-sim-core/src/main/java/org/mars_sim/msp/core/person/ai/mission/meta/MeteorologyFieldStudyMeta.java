/*
 * Mars Simulation Project
 * MeteorologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.science.ScienceType;

/**
 * A meta mission for the MeteorologyFieldStudy.
 */
public class MeteorologyFieldStudyMeta extends FieldStudyMeta {

    MeteorologyFieldStudyMeta() {
    	super(MissionType.METEOROLOGY, 
    		 Set.of(JobType.METEOROLOGIST),
    		 ScienceType.METEOROLOGY);
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new MeteorologyFieldStudy(person, needsReview);
    }
}
