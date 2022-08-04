/*
 * Mars Simulation Project
 * BiologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.science.ScienceType;

/**
 * A meta mission for the BiologyFieldStudy.
 */
public class BiologyFieldStudyMeta extends FieldStudyMeta {
    BiologyFieldStudyMeta() {
		super(MissionType.BIOLOGY, "biologyFieldStudy",
				Set.of(JobType.BIOLOGIST, JobType.BOTANIST),
				ScienceType.BIOLOGY);
	}

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new BiologyFieldStudy(person, needsReview);
    }
}
