/**
 * Mars Simulation Project
 * AreologyFieldStudyMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.science.ScienceType;

/**
 * A meta mission for the AreologyFieldStudy.
 */
public class AreologyFieldStudyMeta extends FieldStudyMeta {

    AreologyFieldStudyMeta() {
		super(MissionType.AREOLOGY, "areologyFieldStudy",
				Set.of(JobType.AREOLOGIST, JobType.CHEMIST),
				ScienceType.AREOLOGY);
	}

    @Override
    public Mission constructInstance(Person person) {
        return new AreologyFieldStudy(person);
    }
}
