/**
 * Mars Simulation Project
 * TesDriveMetaMission.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.MetaMission;
import com.mars_sim.core.robot.Robot;

/**
 * Skeleton implementation of the Meta Mission for a test drive
 */
public class TestDriveMetaMission implements MetaMission {

    @Override
    public MissionType getType() {
        return null;
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
    public Mission constructInstance(Robot robot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'constructInstance'");
    }

    @Override
    public RatingScore getProbability(Person person) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProbability'");
    }

    @Override
    public double getProbability(Robot robot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProbability'");
    }    
}
