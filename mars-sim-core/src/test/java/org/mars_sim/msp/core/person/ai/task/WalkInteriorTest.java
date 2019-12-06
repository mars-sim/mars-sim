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

        MockBuilding building = new MockBuilding(settlement.getBuildingManager());
        logger.info("WalkInteriorTest : " + building + "'s location state type : " + building.getLocationStateType());

        building.setWidth(10D);
        building.setLength(10D);
        settlement.getBuildingManager().addMockBuilding(building);

        BuildingAirlock airlock0 = new BuildingAirlock(building, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building.addFunction(new EVA(building, airlock0));

		Person person = new Person(settlement);
		logger.info("WalkInteriorTest : " + person + "'s location state type : " + person.getLocationStateType());
		
//		settlement.getInventory().storeUnit(person);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonOrRobotToBuilding(person, building);

        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, building, 0D, 0D, 0);

        assertEquals(0D, walkTask.determineDirection(0D, 5D), SMALL_DELTA);
        assertEquals((Math.PI / 2D), walkTask.determineDirection(-5D, 0D), SMALL_DELTA);
        assertEquals(Math.PI, walkTask.determineDirection(0D, -5D), SMALL_DELTA);
        assertEquals((3D * Math.PI / 2D), walkTask.determineDirection(5D, 0D), SMALL_DELTA);
    }

    /**
     * Test the walkInDirection method.
     */
    public void testWalkInDirection() {
        SimulationConfig.instance().loadConfig();	
        Simulation.instance().testRun();
        
        Settlement settlement = new MockSettlement();

        MockBuilding building = new MockBuilding(settlement.getBuildingManager());
        building.setWidth(10D);
        building.setLength(10D);
        settlement.getBuildingManager().addMockBuilding(building);

        BuildingAirlock airlock0 = new BuildingAirlock(building, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building.addFunction(new EVA(building, airlock0));

		Person person = new Person(settlement);
		
//		settlement.getInventory().storeUnit(person);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonOrRobotToBuilding(person, building);

        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, building, 0D, 0D, 0);

        // Walk North 5m.
        walkTask.walkInDirection(0D, 5D);
        assertEquals(0D, person.getXLocation(), SMALL_DELTA);
        assertEquals(5D, person.getYLocation(), SMALL_DELTA);

        // Walk South 5m.
        walkTask.walkInDirection(Math.PI, 5D);
        assertEquals(0D, person.getXLocation(), SMALL_DELTA);
        assertEquals(0D, person.getYLocation(), SMALL_DELTA);

        // Walk West 5m.
        walkTask.walkInDirection((3D * Math.PI / 2D), 5D);
        assertEquals(5D, person.getXLocation(), SMALL_DELTA);
        assertEquals(0D, person.getYLocation(), SMALL_DELTA);

        // Walk East 5m.
        walkTask.walkInDirection((Math.PI / 2D), 5D);
        assertEquals(0D, person.getXLocation(), SMALL_DELTA);
        assertEquals(0D, person.getYLocation(), SMALL_DELTA);
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

        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setTemplateID(0);
        building0.setName("building 0");
        building0.setBuildingNickName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addMockBuilding(building0);

        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));

        MockBuilding building1 = new MockBuilding(buildingManager);
        building1.setTemplateID(1);
        building1.setName("building 1");
        building1.setBuildingNickName("building 1");
        building1.setWidth(6D);
        building1.setLength(9D);
        building1.setXLocation(-12D);
        building1.setYLocation(0D);
        building1.setFacing(270D);
        buildingManager.addMockBuilding(building1);

        MockBuilding building2 = new MockBuilding(buildingManager);
        building2.setTemplateID(2);
        building2.setName("building 2");
        building2.setBuildingNickName("building 2");
        building2.setWidth(2D);
        building2.setLength(3D);
        building2.setXLocation(-6D);
        building2.setYLocation(0D);
        building2.setFacing(270D);
        buildingManager.addMockBuilding(building2);

        connectorManager.addBuildingConnection(new BuildingConnector(building0, -4.5D, 0D, 90D, building2, -4.5D, 0D, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(building1, -7.5D, 0D, 270D, building2, -7.5D, 0D, 90D));

		Person person = new Person(settlement);
		
//		settlement.getInventory().storeUnit(person);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonOrRobotToBuilding(person, building0);

        double walkSpeedMod = person.getWalkSpeedMod();
        // Walking time (millisols) for 1m. distance.
        double walkingTimeMeter = 3.6 / walkSpeedMod / Walk.PERSON_WALKING_SPEED / ClockUtils.SECONDS_PER_MILLISOL;//  0.020275896;// WalkSettlementInterior.PERSON_WALKING_SPEED has changed from 5 to 2 km/hr

        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, building1, -11D, 1D, 0);

        assertEquals(0D, walkTask.walkingPhase(walkingTimeMeter), SMALL_DELTA);

        assertEquals(-1D, person.getXLocation(), SMALL_DELTA);
        assertEquals(0D, person.getYLocation(), SMALL_DELTA);

        assertEquals(0D, walkTask.walkingPhase(walkingTimeMeter * 5D), SMALL_DELTA);

        assertEquals(-6D, person.getXLocation(), SMALL_DELTA);
        assertEquals(0D, person.getYLocation(), SMALL_DELTA);

        assertEquals(0D, walkTask.walkingPhase(walkingTimeMeter * 1.5D), SMALL_DELTA);

        assertEquals(-7.5D, person.getXLocation(), SMALL_DELTA);
        assertEquals(0D, person.getYLocation(), SMALL_DELTA);

        double remainingTime = walkTask.walkingPhase(walkingTimeMeter * 4D);
        assertTrue(remainingTime > 0D);

        assertEquals(-11D, person.getXLocation(), SMALL_DELTA);
        assertEquals(1D, person.getYLocation(), SMALL_DELTA);

        assertTrue(walkTask.isDone());
    }
}