package com.mars_sim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.construction.MockMission;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyManifest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.test.MarsSimUnitTest;

class EntityResolverTest extends MarsSimUnitTest {
        @Test
        void testEntityIdentifierStringConversion() {
            // Test a variety of entity types and ids
            EntityIdentifier[] ids = new EntityIdentifier[] {
                new EntityIdentifier("SETTLEMENT", "123"),
                new EntityIdentifier("PERSON", "456"),
                new EntityIdentifier("VEHICLE", "789"),
                new EntityIdentifier("AUTHORITY", "NASA"),
                new EntityIdentifier("SCIENTIFICSTUDY", "study-001"),
                new EntityIdentifier("MISSION", "mission-abc"),
                new EntityIdentifier("TRANSPORTABLE", "resupply-42"),
                new EntityIdentifier("BUILDING", "321"),
                new EntityIdentifier("ROBOT", "654"),
                new EntityIdentifier("EVA_SUIT", "eva-77")
            };
            for (EntityIdentifier id : ids) {
                String str = EntityResolver.toString(id);
                EntityIdentifier parsed = EntityResolver.fromString(str);
                assertEquals(id, parsed, "EntityIdentifier toString/fromString reversible for: " + id);
            }
        }
    @Test
    void testSettlement() {
        var s1 = buildSettlement("S1");
        var s2 = buildSettlement("S2");

        var id1 = s1.getEntityIdentifier();
        assertNotNull(id1, "Settlement 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(s1, resolved1, "Resolved Settlement 1");

        var id2 = s2.getEntityIdentifier();
        assertNotNull(id2, "Settlement 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(s2, resolved2, "Resolved Settlement 2");
    }


    @Test
    void testAuthority() {
        var config = getConfig();
        var authorityFactory = config.getReportingAuthorityFactory();
        
        var a1 = authorityFactory.getItem("NASA");
        var a2 = authorityFactory.getItem("CNSA");

        var id1 = a1.getEntityIdentifier();
        assertNotNull(id1, "Authority 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(a1, resolved1, "Resolved Authority 1");

        var id2 = a2.getEntityIdentifier();
        assertNotNull(id2, "Authority 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(a2, resolved2, "Resolved Authority 2");
    }

    @Test
    void testVehicle() {
        var settlement = buildSettlement("VehicleTestSettlement");
        var rover1 = buildRover(settlement, "Rover1", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var rover2 = buildRover(settlement, "Rover2", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var id1 = rover1.getEntityIdentifier();
        assertNotNull(id1, "Vehicle 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(rover1, resolved1, "Resolved Vehicle 1");

        var id2 = rover2.getEntityIdentifier();
        assertNotNull(id2, "Vehicle 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(rover2, resolved2, "Resolved Vehicle 2");
    }

    @Test
    void testBuilding() {
        var settlement = buildSettlement("BuildingTestSettlement");
        var buildingManager = settlement.getBuildingManager();
        
        var building1 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0.0);
        var building2 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 45.0);

        var id1 = building1.getEntityIdentifier();
        assertNotNull(id1, "Building 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(building1, resolved1, "Resolved Building 1");

        var id2 = building2.getEntityIdentifier();
        assertNotNull(id2, "Building 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(building2, resolved2, "Resolved Building 2");
    }

    @Test
    void testPerson() {
        var settlement = buildSettlement("PersonTestSettlement");
        var person1 = buildPerson("Person1", settlement);
        var person2 = buildPerson("Person2", settlement);

        var id1 = person1.getEntityIdentifier();
        assertNotNull(id1, "Person 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(person1, resolved1, "Resolved Person 1");

        var id2 = person2.getEntityIdentifier();
        assertNotNull(id2, "Person 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(person2, resolved2, "Resolved Person 2");
    }

    @Test
    void testRobot() {
        var settlement = buildSettlement("RobotTestSettlement");
        var robot1 = buildRobot("Robot1", settlement, RobotType.CHEFBOT, null, null);
        var robot2 = buildRobot("Robot2", settlement, RobotType.CONSTRUCTIONBOT, null, null);

        var id1 = robot1.getEntityIdentifier();
        assertNotNull(id1, "Robot 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(robot1, resolved1, "Resolved Robot 1");

        var id2 = robot2.getEntityIdentifier();
        assertNotNull(id2, "Robot 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(robot2, resolved2, "Resolved Robot 2");
    }

    @Test
    void testScientificStudy() {
        var settlement = buildSettlement("StudyTestSettlement");
        var buildingManager = settlement.getBuildingManager();
        buildResearch(buildingManager, LocalPosition.DEFAULT_POSITION, 0.0);

        var person1 = buildPerson("Botanist", settlement);
        var person2 = buildPerson("Geologist", settlement);

        var study1 = getSim().getScientificStudyManager().createScientificStudy(person1, ScienceType.BOTANY, 5);
        var study2 = getSim().getScientificStudyManager().createScientificStudy(person2, ScienceType.AREOLOGY, 5);

        var id1 = study1.getEntityIdentifier();
        assertNotNull(id1, "Study 1 has identifier");
        var resolved1 = EntityResolver.resolve(getSim(), id1);
        assertEquals(study1, resolved1, "Resolved Study 1");

        var id2 = study2.getEntityIdentifier();
        assertNotNull(id2, "Study 2 has identifier");
        var resolved2 = EntityResolver.resolve(getSim(), id2);
        assertEquals(study2, resolved2, "Resolved Study 2");
    }

    @Test
    void testResupply() {
        var settlement = buildSettlement("ResupplyTestSettlement");

        var template = getConfig().getSettlementTemplateConfiguration().getItem(settlement.getTemplate());
        var manifest = new ResupplyManifest("Test Manifest", 0, template.getSupplies());
        var arrival = getSim().getMasterClock().getMarsTime().addTime(1000D);
        var resupply = new Resupply("Test Resupply", arrival, settlement, manifest);
        getSim().getTransportManager().addNewTransportItem(resupply);

        var id = resupply.getEntityIdentifier();
        assertNotNull(id, "Resupply has identifier");
        var resolved = EntityResolver.resolve(getSim(), id);
        assertEquals(resupply, resolved, "Resolved Resupply");
    }

    @Test
    void testMission() {
        var s = buildSettlement("MissionTestSettlement");

        var mission = new MockMission(s) {
            @Override
            public EntityIdentifier getEntityIdentifier() {
                return new EntityIdentifier("MISSION", "mock-mission-1");
            }
        };

        s.getMissionControl().addMission(mission);

        var id = mission.getEntityIdentifier();
        assertNotNull(id, "Mission has identifier");
        var resolved = EntityResolver.resolve(getSim(), id);
        assertEquals(mission, resolved, "Resolved Mission");
    }
}
