/*
 * Mars Simulation Project
 * MeteorologyFieldStudyMeta.java
 * @date 2022-07-14
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MeteorologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.vehicle.Rover;

/**
 * A meta mission for the MeteorologyFieldStudy.
 */
public class MeteorologyFieldStudyMeta extends FieldStudyMeta {

    public MeteorologyFieldStudyMeta() {
    	super(MissionType.METEOROLOGY, 
    		 Set.of(JobType.METEOROLOGIST, JobType.PHYSICIST, JobType.CHEMIST, 
    				 JobType.ASTRONOMER, JobType.MATHEMATICIAN, JobType.COMPUTER_SCIENTIST, JobType.AREOLOGIST),
    		 ScienceType.METEOROLOGY);
    }
    
    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) {
        return new MeteorologyFieldStudy(crew, needsReview);
    }

	public Mission constructInstance(List<Worker> members, ScientificStudy study,
			Rover rover, Coordinates site) {
		return new MeteorologyFieldStudy(members, study, rover, site);
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
