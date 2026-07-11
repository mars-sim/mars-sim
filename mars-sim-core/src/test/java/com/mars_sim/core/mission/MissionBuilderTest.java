package com.mars_sim.core.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.MetaMissionUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.RangeComparator;

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
    @DisplayName("Test that build flags lack of vehicle")
    void testBuildNoVehicle() {
        var s = buildSettlement("test", 5);
        buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, null, null);

        var meta = new VehicleMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT), VehicleType.ROVER_TYPES);
        var recruitment = new MissionBuilder(meta, l);
        var mission = recruitment.buildMission(false);

        assertNull(mission, "Should not build a mission with too few vehicles");

        var messages = recruitment.getMessages();
        assertEquals(1, messages.size(), "Should have a message about too few vehicles");
        assertEquals("mission.builder.noVehicle", messages.get(0).key(), "Should have the correct message key");
    }
    
    @Test
    @DisplayName("Test that build flags lack of members")
    void testBuildNoMembers() {
        var s = buildSettlement("test", 5);
        buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, null, null);

        var meta = new MockMetaMission(2, 2, Set.of(JobType.TRADER), Set.of(JobType.PILOT));
        var recruitment = new MissionBuilder(meta, l);
        var mission = recruitment.buildMission(false);

        assertNull(mission, "Should not build a mission with too few members");

        var messages = recruitment.getMessages();
        assertEquals(1, messages.size(), "Should have a message about too few members");
        assertEquals("mission.builder.notEnoughMembers", messages.get(0).key(), "Should have the correct message key");
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

    class VehicleMetaMission extends MockMetaMission {
        VehicleMetaMission(int minMembers, int maxMembers, Set<JobType> leaderJobs, Set<JobType> workerJobs, Set<VehicleType> vehicles) {
            super(minMembers, maxMembers, leaderJobs, workerJobs);

            setPreferredVehicle(vehicles);
        }

        @Override
        public Comparator<Vehicle> getVehicleComparator() {
            return new RangeComparator();
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

    @Test
    @DisplayName("Test vehicle reservation selects the best home settlement rover")
    void testReserveVehicle() {
        var home = buildSettlement("home", 5);
        buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var leader = buildPerson("leader", home, JobType.TRADER, null, null);

        var explorer = buildRover(home, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var transport = buildRover(home, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var cargo = buildRover(home, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);

        // Double check the expected range ordering of the rovers to ensure the test is valid
        assertTrue(explorer.getRange() < transport.getRange(), "Explorer has less range than Transport");
        assertTrue(transport.getRange() < cargo.getRange(), "Transport has less range than Cargo");

        var meta = new VehicleMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT), VehicleType.ROVER_TYPES);
        var builder = new MissionBuilder(meta, leader);

        var firstVehicle = builder.selectBestVehicle();
        firstVehicle.setReservedForMission(true);
        var secondVehicle = builder.selectBestVehicle();

        assertEquals(cargo, firstVehicle, "Should choose the best rover available at the leader's settlement");
        assertEquals(transport, secondVehicle, "Should skip the reserved explorer and choose the next best rover");
    }

    @DisplayName("Test ice collection mission can be created")
    @Test
    void testIceMissionSolBased() {
        var settlement = buildIcePreRequisites();
        var leader = buildPerson("Leader", settlement, RoleType.MISSION_SPECIALIST, JobType.PILOT);
    
        // Test on the first sol and should fail
        var missionControl = new MissionControl(settlement);
        var mission = missionControl.getNewMission(leader);
        assertNull(mission, "Mission should not be created");

        // Advance sol pass threshold
        var meta = MetaMissionUtil.getMetaMission(MissionType.COLLECT_ICE);
        var clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(meta.getSolThreshold() * 1000D));

        // Enable ice collection mission
        MissionBuilder builder = new MissionBuilder(meta, leader);
        mission = builder.buildMission(false);
        assertNotNull(mission, "Mission should be created");
        assertNull(mission.getPlan(), "Mission plan should be null since no review is requested");
        assertEquals(MissionType.COLLECT_ICE, mission.getMissionType(), "Mission type created");

        var tm = leader.getMind().getMission();
        assertNotNull(tm, "Mission created and assigned");
        assertEquals(tm, mission, "Selected mission should be Ice");
    }
    
    private Settlement buildIcePreRequisites() {
        var settlement = buildSettlement("Test", true, 5);

        // Add workers
        for(var p = 0; p < 4; p++) {
            buildPerson("Worker" + p, settlement);
        }

        var r = buildRover(settlement, "Rover", new LocalPosition(0, 0), CARGO_ROVER);

        // Build resoruces for collection ice mission
        for(int i = 0; i < CollectIce.REQUIRED_BARRELS+1; i++) {
            EquipmentFactory.createEquipment(EquipmentType.BARREL, settlement);
        }

        // Create enough suits with spares
        for(int e = 0; e < settlement.getCitizens().size() + 2; e++) {
            EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, settlement);
        }

        Map<Integer, Double> resources = Map.of(ResourceUtil.OXYGEN_ID, 100D,
                                                ResourceUtil.FOOD_ID, 100D,
                                                r.getFuelTypeID(), 100D,
                                                ResourceUtil.WATER_ID, 100D);
        loadSettlementAmounts(settlement, resources);  
        
        return settlement;
    }
}