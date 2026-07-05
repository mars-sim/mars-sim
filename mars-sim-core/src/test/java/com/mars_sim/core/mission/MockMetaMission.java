package com.mars_sim.core.mission;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;

class MockMetaMission extends AbstractMetaMission {

    MockMetaMission(int minMembers, int maxMembers, Set<JobType> leaderJobs, Set<JobType> workerJobs) {
        super(MissionType.TRADE, maxMembers, leaderJobs, workerJobs);
        setMinimumMembers(minMembers);
    }

    MockMetaMission(int minMembers, int maxMembers, Set<JobType> leaderJobs, Set<JobType> workerJobs,
                    int populationRatio, int populationThreshold) {
        this(minMembers, maxMembers, leaderJobs, workerJobs);
        setPopulationRatio(populationRatio);
        setPopulationThreshold(populationThreshold);
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return null;
    }
}
