/*
 * Mars Simulation Project
 * AreologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.AreologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.science.ScienceType;

/**
 * A meta mission for the AreologyFieldStudy.
 */
public class AreologyFieldStudyMeta extends FieldStudyMeta {

    public AreologyFieldStudyMeta() {
		super(MissionType.AREOLOGY, 
				Set.of(JobType.AREOLOGIST, JobType.CHEMIST, JobType.PHYSICIST, JobType.METEOROLOGIST, JobType.ASTROBIOLOGIST),
				ScienceType.AREOLOGY);
	}

    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) {
        return new AreologyFieldStudy(crew, needsReview);
    }
}
