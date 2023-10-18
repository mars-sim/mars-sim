/*
 * Mars Simulation Project
 * BiologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.BiologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.science.ScienceType;

/**
 * A meta mission for the BiologyFieldStudy.
 */
public class BiologyFieldStudyMeta extends FieldStudyMeta {
    BiologyFieldStudyMeta() {
		super(MissionType.BIOLOGY, 
				Set.of(JobType.BIOLOGIST, JobType.BOTANIST),
				ScienceType.BIOLOGY);
	}

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new BiologyFieldStudy(person, needsReview);
    }
}
