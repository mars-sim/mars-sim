/**
 * Mars Simulation Project
 * BiologyFieldStudyMeta.java
 * @version 3.2.0 2021-06-20
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
    public Mission constructInstance(Person person) {
        return new BiologyFieldStudy(person);
    }
}
