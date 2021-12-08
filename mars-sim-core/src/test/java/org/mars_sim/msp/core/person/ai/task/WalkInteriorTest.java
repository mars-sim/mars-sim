package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;

/**
 * A unit test suite for the WalkInterior task class.
 */
public class WalkInteriorTest extends AbstractMarsSimUnitTest {

	private static final LocalPosition HATCH2_POSITION = new LocalPosition(-7.5D, 0D);

	private static final LocalPosition HATCH1_POSITION = new LocalPosition(-4.5D, 0D);

	private static final LocalPosition HATCH3_POSITION = new LocalPosition(-10.5D, 0D);

    
    /**
     * Test the walkingPhase method.
     */
    public void testWalkingInSameBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building b1 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

		Person person = buildPerson("Walker", settlement);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addPersonOrRobotToBuilding(person, b1);

        LocalPosition target = new LocalPosition(-2D, 1D);
        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, b1, target, 0D);
        assertFalse("Walk task can start", walkTask.isDone());
        
        executeTask(person, walkTask, 10);

        assertTrue("Interior walk completed", walkTask.isDone());
        assertEquals("Final Person building", b1, person.getBuildingLocation());
        assertEquals("Final Person position", target, person.getPosition());
    }
    
	
    /**
     * Test the walkingPhase method.
     */
    public void testWalkingBetweenThreeBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();


        Building b1 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        Building b2 = buildBuilding(buildingManager, new LocalPosition(-6D, 0D), 270D, 1);
        Building b3 = buildBuilding(buildingManager, new LocalPosition(-12D, 0D), 270D, 2);
        Building b4 = buildBuilding(buildingManager, new LocalPosition(-18D, 0D), 270D, 3);

        connectorManager.addBuildingConnection(new BuildingConnector(b1, HATCH1_POSITION, 90D, b2, HATCH1_POSITION, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(b3, HATCH2_POSITION, 270D, b2, HATCH2_POSITION, 90D));
        connectorManager.addBuildingConnection(new BuildingConnector(b4, HATCH3_POSITION, 270D, b3, HATCH3_POSITION, 90D));
        
		Person person = buildPerson("Walker", settlement);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addPersonOrRobotToBuilding(person, b1);

        LocalPosition target = new LocalPosition(-16D, 1D);
        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, b4, target, 0D);
        assertFalse("Walk task can start", walkTask.isDone());
        
        executeTask(person, walkTask, 10);

        assertTrue("Interior walk completed", walkTask.isDone());
        assertEquals("Final Person building", b4, person.getBuildingLocation());
        assertEquals("Final Person position", target, person.getPosition());
    }
    
    /**
     * Test the walkingPhase method.
     */
    public void testWalkingBetweenTwoBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();


        Building b3 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        Building b4 = buildBuilding(buildingManager, new LocalPosition(-6D, 0D), 270D, 2);
        Building b5 = buildBuilding(buildingManager, new LocalPosition(-12D, 0D), 270D, 1);

        connectorManager.addBuildingConnection(new BuildingConnector(b3, HATCH1_POSITION, 90D, b4, HATCH1_POSITION, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(b5, HATCH2_POSITION, 270D, b4, HATCH2_POSITION, 90D));

		Person person = buildPerson("Walker", settlement);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addPersonOrRobotToBuilding(person, b3);

        LocalPosition target = new LocalPosition(-11D, 1D);
        WalkSettlementInterior walkTask = new WalkSettlementInterior(person, b5, target, 0D);
        assertFalse("Walk task can start", walkTask.isDone());
        
        executeTask(person, walkTask, 10);

        assertTrue("Interior walk completed", walkTask.isDone());
        assertEquals("Final Person building", b5, person.getBuildingLocation());
        assertEquals("Final Person position", target, person.getPosition());
    }
}