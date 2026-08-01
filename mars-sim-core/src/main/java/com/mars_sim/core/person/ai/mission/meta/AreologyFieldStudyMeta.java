/*
 * Mars Simulation Project
 * AreologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.AreologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.vehicle.Rover;

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

	public Mission constructInstance(List<Worker> members, ScientificStudy study,
			Rover rover, Coordinates site) {
		return new AreologyFieldStudy(members, study, rover, site);
	}

	public Mission constructInstance(Roster crew, ScientificStudy study, Coordinates site) {
		return constructInstance(getMembers(crew), study, (Rover) crew.vehicle(), site);
	}

	private static List<Worker> getMembers(Roster crew) {
		List<Worker> members = new ArrayList<>();
		members.add(crew.leader());
		members.addAll(crew.members());
		return members;
	}
}
