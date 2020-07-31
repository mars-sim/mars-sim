package org.mars_sim.msp.core.person.ai.task;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.time.ClockUtils;

import junit.framework.TestCase;

/**
 * A unit test suite for the WalkInterior task class.
 */
public class WalkInteriorTest extends TestCase {

	private static Logger logger = Logger.getLogger(WalkInteriorTest.class.getName());
	 
    private static final double SMALL_DELTA = .00001D;

    /**
     * Test the determineDirection method.
     */
    public void testDetermineDirection() {
        SimulationConfig.instance().loadConfig();	
        Simulation.instance().testRun();
        
        Settlement settlement = new MockSettlement();

        MockBuilding b1 = new MockBuilding(settlement.getBuildingManager(), "B1");
        logger.info("WalkInteriorTest : " + b1 + "'s location state type : " + b1.getLocationStateType());

        b1.setWidth(10D);
        b1.setLength(10D);
//        settlement.getBuildingManager().addMockBuilding(b1);

        BuildingAirlock airlock0 = new BuildingAirlock(b1, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        b1.addFunction(new EVA(b1, airlock0));

		Person person = new Person(settlement);
		logger.info("WalkInteriorTest : " + person + "'s location state type : " + person.getLocationStateType());
		
//		settlement.getInventory().storeUnit(person);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonOrRobotToMockBuilding(person, b1);

//        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, b1, 0D, 0D, 0D);
//
//        assertEquals(0D, walkTask.determineDirection(0D, 5D), SMALL_DELTA);
//        assertEquals((Math.PI / 2D), walkTask.determineDirection(-5D, 0D), SMALL_DELTA);
//        assertEquals(Math.PI, walkTask.determineDirection(0D, -5D), SMALL_DELTA);
//        assertEquals((3D * Math.PI / 2D), walkTask.determineDirection(5D, 0D), SMALL_DELTA);
    }

    /**
     * Test the walkInDirection method.
     */
    public void testWalkInDirection() {
        SimulationConfig.instance().loadConfig();	
        Simulation.instance().testRun();
        
        Settlement settlement = new MockSettlement();

        MockBuilding b2 = new MockBuilding(settlement.getBuildingManager(), "B2");
        logger.info("WalkInteriorTest : " + b2 + "'s location state type : " + b2.getLocationStateType());
        
        b2.setWidth(10D);
        b2.setLength(10D);
//        settlement.getBuildingManager().addMockBuilding(b2);

        BuildingAirlock airlock0 = new BuildingAirlock(b2, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        b2.addFunction(new EVA(b2, airlock0));

		Person person = new Person(settlement);
		logger.info("WalkInteriorTest : " + person + "'s location state type : " + person.getLocationStateType());
		
//		settlement.getInventory().storeUnit(person);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonOrRobotToMockBuilding(person, b2);

//        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, b2, 0D, 0D, 0D);
//
//        // Walk North 5m.
//        walkTask.walkInDirection(0D, 5D);
//        assertEquals(0D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(5D, person.getYLocation(), SMALL_DELTA);
//
//        // Walk South 5m.
//        walkTask.walkInDirection(Math.PI, 5D);
//        assertEquals(0D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
//
//        // Walk West 5m.
//        walkTask.walkInDirection((3D * Math.PI / 2D), 5D);
//        assertEquals(5D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
//
//        // Walk East 5m.
//        walkTask.walkInDirection((Math.PI / 2D), 5D);
//        assertEquals(0D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
    }

    /**
     * Test the walkingPhase method.
     */
    public void testWalkingPhase() {
        SimulationConfig.instance().loadConfig();	
        Simulation.instance().testRun();
		
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);

        MockBuilding b3 = new MockBuilding(buildingManager, "B3");
        b3.setTemplateID(0);
        b3.setName("B3");
        b3.setBuildingNickName("B3");
        b3.setWidth(9D);
        b3.setLength(9D);
        b3.setXLocation(0D);
        b3.setYLocation(0D);
        b3.setFacing(0D);
//        buildingManager.addMockBuilding(b3);

        BuildingAirlock airlock0 = new BuildingAirlock(b3, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        b3.addFunction(new EVA(b3, airlock0));

        MockBuilding b4 = new MockBuilding(buildingManager, "B4");
        b4.setTemplateID(1);
        b4.setName("B4");
        b4.setBuildingNickName("B4");
        b4.setWidth(6D);
        b4.setLength(9D);
        b4.setXLocation(-12D);
        b4.setYLocation(0D);
        b4.setFacing(270D);
//        buildingManager.addMockBuilding(b4);

        MockBuilding b5 = new MockBuilding(buildingManager, "B5");
        b5.setTemplateID(2);
        b5.setName("B5");
        b5.setBuildingNickName("B5");
        b5.setWidth(2D);
        b5.setLength(3D);
        b5.setXLocation(-6D);
        b5.setYLocation(0D);
        b5.setFacing(270D);
//        buildingManager.addMockBuilding(b5);

        connectorManager.addBuildingConnection(new BuildingConnector(b3, -4.5D, 0D, 90D, b5, -4.5D, 0D, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(b4, -7.5D, 0D, 270D, b5, -7.5D, 0D, 90D));

		Person person = new Person(settlement);
		
//		settlement.getInventory().storeUnit(person);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonOrRobotToMockBuilding(person, b3);

        double walkSpeedMod = person.getWalkSpeedMod();
        // Walking time (millisols) for 1m. distance.
        double walkingTimeMeter = 3.6 / walkSpeedMod / Walk.PERSON_WALKING_SPEED / ClockUtils.SECONDS_PER_MILLISOL;//  0.020275896;// WalkSettlementInterior.PERSON_WALKING_SPEED has changed from 5 to 2 km/hr

//        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, b4, -11D, 1D, 0D);
//
//        assertEquals(0D, walkTask.walkingPhase(walkingTimeMeter), SMALL_DELTA);
//
//        assertEquals(-1D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
//
//        assertEquals(0D, walkTask.walkingPhase(walkingTimeMeter * 5D), SMALL_DELTA);
//
//        assertEquals(-6D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
//
//        assertEquals(0D, walkTask.walkingPhase(walkingTimeMeter * 1.5D), SMALL_DELTA);
//
//        assertEquals(-7.5D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
//
//        double remainingTime = walkTask.walkingPhase(walkingTimeMeter * 4D);
//        assertTrue(remainingTime > 0D);
//
//        assertEquals(-11D, person.getXLocation(), SMALL_DELTA);
//        assertEquals(1D, person.getYLocation(), SMALL_DELTA);
//
//        assertTrue(walkTask.isDone());
    }
}