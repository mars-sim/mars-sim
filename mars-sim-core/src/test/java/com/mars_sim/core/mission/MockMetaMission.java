package com.mars_sim.core.mission;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.AbstractMetaMission;

class MockMetaMission extends AbstractMetaMission {

    MockMetaMission(int minMembers, int maxMembers, Set<JobType> leaderJobs, Set<JobType> workerJobs) {
        super(MissionType.TRADE, minMembers, maxMembers, leaderJobs, workerJobs);
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return null;
    }
}
