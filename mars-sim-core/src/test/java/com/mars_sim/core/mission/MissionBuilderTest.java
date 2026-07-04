package com.mars_sim.core.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.MetaMissionUtil;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.test.MarsSimUnitTest;

class MissionBuilderTest extends MarsSimUnitTest {
    @Test
    @DisplayName("Test mission recruitment for Persons")
    void testRecruitMembers() {
        var s = buildSettlement("test", 5);
        buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, null, null);

        Set<Worker> possibles = new HashSet<>();
        var p1 = buildPerson("pilot1", s, JobType.PILOT, null, null);
        var p2 = buildPerson("pilot2", s, JobType.PILOT, null, null);
        possibles.add(p1);
        possibles.add(p2); 

        // Need a few people to force selection
        for(int i = 0; i < 8; i++) {
            possibles.add(buildPerson("worker" + i, s, JobType.CHEF, null, null));
        }

        var meta = new MockMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT));
        var recruitment = new MissionBuilder(meta, l);
        var members = recruitment.recruitMembers(possibles);

        // Take off leader from the count since they are not part of the recruitment process
        assertEquals(meta.getDefaultCapacity() - 1, members.size(), "Should recruit the correct number of members");
        assertTrue(members.contains(p1), "Pilot1 is a member");
        assertTrue(members.contains(p2), "Pilot2 is a member");
    }   

    @Test
    @DisplayName("Test that recruitment does not exceed the minimum number of people remaining in the settlement")
    void testRecruitTooFewRemaining() {
        var s = buildSettlement("test", 5);
        buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, null, null);

        Set<Worker> possibles = new HashSet<>();
        var p1 = buildPerson("pilot1", s, JobType.PILOT, null, null);
        var p2 = buildPerson("pilot2", s, JobType.PILOT, null, null);
        possibles.add(p1);
        possibles.add(p2);
        possibles.add(buildPerson("worker", s, JobType.CHEF, null, null));


        var meta = MetaMissionUtil.getMetaMission(MissionType.TRADE);
        var recruitment = new MissionBuilder(meta, l);
        var members = recruitment.recruitMembers(possibles);

        // TOnly 1 member can be recruited since we need to leave at least 2 people in the settlement
        assertEquals(1, members.size(), "Should recruit the correct number of members");
    }  
    
    class RobotMetaMission extends MockMetaMission {
        RobotMetaMission(int minMembers, int maxMembers, Set<JobType> leaderJobs, Set<JobType> workerJobs, Set<RobotType> robots) {
            super(minMembers, maxMembers, leaderJobs, workerJobs);

            setPreferredRobots(robots);
        }

    }

    @Test
    @DisplayName("Test mission recruitment for Robots")
    void testRecruitRobots() {
        var s = buildSettlement("test", 5);
        buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, null, null);

        Set<Worker> possibles = new HashSet<>();
        var p1 = buildPerson("pilot1", s, JobType.PILOT, null, null);
        var p2 = buildPerson("pilot2", s, JobType.PILOT, null, null);
        possibles.add(p1);
        possibles.add(p2);
        
        var r1 = buildRobot("robot1", s, RobotType.DELIVERYBOT, null, null);
        possibles.add(r1);

        // Need a few people to force selection
        for(int i = 0; i < 6; i++) {
            possibles.add(buildPerson("worker" + i, s, JobType.CHEF, null, null));
        }

        var meta = new RobotMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT),
                        Set.of(RobotType.DELIVERYBOT));
    
        var recruitment = new MissionBuilder(meta, l);
        var members = recruitment.recruitMembers(possibles);

        // Take off leader from the count since they are not part of the recruitment process
        assertEquals(meta.getDefaultCapacity() - 1, members.size(), "Should recruit the correct number of members");
        assertTrue(members.contains(p1) || members.contains(p2), "Pilot is a member");
        assertTrue(members.contains(r1), "Robot1 is a member");
    }   

}