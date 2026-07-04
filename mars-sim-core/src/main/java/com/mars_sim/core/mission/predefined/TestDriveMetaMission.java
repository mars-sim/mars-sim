/**
 * Mars Simulation Project
 * TesDriveMetaMission.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.MetaMission;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * Skeleton implementation of the Meta Mission for a test drive
 */
public class TestDriveMetaMission implements MetaMission {

    @Override
    public MissionType getType() {
        return MissionType.TEST_DRIVE;
    }

    @Override
    public String getName() {
        return "Test Drive";
    }

    @Override
    public double getLeaderSuitability(Person person) {
        return person.getSkillManager().getEffectiveSkillLevel(SkillType.PILOTING)/ 100D;
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new TestDriveMission("Test drive", person);
    }

    @Override
    public int getMinimumMembers() {
        return 2;
    }

    @Override
    public int getDefaultCapacity() {
        return 2;
    }

    @Override
    public double getWorkerSuitability(Worker w) {
        if (w instanceof Person person) {
            return person.getMind().getJobType() == JobType.TECHNICIAN ? 10.0 : 4.0;
        }
        return 0;
    } 
}
