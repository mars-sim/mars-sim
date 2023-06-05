/**
 * Mars Simulation Project
 * TesDriveMetaMission.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission.predefined;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMission;
import org.mars_sim.msp.core.robot.Robot;

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
    public double getProbability(Person person) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProbability'");
    }

    @Override
    public double getProbability(Robot robot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProbability'");
    }    
}
